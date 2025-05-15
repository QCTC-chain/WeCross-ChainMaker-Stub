package com.webank.wecross.stub.chainmaker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.webank.wecross.stub.*;

import com.webank.wecross.stub.chainmaker.abi.ABICodec;
import com.webank.wecross.stub.chainmaker.abi.wrapper.*;
import com.webank.wecross.stub.chainmaker.common.BlockUtility;
import com.webank.wecross.stub.chainmaker.common.ChainMakerConstant;
import com.webank.wecross.stub.chainmaker.common.ChainMakerRequestType;
import com.webank.wecross.stub.chainmaker.common.ChainMakerStatusCode;
import com.webank.wecross.stub.chainmaker.subsriber.ContractEventManager;
import com.webank.wecross.stub.chainmaker.utils.ConfigUtils;

import com.webank.wecross.stub.chainmaker.utils.FunctionUtility;
import com.webank.wecross.stub.chainmaker.utils.Serialization;
import com.webank.wecross.stub.chainmaker.utils.Web3jFunctionBuilder;
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
import java.util.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ChainMakerConnection implements Connection {
    private Logger logger = LoggerFactory.getLogger(ChainMakerConnection.class);
    public static final long RPC_CALL_TIMEOUT = 5000;
    private ChainClient chainClient;
    private String configPath;
    private ConnectionEventHandler connectionEventHandler = null;

    private Map<String, String> properties = new HashMap<>();

    private final CompletableFuture<Boolean> getContractListFuture = new CompletableFuture<>();
    private ContractEventManager contractEventManager;

    public ChainMakerConnection(ChainClient chainClient) {
        this.chainClient = chainClient;
        this.contractEventManager = new ContractEventManager(this);
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
        } else if (request.getType() == ChainMakerRequestType.SEND_RAW_TRANSACTION
                || request.getType() == ChainMakerRequestType.CALL_RAW_TRANSACTION) {
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
    public String addSubscriber(
            TransactionContext context,
            String contract,
            String topic,
            long from,
            long to) throws ChainClientException, ChainMakerCryptoSuiteException {
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
        return contractEventManager.addSubscriber(context, topic, from, to);
    }

    public void cancelSubscriber(String subscriberId) {
        contractEventManager.cancelSubscriber(subscriberId);
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
            String abiContent = "";

            if (args != null && args.length > 1 && args.length % 2 != 0) {
                response.setErrorCode(ChainMakerStatusCode.InnerError);
                response.setErrorMessage("参数格式错误，需按 key1 value1 key2 value2 形式组织参数");
                callback.onResponse(response);
                return;
            }

            Function function = null;
            if (contractInfo.getRuntimeType().name().equals("DOCKER_GO")) {
                logger.info("DOCKER_GO call {} {}", method, args);
                if (args != null) {
                    for(int i = 0; i < args.length; i+=2) {
                        params.put(args[i], args[i + 1].getBytes(StandardCharsets.UTF_8));
                    }
                }

                if (params.isEmpty()) {
                    params = null;
                }
            } else if(contractInfo.getRuntimeType().name().equals("EVM")) {
                logger.info("EVM call {} {}", method, args);

                if (path.getResource().equals("WeCrossHub") && method.equals("getInterchainRequests")) {
                    function = FunctionUtility.newGetInterChainRequestHubFunction(Integer.valueOf(args[0]));
                    contractName = ChainMakerConstant.CHAINMAKER_PROXY_NAME;
                } else {
                    Web3jFunctionBuilder builder = new Web3jFunctionBuilder();
                    abiContent = ConfigUtils.getContractABI(getConfigPath(), contractName);
                    function = builder.buildFunctionFromAbi(abiContent, method, args);
                }

                String encodedFunction = FunctionEncoder.encode(function);
                method = encodedFunction.substring(0, 10);
                params.put("data", encodedFunction.getBytes(StandardCharsets.UTF_8));
            }

            if (request.getType() == ChainMakerRequestType.CALL_RAW_TRANSACTION) {
                responseInfo = chainClient.queryContract(
                        contractName,
                        method,
                        null,
                        params,
                        RPC_CALL_TIMEOUT);
            } else if (request.getType() == ChainMakerRequestType.SEND_RAW_TRANSACTION) {
                responseInfo = chainClient.invokeContract(
                        contractName,
                        method,
                        null,
                        params,
                        RPC_CALL_TIMEOUT,
                        RPC_CALL_TIMEOUT);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("txId", responseInfo.getTxId());
            result.put("blockNum", responseInfo.getTxBlockHeight());
            result.put("gasUsed", responseInfo.getContractResult().getGasUsed());
            result.put("message", responseInfo.getContractResult().getMessage());
            if(responseInfo.getContractResult().getCode() == ResultOuterClass.TxStatusCode.SUCCESS.getNumber()) {
                response.setErrorCode(ChainMakerStatusCode.Success);
                if (contractInfo.getRuntimeType().name().equals("EVM")) {
                    String hexOutput = Hex.toHexString(responseInfo.getContractResult().getResult().toByteArray());

                    // 解析 output
                    ABIDefinitionFactory abiDefinitionFactory = new ABIDefinitionFactory();
                    ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(abiContent);
                    ABIDefinition abiDefinition = contractABIDefinition
                            .getFunctions()
                            .get(function.getName())
                            .get(0);
                    ABIObject abiOutputObject = ABIObjectFactory.createOutputObject(abiDefinition);
                    ABICodecObject abiCodecObject = new ABICodecObject();
                    List<Object> decodeObject = abiCodecObject.decodeJavaObject(abiOutputObject, hexOutput);
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> finalResult = mergeOutputResult(abiDefinition.getOutputs(), decodeObject);
                    result.put("data", objectMapper.writeValueAsString(finalResult));
                } else {
                    result.put("data", responseInfo.getContractResult().getResult().toStringUtf8());
                }
                response.setData(Serialization.serialize(result));
            } else {
                response.setErrorCode(ChainMakerStatusCode.ContractResultFailed);
                if (contractInfo.getRuntimeType().name().equals("DOCKER_GO")) {
                    response.setErrorMessage(responseInfo.getContractResult().getResult().toStringUtf8());
                } else if (contractInfo.getRuntimeType().name().equals("EVM")) {
                    response.setErrorMessage(responseInfo.getContractResult().getResult().toStringUtf8());
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

    private Map<String, Object>  mergeOutputResult(
            List<ABIDefinition.NamedType> outputs,
            List<Object> decodeResults) {
        Map<String, Object> finalOutput = new HashMap<>();

        for(int i = 0; i < outputs.size(); i++) {
            Object decodeResult = decodeResults.get(i);
            ABIDefinition.NamedType output = outputs.get(i);
            if(output.getType().equals("tuple")) {
                fillTupleType(decodeResult, output, finalOutput);
            } else {
                fillOtherType(decodeResult, output, finalOutput);
            }
        }

        return finalOutput;
    }

    private void fillTupleType(
            Object object,
            ABIDefinition.NamedType namedType,
            Map<String, Object> finalOutput) {
        List<Object> objects = (List<Object>) object;
        List<ABIDefinition.NamedType> components = namedType.getComponents();
        for(int i = 0; i < components.size(); i++) {
            ABIDefinition.NamedType comp = components.get(i);
            if (comp.getType().equals("tuple")) {
                Map<String, Object> struct = new HashMap<>();
                fillTupleType(objects.get(i), comp, struct);
                finalOutput.put(comp.getName(), struct);
            } else {
                fillOtherType(objects.get(i), comp, finalOutput);
            }
        }
    }

    private void fillOtherType(
            Object object,
            ABIDefinition.NamedType namedType,
            Map<String, Object> finalOutput) {
        finalOutput.put(namedType.getName(), object);
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
