package com.webank.wecross.stub.chainmaker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Stub;
import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stub.WeCrossContext;
import com.webank.wecross.stub.chainmaker.account.ChainMakerAccountFactory;

import com.webank.wecross.stub.chainmaker.common.ChainMakerConstant;
import com.webank.wecross.stub.chainmaker.custom.CommandHandlerDispatcher;
import com.webank.wecross.stub.chainmaker.custom.DeployContractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Stub("chainmaker")
public class ChainMakerStubFactory implements StubFactory {
    private Logger logger = LoggerFactory.getLogger(ChainMakerStubFactory.class);

    private String stubConfigPath = "";

    public static void main(String[] args) throws Exception {
        System.out.println("This is chainmaker Stub Plugin. Please copy this file to router/plugin/");
    }

    @Override
    public void init(WeCrossContext context) {
        logger.info("init chainmaker stub factory");
    }

    @Override
    public Driver newDriver() {
        logger.info("New Driver");

        if (stubConfigPath.isEmpty()) {
            logger.warn("stubConfigPath is empty.");
            return null;
        }
        ChainMakerDriver driver = new ChainMakerDriver();

        DeployContractHandler deployContractHandler = DeployContractHandler.build(stubConfigPath, "stub.toml");
        deployContractHandler.setDriver(driver);

        CommandHandlerDispatcher commandHandlerDispatcher = new CommandHandlerDispatcher();
        commandHandlerDispatcher.registerCommandHandler(ChainMakerConstant.CUSTOM_COMMAND_DEPLOY, deployContractHandler);


        driver.setCommandHandlerDispatcher(commandHandlerDispatcher);

        return driver;
    }

    @Override
    public Connection newConnection(String path) {
        try {
            logger.info("New connection: {}", path);
            stubConfigPath = path;
            ChainMakerConnection connection = ChainMakerConnectionFactory.build(stubConfigPath, "sdk_config.yml");
            
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
        try {
            String ext = (String) properties.get("ext0");
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> chainMakerProperties =
                    objectMapper.readValue(ext, new TypeReference<Map<String, String>>(){});

            chainMakerProperties.put("userKey", (String) properties.get("secKey"));
            chainMakerProperties.put("userCert", (String) properties.get("pubKey"));
            chainMakerProperties.put("username", (String) properties.get("username"));
            chainMakerProperties.put("type", (String) properties.get("type"));

            logger.info("newAccount, properties: {}", chainMakerProperties);

            return ChainMakerAccountFactory.build(chainMakerProperties);
        } catch (JsonProcessingException e) {
            logger.warn("newAccount was failure. e: ", e);
            return null;
        }
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
