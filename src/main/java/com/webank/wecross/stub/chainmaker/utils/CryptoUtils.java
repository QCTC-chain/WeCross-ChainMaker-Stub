package com.webank.wecross.stub.chainmaker.utils;

import org.chainmaker.pb.config.ChainConfigOuterClass;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CryptoUtils {

    private static Logger logger = LoggerFactory.getLogger(CryptoUtils.class);

    public static String generateAddress(String contractName) {
        String address = "";
        try {
            Method method = org.chainmaker.sdk.utils.CryptoUtils.class.getDeclaredMethod(
                    "generteAddrStr",
                    byte[].class,
                    ChainConfigOuterClass.AddrType.class);
            method.setAccessible(true);
            // 通过 null 调用静态方法（如果方法是静态的）
            address = (String)method.invoke(null, contractName.getBytes(StandardCharsets.UTF_8),
                    ChainConfigOuterClass.AddrType.CHAINMAKER);
        } catch (Exception e) {
            logger.error("通过合约名生成合约地址失败。{}", contractName);
        }
        return address;
    }
}
