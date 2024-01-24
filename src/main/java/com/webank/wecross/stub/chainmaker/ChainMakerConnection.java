package com.webank.wecross.stub.chainmaker;

import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.Response;

import com.webank.wecross.stub.chainmaker.common.ChainMakerRequestType;
import com.webank.wecross.stub.chainmaker.common.ChainMakerStatusCode;

import org.chainmaker.pb.common.ChainmakerBlock;
import org.chainmaker.pb.common.ChainmakerTransaction;
import org.chainmaker.sdk.ChainClient;
import org.chainmaker.sdk.ChainClientException;
import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;

import java.util.HashMap;
import java.util.Map;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class ChainMakerConnection implements Connection {
    private static final long RPC_CALL_TIMEOUT = 5000;
    private ChainClient chainClient;

    public ChainMakerConnection(ChainClient chainClient) {
        this.chainClient = chainClient;
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
        }
    }

    @Override
    public void setConnectionEventHandler(ConnectionEventHandler eventHandler) {

    }

    @Override
    public Map<String, String> getProperties() {
        return new HashMap<>();
    }

    public boolean hasProxyDeployed() {
        // return getProperties().containsKey(ChainMakerConstant.BCOS_PROXY_NAME);
        return true;
    }

    public boolean hasHubDeployed() {
        // return getProperties().containsKey(ChainMakerConstant.BCOS_HUB_NAME);
        return true;
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
            response.setData(blockInfo.getBlock().toByteArray());
            
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

    }
}
