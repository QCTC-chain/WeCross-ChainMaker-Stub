package wecross.stub.ChainMaker;

import com.webank.wecross.stub.chainmaker.utils.Web3jFunctionBuilder;
import org.junit.Test;

import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.StaticStruct;

import java.io.IOException;
import java.util.List;

public class Web3jFunctionBuilderTest {

    @Test
    public void funTest() {
        String contractAbiJson = "[\n" +
                "\t{\n" +
                "\t\t\"constant\": false,\n" +
                "\t\t\"inputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"name\": \"account\",\n" +
                "\t\t\t\t\"type\": \"address\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"name\": \"category\",\n" +
                "\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"name\": \"authorize\",\n" +
                "\t\t\"outputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"name\": \"\",\n" +
                "\t\t\t\t\"type\": \"bool\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"payable\": false,\n" +
                "\t\t\"stateMutability\": \"nonpayable\",\n" +
                "\t\t\"type\": \"function\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"constant\": false,\n" +
                "\t\t\"inputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"components\": [\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"components\": [\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"name\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"hash\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"mediaType\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"shareParty\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"isSender\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"bool\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"businessType\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"businessData\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"createTime\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"remark\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"txHash\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"bytes32\"\n" +
                "\t\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t\t],\n" +
                "\t\t\t\t\t\t\"name\": \"dataInfo\",\n" +
                "\t\t\t\t\t\t\"type\": \"tuple\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"name\": \"id\",\n" +
                "\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t],\n" +
                "\t\t\t\t\"name\": \"setDataReq\",\n" +
                "\t\t\t\t\"type\": \"tuple\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"name\": \"setOrderData\",\n" +
                "\t\t\"outputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"name\": \"\",\n" +
                "\t\t\t\t\"type\": \"bool\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"payable\": false,\n" +
                "\t\t\"stateMutability\": \"nonpayable\",\n" +
                "\t\t\"type\": \"function\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"constant\": false,\n" +
                "\t\t\"inputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"name\": \"newOwner\",\n" +
                "\t\t\t\t\"type\": \"address\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"name\": \"transferOwner\",\n" +
                "\t\t\"outputs\": [],\n" +
                "\t\t\"payable\": false,\n" +
                "\t\t\"stateMutability\": \"nonpayable\",\n" +
                "\t\t\"type\": \"function\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"inputs\": [],\n" +
                "\t\t\"payable\": false,\n" +
                "\t\t\"stateMutability\": \"nonpayable\",\n" +
                "\t\t\"type\": \"constructor\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"anonymous\": false,\n" +
                "\t\t\"inputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"indexed\": false,\n" +
                "\t\t\t\t\"name\": \"eventType\",\n" +
                "\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"indexed\": false,\n" +
                "\t\t\t\t\"name\": \"dataId\",\n" +
                "\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"indexed\": false,\n" +
                "\t\t\t\t\"name\": \"shareParty\",\n" +
                "\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"indexed\": false,\n" +
                "\t\t\t\t\"name\": \"senderFlag\",\n" +
                "\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"name\": \"ShareData\",\n" +
                "\t\t\"type\": \"event\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"anonymous\": false,\n" +
                "\t\t\"inputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"indexed\": false,\n" +
                "\t\t\t\t\"name\": \"eventType\",\n" +
                "\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"indexed\": false,\n" +
                "\t\t\t\t\"name\": \"sender\",\n" +
                "\t\t\t\t\"type\": \"address\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"components\": [\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"name\": \"id\",\n" +
                "\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"components\": [\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"name\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"hash\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"mediaType\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"shareParty\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"isSender\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"bool\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"businessType\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"businessData\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"createTime\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"remark\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"txHash\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"bytes32\"\n" +
                "\t\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t\t],\n" +
                "\t\t\t\t\t\t\"name\": \"sender\",\n" +
                "\t\t\t\t\t\t\"type\": \"tuple\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"components\": [\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"name\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"hash\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"mediaType\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"shareParty\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"isSender\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"bool\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"businessType\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"businessData\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"createTime\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"remark\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"txHash\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"bytes32\"\n" +
                "\t\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t\t],\n" +
                "\t\t\t\t\t\t\"name\": \"receiver\",\n" +
                "\t\t\t\t\t\t\"type\": \"tuple\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"name\": \"senderSet\",\n" +
                "\t\t\t\t\t\t\"type\": \"bool\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"name\": \"receiverSet\",\n" +
                "\t\t\t\t\t\t\"type\": \"bool\"\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t],\n" +
                "\t\t\t\t\"indexed\": false,\n" +
                "\t\t\t\t\"name\": \"data\",\n" +
                "\t\t\t\t\"type\": \"tuple\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"indexed\": false,\n" +
                "\t\t\t\t\"name\": \"authResult\",\n" +
                "\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"name\": \"SetShareData\",\n" +
                "\t\t\"type\": \"event\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"constant\": true,\n" +
                "\t\t\"inputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"name\": \"id\",\n" +
                "\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"name\": \"checkOrderExist\",\n" +
                "\t\t\"outputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"name\": \"\",\n" +
                "\t\t\t\t\"type\": \"bool\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"payable\": false,\n" +
                "\t\t\"stateMutability\": \"view\",\n" +
                "\t\t\"type\": \"function\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"constant\": true,\n" +
                "\t\t\"inputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"name\": \"id\",\n" +
                "\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"name\": \"getOrderData\",\n" +
                "\t\t\"outputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"components\": [\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"name\": \"id\",\n" +
                "\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"components\": [\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"name\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"hash\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"mediaType\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"shareParty\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"isSender\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"bool\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"businessType\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"businessData\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"createTime\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"remark\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"txHash\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"bytes32\"\n" +
                "\t\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t\t],\n" +
                "\t\t\t\t\t\t\"name\": \"sender\",\n" +
                "\t\t\t\t\t\t\"type\": \"tuple\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"components\": [\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"name\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"hash\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"mediaType\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"shareParty\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"isSender\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"bool\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"businessType\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"businessData\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"createTime\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"remark\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"txHash\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"bytes32\"\n" +
                "\t\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t\t],\n" +
                "\t\t\t\t\t\t\"name\": \"receiver\",\n" +
                "\t\t\t\t\t\t\"type\": \"tuple\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"name\": \"senderSet\",\n" +
                "\t\t\t\t\t\t\"type\": \"bool\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"name\": \"receiverSet\",\n" +
                "\t\t\t\t\t\t\"type\": \"bool\"\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t],\n" +
                "\t\t\t\t\"name\": \"\",\n" +
                "\t\t\t\t\"type\": \"tuple\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"payable\": false,\n" +
                "\t\t\"stateMutability\": \"view\",\n" +
                "\t\t\"type\": \"function\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"constant\": true,\n" +
                "\t\t\"inputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"name\": \"pageSize\",\n" +
                "\t\t\t\t\"type\": \"uint256\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"name\": \"pageNum\",\n" +
                "\t\t\t\t\"type\": \"uint256\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"name\": \"getOrders\",\n" +
                "\t\t\"outputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"components\": [\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"name\": \"id\",\n" +
                "\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"components\": [\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"name\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"hash\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"mediaType\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"shareParty\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"isSender\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"bool\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"businessType\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"businessData\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"createTime\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"remark\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"txHash\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"bytes32\"\n" +
                "\t\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t\t],\n" +
                "\t\t\t\t\t\t\"name\": \"sender\",\n" +
                "\t\t\t\t\t\t\"type\": \"tuple\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"components\": [\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"name\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"hash\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"mediaType\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"shareParty\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"isSender\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"bool\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"businessType\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"businessData\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"createTime\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"remark\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\t\"name\": \"txHash\",\n" +
                "\t\t\t\t\t\t\t\t\"type\": \"bytes32\"\n" +
                "\t\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t\t],\n" +
                "\t\t\t\t\t\t\"name\": \"receiver\",\n" +
                "\t\t\t\t\t\t\"type\": \"tuple\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"name\": \"senderSet\",\n" +
                "\t\t\t\t\t\t\"type\": \"bool\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"name\": \"receiverSet\",\n" +
                "\t\t\t\t\t\t\"type\": \"bool\"\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t],\n" +
                "\t\t\t\t\"name\": \"\",\n" +
                "\t\t\t\t\"type\": \"tuple[]\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"payable\": false,\n" +
                "\t\t\"stateMutability\": \"view\",\n" +
                "\t\t\"type\": \"function\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"constant\": true,\n" +
                "\t\t\"inputs\": [],\n" +
                "\t\t\"name\": \"getOrderSize\",\n" +
                "\t\t\"outputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"name\": \"\",\n" +
                "\t\t\t\t\"type\": \"uint256\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"payable\": false,\n" +
                "\t\t\"stateMutability\": \"view\",\n" +
                "\t\t\"type\": \"function\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"constant\": true,\n" +
                "\t\t\"inputs\": [],\n" +
                "\t\t\"name\": \"owner\",\n" +
                "\t\t\"outputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"name\": \"\",\n" +
                "\t\t\t\t\"type\": \"address\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"payable\": false,\n" +
                "\t\t\"stateMutability\": \"view\",\n" +
                "\t\t\"type\": \"function\"\n" +
                "\t}\n" +
                "]";

        Web3jFunctionBuilder builder = new Web3jFunctionBuilder();

        // Test case for createEvidence
        String functionNameCreate = "authorize";
        // Input values as per the user's request format
        String[] inputValuesCreate = {"account", "ec960e7a24968ab2640cb566710a46ed3f065b9a", "category", "1"};

        try {
            System.out.println("--- Testing createEvidence ---");

            Function web3jFunctionCreate = builder.buildFunctionFromAbi(contractAbiJson, functionNameCreate, inputValuesCreate);
            System.out.println("Function Name: " + web3jFunctionCreate.getName());
            System.out.println("Input Parameters (" + web3jFunctionCreate.getInputParameters().size() + "):");

            for (Type<?> param : web3jFunctionCreate.getInputParameters()) {
                System.out.println("  Type: " + param.getTypeAsString() + ", Value: " + param.getValue() +
                        ", Web3j Class: " + param.getClass().getSimpleName());
                if (param instanceof StaticStruct || param instanceof DynamicStruct) {
                    List<Type> components;
                    if (param instanceof StaticStruct) {
                        components = ((StaticStruct) param).getValue();
                    } else {
                        components = ((DynamicStruct) param).getValue();
                    }
                    System.out.println("    Struct Components:");
                    for(Type component : components) {
                        System.out.println("      Component Type: " + component.getTypeAsString() +
                                ", Value: " + component.getValue() +
                                ", Web3j Class: " + component.getClass().getSimpleName());
                    }
                }
            }
            System.out.println("Output Parameters (" + web3jFunctionCreate.getOutputParameters().size() + "):");
            for (org.web3j.abi.TypeReference<?> outParam : web3jFunctionCreate.getOutputParameters()) {
                System.out.println("  TypeReference Class: " + outParam.getClassType().getCanonicalName());
            }
            // To get the encoded function call data:
             String encodedFunctionCall = org.web3j.abi.FunctionEncoder.encode(web3jFunctionCreate);
             System.out.println("Encoded function call: " + encodedFunctionCall);

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error building function 'createEvidence': " + e.getMessage());
            e.printStackTrace();
        }
    }
}
