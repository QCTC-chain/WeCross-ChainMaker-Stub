package com.webank.wecross.stub.chainmaker;

import com.google.protobuf.InvalidProtocolBufferException;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.*;

import com.webank.wecross.stub.chainmaker.abi.ABICodec;
import com.webank.wecross.stub.chainmaker.common.BlockUtility;
import com.webank.wecross.stub.chainmaker.common.ChainMakerConstant;
import com.webank.wecross.stub.chainmaker.common.ChainMakerRequestType;
import com.webank.wecross.stub.chainmaker.common.ChainMakerStatusCode;
import com.webank.wecross.stub.chainmaker.utils.ConfigUtils;

import com.webank.wecross.stub.chainmaker.utils.FunctionUtility;
import com.webank.wecross.stub.chainmaker.utils.Serialization;
import org.bouncycastle.util.encoders.Hex;
import org.chainmaker.pb.common.ChainmakerBlock;
import org.chainmaker.pb.common.ChainmakerTransaction;
import org.chainmaker.pb.common.ContractOuterClass;
import org.chainmaker.pb.common.ResultOuterClass;
import org.chainmaker.pb.config.ChainConfigOuterClass;
import org.chainmaker.sdk.ChainClient;
import org.chainmaker.sdk.ChainClientException;
import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;
import org.chainmaker.sdk.utils.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.stub.StreamObserver;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.utils.Numeric;
import org.web3j.crypto.Hash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ChainMakerConnection implements Connection {
    private Logger logger = LoggerFactory.getLogger(ChainMakerConnection.class);
    private static final long RPC_CALL_TIMEOUT = 5000;
    private ChainClient chainClient;
    private String configPath;
    private ConnectionEventHandler connectionEventHandler = null;

    private Map<String, String> properties = new HashMap<>();

    private final CompletableFuture<Boolean> getContractListFuture = new CompletableFuture<>();

    private Map<String, StreamObserver<ResultOuterClass.SubscribeResult>> subscribers = new HashMap<>();

    public ChainMakerConnection(ChainClient chainClient) {
        this.chainClient = chainClient;
    }

    public ChainClient getChainClient() {
        return this.chainClient;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    public String getConfigPath() {
        return this.configPath;
    }

    @Override
    public void asyncSend(Request request, Callback callback) {
        if(request.getType() == ChainMakerRequestType.GET_BLOCK_NUMBER) {
            handleAsyncGetBlockNumberRequest(request, callback);
        } else if (request.getType() == ChainMakerRequestType.GET_BLOCK_BY_NUMBER) {
            handleAsyncGetBlockRequest(request, callback);
        } else if (request.getType() == ChainMakerRequestType.GET_TRANSACTION) {
            handleAsyncGetTransaction(request, callback);
        } else if (request.getType() == ChainMakerRequestType.CALL) {
            handleAsyncCallRequest(request, callback);
        } else if (request.getType() == ChainMakerRequestType.SEND_TRANSACTION) {
            handleAsyncTransactionRequest(request, callback);
        } else if (request.getType() == ChainMakerRequestType.SEND_RAW_TRANSACTION) {
            handleAsyncRawTransactionRequest(request, callback);
        } else if (request.getType() == ChainMakerRequestType.GET_CONTRACT_LIST) {
            handleGetContractListRequest(request, callback);
        } else if (request.getType() == ChainMakerRequestType.CREATE_CUSTOMER_CONTRACT
            || request.getType() == ChainMakerRequestType.UPGRADE_CUSTOMER_CONTRACT) {
            handleCreateCustomerContractRequest(request, callback);
        }
    }

    @Override
    public void setConnectionEventHandler(ConnectionEventHandler eventHandler) {
        this.connectionEventHandler = eventHandler;
    }

    @Override
    public Map<String, String> getProperties() {
        logger.info("getProperties: {}", properties);
        return properties;
    }

    public void addProperty(String key, String value) {
        this.properties.put(key, value);
    }

    private String stringToTopic(String s) {
        final byte[] input = s.getBytes();
        final byte[] hash = Hash.sha3(input);
        return Numeric.cleanHexPrefix(Numeric.toHexString(hash));
    }
    public StreamObserver<ResultOuterClass.SubscribeResult> addSubscriber(
            TransactionContext context,
            String contract,
            String topic,
            long from,
            long to) throws ChainClientException, ChainMakerCryptoSuiteException {
        String rawTopic = topic;
        if(topic.equals("*")
                || (topic.length() == 2 && topic.charAt(0) == '\'' && topic.charAt(1) == '\'')
                || (topic.length() == 2 && topic.charAt(0) == '"' && topic.charAt(1) == '"')) {
            topic = "";
        } else {
            ContractOuterClass.Contract contractInfo = chainClient.getContractInfo(contract, RPC_CALL_TIMEOUT);
            if (contractInfo.getRuntimeType().name().equals("EVM")) {
                topic = stringToTopic(topic);
            }
        }
        String key = String.format("%s-%s-%d-%d", contract, topic, from, to);
        StreamObserver<ResultOuterClass.SubscribeResult> subscriber = subscribers.getOrDefault(
                key, null);
        if (subscriber == null) {
            subscriber = new StreamObserver<ResultOuterClass.SubscribeResult>() {
                // refer to:
                // https://git.chainmaker.org.cn/chainmaker/sdk-java/-/blob/master/src/test/java/org/chainmaker/sdk/TestSubscribe.java
                @Override
                public void onNext(ResultOuterClass.SubscribeResult value) {
                    try {
                        ResultOuterClass.ContractEventInfoList contract = ResultOuterClass
                                .ContractEventInfoList
                                .parseFrom(value.getData());
                        int count = contract.getContractEventsCount();
                        for (int i = 0; i < count; i++) {
                            ResultOuterClass.ContractEventInfo eventInfo = contract.getContractEvents(i);

                            logger.info("contract event: {}", eventInfo);

                            Map<String, Object> result = new HashMap<>();
                            result.put("block_height", eventInfo.getBlockHeight());
                            result.put("chain_id", eventInfo.getChainId());
                            result.put("topic", eventInfo.getTopic());
                            result.put("contract_name", eventInfo.getContractName());
                            result.put("contract_version", eventInfo.getContractVersion());

                            ContractOuterClass.Contract contractInfo = chainClient.getContractInfo(eventInfo.getContractName(), RPC_CALL_TIMEOUT);
                            if (contractInfo.getRuntimeType().name().equals("DOCKER_GO")) {
                                result.put("event_data", eventInfo.getEventDataList());
                            } else if (contractInfo.getRuntimeType().name().equals("EVM")) {
                                String abiContent = "";
                                try {
                                    abiContent = ConfigUtils.getContractABI(
                                            getConfigPath(),
                                            eventInfo.getContractName());
                                } catch (Exception e) {
                                    logger.error("获取 ABI 失败。 {}/{}",
                                            getConfigPath(), eventInfo.getContractName());
                                }
                                logger.info("获取 ABI 数据: {}", abiContent.length());

                                if (!abiContent.isEmpty()) {
                                    ABICodec abiCodec = new ABICodec();
                                    Map<String, Object> decodedData = abiCodec.decodeEvent(abiContent, eventInfo);
                                    result.put("event_data", decodedData);
                                }
                            }
                            context.getCallback().onSubscribe(
                                    eventInfo.getContractName(),
                                    rawTopic,
                                    result);
                        }
                    } catch (InvalidProtocolBufferException e) {
                        logger.error("处理订阅事件 {}:{} 失败。{}", contract, rawTopic, e.getMessage());
                    } catch (ChainMakerCryptoSuiteException | ChainClientException e) {
                        logger.error("获取合约 {} 信息 失败。{}", contract, e.getMessage());
                    }
                }

                @Override
                public void onError(Throwable t) {
                    logger.error("处理订阅事件 {}:{} 失败。{}", contract, rawTopic, t.getMessage());
                }

                @Override
                public void onCompleted() {

                }
            };
            subscribers.put(key, subscriber);
            this.chainClient.subscribeContractEvent(from, to, topic, contract, subscriber);
        }
        return subscriber;
    }

    public List<ResourceInfo> getResources() {
        List<ResourceInfo> resourceInfos = new ArrayList<>();
        Request request = Request.newRequest(ChainMakerRequestType.GET_CONTRACT_LIST, "");

        asyncSend(
                request,
                response -> {
                    if(response.getErrorCode() != 0) {
                        logger.warn(
                                " asyncGetTransaction, errorCode: {},  errorMessage: {}",
                                response.getErrorCode(),
                                response.getErrorMessage());
                    } else {
                        try {
                            ContractOuterClass.Contract contract = ContractOuterClass
                                    .Contract
                                    .parseFrom(response.getData());

                            logger.info("got a contract. name = {}, address = {}, version = {}",
                                    contract.getName(), contract.getAddress(), contract.getVersion());

                            ResourceInfo resourceInfo = new ResourceInfo();
                            resourceInfo.setStubType(
                                    this.getProperties().get(ChainMakerConstant.CHAINMAKER_STUB_TYPE));
                            resourceInfo.setName(contract.getName());
                            Map<Object, Object> resourceProperties = new HashMap<>();
                            resourceProperties.put(
                                    ChainMakerConstant.CHAINMAKER_CHAIN_ID,
                                    this.getProperties().get(ChainMakerConstant.CHAINMAKER_CHAIN_ID));
                            resourceProperties.put(
                                    ChainMakerConstant.CHAINMAKER_CONTRACT_ADDRESS,
                                    contract.getAddress());
                            resourceProperties.put(
                                    ChainMakerConstant.CHAINMAKER_CONTRACT_VERSION,
                                    contract.getVersion());
                            resourceInfo.setProperties(resourceProperties);
                            resourceInfos.add(resourceInfo);
                        } catch (InvalidProtocolBufferException e) {
                            logger.warn("getResources was failure. e: {}", e.getMessage());
                        }
                    }
                }
        );

        try {
            getContractListFuture.get();
        } catch (InterruptedException e) {

        } catch (ExecutionException e) {

        }

        return resourceInfos;
    }

    public boolean hasProxyDeployed() {
         return getProperties().containsKey(ChainMakerConstant.CHAINMAKER_PROXY_NAME);
    }

    public boolean hasHubDeployed() {
         return getProperties().containsKey(ChainMakerConstant.CHAINMAKER_HUB_NAME);
    }

    private void handleAsyncGetBlockNumberRequest(Request request, Callback callback) {
        Response response = new Response();
        try {
            ChainmakerBlock.BlockInfo blockInfo = chainClient.getLastBlock(false, RPC_CALL_TIMEOUT);
            BigInteger blockNumber = BigInteger.valueOf(blockInfo.getBlock().getHeader().getBlockHeight());

            response.setErrorCode(ChainMakerStatusCode.Success);
            response.setErrorMessage(ChainMakerStatusCode.getStatusMessage(ChainMakerStatusCode.Success));
            response.setData(blockNumber.toByteArray());
        } catch (ChainClientException ec) {
            response.setErrorCode(ChainMakerStatusCode.HandleGetBlockNumberFailed);
            response.setErrorMessage(ec.getMessage());
        } catch (ChainMakerCryptoSuiteException ec) {
            response.setErrorCode(ChainMakerStatusCode.HandleGetBlockNumberFailed);
            response.setErrorMessage(ec.getMessage());
        } catch (Exception ec) {
            response.setErrorCode(ChainMakerStatusCode.HandleGetBlockNumberFailed);
            response.setErrorMessage(ec.getMessage());
        }
        callback.onResponse(response);
    } 

    private void handleAsyncGetBlockRequest(Request request, Callback callback) {
        Response response = new Response();
        try {
            BigInteger blockNumber = new BigInteger(request.getData());

            ChainmakerBlock.BlockInfo blockInfo = chainClient.getBlockByHeight(
                blockNumber.longValue(), false, RPC_CALL_TIMEOUT);

            response.setErrorCode(ChainMakerStatusCode.Success);
            response.setErrorMessage(ChainMakerStatusCode.getStatusMessage(ChainMakerStatusCode.Success));
            response.setData(blockInfo.toByteArray());
            
        } catch (ChainClientException ec) {
            response.setErrorCode(ChainMakerStatusCode.HandleGetBlockFailed);
            response.setErrorMessage(ec.getMessage());
        } catch (ChainMakerCryptoSuiteException ec) {
            response.setErrorCode(ChainMakerStatusCode.HandleGetBlockFailed);
            response.setErrorMessage(ec.getMessage());
        } catch (Exception ec) {
            response.setErrorCode(ChainMakerStatusCode.HandleGetBlockFailed);
            response.setErrorMessage(ec.getMessage());
        }
        callback.onResponse(response);
    }

    private void handleAsyncGetTransaction(Request request, Callback callback) {
        Response response = new Response();
        try {
            String txHash = new String(request.getData(), StandardCharsets.UTF_8);
            ChainmakerTransaction.Transaction transaction = chainClient.getTxByTxId(txHash, RPC_CALL_TIMEOUT).getTransaction();
            response.setErrorCode(ChainMakerStatusCode.Success);
            response.setErrorMessage(ChainMakerStatusCode.getStatusMessage(ChainMakerStatusCode.Success));
            response.setData(transaction.toByteArray());

        } catch (ChainClientException ec) {
            response.setErrorCode(ChainMakerStatusCode.HandleGetBlockFailed);
            response.setErrorMessage(ec.getMessage());
        } catch (ChainMakerCryptoSuiteException ec) {
            response.setErrorCode(ChainMakerStatusCode.HandleGetBlockFailed);
            response.setErrorMessage(ec.getMessage());
        } catch (Exception ec) {
            response.setErrorCode(ChainMakerStatusCode.HandleGetBlockFailed);
            response.setErrorMessage(ec.getMessage());
        }

        callback.onResponse(response);
    }

    private void handleAsyncCallRequest(Request request, Callback callback) {
    }

    private void handleAsyncTransactionRequest(Request request, Callback callback) {
        Response response = new Response();
        try {
            Map<String, byte[]> params = new HashMap<>();
            byte[] requestBytes = request.getData();
            String method = new String(requestBytes, StandardCharsets.UTF_8);
            method = method.substring(0, 10);
            params.put("data", requestBytes);
            ResultOuterClass.TxResponse chainMakerResponse = chainClient.invokeContract(
                    ChainMakerConstant.CHAINMAKER_PROXY_NAME,
                    method,
                    null,
                    params,
                    RPC_CALL_TIMEOUT,
                    RPC_CALL_TIMEOUT
            );
            response.setErrorCode(ChainMakerStatusCode.Success);
            response.setData(chainMakerResponse.toByteArray());
        } catch (ChainClientException ec) {
            response.setErrorCode(ChainMakerStatusCode.HandleInvokeWeCrossProxyFailed);
            response.setErrorMessage(ec.getMessage());
        } catch (ChainMakerCryptoSuiteException ec) {
            response.setErrorCode(ChainMakerStatusCode.HandleInvokeWeCrossProxyFailed);
            response.setErrorMessage(ec.getMessage());
        }
        callback.onResponse(response);
    }

    private void handleAsyncRawTransactionRequest(Request request, Callback callback) {
        Response response = new Response();
        try {
            Path path = new Path(request.getPath());
            String contractName = path.getResource();
            Map<String, Object> requestData = (Map<String, Object>)Serialization.deserialize(request.getData());
            String method = (String)requestData.get("method");
            String[] args = (String[])requestData.get("args");
            ContractOuterClass.Contract contractInfo = chainClient.getContractInfo(contractName, RPC_CALL_TIMEOUT);
            ResultOuterClass.TxResponse responseInfo = null;
            Map<String, byte[]> params = new HashMap<>();
            if (contractInfo.getRuntimeType().name().equals("DOCKER_GO")) {

                if (args.length > 1 && args.length % 2 != 0) {
                    response.setErrorCode(ChainMakerStatusCode.InnerError);
                    response.setErrorMessage("参数格式错误，需按 key1 value1 key2 value2 形式组织参数");
                    callback.onResponse(response);
                    return;
                }

                for(int i = 0; i < args.length; i+=2) {
                    params.put(args[i], args[i + 1].getBytes(StandardCharsets.UTF_8));
                }
                if (params.isEmpty()) {
                    params = null;
                }
                responseInfo = chainClient.invokeContract(
                        contractName,
                        method,
                        null,
                        params,
                        RPC_CALL_TIMEOUT,
                        RPC_CALL_TIMEOUT);
                response.setErrorCode(ChainMakerStatusCode.Success);
                response.setData(responseInfo.toByteArray());
            } else if(contractInfo.getRuntimeType().name().equals("EVM")) {
                logger.info("EVM call {} {}", method, args);
                if (path.getResource().equals("WeCrossHub") && method.equals("getInterchainRequests")) {
                    Function function = FunctionUtility.newGetInterChainRequestHubFunction(Integer.valueOf(args[0]));
                    byte[] requestBytes = FunctionEncoder.encode(function).getBytes();
                    String method_id = new String(requestBytes, StandardCharsets.UTF_8);
                    method_id = method_id.substring(0, 10);
                    params.put("data", requestBytes);
                    responseInfo = chainClient.queryContract(
                            ChainMakerConstant.CHAINMAKER_PROXY_NAME,
                            method_id,
                            null,
                            params,
                            RPC_CALL_TIMEOUT
                    );
                    String hexString = Hex.toHexString(
                            responseInfo
                                    .getContractResult()
                                    .getResult()
                                    .toByteArray());
                    response.setErrorCode(ChainMakerStatusCode.Success);
                    response.setData(hexString.getBytes());
                }

            }
        } catch (IOException | ClassNotFoundException e) {
            String errorMsg = String.format("反序列化请求对象失败。 %s", e.getMessage());
            logger.error(errorMsg);
            response.setErrorCode(ChainMakerStatusCode.InnerError);
            response.setErrorMessage(errorMsg);
        } catch(ChainMakerCryptoSuiteException | ChainClientException e) {
            String errorMsg = String.format("调用合约失败。 %s", e.getMessage());
            logger.error(errorMsg);
            response.setErrorCode(ChainMakerStatusCode.HandleInvokeWeCrossProxyFailed);
            response.setErrorMessage(errorMsg);
        } catch (Exception e) {
            String errorMsg = String.format("处理请求错误。 %s", e.getMessage());
            logger.error(errorMsg);
            response.setErrorCode(ChainMakerStatusCode.InnerError);
            response.setErrorMessage(errorMsg);
        }
        callback.onResponse(response);
    }
    private void handleGetContractListRequest(Request request, Callback callback) {
        Response response = new Response();
        try {
            ContractOuterClass.Contract[] contracts = chainClient.getContractList(RPC_CALL_TIMEOUT);

            response.setErrorCode(ChainMakerStatusCode.Success);
            response.setErrorMessage(ChainMakerStatusCode.getStatusMessage(ChainMakerStatusCode.Success));

            for(ContractOuterClass.Contract contract : contracts) {
                if (BlockUtility.isSystemContract(contract.getName())) {
                    continue;
                }
                response.setData(contract.toByteArray());
                callback.onResponse(response);
            }
        } catch (ChainClientException ec) {
            response.setErrorCode(ChainMakerStatusCode.HandleGetContracts);
            response.setErrorMessage(ec.getMessage());
        } catch (ChainMakerCryptoSuiteException ec) {
            response.setErrorCode(ChainMakerStatusCode.HandleGetContracts);
            response.setErrorMessage(ec.getMessage());
        } catch (Exception ec) {
            response.setErrorCode(ChainMakerStatusCode.HandleGetContracts);
            response.setErrorMessage(ec.getMessage());
        } finally {
            getContractListFuture.complete(Boolean.TRUE);
        }
    }

    private void handleCreateCustomerContractRequest(Request request, Callback callback) {
        Response response = new Response();
        try {
            org.chainmaker.pb.common.Request.Payload payload = org.chainmaker.pb.common.Request.Payload.parseFrom(
                    request.getData()
            );
            ResourceInfo resourceInfo = request.getResourceInfo();
            org.chainmaker.pb.common.Request.EndorsementEntry[] endorsementEntries =
                    (org.chainmaker.pb.common.Request.EndorsementEntry[])resourceInfo.getProperties().get(
                            ChainMakerConstant.CHAINMAKER_ENDORSEMENTENTRY);

            ResultOuterClass.TxResponse weCrossResponse = chainClient.sendContractManageRequest(
                    payload, endorsementEntries, RPC_CALL_TIMEOUT, RPC_CALL_TIMEOUT);

            if(weCrossResponse.getCode() == ResultOuterClass.TxStatusCode.SUCCESS) {
                logger.info("deploy a contract {} was successful. tx_id: {}",
                        resourceInfo.getName(), weCrossResponse.getTxId());
                response.setErrorCode(ChainMakerStatusCode.Success);
                response.setData(weCrossResponse.getContractResult().toByteArray());

                if(this.connectionEventHandler != null) {
                    resourceInfo.setStubType(this.getProperties().get(ChainMakerConstant.CHAINMAKER_STUB_TYPE));
                    Map<Object, Object> resourceProperties = new HashMap<>();
                    resourceProperties.put(
                            ChainMakerConstant.CHAINMAKER_CHAIN_ID,
                            this.getProperties().get(ChainMakerConstant.CHAINMAKER_CHAIN_ID));
                    String contractAddress = CryptoUtils.nameToAddrStr(
                            (String) resourceInfo.getProperties().get(ChainMakerConstant.CHAINMAKER_CONTRACT_NAME),
                            ChainConfigOuterClass.AddrType.CHAINMAKER);
                    resourceProperties.put(
                            ChainMakerConstant.CHAINMAKER_CONTRACT_ADDRESS,
                            contractAddress);
                    String contractVersion = (String) resourceInfo.getProperties().get(
                            ChainMakerConstant.CHAINMAKER_CONTRACT_VERSION);
                    resourceProperties.put(
                            ChainMakerConstant.CHAINMAKER_CONTRACT_VERSION,
                            contractVersion);
                    resourceInfo.setProperties(resourceProperties);
                    this.connectionEventHandler.onANewResource(resourceInfo);
                }

            } else {
                logger.warn("deploy a contract {} was failure. response: {}",
                        resourceInfo.getName(), weCrossResponse.getContractResult().getMessage());
                response.setErrorCode(ChainMakerStatusCode.HandleDeployContract);
                response.setErrorMessage(weCrossResponse.getContractResult().getMessage());
            }
        } catch (InvalidProtocolBufferException e) {
            response.setErrorCode(ChainMakerStatusCode.HandleDeployContract);
            response.setErrorMessage(e.getMessage());
        } catch (ChainMakerCryptoSuiteException e) {
            response.setErrorCode(ChainMakerStatusCode.HandleDeployContract);
            response.setErrorMessage(e.getMessage());
        } catch (ChainClientException e) {
            response.setErrorCode(ChainMakerStatusCode.HandleDeployContract);
            response.setErrorMessage(e.getMessage());
        }
        callback.onResponse(response);
    }
}
