package com.webank.wecross.stub.chainmaker.client;

import org.chainmaker.sdk.ChainManager;
import org.chainmaker.sdk.config.NodeConfig;
import org.chainmaker.sdk.config.SdkConfig;
import org.chainmaker.sdk.utils.FileUtils;
import org.chainmaker.sdk.ChainClient;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.Yaml;

import com.webank.wecross.stub.chainmaker.utils.ConfigUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ChainMakerClient {
    private static final Logger logger = LoggerFactory.getLogger(ChainMakerClient.class);

    private static ChainManager chainManager;

    public static SdkConfig loadConfig(String stubConfigPath, String configName) throws Exception {
        if (stubConfigPath.contains("classpath:")) {
            stubConfigPath = ConfigUtils.classpath2Absolute(stubConfigPath);
        }
        String sdk_config_path = String.format("%s/%s", stubConfigPath, configName);

        logger.info("load config: {}", sdk_config_path);

        Yaml yaml = new Yaml();
        InputStream in = new FileInputStream(sdk_config_path);

        SdkConfig sdkConfig;
        sdkConfig = yaml.loadAs(in, SdkConfig.class);
        assert in != null;
        in.close();

        sdkConfig.getChainClient().setUserCrtFilePath(
            stubConfigPath + File.separator + sdkConfig.getChainClient().getUserCrtFilePath());
        sdkConfig.getChainClient().setUserKeyFilePath(
            stubConfigPath + File.separator + sdkConfig.getChainClient().getUserKeyFilePath());
        sdkConfig.getChainClient().setUserSignCrtFilePath(
            stubConfigPath + File.separator + sdkConfig.getChainClient().getUserSignCrtFilePath());
        sdkConfig.getChainClient().setUserSignKeyFilePath(
            stubConfigPath + File.separator + sdkConfig.getChainClient().getUserSignKeyFilePath());

        logger.info("load user crt: {}", sdkConfig.getChainClient().getUserCrtFilePath());
        logger.info("load user key: {}", sdkConfig.getChainClient().getUserKeyFilePath());
        logger.info("load user sign crt: {}", sdkConfig.getChainClient().getUserSignCrtFilePath());
        logger.info("load user sign key: {}", sdkConfig.getChainClient().getUserSignKeyFilePath());

        for (NodeConfig nodeConfig : sdkConfig.getChainClient().getNodes()) {
            List<byte[]> tlsCaCertList = new ArrayList<>();
            if (nodeConfig.getTrustRootPaths() != null) {
                for (String rootPath : nodeConfig.getTrustRootPaths()) {
                    rootPath = stubConfigPath + File.separator + rootPath;
                    logger.info("load root path: {}", rootPath);
                    List<String> filePathList = FileUtils.getFilesByPath(rootPath);
                    for (String filePath : filePathList) {
                        tlsCaCertList.add(FileUtils.getFileBytes(filePath));
                    }
                }
            }
            byte[][] tlsCaCerts = new byte[tlsCaCertList.size()][];
            tlsCaCertList.toArray(tlsCaCerts);
            nodeConfig.setTrustRootBytes(tlsCaCerts);
        }

        return sdkConfig;
    }

    public static ChainClient createChainClient(String stubConfigPath, String configName) throws Exception{
        SdkConfig sdkConfig = loadConfig(stubConfigPath, configName);
        chainManager = ChainManager.getInstance();
        ChainClient chainClient = chainManager.getChainClient(sdkConfig.getChainClient().getChainId());
        if(chainClient == null) {
            chainClient = chainManager.createChainClientWithoutPool(sdkConfig);
        }
        logger.info("chianmaker's config: {}", chainClient.getChainConfig(5000));
        return chainClient;
    }
}
