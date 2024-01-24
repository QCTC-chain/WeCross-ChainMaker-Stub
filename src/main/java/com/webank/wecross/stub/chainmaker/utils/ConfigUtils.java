package com.webank.wecross.stub.chainmaker.utils;

import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.webank.wecross.exception.WeCrossException;

public class ConfigUtils {
    public static String classpath2Absolute(String fileName) throws WeCrossException {
        try {
            PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver();
            return resolver.getResource(fileName).getFile().getAbsolutePath();
        } catch (Exception e) {
            throw new WeCrossException(
                    WeCrossException.ErrorCode.INTERNAL_ERROR,
                    "Something wrong with parse " + fileName + ": " + e.getMessage());
        }
    }
}
