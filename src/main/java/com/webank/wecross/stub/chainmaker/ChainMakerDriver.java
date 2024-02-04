package com.webank.wecross.stub.chainmaker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.*;
import com.webank.wecross.stub.chainmaker.account.ChainMakerAccount;
import com.webank.wecross.stub.chainmaker.common.BlockUtility;
import com.webank.wecross.stub.chainmaker.common.ChainMakerConstant;
import com.webank.wecross.stub.chainmaker.common.ChainMakerRequestType;

import com.google.protobuf.InvalidProtocolBufferException;

import com.webank.wecross.stub.chainmaker.common.ChainMakerStatusCode;
import com.webank.wecross.stub.chainmaker.custom.CommandHandler;
import com.webank.wecross.stub.chainmaker.custom.CommandHandlerDispatcher;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.chainmaker.pb.common.ChainmakerBlock;
import org.chainmaker.pb.common.ChainmakerTransaction;
import org.chainmaker.pb.common.ContractOuterClass;
import org.chainmaker.pb.common.ResultOuterClass;
import org.chainmaker.sdk.ChainClientException;
import org.chainmaker.sdk.User;
import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;
import org.chainmaker.sdk.utils.SdkUtils;
import org.chainmaker.sdk.utils.UtilsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChainMakerDriver implements Driver {
    private static final Logger logger = LoggerFactory.getLogger(ChainMakerDriver.class);
    private CommandHandlerDispatcher commandHandlerDispatcher;

    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    public void setCommandHandlerDispatcher(CommandHandlerDispatcher commandHandlerDispatcher) {
        this.commandHandlerDispatcher = commandHandlerDispatcher;
    }
    @Override
    public ImmutablePair<Boolean, TransactionRequest> decodeTransactionRequest(Request request) {
        logger.debug("decodeTransactionRequest, request: {}", request);
        return new ImmutablePair<>(true, null);
    }

    @Override
    public List<ResourceInfo> getResources(Connection connection) {
        logger.info("xxxxxx getResources:w");
        if (connection instanceof ChainMakerConnection) {
            return ((ChainMakerConnection) connection).getResources();
        }

        logger.error(" Not chainMaker connection, connection name: {}", connection.getClass().getName());
        return new ArrayList<>();
    }

    @Override
    public void asyncCall(
        TransactionContext context, 
        TransactionRequest request, 
        boolean byProxy, 
        Connection connection, 
        Callback callback) {
        logger.info("async, context: {}, request: {}, proxy: {}", context, request, byProxy);
    }

    @Override
    public void asyncSendTransaction(
        TransactionContext context, 
        TransactionRequest request, 
        boolean byProxy, 
        Connection connection, 
        Callback callback) {
        asyncSendTransactionByProxy(context, request, connection, callback);
    }

    private void asyncSendTransactionByProxy(
            TransactionContext context,
            TransactionRequest request,
            Connection connection,
            Callback callback) {
        String method = request.getMethod();
        Path path = context.getPath();
        logger.info("asyncSendTransaction. path: {}, context: {}, request: {}", path, context, request);

        if(method.equals(ChainMakerConstant.CUSTOM_COMMAND_DEPLOY)) {
            // 部署合约
            deployContract(context, request, connection, callback);
        } else {

        }
    }

    private void deployContract(
            TransactionContext context,
            TransactionRequest request,
            Connection connection,
            Callback callback
    ) {
        ChainMakerConnection chainMakerConnection = (ChainMakerConnection)connection;
        byte[] contractBinary = request.getArgs()[1].getBytes();
        String contractName = request.getArgs()[2];
        String version = request.getArgs()[3];
        List<User> users = (List<User>)request.getOptions().get("EndorsementEntries");
        try {
            org.chainmaker.pb.common.Request.Payload payload = chainMakerConnection
                    .getChainClient()
                    .createContractCreatePayload(
                            contractName,
                            version,
                            contractBinary, ContractOuterClass.RuntimeType.EVM, null);
            org.chainmaker.pb.common.Request.EndorsementEntry[] endorsementEntries = SdkUtils.getEndorsers(
                    payload, users.stream().toArray(User[]::new));

            Request weCrossRequest = Request.newRequest(
                    ChainMakerRequestType.CREATE_CUSTOMER_CONTRACT,
                    payload.toByteArray());
            ResourceInfo resourceInfo = new ResourceInfo();
            resourceInfo.setName(context.getPath().getResource());
            resourceInfo.getProperties().put(ChainMakerConstant.CHAINMAKER_ENDORSEMENTENTRY, endorsementEntries);
            weCrossRequest.setResourceInfo(resourceInfo);

            chainMakerConnection.asyncSend(
                    weCrossRequest,
                    response -> {
                        if (response.getErrorCode() != ChainMakerStatusCode.Success) {
                            logger.warn("deployContract, errorCode: {}, errorMessage: {}",
                                    response.getErrorCode(),
                                    response.getErrorMessage());
                            callback.onTransactionResponse(
                                    new TransactionException(
                                            ChainMakerStatusCode.HandleDeployContract, response.getErrorMessage()),
                                    null
                            );
                        } else {
                            try {

                                ResultOuterClass.ContractResult contractResult = ResultOuterClass
                                        .ContractResult.parseFrom(response.getData());
                                TransactionResponse transactionResponse = new TransactionResponse();
                                transactionResponse.setErrorCode(ResultOuterClass.TxStatusCode.SUCCESS.getNumber());
                                String[] result = new String[]{String.format("%d", contractResult.getCode()), contractResult.getMessage()};
                                transactionResponse.setResult(result);
                                callback.onTransactionResponse(null, transactionResponse);
                            } catch (InvalidProtocolBufferException e) {
                                callback.onTransactionResponse(
                                        new TransactionException(ChainMakerStatusCode.HandleDeployContract, e.getMessage()),
                                        null
                                );
                            }
                        }
                    });

        } catch (ChainMakerCryptoSuiteException e) {
            logger.warn("deploy contract {} was failure. e: {}", contractName, e.getMessage());
            callback.onTransactionResponse(
                    new TransactionException(ChainMakerStatusCode.HandleDeployContract, e.getMessage()),
                    null
            );
        } catch (UtilsException e) {
            logger.warn("deploy contract {} was failure. e: {}", contractName, e.getMessage());
            callback.onTransactionResponse(
                    new TransactionException(ChainMakerStatusCode.HandleDeployContract, e.getMessage()),
                    null
            );
        }
    }

    @Override
    public void asyncGetBlockNumber(
        Connection connection, 
        GetBlockNumberCallback callback) {
        Request request = Request.newRequest(ChainMakerRequestType.GET_BLOCK_NUMBER, "");
        connection.asyncSend(
            request, 
            response -> {
                if (response.getErrorCode() != 0) {
                    logger.warn("asyncGetBlockNumber, errorCode: {}, errorMessage: {}", 
                        response.getErrorCode(), 
                        response.getErrorMessage());
                    callback.onResponse(new Exception(response.getErrorMessage()), -1);
                } else {
                    BigInteger blockNumber = new BigInteger(response.getData());
                    callback.onResponse(null, blockNumber.longValue());
                }
            });
    }

    @Override
    public void asyncGetBlock(
        long blockNumber, 
        boolean onlyHeader, 
        Connection connection, 
        GetBlockCallback callback) {
        Request request = Request.newRequest(
            ChainMakerRequestType.GET_BLOCK_BY_NUMBER, 
            BigInteger.valueOf(blockNumber).toByteArray());
        
        connection.asyncSend(
            request, 
            response -> {
                if(response.getErrorCode() != 0) {
                    logger.warn(
                        " asyncGetBlock, errorCode: {},  errorMessage: {}",
                        response.getErrorCode(),
                        response.getErrorMessage());

                    callback.onResponse(new Exception(response.getErrorMessage()), null);
                } else {
                    try {
                        ChainmakerBlock.BlockInfo blockInfo = ChainmakerBlock.BlockInfo.parseFrom(response.getData());
                        Block block = BlockUtility.convertToBlock(blockInfo);
                        callback.onResponse(null, block);
                    } catch (InvalidProtocolBufferException ec) {
                        logger.warn("blockNumber: {}, e: ", blockNumber, ec);
                        callback.onResponse(ec, null);
                    }
                }               
            });    
    }

    @Override
    public void asyncGetTransaction(
        String transactionHash, 
        long blockNumber, 
        BlockManager blockManager, 
        boolean isVerified, 
        Connection connection, 
        GetTransactionCallback callback) {
        
        Request request = Request.newRequest(ChainMakerRequestType.GET_TRANSACTION, transactionHash);
        connection.asyncSend(
            request, 
            response -> {
                if(response.getErrorCode() != 0) {
                    logger.warn(
                        " asyncGetTransaction, errorCode: {},  errorMessage: {}",
                        response.getErrorCode(),
                        response.getErrorMessage());

                    callback.onResponse(new Exception(response.getErrorMessage()), null);
                } else {
                    try {
                        ChainmakerTransaction.Transaction chainMakerTransaction = ChainmakerTransaction.Transaction.parseFrom(response.getData());
                        Transaction transaction = BlockUtility.convertToTransaction(chainMakerTransaction);
                        callback.onResponse(null, transaction);
                    } catch (InvalidProtocolBufferException ec) {
                        logger.warn("blockNumber: {}, e: ", blockNumber, ec);
                        callback.onResponse(ec, null);
                    }
                }
            });
    }

    @Override
    public void asyncCustomCommand(
        String command, 
        Path path, 
        Object[] args, 
        Account account, 
        BlockManager blockManager, 
        Connection connection, 
        CustomCommandCallback callback) {
        logger.info("asyncCustomCommand, commond: {}, path: {}, args: {}", command, path, args);
        CommandHandler commandHandler = commandHandlerDispatcher.matchCommandHandler(command);
        if (Objects.isNull(commandHandler)) {
            callback.onResponse(new Exception("Command not found, command: " + command), null);
            return;
        }
        commandHandler.handle(path, args, account, blockManager, connection, callback);
    }

    @Override
    public byte[] accountSign(Account account, byte[] message) {
        if (!(account instanceof ChainMakerAccount)) {
            throw new UnsupportedOperationException(
                "Not ChainMakerAccount, account name: " + account.getClass().getName());
        }
        ChainMakerAccount chainMakerAccount = (ChainMakerAccount)account;
        return chainMakerAccount.sign(message);
    }

    @Override
    public boolean accountVerify(String identity, byte[] signBytes, byte[] message) {
        return false;
    }
}
