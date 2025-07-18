package com.webank.wecross.stub.chainmaker.account;

import org.chainmaker.sdk.User;
import org.chainmaker.sdk.config.AuthType;
import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;
import org.chainmaker.sdk.utils.CryptoUtils;
import org.chainmaker.sdk.utils.UtilsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

public class ChainMakerAccountFactory {
    private static final Logger logger = LoggerFactory.getLogger(ChainMakerAccountFactory.class);

    public static ChainMakerAccount build(Map<String, Object> properties) {
        User user;

        String accountType = (String) properties.get("type");
        String authType = (String) properties.get("authType");
        String orgId = (String)  properties.get("orgId");
        String name = (String) properties.get("username");
        int keyID = (int) properties.get("keyID");
        Boolean isDefault = (Boolean) properties.get("isDefault");


        if(authType.equals(AuthType.PermissionedWithKey.getMsg()) || authType.equals(AuthType.Public.getMsg())) {
            String privateKeyStr = (String) properties.get("userKey");
            try {
                user = new User(orgId);
                PrivateKey privateKey = CryptoUtils.getPrivateKeyFromBytes(privateKeyStr.getBytes());
                user.setPrivateKey(privateKey);
                try {
                    PublicKey publicKey = CryptoUtils.getPublicKeyFromPrivateKey(privateKey);
                    user.setPublicKey(publicKey);
                    user.setPukBytes(CryptoUtils.getPemStrFromPublicKey(publicKey).getBytes());
                } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException | UtilsException ec) {
                    logger.warn("build a chainmaker account was failure. e: ", ec);
                    return null;
                }

            } catch (ChainMakerCryptoSuiteException ec) {
                logger.warn("build a chainmaker account was failure. e: ", ec);
                return null;
            }
        } else {
            String userSignKey = (String) properties.get("userSignKey");
            String userSignCrt = (String) properties.get("userSignCert");
            String userKey = (String) properties.get("userKey");
            String userCrt = (String) properties.get("userCert");
            boolean pkcs11Enable = (boolean) properties.get("pkcs11Enable");
            try {
                user = new User(
                    orgId, 
                    userSignKey.getBytes(), 
                    userSignCrt.getBytes(),
                    userKey.getBytes(),
                    userCrt.getBytes(),
                    pkcs11Enable);
            } catch (ChainMakerCryptoSuiteException ec) {
                logger.warn("build a chainmaker account was failure. e: ", ec);
                return null;
            }
        }
        user.setAuthType(authType);
        user.setEnableTxResultDispatcher(false);
        return new ChainMakerAccount(user, name, accountType, keyID, isDefault);
    }
}
