package com.webank.wecross.stub.chainmaker.abi;

import com.webank.wecross.stub.chainmaker.abi.TypeDecoder;
import static com.webank.wecross.stub.chainmaker.abi.Utils.getParameterizedTypeFromArray;
import com.webank.wecross.stub.chainmaker.abi.TypeReference;

import org.web3j.abi.datatypes.*;

public class DefaultFunctionReturnDecoder {
    public static <T extends Type> int getDataOffset(
            String input, int offset, TypeReference<?> typeReference)
            throws ClassNotFoundException {
        @SuppressWarnings("unchecked")
        Class<Type> type = (Class<Type>) typeReference.getClassType();
        if (DynamicBytes.class.isAssignableFrom(type)
                || Utf8String.class.isAssignableFrom(type)
                || DynamicArray.class.isAssignableFrom(type)
                || hasDynamicOffsetInStaticArray(typeReference, offset)) {
            return TypeDecoder.decodeUintAsInt(input, offset) << 1;
        } else {
            return offset;
        }
    }

    private static boolean hasDynamicOffsetInStaticArray(TypeReference<?> typeReference, int offset)
            throws ClassNotFoundException {
        @SuppressWarnings("unchecked")
        Class<Type> type = (Class<Type>) typeReference.getClassType();
        try {
            return StaticArray.class.isAssignableFrom(type)
                    && (DynamicStruct.class.isAssignableFrom(
                    getParameterizedTypeFromArray(typeReference))
                    || isDynamic(getParameterizedTypeFromArray(typeReference)));
        } catch (ClassCastException e) {
            return false;
        }
    }

    static <T extends Type> boolean isDynamic(Class<T> parameter) {
        return DynamicBytes.class.isAssignableFrom(parameter)
                || Utf8String.class.isAssignableFrom(parameter)
                || DynamicArray.class.isAssignableFrom(parameter);
    }
}
