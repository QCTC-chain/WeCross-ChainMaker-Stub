package com.webank.wecross.stub.chainmaker.custom;

import com.webank.wecross.stub.*;

public interface CommandHandler {
    /**
     * handle custom command
     *
     * @param path rule id
     * @param args command args
     * @param account if needs to sign
     * @param blockManager if needs to verify transaction
     * @param connection chain connection
     * @param callback
     */
    void handle(
            Path path,
            Object[] args,
            Account account,
            BlockManager blockManager,
            Connection connection,
            Driver.CustomCommandCallback callback);
}
