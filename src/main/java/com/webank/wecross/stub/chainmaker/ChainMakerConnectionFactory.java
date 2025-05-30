package com.webank.wecross.stub.chainmaker;

import com.webank.wecross.stub.chainmaker.common.ChainMakerConstant;
import com.webank.wecross.stub.chainmaker.config.ChainMakerStubConfigParser;
import com.webank.wecross.stub.chainmaker.utils.CryptoUtils;
import org.chainmaker.pb.common.ContractOuterClass;
import org.chainmaker.sdk.ChainClient;

import com.webank.wecross.stub.chainmaker.client.ChainMakerClient;
import org.chainmaker.sdk.ChainClientException;
import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;

public class ChainMakerConnectionFactory {
    public static ChainMakerConnection build(String stubConfigPath, String configName) throws Exception {
        ChainClient chainClient = ChainMakerClient.createChainClient(stubConfigPath, configName);
        ChainMakerConnection connection = new ChainMakerConnection(chainClient);
        try {
            ChainMakerStubConfigParser stubConfigParser = new ChainMakerStubConfigParser(
                    stubConfigPath, "stub.toml");
            connection.addProperty(ChainMakerConstant.CHAINMAKER_CHAIN_ID, chainClient.getChainId());
            connection.addProperty(ChainMakerConstant.CHAINMAKER_STUB_TYPE, stubConfigParser.getStubType());

            ContractOuterClass.Contract[] contracts = chainClient.getContractList(5000);
            for(ContractOuterClass.Contract contract : contracts) {
                if (ChainMakerConstant.CHAINMAKER_HUB_NAME.equals(contract.getName())
                        || CryptoUtils.generateAddress(ChainMakerConstant.CHAINMAKER_HUB_NAME).equals(contract.getName())) {
                    connection.addProperty(ChainMakerConstant.CHAINMAKER_HUB_NAME, contract.getAddress());
                }
                if (ChainMakerConstant.CHAINMAKER_PROXY_NAME.equals(contract.getName())
                        || CryptoUtils.generateAddress(ChainMakerConstant.CHAINMAKER_PROXY_NAME).equals(contract.getName())) {
                    connection.addProperty(ChainMakerConstant.CHAINMAKER_PROXY_NAME, contract.getAddress());
                }
            }
        } catch (ChainMakerCryptoSuiteException e) {

        } catch (ChainClientException e) {

        } catch (Exception e) {

        }
        return  connection;
    }
}
