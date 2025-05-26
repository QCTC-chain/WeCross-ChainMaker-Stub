package wecross.stub.ChainMaker;

import com.webank.wecross.stub.chainmaker.ChainMakerConnection;
import com.webank.wecross.stub.chainmaker.ChainMakerConnectionFactory;
import com.webank.wecross.stub.chainmaker.abi.wrapper.*;
import com.webank.wecross.stub.chainmaker.custom.DeployContractHandler;
import com.webank.wecross.stub.chainmaker.utils.ConfigUtils;
import com.webank.wecross.stub.chainmaker.utils.FunctionUtility;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;
import org.chainmaker.pb.common.*;
import org.chainmaker.pb.config.ChainConfigOuterClass;
import org.chainmaker.sdk.User;
import org.chainmaker.sdk.utils.CryptoUtils;
import org.chainmaker.sdk.utils.FileUtils;
import org.chainmaker.sdk.utils.SdkUtils;
import org.junit.Assert;
import org.junit.Test;

import com.webank.wecross.stub.chainmaker.ChainMakerStubFactory;
import org.web3j.abi.*;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.utils.Numeric;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ChainMakerStubTest {
    @Test
    public void ChainMakerStubFactoryTest() {
        try {
            ChainMakerStubFactory stubFactory = new ChainMakerStubFactory("ChainMakerWithCert");
            Assert.assertNotNull("stubFactory object is null", stubFactory);
            ChainMakerConnection connection = ChainMakerConnectionFactory.build(
                    "/Users/dbliu/work/java/WeCross-ChainMaker-Stub/src/main/resources",
                    "sdk_config.yml");
            ContractOuterClass.Contract[] contracts = connection.getChainClient().getContractList(5000);
            for(ContractOuterClass.Contract contract : contracts) {
                System.out.println("address = " + contract.getAddress() + ", name = " + contract.getName() + ", version = " + contract.getVersion());
            }

        } catch (Exception ec) {
            System.out.println(ec);
        }
    }

    @Test
    public void deployContract() {
        //ChainMakerStubFactory stubFactory = new ChainMakerStubFactory();
        //Assert.assertNotNull("stubFactory object is null", stubFactory);
        //ChainMakerConnection connection = (ChainMakerConnection) stubFactory.newConnection(
        //        "/Users/dbliu/work/java/WeCross-ChainMaker-Stub/src/main/resources");
        try {
            ResultOuterClass.TxResponse response = null;
            ChainMakerConnection connection = ChainMakerConnectionFactory.build(
                    "/Users/dbliu/work/java/WeCross-ChainMaker-Stub/src/main/resources",
                    "sdk_config.yml");
            User user1 = connection.getChainClient().getClientUser();
            User user2 = new User(
                    "qctc003-org-id-002",
                    FileUtils.getResourceFileBytes("crypto-config/qctc003-org-id-002/user/qctc003-org-admin-002/qctc003-org-admin-002.sign.key"),
                    FileUtils.getResourceFileBytes("crypto-config/qctc003-org-id-002/user/qctc003-org-admin-002/qctc003-org-admin-002.sign.crt"),
                    FileUtils.getResourceFileBytes("crypto-config/qctc003-org-id-002/user/qctc003-org-admin-002/qctc003-org-admin-002.tls.key"),
                    FileUtils.getResourceFileBytes("crypto-config/qctc003-org-id-002/user/qctc003-org-admin-002/qctc003-org-admin-002.tls.crt"));

            User user3 = new User(
                    "qctc003-org-id-003",
                    FileUtils.getResourceFileBytes("crypto-config/qctc003-org-id-003/user/qctc003-org-admin-003/qctc003-org-admin-003.sign.key"),
                    FileUtils.getResourceFileBytes("crypto-config/qctc003-org-id-003/user/qctc003-org-admin-003/qctc003-org-admin-003.sign.crt"),
                    FileUtils.getResourceFileBytes("crypto-config/qctc003-org-id-003/user/qctc003-org-admin-003/qctc003-org-admin-003.tls.key"),
                    FileUtils.getResourceFileBytes("crypto-config/qctc003-org-id-003/user/qctc003-org-admin-003/qctc003-org-admin-003.tls.crt"));

            byte[] byteCode = FileUtils.getResourceFileBytes("HelloWeCross.bin");
            Request.Payload payload = connection.getChainClient().createContractCreatePayload(
                    "HelloWeCrossV3",
                    "v1.0.0",
                    byteCode, ContractOuterClass.RuntimeType.EVM, null);
            Request.EndorsementEntry[] endorsementEntries = SdkUtils.getEndorsers(
                    payload,
                    new User[]{user1, user2, user3});

            response = connection.getChainClient().sendContractManageRequest(
                    payload, endorsementEntries, 10000, 10000);
        } catch (Exception e) {

        }

    }

    @Test
    public void deployCustomeContract() {
        try {
            ChainMakerStubFactory stubFactory = new ChainMakerStubFactory("ChainMakerWithCert");
            Assert.assertNotNull("stubFactory object is null", stubFactory);
            ChainMakerConnection connection = ChainMakerConnectionFactory.build(
                    "/Users/dbliu/work/java/WeCross-ChainMaker-Stub/src/main/resources", "sdk_config.yml");
            Map<String, byte[]> params = new HashMap<>();

            byte[] byteCode = FileUtils.getResourceFileBytes("HelloWeCross.bin");
            DynamicBytes customsContractBin = new DynamicBytes(byteCode);

            Function function = new Function(
                    "deployContract", Arrays.asList(customsContractBin), Collections.emptyList());
            String methodDataStr = FunctionEncoder.encode(function);
            String method = methodDataStr.substring(0,10);
            params.put("data", methodDataStr.getBytes());

            ResultOuterClass.TxResponse response = connection.getChainClient().invokeContract(
                    "WeCrossProxy",
                    method,
                    null, params, 5000, 5000);
            System.out.println(response);
            String hex = Hex.toHexString(response.getContractResult().getResult().toByteArray());
            List<Type> outputs = FunctionReturnDecoder.decode(hex, function.getOutputParameters());

            User use =  connection.getChainClient().getClientUser();
            String address = CryptoUtils.makeAddrFromCert(use.getCertificate());
            System.out.print(address);

        } catch (Exception ec) {
            System.out.println(ec);
        }
    }

    @Test
    public void invokeContract() {
        try {
            String abi = "[{\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"s\",\"type\":\"string\"}],\"name\":\"get1\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"}],\"stateMutability\":\"pure\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"s1\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"s2\",\"type\":\"string\"}],\"name\":\"get2\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"}],\"stateMutability\":\"pure\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"n\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
            //String abi = ConfigUtils.getContractABI("/Users/dbliu/Desktop/WeCross", "HelloWeCross");
            ABIDefinitionFactory abiDefinitionFactory = new ABIDefinitionFactory();
            ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(abi);
            List<ABIDefinition> functions =
                    contractABIDefinition
                            .getFunctions()
                            .get("get");
            List<ABIDefinition.NamedType> inputs = functions.get(0).getInputs();
            String _sig =  functions.get(0).getMethodSignatureAsString();
            Function set_function = new Function("set", Arrays.asList(new Utf8String("helloQCTCv2")), Collections.emptyList());
            Function get_function = new Function("get", Collections.emptyList(), Arrays.asList(new TypeReference<Utf8String>() {
            }));

            String encode_set_function = FunctionEncoder.encode(set_function);
            String set_abi = "0x" + encode_set_function.substring(10);
            String encode_get_function = FunctionEncoder.encode(get_function);
            String subString  = encode_get_function.substring(10);
            if (subString == null || subString.isEmpty()) {
                subString = "00";
            }
            String get_abi = "0x" + subString;

            Map<String, byte[]> params = new HashMap<>();

            Function function = new Function(
                    "get",
                    Collections.emptyList(),
                    Arrays.asList(new TypeReference<Utf8String>() {
                    }));

            String methodDataStr = FunctionEncoder.encode(function);
            String method = methodDataStr.substring(0,10);
            params.put("data", methodDataStr.getBytes());

            ChainMakerStubFactory stubFactory = new ChainMakerStubFactory("ChainMakerWithCert");
            Assert.assertNotNull("stubFactory object is null", stubFactory);
            ChainMakerConnection connection = ChainMakerConnectionFactory.build(
                    "/Users/dbliu/work/java/WeCross-ChainMaker-Stub/src/main/resources",
                    "sdk_config.yml");

            ResultOuterClass.TxResponse response = connection.getChainClient().invokeContract(
                    "HelloWeCross",
                    method,
                    null, params, 5000, 5000);
            System.out.println(response);
            String hex = Hex.toHexString(response.getContractResult().getResult().toByteArray());
            List<Type> outputs = FunctionReturnDecoder.decode(hex, function.getOutputParameters());
            System.out.println("get: " + outputs.get(0).getValue());

            User use =  connection.getChainClient().getClientUser();
            String address = CryptoUtils.makeAddrFromCert(use.getCertificate());
            System.out.print("address: " + address);

        } catch (Exception ec) {
            System.out.println(ec);
        }
    }

    @Test
    public void InvokeContractV2() {
        try {
            String[] args = null;
            ABICodecJsonWrapper abiCodecJsonWrapper = new ABICodecJsonWrapper();
            ChainMakerConnection connection = ChainMakerConnectionFactory.build(
                    "/Users/dbliu/work/java/WeCross-ChainMaker-Stub/src/main/resources",
                    "sdk_config.yml");

            String abi = ConfigUtils.getContractABI(
                    "/Users/dbliu/work/java/WeCross/dist/conf/chains/chainmaker",
                    "HelloWeCross");
            ABIDefinitionFactory abiDefinitionFactory = new ABIDefinitionFactory();
            ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(abi);
            List<ABIDefinition> abiFunctions =
                    contractABIDefinition
                            .getFunctions()
                            .get("get");

            ABIObject inputObj =
                    ABIObjectFactory.createInputObject(abiFunctions.get(0));
            String encodedArgs = "";
            if (!Objects.isNull(args)) {
                ABIObject encodedObj =
                        abiCodecJsonWrapper.encode(
                                inputObj, Arrays.asList(args));
                encodedArgs = encodedObj.encode();
            }

            Function function = FunctionUtility
                    .newSendTransactionProxyFunction(
                            "",
                            "",
                            0,
                            "",
                            abiFunctions.get(0).getMethodSignatureAsString(),
                            encodedArgs);
            String methodEncoded = FunctionEncoder.encode(function);
            System.out.println(methodEncoded);

            String method = methodEncoded.substring(0,10);
            Map<String, byte[]> params = new HashMap<>();
            params.put("data", methodEncoded.getBytes());

            ResultOuterClass.TxResponse response = connection.getChainClient().invokeContract(
                    "WeCrossProxy",
                    method,
                    null, params, 5000, 5000);
            System.out.println(response);
            String hex = Hex.toHexString(response.getContractResult().getResult().toByteArray());
            String output = hex.substring(128);
            //List<Type> outputs = FunctionReturnDecoder.decode(output, function.getOutputParameters());
            //byte[] bytes = (byte[]) outputs.get(0).getValue();
            //String resultOutput = new String(bytes, StandardCharsets.UTF_8);
            String resultOutput = FunctionUtility.decodeOutputAsString(output);
            System.out.println(resultOutput);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Test
    public void makeContractAddressTest() {
        String contractName = "WeCrossHub";
        String contractAddress = CryptoUtils.nameToAddrStr(contractName, ChainConfigOuterClass.AddrType.CHAINMAKER);
        System.out.println(contractAddress);

        contractAddress = CryptoUtils.nameToAddrStr(contractName, ChainConfigOuterClass.AddrType.ETHEREUM);
        System.out.println(contractAddress);

        contractName = "WeCrossProxy";
        contractAddress = CryptoUtils.nameToAddrStr(contractName, ChainConfigOuterClass.AddrType.CHAINMAKER);
        System.out.println(contractAddress);

        contractAddress = CryptoUtils.nameToAddrStr(contractName, ChainConfigOuterClass.AddrType.ETHEREUM);
        System.out.println(contractAddress);

        contractName = "sharedata_go";
        contractAddress = CryptoUtils.nameToAddrStr(contractName, ChainConfigOuterClass.AddrType.CHAINMAKER);
        System.out.println(contractAddress);

        contractAddress = CryptoUtils.nameToAddrStr(contractName, ChainConfigOuterClass.AddrType.ETHEREUM);
        System.out.println(contractAddress);

        try {
            Method method = CryptoUtils.class.getDeclaredMethod(
                    "generteAddrStr",
                    byte[].class,
                    ChainConfigOuterClass.AddrType.class);
            method.setAccessible(true);
            contractName = "ShareDataContract";
            // 通过 null 调用静态方法（如果方法是静态的）
            contractAddress = (String)method.invoke(null, contractName.getBytes(StandardCharsets.UTF_8),
                    ChainConfigOuterClass.AddrType.ETHEREUM);
            System.out.println(contractAddress);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    public void getLastBlockTest() {
        try {
            ResultOuterClass.TxResponse response = null;
            ChainMakerConnection connection = ChainMakerConnectionFactory.build(
                    "/Users/dbliu/work/java/WeCross-ChainMaker-Stub/src/main/resources",
                    "sdk_config.yml");
            ChainmakerBlock.BlockInfo blockInfo = connection.getChainClient().getLastBlock(false, 5000);
            BigInteger blockNumber = BigInteger.valueOf(blockInfo.getBlock().getHeader().getBlockHeight());
            System.out.println(blockNumber);

            ContractOuterClass.Contract[] contracts = connection.getChainClient().getContractList(5000);
            for(ContractOuterClass.Contract contract : contracts) {
                System.out.println(contract.getAddress() + ":" + contract.getName());
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    @Test
    public void funcTest() {
        Keccak.Digest256 digest256 = new Keccak.Digest256();
        String data = "set_event(string,string)";
        byte[] bytes = digest256.digest(data.getBytes());
        String topic = Numeric.toHexString(bytes);
        System.out.println(topic);
    }

    @Test
    public void generateConnectionTest() {
        ChainMakerStubFactory factory = new ChainMakerStubFactory("ChainMakerGMWithCert");
        String path = "/Users/dbliu/Desktop/chainmaker";
        String stubConfig = "{\n" +
                "  \"chainClient\": {\n" +
                "    \"chainId\": \"onechain\",\n" +
                "    \"orgId\": \"digital\",\n" +
                "    \"nodes\": {\n" +
                "      \"address\": \"127.0.0.1:12301\",\n" +
                "      \"enableTLS\": true\n" +
                "    }\n" +
                "  },\n" +
                "  \"organizations\": [\n" +
                "    {\n" +
                "      \"orgId\": \"digital\",\n" +
                "      \"orgName\": \"数字化部\",\n" +
                "      \"signCert\": \"signCertOssId\",\n" +
                "      \"signKey\": \"signKeyOssId\",\n" +
                "      \"users\": [\n" +
                "        {\n" +
                "          \"id\": \"lxm\",\n" +
                "          \"name\": \"lxm\",\n" +
                "          \"signCert\": \"oss://signcert\",\n" +
                "          \"signKey\": \"oss://signKey\",\n" +
                "          \"tlsCert\": \"oss://tlsCert\",\n" +
                "          \"tlsKey\": \"oss://tlsKey\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        String[] args = new String[] {"ChainMakerGMWithCert", "chainmaker", stubConfig};
        factory.generateConnection(path, args);
    }
}
