package com.webank.wecross.stub.chainmaker.utils;

import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.Utils;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Hash;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tuples.generated.Tuple4;
import org.web3j.tuples.generated.Tuple6;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.*;

/**
 * Function object used across blockchain chain. Wecross requires that a cross-chain contract
 * interface must conform to the following format:
 *
 * <p>function funcName(string[] params) public returns(string[])
 *
 * <p>or
 *
 * <p>function funcName() public returns(string[])
 */
@SuppressWarnings("rawtypes")
public class FunctionUtility {

    public static final int MethodIDLength = 8;
    public static final int MethodIDWithHexPrefixLength = MethodIDLength + 2;

    private static String buildMethodId(final String methodSignature) {
        final byte[] input = methodSignature.getBytes();
        final byte[] hash = Hash.sha3(input);
        return Numeric.toHexString(hash).substring(0, 10);
    }

    /**
     * WeCrossHub getInterchainRequests function <br>
     * </>function getInterchainRequests(uint256 _num) public view
     * returns(string memory)
     *
     * @param num
     * @return
     */
    public static Function newGetInterChainRequestHubFunction(int num) {
        Function function =
                new Function(
                        "getInterchainRequests",
                        Arrays.<Type>asList(new Uint256(num)),
                        Collections.<TypeReference<?>>emptyList());
        return function;
    }

    /**
     * WeCrossProxy constantCall function <br>
     * </>function sendTransaction(string memory _name, bytes memory _argsWithMethodId) public
     * returns(bytes memory)
     *
     * @param id
     * @param path
     * @param methodSignature
     * @param abi
     * @return
     */
    public static Function newConstantCallProxyFunction(
            String id, String path, String methodSignature, String abi) {
        Function function =
                new Function(
                        "constantCall",
                        Arrays.<Type>asList(
                                new Utf8String(id),
                                new Utf8String(path),
                                new Utf8String(methodSignature),
                                new DynamicBytes(Numeric.hexStringToByteArray(abi))),
                        Collections.<TypeReference<?>>emptyList());
        return function;
    }

    /**
     * WeCrossProxy constantCall function function sendTransaction(string memory _name, bytes memory
     * _argsWithMethodId) public returns(bytes memory)
     *
     * @param name
     * @param methodSignature
     * @param abi
     * @return
     */
    public static Function newConstantCallProxyFunction( String name, String methodSignature, String abi) {
        String methodId = buildMethodId(methodSignature);
        Function function =
                new Function(
                        "constantCall",
                        Arrays.<Type>asList(
                                new Utf8String(name),
                                new DynamicBytes(Numeric.hexStringToByteArray(methodId + abi))),
                        Collections.<TypeReference<?>>emptyList());
        return function;
    }

    /**
     * WeCrossProxy sendTransaction function function sendTransaction(string memory _name, bytes
     * memory _argsWithMethodId) public returns(bytes memory)
     *
     * @param uid
     * @param name
     * @param methodSignature
     * @param abi
     * @return
     */
    public static Function newSendTransactionProxyFunction(
            String uid,
            String name,
            String methodSignature,
            String abi) {
        String methodId = buildMethodId(methodSignature);
        Function function =
                new Function(
                        "sendTransaction",
                        Arrays.<Type>asList(
                                new Utf8String(uid),
                                new Utf8String(name),
                                new DynamicBytes(Numeric.hexStringToByteArray(methodId + abi))),
                        Collections.<TypeReference<?>>emptyList());
        return function;
    }

    /**
     * WeCrossProxy sendTransaction function function sendTransaction(string memory _transactionID,
     * uint256 _seq, string memory _path, string memory _func, bytes memory _args) public
     * returns(bytes memory)
     *
     * @param uid
     * @param tid
     * @param seq
     * @param path
     * @param methodSignature
     * @param abi
     * @return
     */
    public static Function newSendTransactionProxyFunction(
            String uid, String tid, long seq, String path, String methodSignature, String abi) {
        Function function =
                new Function(
                        "sendTransaction",
                        Arrays.<Type>asList(
                                new Utf8String(uid),
                                new Utf8String(tid),
                                new Uint256(seq),
                                new Utf8String(path),
                                new Utf8String(methodSignature),
                                new DynamicBytes(Numeric.hexStringToByteArray(abi))),
                        Collections.<TypeReference<?>>emptyList());
        return function;
    }

    public static Function newRegisterCNS(String path, String contractAddress) {
        Function function = new Function(
                "registerCNS",
                Arrays.asList(new Utf8String(path), new Utf8String(contractAddress)),
                Collections.emptyList());
        return function;
    }

    public static String decodeOutputAsString(String output) {
        if (Objects.isNull(output) || output.length() < MethodIDWithHexPrefixLength) {
            return null;
        }

        List<Type> outputTypes =
                FunctionReturnDecoder.decode(
                        output,
                        Utils.convert(
                                Collections.singletonList(new TypeReference<Utf8String>() {})));
        if (Objects.isNull(outputTypes) || outputTypes.isEmpty()) {
            return null;
        }

        return (String) outputTypes.get(0).getValue();
    }
}
