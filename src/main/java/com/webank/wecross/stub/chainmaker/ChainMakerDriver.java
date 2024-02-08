package com.webank.wecross.stub.chainmaker;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bouncycastle.util.encoders.Hex;
import org.chainmaker.pb.common.ChainmakerBlock;
import org.chainmaker.pb.common.ChainmakerTransaction;
import org.chainmaker.pb.common.ContractOuterClass;
import org.chainmaker.pb.common.ResultOuterClass;
import org.chainmaker.sdk.User;
import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;
import org.chainmaker.sdk.utils.SdkUtils;
import org.chainmaker.sdk.utils.UtilsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ChainMakerDriver implements Driver {
    private static final Logger logger = LoggerFactory.getLogger(ChainMakerDriver.class);
    private CommandHandlerDispatcher commandHandlerDispatcher;

    private ABICodecJsonWrapper abiCodecJsonWrapper = new ABICodecJsonWrapper();

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
        asyncCallByProxy(context, request, connection, callback);
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
        }else {
            invokeSendTransaction(context, request, connection, callback);
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
        byte[] contractBinary = request.getArgs()[1].getBytes();
        String contractABIContent = request.getArgs()[2];
        String contractName = request.getArgs()[3];
        String version = request.getArgs()[4];
        List<User> users = (List<User>)request.getOptions().get("EndorsementEntries");
        try {
            ConfigUtils.writeContractABI(chainMakerConnection.getConfigPath(), path.getResource(), contractABIContent);

            org.chainmaker.pb.common.Request.Payload payload = null;
            if(deploy) {
                payload = chainMakerConnection
                        .getChainClient()
                        .createContractCreatePayload(
                                contractName,
                                version,
                                contractBinary, ContractOuterClass.RuntimeType.EVM, null);
            } else {
                payload = chainMakerConnection
                        .getChainClient()
                        .createContractUpgradePayload(
                                contractName,
                                version,
                                contractBinary, ContractOuterClass.RuntimeType.EVM, null);
            }
            org.chainmaker.pb.common.Request.EndorsementEntry[] endorsementEntries = SdkUtils.getEndorsers(
                    payload, users.stream().toArray(User[]::new));

            Request weCrossRequest = Request.newRequest(
                    deploy ? ChainMakerRequestType.CREATE_CUSTOMER_CONTRACT : ChainMakerRequestType.UPGRADE_CUSTOMER_CONTRACT,
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

    private void asyncCallByProxy(
            TransactionContext context,
            TransactionRequest request,
            Connection connection,
            Callback callback) {
        Path path = context.getPath();
        ChainMakerConnection chainMakerConnection = (ChainMakerConnection)connection;
        List<ABIDefinition> abiDefinitions = null;
        String encodedArgs = "";
        try {
            abiDefinitions = getABIFunctions(
                    chainMakerConnection.getConfigPath(),
                    path.getResource(),
                    request.getMethod());
            encodedArgs = encodeFunctionArgs(abiDefinitions.get(0), request.getArgs());
        } catch (IOException e) {
            callback.onTransactionResponse(
                    new TransactionException(
                            ChainMakerStatusCode.HandleInvokeWeCrossProxyFailed,
                            e.getMessage()),
                    null
            );
            return;
        } catch (WeCrossException e) {
            callback.onTransactionResponse(
                    new TransactionException(
                            ChainMakerStatusCode.HandleInvokeWeCrossProxyFailed,
                            e.getMessage()),
                    null
            );
            return;
        }

        String transactionID = (String)request.getOptions().get(StubConstant.XA_TRANSACTION_ID);
        Function function = null;
        if (Objects.isNull(transactionID)
                || transactionID.isEmpty()
                || "0".equals(transactionID)) {
            function = FunctionUtility.newConstantCallProxyFunction(
                    path.getResource(),
                    abiDefinitions.get(0).getMethodSignatureAsString(),
                    encodedArgs
            );
        } else {
            function = FunctionUtility.newConstantCallProxyFunction(
                    transactionID,
                    path.toString(),
                    abiDefinitions.get(0).getMethodSignatureAsString(),
                    encodedArgs
            );
        }

        invokeWeCrossProxy(context, request, connection, function, callback);
    }

    private void invokeSendTransaction(
            TransactionContext context,
            TransactionRequest request,
            Connection connection,
            Callback callback
    ) {
        Path path = context.getPath();
        ChainMakerConnection chainMakerConnection = (ChainMakerConnection)connection;
        List<ABIDefinition> abiDefinitions = null;
        String encodedArgs = "";
        try {
            abiDefinitions = getABIFunctions(
                    chainMakerConnection.getConfigPath(),
                    path.getResource(),
                    request.getMethod());
            encodedArgs = encodeFunctionArgs(abiDefinitions.get(0), request.getArgs());
        } catch (IOException e) {
            callback.onTransactionResponse(
                    new TransactionException(
                            ChainMakerStatusCode.HandleInvokeWeCrossProxyFailed,
                            e.getMessage()),
                    null
            );
            return;
        } catch (WeCrossException e) {
            callback.onTransactionResponse(
                    new TransactionException(
                            ChainMakerStatusCode.HandleInvokeWeCrossProxyFailed,
                            e.getMessage()),
                    null);
            return;
        }

        String uniqueID = (String)request.getOptions().get(StubConstant.TRANSACTION_UNIQUE_ID);
        String uid = Objects.nonNull(uniqueID) ? uniqueID : UUID.randomUUID().toString().replaceAll("-", "");
        String transactionID = (String)request.getOptions().get(StubConstant.XA_TRANSACTION_ID);
        Long transactionSeq =(Long)request.getOptions().get(StubConstant.XA_TRANSACTION_SEQ);
        Long seq = Objects.isNull(transactionSeq) ? 0 : transactionSeq;

        Function function = null;
        if (Objects.isNull(transactionID)
                || transactionID.isEmpty()
                || "0".equals(transactionID)) {
            function = FunctionUtility
                    .newSendTransactionProxyFunction(
                            uid,
                            path.getResource(),
                            abiDefinitions.get(0).getMethodSignatureAsString(),
                            encodedArgs

                    );
        } else {
            function = FunctionUtility
                    .newSendTransactionProxyFunction(
                            uid,
                            transactionID,
                            seq,
                            path.toString(),
                            abiDefinitions.get(0).getMethodSignatureAsString(),
                            encodedArgs);
        }
        invokeWeCrossProxy(context, request, connection, function, callback);
    }

    private void invokeWeCrossProxy(
            TransactionContext context,
            TransactionRequest request,
            Connection connection,
            Function weCrossFunction,
            Callback callback
    ) {
        ChainMakerConnection chainMakerConnection = (ChainMakerConnection)connection;
        String methodEncode = FunctionEncoder.encode(weCrossFunction);
        Request weCrossRequest = Request.newRequest(
                ChainMakerRequestType.SEND_TRANSACTION,
                methodEncode.getBytes()
        );

        chainMakerConnection.asyncSend(
                weCrossRequest,
                response -> {
                    handleAsyncSendResponse(response, context, request, callback);
                }
        );
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
                ResultOuterClass.TxResponse chainMakerResponse = ResultOuterClass
                        .TxResponse.parseFrom(response.getData());

                if(chainMakerResponse.getContractResult().getCode() == ResultOuterClass.TxStatusCode.SUCCESS.getNumber()) {

                    // 获取区块高度
                    final CompletableFuture<Long> getContractListFuture = new CompletableFuture<Long>();
                    context.getBlockManager().asyncGetBlockNumber(
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

                    TransactionResponse transactionResponse = new TransactionResponse();
                    transactionResponse.setErrorCode(ResultOuterClass.TxStatusCode.SUCCESS.getNumber());
                    transactionResponse.setHash(chainMakerResponse.getTxId());
                    transactionResponse.setBlockNumber(blockNum);
                    transactionResponse.setMessage(chainMakerResponse.getContractResult().getMessage());
                    String output = Hex.toHexString(
                                    chainMakerResponse
                                            .getContractResult()
                                            .getResult()
                                            .toByteArray())
                            .substring(128);
                    String decodeOutput = FunctionUtility.decodeOutputAsString(output);
                    List<String> result = new ArrayList<>();
                    if (Objects.isNull(decodeOutput) || decodeOutput.isEmpty()) {
                        result.add(String.format("%s invoke successfully.", request.getMethod()));
                    } else {
                        result.add(decodeOutput);
                    }
                    transactionResponse.setResult(result.stream().toArray(String[]::new));
                    callback.onTransactionResponse(null, transactionResponse);
                } else {
                    logger.warn("invoking contract was failure. resultCode: {}, txId: {}",
                            chainMakerResponse.getContractResult().getCode(),
                            chainMakerResponse.getTxId());
                    callback.onTransactionResponse(
                            new TransactionException(
                                    ChainMakerStatusCode.HandleInvokeWeCrossProxyFailed,
                                    String.format("{contractResultCode: %d, contractResult: %s, txId: %s}",
                                            chainMakerResponse.getContractResult().getCode(),
                                            chainMakerResponse.getContractResult().getMessage(),
                                            chainMakerResponse.getTxId())),
                            null
                    );
                }

            } catch (InvalidProtocolBufferException e) {
                logger.warn("loading chainmaker's response was failure. {}", e.getMessage());
                callback.onTransactionResponse(
                        new TransactionException(
                                ChainMakerStatusCode.HandleInvokeWeCrossProxyFailed,
                                String.format("loading chainmaker's response was failure.")),
                        null
                );
            }
        }
    }

    private List<ABIDefinition> getABIFunctions(
            String abiPath,
            String abiFileName,
            String method) throws IOException, WeCrossException {
        String contractABIContent = ConfigUtils.getContractABI(abiPath, abiFileName);
        if(contractABIContent.isEmpty()) {
            logger.error("{}/{}'s abi content is empty.", abiPath, abiFileName);
            return null;
        }
        ABIDefinitionFactory abiDefinitionFactory = new ABIDefinitionFactory();
        ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(contractABIContent);
        List<ABIDefinition> abiDefinitions = contractABIDefinition
                .getFunctions()
                .get(method);

        return abiDefinitions;
    }

    private String encodeFunctionArgs(ABIDefinition abiFunction, String[] args) throws IOException {
        String encodedArgs = "";
        ABIObject inputObj =
                ABIObjectFactory.createInputObject(abiFunction);
        if(!Objects.isNull(args)) {
            ABIObject encodeObj = abiCodecJsonWrapper.encode(inputObj, Arrays.asList(args));
            encodedArgs = encodeObj.encode();
        }
        return encodedArgs;
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
