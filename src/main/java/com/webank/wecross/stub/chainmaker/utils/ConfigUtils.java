package com.webank.wecross.stub.chainmaker.utils;

import org.chainmaker.sdk.utils.FileUtils;
import org.chainmaker.sdk.utils.UtilsException;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.webank.wecross.exception.WeCrossException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public static void writeContractABI(String filePath, String fileName, String data) throws IOException, WeCrossException {
        if(filePath.contains("classpath:")) {
            filePath = classpath2Absolute(filePath);
        }

        filePath += File.separator + fileName;
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        File file = new File(filePath + File.separator + fileName + ".abi");
        FileWriter fw = new FileWriter(file);
        fw.write(data);
        fw.close();
    }

    public static String getContractABI(String filePath, String fileName) throws IOException, WeCrossException {
        if(filePath.contains("classpath:")) {
            filePath = classpath2Absolute(filePath);
        }

        filePath +=  File.separator + fileName;
        String content = new String(Files.readAllBytes(Paths.get(filePath + File.separator + fileName + ".abi")));
        return content;
    }
}
