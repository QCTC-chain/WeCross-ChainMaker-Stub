package com.webank.wecross.stub.chainmaker.custom;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.*;
import com.webank.wecross.stub.chainmaker.ChainMakerConnection;
import com.webank.wecross.stub.chainmaker.common.ChainMakerConstant;
import com.webank.wecross.stub.chainmaker.common.ChainMakerStatusCode;
import com.webank.wecross.stub.chainmaker.utils.ConfigUtils;
import org.chainmaker.pb.common.ContractOuterClass;
import org.chainmaker.pb.config.ChainConfigOuterClass;
import org.chainmaker.sdk.ChainClientException;
import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;

import org.chainmaker.sdk.utils.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RegisterContractHandler implements CommandHandler {
    private Logger logger = LoggerFactory.getLogger(RegisterContractHandler.class);

    private boolean isExistsABI(String configPath, String contractName) throws WeCrossException {
        try {
            ConfigUtils.getContractABI(configPath, contractName);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public void handle(
            Path path,
            Object[] args,
            Account account,
            BlockManager blockManager,
            Connection connection,
            Driver.CustomCommandCallback callback) {

        ChainMakerConnection chainMakerConnection = (ChainMakerConnection) connection;
        try {
            ContractOuterClass.Contract contract = chainMakerConnection.getChainClient().getContractInfo(
                    path.getResource(),
                    ChainMakerConnection.RPC_CALL_TIMEOUT);
            ResourceInfo resourceInfo = new ResourceInfo();
            resourceInfo.setStubType(chainMakerConnection.getProperties().get(ChainMakerConstant.CHAINMAKER_STUB_TYPE));
            resourceInfo.setName(path.getResource());
            Map<Object, Object> resourceProperties = new HashMap<>();
            resourceProperties.put(
                    ChainMakerConstant.CHAINMAKER_CHAIN_ID,
                    chainMakerConnection.getProperties().get(ChainMakerConstant.CHAINMAKER_CHAIN_ID));
            resourceProperties.put(
                    ChainMakerConstant.CHAINMAKER_CONTRACT_ADDRESS,
                    contract.getName());
            resourceProperties.put(
                    ChainMakerConstant.CHAINMAKER_CONTRACT_VERSION,
                    contract.getVersion());
            resourceProperties.put(
                    ChainMakerConstant.CHAINMAKER_CONTRACT_RUNTIME_TYPE,
                    contract.getRuntimeType().name());
            resourceInfo.setProperties(resourceProperties);

            if(!isExistsABI(chainMakerConnection.getConfigPath(), contract.getName())) {
                // args 最后一个参数是命令
                if(args.length == 1) {
                    callback.onResponse(
                            new TransactionException(
                                    ChainMakerStatusCode.HandleGetContracts,
                                    String.format("请提供 %s 合约的 ABI", contract.getName())),
                            null);
                    return;
                } else {
                    ConfigUtils.writeContractABI(
                            chainMakerConnection.getConfigPath(),
                            contract.getName(),
                            (String) args[0]);
                }
            }
            chainMakerConnection.getConnectionEventHandler().onANewResource(resourceInfo);
            callback.onResponse(null, resourceProperties);
        } catch (ChainMakerCryptoSuiteException | ChainClientException | WeCrossException | IOException e) {
            logger.error("获取 {} 合约信息失败。{}", path.getResource(), e.getMessage());
            callback.onResponse(
                    new TransactionException(
                            ChainMakerStatusCode.HandleGetContracts,
                            String.format("获取 %s 合约信息失败", path.getResource())),
                    null);
        }
    }
}
