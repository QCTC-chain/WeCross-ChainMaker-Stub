package com.webank.wecross.stub.chainmaker.account;

import org.bouncycastle.util.encoders.Hex;
import org.chainmaker.sdk.User;
import org.chainmaker.sdk.config.AuthType;
import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;
import org.chainmaker.sdk.utils.CryptoUtils;
import org.chainmaker.sdk.utils.UtilsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.webank.wecross.stub.Account;

import java.security.cert.Certificate;
import java.io.IOException;

public class ChainMakerAccount implements Account {
    private static final Logger logger = LoggerFactory.getLogger(ChainMakerAccount.class);

    private final User user;
    private final String name;
    private final String type;

    public ChainMakerAccount(User user, String name, String type) {
        this.user = user;
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public String getIdentity() {
        String authType = this.user.getAuthType();
        if(authType.equals(AuthType.PermissionedWithCert.getMsg())) {
            try {
                return CryptoUtils.makeAddrFromCert(this.user.getCertificate());
            } catch (UtilsException ec) {
                logger.error("makeAddrFromCert was failure. ec: ", ec);
                return null;
            }
        } else {
            try {
                return  CryptoUtils.makeAddrFromPubKeyPem(this.user.getPublicKey());
            } catch (IOException ec) {
                logger.error("makeAddrFromPubKeyPem was failure. ec: ", ec);
                return null;
            }
        }
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
                if(this.user.getKeyId() == null || this.user.getKeyId().equals("")) {
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
        logger.info("sign, message: {}, signature: {}", Hex.toHexString(message), Hex.toHexString(signature));
        return signature;
    }

    public boolean verfiySignature(byte[] signBytes, byte[] message) {
        logger.info("verfiySignature: {}, {}", Hex.toHexString(message), Hex.toHexString(signBytes));
        boolean verified = false;
        try {
            Certificate certificate = this.user.getCryptoSuite().getCertificateFromBytes(this.user.getCertBytes());
            verified = this.user.getCryptoSuite().verify(certificate, signBytes, message);
        } catch (ChainMakerCryptoSuiteException e) {
            verified = false;
        }

        return verified;
    }
}
