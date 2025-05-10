package wecross.stub.ChainMaker;

import com.webank.wecross.stub.chainmaker.abi.ABICodec;
import com.webank.wecross.stub.chainmaker.abi.wrapper.*;
import com.webank.wecross.stub.chainmaker.config.ChainMakerStubConfigParser;
import com.webank.wecross.stub.chainmaker.utils.ConfigUtils;

import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ABITest {

    @Test
    public void testStubConfigParse() {
        try {
            ChainMakerStubConfigParser parser = new ChainMakerStubConfigParser(
                    "classpath:",
                    "stub.toml"
            );
            parser.loadEndorsementEntry();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private File[] getOrgIdPaths(File path) {
        File[] orgIdPaths = null;
        if (path.isDirectory()) {
            orgIdPaths = path.listFiles();
        }
        return orgIdPaths;
    }

    private File[] getUserPaths(File path) {
        File[] userPaths = null;
        if(path.isDirectory()) {
            userPaths = new File(path.getPath() + File.separator + "user").listFiles();
        }
        return userPaths;
    }

    private File getSignCertFilePath(File path) {
        return path.listFiles((dir, name) -> name.contains("sign.crt"))[0];
    }

    private File getSignKeyFilePath(File path) {
        return path.listFiles((dir, name) -> name.contains("sign.key"))[0];
    }

    private File getTLSCertFilePath(File path) {
        return path.listFiles((dir, name) -> name.contains("tls.crt"))[0];
    }

    private File getTLSKeyFilePath(File path) {
        return path.listFiles((dir, name) -> name.contains("tls.key"))[0];
    }
    @Test
    public void testGenerateStubToml() {
        String chainName = "ChainMakerWithCert";
        String stubType = "ChainMakerWithCert";
        String entries = "";
        String endorsement = "";
        String accountTemplate =
                "[common]\n"
                        + "    name = '"
                        + chainName
                        + "'\n"
                        + "    type = '"
                        + stubType
                        + "\n"
                        + "[endorsement]\n"
                        + "    entries = [%s]\n"
                        + "\n"
                        + "%s"
                        + "\n";

        String basePath = "/Users/dbliu/work/java/WeCross-ChainMaker-Stub/src/main/resources/";
        File sdkConfigPath = new File(
                basePath + File.separator + "crypto-config");
        File[] orgIds = getOrgIdPaths(sdkConfigPath);
        int lastIndex = 0;
        for(File orgId: orgIds) {
            entries += "'" + orgId.getName() + "'";
            if(++lastIndex != orgIds.length) {
                entries += ",";
            }

            endorsement += "[[" + orgId.getName() + "]]\n";
            File[] users = getUserPaths(orgId);
            for(File user: users) {
                String userName = user.getName();
                File signCertFilePath = getSignCertFilePath(user);
                File signKeyFilePath = getSignKeyFilePath(user);
                File tlsCertFilePath = getTLSCertFilePath(user);
                File tlsKeyFilePath = getTLSKeyFilePath(user);
                endorsement +=
                        "    userName = '" + userName + "'\n"
                                + "    user_key_file_path = '" + tlsKeyFilePath.getPath().substring(basePath.length()) + "'\n"
                                + "    user_crt_file_path = '" + tlsCertFilePath + "'\n"
                                + "    user_sign_key_file_path = '" + signKeyFilePath + "'\n"
                                + "    user_sign_crt_file_path = '" + signCertFilePath + "'\n"
                                + "\n";
            }
        }
        String stubContent = String.format(accountTemplate, entries, endorsement);
        System.out.println(stubContent);
    }

    @Test
    public void testNormal() {
        try {
            String[] params = new String[] {"hello,qctc!!!"};
            String abi = "[\n" +
                    "\t{\n" +
                    "\t\t\"constant\": false,\n" +
                    "\t\t\"inputs\": [\n" +
                    "\t\t\t{\n" +
                    "\t\t\t\t\"internalType\": \"string\",\n" +
                    "\t\t\t\t\"name\": \"n\",\n" +
                    "\t\t\t\t\"type\": \"string\"\n" +
                    "\t\t\t}\n" +
                    "\t\t],\n" +
                    "\t\t\"name\": \"set\",\n" +
                    "\t\t\"outputs\": [],\n" +
                    "\t\t\"payable\": false,\n" +
                    "\t\t\"stateMutability\": \"nonpayable\",\n" +
                    "\t\t\"type\": \"function\"\n" +
                    "\t},\n" +
                    "\t{\n" +
                    "\t\t\"constant\": false,\n" +
                    "\t\t\"inputs\": [\n" +
                    "\t\t\t{\n" +
                    "\t\t\t\t\"internalType\": \"bytes\",\n" +
                    "\t\t\t\t\"name\": \"_bs\",\n" +
                    "\t\t\t\t\"type\": \"bytes\"\n" +
                    "\t\t\t}\n" +
                    "\t\t],\n" +
                    "\t\t\"name\": \"setBytes\",\n" +
                    "\t\t\"outputs\": [],\n" +
                    "\t\t\"payable\": false,\n" +
                    "\t\t\"stateMutability\": \"nonpayable\",\n" +
                    "\t\t\"type\": \"function\"\n" +
                    "\t},\n" +
                    "\t{\n" +
                    "\t\t\"constant\": true,\n" +
                    "\t\t\"inputs\": [],\n" +
                    "\t\t\"name\": \"get\",\n" +
                    "\t\t\"outputs\": [\n" +
                    "\t\t\t{\n" +
                    "\t\t\t\t\"internalType\": \"string\",\n" +
                    "\t\t\t\t\"name\": \"\",\n" +
                    "\t\t\t\t\"type\": \"string\"\n" +
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
                    "\t\t\t\t\"internalType\": \"string\",\n" +
                    "\t\t\t\t\"name\": \"s\",\n" +
                    "\t\t\t\t\"type\": \"string\"\n" +
                    "\t\t\t}\n" +
                    "\t\t],\n" +
                    "\t\t\"name\": \"get1\",\n" +
                    "\t\t\"outputs\": [\n" +
                    "\t\t\t{\n" +
                    "\t\t\t\t\"internalType\": \"string\",\n" +
                    "\t\t\t\t\"name\": \"\",\n" +
                    "\t\t\t\t\"type\": \"string\"\n" +
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
                    "\t\t\t\t\"internalType\": \"string\",\n" +
                    "\t\t\t\t\"name\": \"s1\",\n" +
                    "\t\t\t\t\"type\": \"string\"\n" +
                    "\t\t\t},\n" +
                    "\t\t\t{\n" +
                    "\t\t\t\t\"internalType\": \"string\",\n" +
                    "\t\t\t\t\"name\": \"s2\",\n" +
                    "\t\t\t\t\"type\": \"string\"\n" +
                    "\t\t\t}\n" +
                    "\t\t],\n" +
                    "\t\t\"name\": \"get2\",\n" +
                    "\t\t\"outputs\": [\n" +
                    "\t\t\t{\n" +
                    "\t\t\t\t\"internalType\": \"string\",\n" +
                    "\t\t\t\t\"name\": \"\",\n" +
                    "\t\t\t\t\"type\": \"string\"\n" +
                    "\t\t\t}\n" +
                    "\t\t],\n" +
                    "\t\t\"payable\": false,\n" +
                    "\t\t\"stateMutability\": \"view\",\n" +
                    "\t\t\"type\": \"function\"\n" +
                    "\t}\n" +
                    "]";
            abi = "[{\"constant\":true,\"inputs\":[],\"name\":\"getVersion\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_num\",\"type\":\"uint256\"}],\"name\":\"getInterchainRequests\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_index\",\"type\":\"uint256\"}],\"name\":\"updateCurrentRequestIndex\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_path\",\"type\":\"string\"},{\"name\":\"_method\",\"type\":\"string\"},{\"name\":\"_args\",\"type\":\"string[]\"},{\"name\":\"_callbackPath\",\"type\":\"string\"},{\"name\":\"_callbackMethod\",\"type\":\"string\"}],\"name\":\"interchainInvoke\",\"outputs\":[{\"name\":\"uid\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"getIncrement\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_uid\",\"type\":\"string\"},{\"name\":\"_tid\",\"type\":\"string\"},{\"name\":\"_seq\",\"type\":\"string\"},{\"name\":\"_errorCode\",\"type\":\"string\"},{\"name\":\"_errorMsg\",\"type\":\"string\"},{\"name\":\"_result\",\"type\":\"string[]\"}],\"name\":\"registerCallbackResult\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_path\",\"type\":\"string\"},{\"name\":\"_method\",\"type\":\"string\"},{\"name\":\"_args\",\"type\":\"string[]\"},{\"name\":\"_callbackPath\",\"type\":\"string\"},{\"name\":\"_callbackMethod\",\"type\":\"string\"}],\"name\":\"interchainQuery\",\"outputs\":[{\"name\":\"uid\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_uid\",\"type\":\"string\"}],\"name\":\"selectCallbackResult\",\"outputs\":[{\"name\":\"\",\"type\":\"string[]\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}]";
            ABIDefinitionFactory abiDefinitionFactory = new ABIDefinitionFactory();
            ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(abi);
            ABICodecJsonWrapper abiCodecJsonWrapper = new ABICodecJsonWrapper();
            ABIDefinition abiDefinition = contractABIDefinition.getFunctions().get("setBytes").get(0);
            ABIObject inputObject = ABIObjectFactory.createInputObject(abiDefinition);
            ABIObject encoded = abiCodecJsonWrapper.encode(inputObject, Arrays.asList(params));
            String s = encoded.encode();
            String signature = abiDefinition.getMethodSignatureAsString();
            System.out.println(s);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private String buildMethodId(String methodSignature) {
        final byte[] input = methodSignature.getBytes();
        final byte[] hash = Hash.sha3(input);
        return Numeric.toHexString(hash);
    }

    @Test
    public void decodeEventTest() {
        try {

            String method_1 = buildMethodId("set_event_v2(string,string)");
            String method_2 = buildMethodId("set_event(string,string)");
            String method_3 = buildMethodId("get()");

            String abiContent = ConfigUtils.getContractABI(
                    "/Users/dbliu/Desktop/Desktop/WeCross/chainmaker/conf/chains/chainmaker",
                    "HelloWorld");
            String topic = "947bf05b6149af7d137ed47b3be724b62179b971a51ccd0abf3d02adb0db14ee";
            String eventData = "0000000000000000000000000000000000000000000000000000000000000004";
            ABICodec abiCodec = new ABICodec();
            List<Object> params = abiCodec.decodeEvent(abiContent, topic, eventData);
            System.out.println("");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void decodeContractResultOutputTest() {
        try {
            String abi = "[\n" +
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
                    "\t}\n" +
                    "]";
            String output = "000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000a000000000000000000000000000000000000000000000000000000000000000e000000000000000000000000000000000000000000000000000000000000004600000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000096969696964646431360000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000014000000000000000000000000000000000000000000000000000000000000001a0000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000002400000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000028000000000000000000000000000000000000000000000000000000000000002c0000000000000000000000000000000000000000000000000000000000000030000000000000000000000000000000000000000000000000000000000000003404044e8c0ca70b0810e8dda4356ed122b015da75df547989a33733fc62b64d998000000000000000000000000000000000000000000000000000000000000003974636b7032303235303130373033313330353336313637305f303030315f434d505f474d535f32303235303131303033323135362e6a736f6e000000000000000000000000000000000000000000000000000000000000000000000000000040613332363439373266646265393038316433353465363131386537303938626636383962663936336633613338313737326432643732393934306330383663320000000000000000000000000000000000000000000000000000000000000004746578740000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000026c7500000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000073131313333333300000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001f7b2274657374223a2231313131222c227465737431223a223232323232227d0000000000000000000000000000000000000000000000000000000000000000093132343435353636360000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000c736861726573657276696365000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001400000000000000000000000000000000000000000000000000000000000000160000000000000000000000000000000000000000000000000000000000000018000000000000000000000000000000000000000000000000000000000000001a0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001c000000000000000000000000000000000000000000000000000000000000001e000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000220000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
            ABIDefinitionFactory abiDefinitionFactory = new ABIDefinitionFactory();
            ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(abi);
            ABIDefinition function = contractABIDefinition.getFunctions().get("getOrderData").get(0);
            ABIObject abiOutputObject = ABIObjectFactory.createOutputObject(function);
            ABICodecObject abiCodecObject = new ABICodecObject();
            List<Object> decodeObject = abiCodecObject.decodeJavaObject(abiOutputObject, output);
            String decodedStr = decodeObject.toString();
            System.out.println(decodedStr);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
