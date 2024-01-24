package com.webank.wecross.stub.chainmaker.account;

import org.chainmaker.sdk.User;
import org.chainmaker.sdk.config.AuthType;
import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;
import org.chainmaker.sdk.utils.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webank.wecross.stub.Account;

public class ChainMakerAccount implements Account {
    private static final Logger logger = LoggerFactory.getLogger(ChainMakerAccount.class);

    private final User user;
    private final String name;

    public ChainMakerAccount(User user, String name) {
        this.user = user;
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getType() {
        return this.user.getKeyType();
    }

    @Override
    public String getIdentity() {
        return this.user.getPublicKey().toString();
    }

    @Override
    public int getKeyID() {
        return 0;
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    public byte[] sign(byte[] message) {
        byte[] signature = null;
        try {
            if(this.user.getAuthType().equals(AuthType.PermissionedWithCert.getMsg())) {
                if(this.user.getKeyId().equals("") || this.user.getKeyId() == null) {
                    signature = this.user.getCryptoSuite().sign(this.user.getPrivateKey(), message);
                } else {
                    signature = this.user.getCryptoSuite().signWithHsm(
                        Integer.parseInt(this.user.getKeyId()), 
                        this.user.getKeyType(),
                        message);
                }
            } else {
                signature = this.user.getCryptoSuite().rsaSign(
                    CryptoUtils.getPrivateKeyFromBytes(this.user.getPriBytes()), 
                    message);
            }
        } catch (ChainMakerCryptoSuiteException ec) {
            logger.warn("signature was failure. ec:", ec);
        }
        return signature;
    }

    public boolean verfiySignature(byte[] signBytes, byte[] message) {
        return false;
    }
}
