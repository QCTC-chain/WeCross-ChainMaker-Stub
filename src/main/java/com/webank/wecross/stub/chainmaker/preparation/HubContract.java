package com.webank.wecross.stub.chainmaker.preparation;

import com.webank.wecross.stub.chainmaker.ChainMakerConnection;
import com.webank.wecross.stub.chainmaker.ChainMakerConnectionFactory;
import com.webank.wecross.stub.chainmaker.config.ChainMakerStubConfigParser;
import com.webank.wecross.stub.chainmaker.config.EndorsementEntry;
import com.webank.wecross.stub.chainmaker.utils.ConfigUtils;
import org.chainmaker.sdk.User;
import org.chainmaker.sdk.utils.FileUtils;
import org.chainmaker.sdk.utils.UtilsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HubContract {
    private static final Logger logger = LoggerFactory.getLogger(HubContract.class);

    private ChainMakerConnection chainMakerConnection;
    private String chainPath;
    private List<User> endorsementUsers = new ArrayList<>();

    public HubContract(String chainPath) throws Exception {
        if(!chainPath.contains("classpath:")) {
            this.chainPath = "classpath:" + chainPath;
        }

        this.chainMakerConnection = ChainMakerConnectionFactory.build(
                this.chainPath, "sdk_config.yml");

        try {
            List<EndorsementEntry> endorsementEntries = null;
            ChainMakerStubConfigParser stubConfigParser = new ChainMakerStubConfigParser(
                    chainPath, "stub.toml");
            endorsementEntries = stubConfigParser.loadEndorsementEntry();
            for(EndorsementEntry entry: endorsementEntries) {
                try {
                    String userSignKeyFilePath = this.chainPath + File.separator + entry.getUserSignKeyFilePath();
                    userSignKeyFilePath = ConfigUtils.classpath2Absolute(userSignKeyFilePath);
                    String userSignCrtFilePath = this.chainPath + File.separator + entry.getUserSignCrtFilePath();
                    userSignCrtFilePath = ConfigUtils.classpath2Absolute(userSignCrtFilePath);

                    String userKeyFilePath = this.chainPath + File.separator + entry.getUserKeyFilePath();
                    userKeyFilePath = ConfigUtils.classpath2Absolute(userKeyFilePath);
                    String userCrtFilePath = this.chainPath + File.separator + entry.getUserCrtFilePath();
                    userCrtFilePath = ConfigUtils.classpath2Absolute(userCrtFilePath);

                    this.endorsementUsers.add(
                            new User(
                                    entry.getOrgId(),
                                    FileUtils.getFileBytes(userSignKeyFilePath),
                                    FileUtils.getFileBytes(userSignCrtFilePath),
                                    FileUtils.getFileBytes(userKeyFilePath),
                                    FileUtils.getFileBytes(userCrtFilePath))
                    );
                } catch (UtilsException e) {
                    logger.warn("getResourceFileBytes was failure. e: {}", e);
                    throw e;
                }
            }
        } catch (IOException e) {
            logger.warn("parse a stub config was failure. e: {}", e);
            throw e;
        }
    }

    public void deploy() throws Exception {
        if(!chainMakerConnection.hasHubDeployed()) {
            DeployWeCrossContract deployWeCrossContract = new DeployWeCrossContract(
                    this.chainMakerConnection,
                    this.endorsementUsers,
                    this.chainPath);
            deployWeCrossContract.deployWeCrossHub(true);
        } else {
            System.out.println(
                    "SUCCESS: WeCrossHub has already been deployed! chain: " + chainPath);
        }
    }

    public void upgrade() throws Exception {
        if(chainMakerConnection.hasHubDeployed()) {
            DeployWeCrossContract deployWeCrossContract = new DeployWeCrossContract(
                    this.chainMakerConnection,
                    this.endorsementUsers,
                    this.chainPath);
            deployWeCrossContract.deployWeCrossHub(false);
        } else {
            System.err.println(
                    "FAILURE: WeCrossHub has not been deployed! chain: " + chainPath);
        }
    }
}
