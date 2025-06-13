package com.webank.wecross.stub.chainmaker;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.*;
import com.webank.wecross.stub.chainmaker.abi.wrapper.*;
import com.webank.wecross.stub.chainmaker.account.ChainMakerAccount;
import com.webank.wecross.stub.chainmaker.common.BlockUtility;
import com.webank.wecross.stub.chainmaker.common.ChainMakerConstant;
import com.webank.wecross.stub.chainmaker.common.ChainMakerRequestType;

import com.google.protobuf.InvalidProtocolBufferException;

import com.webank.wecross.stub.chainmaker.common.ChainMakerStatusCode;
import com.webank.wecross.stub.chainmaker.custom.CommandHandler;
import com.webank.wecross.stub.chainmaker.custom.CommandHandlerDispatcher;
import com.webank.wecross.stub.chainmaker.utils.ConfigUtils;
import com.webank.wecross.stub.chainmaker.utils.FunctionUtility;
import com.webank.wecross.stub.chainmaker.utils.Serialization;
import com.webank.wecross.stub.chainmaker.utils.Web3jFunctionBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.chainmaker.pb.common.ChainmakerBlock;
import org.chainmaker.pb.common.ChainmakerTransaction;
import org.chainmaker.pb.common.ContractOuterClass;
import org.chainmaker.pb.common.ResultOuterClass;
import org.chainmaker.sdk.ChainClient;
import org.chainmaker.sdk.ChainManager;
import org.chainmaker.sdk.User;
import org.chainmaker.sdk.ChainClientException;
import org.chainmaker.sdk.config.ChainClientConfig;
import org.chainmaker.sdk.config.SdkConfig;
import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;
import org.chainmaker.sdk.utils.SdkUtils;
import org.chainmaker.sdk.utils.UtilsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.*;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ChainMakerDriver implements Driver {
    private static final Logger logger = LoggerFactory.getLogger(ChainMakerDriver.class);
    private CommandHandlerDispatcher commandHandlerDispatcher;

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
        asyncCallOrInvokeDirectly(false, context, request, connection, callback);
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
        if(method.equals(ChainMakerConstant.CUSTOM_COMMAND_DEPLOY_CONTRACT)) {
            // 部署用户合约
            deployWeCrossCustomerContract(true, context, request, connection, callback);
        } else if (method.equals(ChainMakerConstant.CUSTOM_COMMAND_UPGRADE_CONTRACT)) {
            // 升级用户合约
            deployWeCrossCustomerContract(false, context, request, connection, callback);
        } else {
            //invokeSendTransaction(context, request, connection, callback);
            asyncCallOrInvokeDirectly(true, context, request, connection, callback);
        }
    }

    private void deployWeCrossCustomerContract(
            boolean deploy,
            TransactionContext context,
            TransactionRequest request,
            Connection connection,
            Callback callback
    ) {
        Path path = context.getPath();
        ChainMakerConnection chainMakerConnection = (ChainMakerConnection)connection;
        try {
            chainMakerConnection = ChainMakerConnectionFactory.build(chainMakerConnection.getConfigPath(), context.getAccount());
        } catch (Exception e) {}

        ContractOuterClass.RuntimeType runtimeType = ContractOuterClass.RuntimeType.valueOf(request.getArgs()[0]);
        byte[] contractBinary = request.getArgs()[1].getBytes();
        String contractABIContent = request.getArgs()[2];
        String contractName = request.getArgs()[3];
        String version = request.getArgs()[4];
        int leftSize = request.getArgs().length - 5;
        if(leftSize > 0 && (leftSize % 2) != 0) {
            callback.onTransactionResponse(
                    new TransactionException(
                            ChainMakerStatusCode.HandleDeployContract,
                            String.format("%s合约的参数不匹配。", deploy ? "部署" : "升级")),
                    null
            );
            return;
        }

        try {
            Map<String, byte[]> params = new HashMap<>();
            if (runtimeType == ContractOuterClass.RuntimeType.DOCKER_GO) {
                for(int i = 5; i < request.getArgs().length; i+=2) {
                    params.put(request.getArgs()[i], request.getArgs()[i + 1].getBytes(StandardCharsets.UTF_8));
                }

                if (params.isEmpty()) {
                    params = null;
                }
            } else if (runtimeType == ContractOuterClass.RuntimeType.EVM) {
                if(contractABIContent.isEmpty()) {
                    callback.onTransactionResponse(
                            new TransactionException(
                                    ChainMakerStatusCode.HandleDeployContract,
                                    "缺少合约 ABI"),
                            null
                    );
                    return;
                }
                ConfigUtils.writeContractABI(chainMakerConnection.getConfigPath(), path.getResource(), contractABIContent);
                Web3jFunctionBuilder builder = new Web3jFunctionBuilder();
                Function constructor = builder.buildFunctionFromAbi(
                        contractABIContent,
                        "constructor",
                        Arrays.copyOfRange(request.getArgs(), 5, request.getArgs().length));
                String callData = FunctionEncoder.encodeConstructor(constructor.getInputParameters());
                params.put("data", callData.getBytes(StandardCharsets.UTF_8));
            }

            org.chainmaker.pb.common.Request.Payload payload = null;
            if(deploy) {
                payload = chainMakerConnection
                        .getChainClient()
                        .createContractCreatePayload(
                                contractName,
                                version,
                                contractBinary, runtimeType, params);
            } else {
                payload = chainMakerConnection
                        .getChainClient()
                        .createContractUpgradePayload(
                                contractName,
                                version,
                                contractBinary, runtimeType, params);
            }

            List<User> users = (List<User>)request.getOptions().get("EndorsementEntries");
            org.chainmaker.pb.common.Request.EndorsementEntry[] endorsementEntries = SdkUtils.getEndorsers(
                    payload, users.stream().toArray(User[]::new));

            Request weCrossRequest = Request.newRequest(
                    deploy ? ChainMakerRequestType.CREATE_CUSTOMER_CONTRACT : ChainMakerRequestType.UPGRADE_CUSTOMER_CONTRACT,
                    payload.toByteArray());
            ResourceInfo resourceInfo = new ResourceInfo();
            resourceInfo.setName(context.getPath().getResource());
            resourceInfo.getProperties().put(ChainMakerConstant.CHAINMAKER_ENDORSEMENTENTRY, endorsementEntries);
            resourceInfo.getProperties().put(ChainMakerConstant.CHAINMAKER_CONTRACT_NAME, contractName);
            resourceInfo.getProperties().put(ChainMakerConstant.CHAINMAKER_CONTRACT_VERSION, version);
            resourceInfo.getProperties().put(ChainMakerConstant.CHAINMAKER_CONTRACT_RUNTIME_TYPE, runtimeType.name());
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
        } catch (IOException e) {
            logger.warn("deploy contract {} was failure. e: {}", contractName, e.getMessage());
            callback.onTransactionResponse(
                    new TransactionException(ChainMakerStatusCode.HandleDeployContract, e.getMessage()),
                    null
            );
        } catch (WeCrossException e) {
            logger.warn("deploy contract {} was failure. e: {}", contractName, e.getMessage());
            callback.onTransactionResponse(
                    new TransactionException(ChainMakerStatusCode.HandleDeployContract, e.getMessage()),
                    null
            );
        }
    }

    private void asyncCallOrInvokeDirectly(
            boolean isInvoke,
            TransactionContext context,
            TransactionRequest request,
            Connection connection,
            Callback callback) {

        Path path = context.getPath();
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("method", request.getMethod());
        requestData.put("args", request.getArgs());
        requestData.put("identify", context.getAccount().getIdentity());
        try {
            Request weCrossRequest = Request.newRequest(
                    isInvoke ? ChainMakerRequestType.SEND_RAW_TRANSACTION : ChainMakerRequestType.CALL_RAW_TRANSACTION,
                    Serialization.serialize(requestData));
            weCrossRequest.setPath(path.toString());
            ChainMakerConnection chainMakerConnection = (ChainMakerConnection)connection;
            chainMakerConnection = ChainMakerConnectionFactory.build(
                    chainMakerConnection.getConfigPath(),
                    context.getAccount());
            chainMakerConnection.asyncSend(
                    weCrossRequest,
                    response -> {
                        handleAsyncSendResponse(response, context, request, callback);
                    }
            );
        } catch (Exception e) {
            logger.error("调用 asyncCallOrInvokeDirectly 失败。 {}", e.getMessage());
        }
    }

    // 获取区块高度
    private long getBlockNumber(BlockManager blockManager) {
        final CompletableFuture<Long> getContractListFuture = new CompletableFuture<Long>();
        blockManager.asyncGetBlockNumber(
                (blockNumberException, blockNumber) -> {
                    if (Objects.nonNull(blockNumberException)) {
                        getContractListFuture.complete(0L);
                    } else {
                        getContractListFuture.complete(blockNumber);
                    }
                }
        );
        long blockNum = 0;
        try {
            blockNum = getContractListFuture.get();
        } catch (Exception e) {
            blockNum = 0;
        }

        return  blockNum;
    }

    private void handleAsyncSendResponse(
            Response response,
            TransactionContext context,
            TransactionRequest request,
            Callback callback) {
        if (response.getErrorCode() != ChainMakerStatusCode.Success) {
            logger.warn("sendTransaction, errorCode: {}, errorMessage: {}",
                    response.getErrorCode(),
                    response.getErrorMessage());
            callback.onTransactionResponse(
                    new TransactionException(
                            ChainMakerStatusCode.HandleInvokeWeCrossProxyFailed,
                            response.getErrorMessage()),
                    null
            );
        } else {
            try {
                Map<String, Object> responseBody = (Map<String, Object>)Serialization.deserialize(response.getData());
                // 获取区块高度
                long blockNum = (long) responseBody.get("blockNum");
                if(blockNum == 0) {
                    blockNum = getBlockNumber(context.getBlockManager());
                }
                TransactionResponse transactionResponse = new TransactionResponse();
                transactionResponse.setErrorCode(ResultOuterClass.TxStatusCode.SUCCESS.getNumber());
                transactionResponse.setHash((String) responseBody.get("txId"));
                transactionResponse.setBlockNumber(blockNum);
                transactionResponse.setMessage((String) responseBody.get("message"));
                List<Object> result = new ArrayList<>();
                result.add((String)responseBody.get("data"));
                transactionResponse.setResult(result.stream().toArray(String[]::new));
                callback.onTransactionResponse(null, transactionResponse);

            } catch (IOException | ClassNotFoundException e) {
                logger.error("处理交易失败. {}", e.getMessage());
                callback.onTransactionResponse(
                        new TransactionException(
                                ChainMakerStatusCode.HandleInvokeWeCrossProxyFailed,
                                String.format("处理交易失败。%s", e.getMessage())),
                        null
                );
            }
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
    public void subscribeEvent(
            TransactionContext context,
            SubscribeRequest request,
            Connection connection,
            Driver.Callback callback) {
        ChainMakerConnection chainMakerConnection = (ChainMakerConnection) connection;
        String contractName = context.getPath().getResource();
        String topic = request.getTopics().get(0).trim();
        try {
            TransactionResponse response = new TransactionResponse();
            if("@cancel".equals(topic)) {
                String subscriberId = request.getTopics().get(1).trim();
                chainMakerConnection.cancelSubscriber(subscriberId);
                response.setMessage(String.format("订阅事件取消成功。%s", subscriberId));
            } else {
                long fromBlockNumber = request.getFromBlockNumber();
                long toBlockNumber = request.getToBlockNumber();
                String subscriberId = chainMakerConnection.addSubscriber(
                        context,
                        contractName,
                        topic,
                        fromBlockNumber,
                        toBlockNumber);

                response.setMessage(subscriberId);
                List<String> result = new ArrayList<>();
                result.add(String.format("path:%s", context.getPath()));
                result.add(String.format("topics:%s", topic));
                result.add(String.format("raw topics:%s", request.getTopics().get(0)));
                result.add(String.format("from:%d", fromBlockNumber));
                result.add(String.format("to:%d", toBlockNumber));
                response.setResult(result.stream().toArray(String[]::new));
            }

            callback.onTransactionResponse(null, response);

        } catch (ChainClientException e) {
            logger.warn("subscribe event {} was failure. e: {}", contractName, e.getMessage());
            callback.onTransactionResponse(
                    new TransactionException(ChainMakerStatusCode.HandleSubscribeEvent, e.getMessage()),
                    null
            );
        } catch (ChainMakerCryptoSuiteException e) {
            logger.warn("subscribe event {} was failure. e: {}", contractName, e.getMessage());
            callback.onTransactionResponse(
                    new TransactionException(ChainMakerStatusCode.HandleSubscribeEvent, e.getMessage()),
                    null
            );
        }

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
                        ChainmakerTransaction.Transaction chainMakerTransaction = ChainmakerTransaction
                                .Transaction.parseFrom(response.getData());
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

        // 最后一个参数为 command
        List<Object> listArgs = new ArrayList<>();
        for(Object arg : args) {
            listArgs.add(arg);
        }
        listArgs.add(command);
        commandHandler.handle(
                path,
                listArgs.stream().toArray(String[]::new),
                account,
                blockManager,
                connection,
                callback);
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
