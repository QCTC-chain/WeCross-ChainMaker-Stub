package com.webank.wecross.stub.chainmaker.common;

import org.chainmaker.pb.common.ChainmakerBlock;
import org.chainmaker.pb.common.ChainmakerTransaction;

import com.webank.wecross.stub.Block;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.Transaction;

import java.util.ArrayList;
import java.util.List;

public class BlockUtility {

    public static Block convertToBlock(ChainmakerBlock.BlockInfo blockInfo) {
        Block block = new Block();
        block.setRawBytes(blockInfo.getBlock().toByteArray());

        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setNumber(blockInfo.getBlock().getHeader().getBlockHeight());
        blockHeader.setHash(blockInfo.getBlock().getHeader().getBlockHash().toString());
        blockHeader.setPrevHash(blockInfo.getBlock().getHeader().getPreBlockHash().toString());
        blockHeader.setTransactionRoot(blockInfo.getBlock().getHeader().getTxRoot().toString());
        blockHeader.setStateRoot(blockInfo.getBlock().getHeader().getRwSetRoot().toString());
        block.setBlockHeader(blockHeader);

        List<String> txsHash = new ArrayList<>();
        int txsCount = blockInfo.getBlock().getTxsCount();
        for(int i = 0; i < txsCount; i++) {
            txsHash.add(blockInfo.getBlock().getTxs(i).getResult().getRwSetHash().toString());
        }
        block.setTransactionsHashes(txsHash);
        return block;
    }

    
    public static Transaction convertToTransaction(ChainmakerTransaction.Transaction chainMakerTransaction) {
        Transaction transaction = new Transaction();
        transaction.setTxBytes(chainMakerTransaction.toByteArray());
        transaction.setAccountIdentity(chainMakerTransaction.getSender().toString());
        return transaction;
    }
}
