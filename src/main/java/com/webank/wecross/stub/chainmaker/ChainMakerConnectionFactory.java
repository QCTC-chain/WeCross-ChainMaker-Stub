package com.webank.wecross.stub.chainmaker;

import org.chainmaker.sdk.ChainClient;

import com.webank.wecross.stub.chainmaker.client.ChainMakerClient;

public class ChainMakerConnectionFactory {
    public static ChainMakerConnection build(String stubConfigPath, String configName) throws Exception {
        ChainClient chainClient = ChainMakerClient.createChainClient(stubConfigPath, configName);
        return new ChainMakerConnection(chainClient);
    }
}
