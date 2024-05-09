package com.webank.wecross.stub.chainmaker;

import com.google.protobuf.InvalidProtocolBufferException;
import com.webank.wecross.stub.*;

import com.webank.wecross.stub.chainmaker.common.BlockUtility;
import com.webank.wecross.stub.chainmaker.common.ChainMakerConstant;
import com.webank.wecross.stub.chainmaker.common.ChainMakerRequestType;
import com.webank.wecross.stub.chainmaker.common.ChainMakerStatusCode;

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
            new String(requestBytes, StandardCharsets.UTF_8);
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
