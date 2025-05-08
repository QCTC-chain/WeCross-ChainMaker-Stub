package com.webank.wecross.stub.chainmaker.utils;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

public class Serialization {
    // 序列化方法
    public static byte[] serialize(Object obj) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {

            oos.writeObject(obj);
            return bos.toByteArray();
        }
    }

    // 反序列化方法
    public static Object deserialize(byte[] bytes)
            throws IOException, ClassNotFoundException {

        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {

            return ois.readObject();
        }
    }
}
