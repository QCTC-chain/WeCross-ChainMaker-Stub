package com.webank.wecross.stub.chainmaker;

import com.webank.wecross.stub.chainmaker.account.ChainMakerAccount;
import com.webank.wecross.stub.chainmaker.common.BlockUtility;
import com.webank.wecross.stub.chainmaker.common.ChainMakerRequestType;

import com.google.protobuf.InvalidProtocolBufferException;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Block;
import com.webank.wecross.stub.BlockManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.Transaction;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionRequest;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.chainmaker.pb.common.ChainmakerBlock;
import org.chainmaker.pb.common.ChainmakerTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ChainMakerDriver implements Driver {
    private static final Logger logger = LoggerFactory.getLogger(ChainMakerDriver.class);
    @Override
    public ImmutablePair<Boolean, TransactionRequest> decodeTransactionRequest(Request request) {
        logger.debug("decodeTransactionRequest, request: {}", request);
        return new ImmutablePair<>(true, null);
    }

    @Override
    public List<ResourceInfo> getResources(Connection connection) {
        return new ArrayList<>();
    }

    @Override
    public void asyncCall(
        TransactionContext context, 
        TransactionRequest request, 
        boolean byProxy, 
        Connection connection, 
        Callback callback) {

    }

    @Override
    public void asyncSendTransaction(
        TransactionContext context, 
        TransactionRequest request, 
        boolean byProxy, 
        Connection connection, 
        Callback callback) {

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
        logger.info("asyncCustomCommand, commond: {} path: {}", command, path);
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
