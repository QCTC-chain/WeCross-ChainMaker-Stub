package com.webank.wecross.stub.chainmaker.common;

import org.bouncycastle.util.encoders.Hex;
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

        ChainmakerBlock.BlockHeader cmBLockHeader = blockInfo.getBlock().getHeader();

        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setNumber(cmBLockHeader.getBlockHeight());
        blockHeader.setHash(Hex.toHexString(cmBLockHeader.getBlockHash().toByteArray()));
        blockHeader.setPrevHash(Hex.toHexString(cmBLockHeader.getPreBlockHash().toByteArray()));
        blockHeader.setTransactionRoot(Hex.toHexString(cmBLockHeader.getTxRoot().toByteArray()));
        blockHeader.setStateRoot(Hex.toHexString(cmBLockHeader.getRwSetRoot().toByteArray()));
        block.setBlockHeader(blockHeader);

        List<String> txsHash = new ArrayList<>();
        int txsCount = blockInfo.getBlock().getTxsCount();
        for(int i = 0; i < txsCount; i++) {
            txsHash.add(Hex.toHexString(blockInfo.getBlock().getTxs(i).getResult().getRwSetHash().toByteArray()));
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
