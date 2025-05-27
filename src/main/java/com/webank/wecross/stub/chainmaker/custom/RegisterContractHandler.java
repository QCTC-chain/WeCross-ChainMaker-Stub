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
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
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

    private String generateAddress(String contractName) {
        String address = "";
        try {
            Method method = CryptoUtils.class.getDeclaredMethod(
                    "generteAddrStr",
                    byte[].class,
                    ChainConfigOuterClass.AddrType.class);
            method.setAccessible(true);
            // 通过 null 调用静态方法（如果方法是静态的）
            address = (String)method.invoke(null, contractName.getBytes(StandardCharsets.UTF_8),
                    ChainConfigOuterClass.AddrType.CHAINMAKER);
        } catch (Exception e) {
            logger.error("通过合约名生成合约地址失败。{}", contractName);
        }
        return address;
    }

    public void handle(
            Path path,
            Object[] args,
            Account account,
            BlockManager blockManager,
            Connection connection,
            Driver.CustomCommandCallback callback) {

        // 第一个参数是合约类型
        // 最后一个参数是 REGISTER_CHAINMAEKER_CONTRACT 指令
        // args: ["EVM", "{{abi_content}}", "REGISTER_CHAINMAEKER_CONTRACT"]
        // or: ["DOCKER_GO", "REGISTER_CHAINMAEKER_CONTRACT"]
        if(args.length < 2) {
            callback.onResponse(
                    new TransactionException(
                            ChainMakerStatusCode.HandleGetContracts,
                            String.format("注册合约 %s 失败，参数异常。args: [EVM, {solidity_contract_abi}] | [DOCKER_GO]",
                                    path.getResource())),
                    null);
            return;
        }
        String contractType = (String) args[0];
        String contractName = path.getResource();
        if("EVM".equals(contractType) && contractName.length() != 40) {
            contractName = generateAddress(contractName);
        }

        if("EVM".equals(contractType) && args.length == 2) {
            callback.onResponse(
                    new TransactionException(
                            ChainMakerStatusCode.HandleGetContracts,
                            String.format("请提供合约的 ABI")),
                    null);
            return;
        }

        ChainMakerConnection chainMakerConnection = (ChainMakerConnection) connection;
        try {
            ContractOuterClass.Contract contract = chainMakerConnection.getChainClient().getContractInfo(
                    contractName,
                    ChainMakerConnection.RPC_CALL_TIMEOUT);
            ResourceInfo resourceInfo = new ResourceInfo();
            resourceInfo.setStubType(chainMakerConnection.getProperties().get(ChainMakerConstant.CHAINMAKER_STUB_TYPE));
            resourceInfo.setName(path.getResource());
            Map<Object, Object> resourceProperties = new HashMap<>();
            resourceProperties.put(
                    ChainMakerConstant.CHAINMAKER_CHAIN_ID,
                    chainMakerConnection.getProperties().get(ChainMakerConstant.CHAINMAKER_CHAIN_ID));
            resourceProperties.put(
                    ChainMakerConstant.CHAINMAKER_CONTRACT_NAME,
                    contract.getName());
            resourceProperties.put(
                    ChainMakerConstant.CHAINMAKER_CONTRACT_ADDRESS,
                    contract.getAddress());
            resourceProperties.put(
                    ChainMakerConstant.CHAINMAKER_CONTRACT_VERSION,
                    contract.getVersion());
            resourceProperties.put(
                    ChainMakerConstant.CHAINMAKER_CONTRACT_RUNTIME_TYPE,
                    contract.getRuntimeType().name());
            resourceInfo.setProperties(resourceProperties);

            if("EVM".equals(contractType)
                    && !isExistsABI(chainMakerConnection.getConfigPath(), contract.getName())) {
                ConfigUtils.writeContractABI(
                        chainMakerConnection.getConfigPath(),
                        contract.getName(),
                        (String) args[1]);
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
