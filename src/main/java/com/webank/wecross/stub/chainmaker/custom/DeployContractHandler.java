package com.webank.wecross.stub.chainmaker.custom;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.*;
import com.webank.wecross.stub.chainmaker.common.ChainMakerConstant;
import com.webank.wecross.stub.chainmaker.common.ChainMakerStatusCode;
import com.webank.wecross.stub.chainmaker.config.ChainMakerStubConfigParser;
import com.webank.wecross.stub.chainmaker.config.EndorsementEntry;
import com.webank.wecross.stub.chainmaker.utils.ConfigUtils;
import org.chainmaker.sdk.User;
import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;
import org.chainmaker.sdk.utils.FileUtils;
import org.chainmaker.sdk.utils.UtilsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DeployContractHandler implements CommandHandler {

    private Logger logger = LoggerFactory.getLogger(DeployContractHandler.class);
    private List<User> endorsementUsers = new ArrayList<>();
    private Driver driver;
    public static DeployContractHandler build(String stubConfigPath, String configName) {
        DeployContractHandler handler = new DeployContractHandler();
        try {
            List<EndorsementEntry> endorsementEntries = null;
            ChainMakerStubConfigParser stubConfigParser = new ChainMakerStubConfigParser(stubConfigPath, configName);
            endorsementEntries = stubConfigParser.loadEndorsementEntry();
            for(EndorsementEntry entry: endorsementEntries) {
                try {
                    String userSignKeyFilePath = stubConfigPath + File.separator + entry.getUserSignKeyFilePath();
                    userSignKeyFilePath = ConfigUtils.classpath2Absolute(userSignKeyFilePath);
                    String userSignCrtFilePath = stubConfigPath + File.separator + entry.getUserSignCrtFilePath();
                    userSignCrtFilePath = ConfigUtils.classpath2Absolute(userSignCrtFilePath);

                    String userKeyFilePath = stubConfigPath + File.separator + entry.getUserKeyFilePath();
                    userKeyFilePath = ConfigUtils.classpath2Absolute(userKeyFilePath);
                    String userCrtFilePath = stubConfigPath + File.separator + entry.getUserCrtFilePath();
                    userCrtFilePath = ConfigUtils.classpath2Absolute(userCrtFilePath);

                    handler.endorsementUsers.add(
                            new User(
                                    entry.getOrgId(),
                                    FileUtils.getFileBytes(userSignKeyFilePath),
                                    FileUtils.getFileBytes(userSignCrtFilePath),
                                    FileUtils.getFileBytes(userKeyFilePath),
                                    FileUtils.getFileBytes(userCrtFilePath))
                    );
                } catch (UtilsException e) {
                    handler.logger.warn("getResourceFileBytes was failure. e: {}", e);
                    return null;
                } catch (ChainMakerCryptoSuiteException e) {
                    handler.logger.warn("build a deploy handler was failure. e: {}", e);
                    return null;
                } catch (WeCrossException e) {
                    handler.logger.warn("classpath2Absolute was failure. e: {}", e);
                    return null;
                }
            }
        } catch (IOException e) {
            handler.logger.warn("parse a stub config was failure. e: {}", e);
            return null;
        }
        return  handler;
    }

    @Override
    public void handle(
            Path path,
            Object[] args,
            Account account,
            BlockManager blockManager,
            Connection connection,
            Driver.CustomCommandCallback callback) {

        TransactionContext transactionContext = new TransactionContext(
                account,
                path,
                new ResourceInfo(),
                blockManager);

        List<String> requestArgs = new ArrayList<>();
        String method = (String) args[args.length - 1];
        Object[] newArgs = Arrays.copyOf(args, args.length - 1);
        for(Object arg: newArgs) {
            requestArgs.add((String)arg);
        }

        TransactionRequest transactionRequest = new TransactionRequest(
                method,
                requestArgs.stream().toArray(String[]::new));

        Map<String, Object> options = new HashMap<>();
        options.put("EndorsementEntries", this.endorsementUsers);
        transactionRequest.setOptions(options);

        this.driver.asyncSendTransaction(
                transactionContext,
                transactionRequest,
                false,
                connection,
                (exception, res) -> {
                    if (Objects.nonNull(exception)) {
                        logger.error(" deploy a contract was failure. e: ", exception);
                        callback.onResponse(exception, null);
                        return;
                    }

                    if (res.getErrorCode() != ChainMakerStatusCode.Success) {
                        logger.error(
                                " deploy a contract, error: {}, message: {}",
                                res.getErrorCode(),
                                res.getMessage());
                        callback.onResponse(new Exception(res.getMessage()), null);
                        return;
                    }

                    callback.onResponse(null, res.getResult()[0]);
                });
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }
}
