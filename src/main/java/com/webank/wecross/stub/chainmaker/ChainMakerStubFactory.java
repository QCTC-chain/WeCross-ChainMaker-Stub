package com.webank.wecross.stub.chainmaker;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Stub;
import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stub.WeCrossContext;
import com.webank.wecross.stub.chainmaker.account.ChainMakerAccountFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Stub("chainmaker")
public class ChainMakerStubFactory implements StubFactory {
    private Logger logger = LoggerFactory.getLogger(ChainMakerStubFactory.class);

    public static void main(String[] args) throws Exception {
        System.out.println("This is chainmaker Stub Plugin. Please copy this file to router/plugin/");
    }

    @Override
    public void init(WeCrossContext context) {
        logger.info("init chainmaker stub factory");
    }

    @Override
    public Driver newDriver() {
        Driver driver = new ChainMakerDriver();
        return driver;
    }

    @Override
    public Connection newConnection(String path) {
        try {
            logger.info("New connection: {}", path);
            ChainMakerConnection connection = ChainMakerConnectionFactory.build(path, "sdk_config.yml");
            
            // check proxy contract
            if(connection.hasProxyDeployed() == false) {
                String errorMsg = "WeCrossProxy error: WeCrossProxy contract has not been deployed!";
                throw new Exception(errorMsg);
            }

            // check hub contract
            if (!connection.hasHubDeployed()) {
                String errorMsg = "WeCrossHub error: WeCrossHub contract has not been deployed!";
                throw new Exception(errorMsg);
            }

            return connection;
        } catch (Exception ec) {
            logger.error("New connection, e: ", ec);
            return null;
        }
    }

    @Override
    public Account newAccount(Map<String, Object> properties) {
        logger.info("xxxxxxx xxxxxx newAccount, properties: {}", properties);
        return ChainMakerAccountFactory.build(properties);
    }

    @Override
    public void generateAccount(String path, String[] args) {
        logger.info("generateAccount, path: {}, args: {}", path, args);
    }

    @Override
    public void generateConnection(String path, String[] args) {
        logger.info("generateConnection, path: {}, args: {}", path, args);
    }
}
