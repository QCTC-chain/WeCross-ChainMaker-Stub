package com.webank.wecross.stub.chainmaker;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.chainmaker.account.ChainMakerAccount;
import com.webank.wecross.stub.chainmaker.common.ChainMakerConstant;
import com.webank.wecross.stub.chainmaker.config.ChainMakerStubConfigParser;
import com.webank.wecross.stub.chainmaker.utils.CryptoUtils;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;
import org.chainmaker.pb.common.ContractOuterClass;
import org.chainmaker.sdk.ChainClient;

import com.webank.wecross.stub.chainmaker.client.ChainMakerClient;
import org.chainmaker.sdk.ChainClientException;
import org.chainmaker.sdk.ChainManager;
import org.chainmaker.sdk.User;
import org.chainmaker.sdk.config.SdkConfig;
import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChainMakerConnectionFactory {

    private static final Logger logger = LoggerFactory.getLogger(ChainMakerConnectionFactory.class);

    private static Map<String, ChainMakerConnection> connectionMap = new HashMap<>();

    private static void addProperties(ChainMakerConnection connection, String stubConfigPath) throws Exception {
        ChainMakerStubConfigParser stubConfigParser = new ChainMakerStubConfigParser(
                stubConfigPath, "stub.toml");
        connection.addProperty(ChainMakerConstant.CHAINMAKER_CHAIN_ID, connection.getChainClient().getChainId());
        connection.addProperty(ChainMakerConstant.CHAINMAKER_STUB_TYPE, stubConfigParser.getStubType());

        ContractOuterClass.Contract[] contracts = connection.getChainClient().getContractList(5000);
        for(ContractOuterClass.Contract contract : contracts) {
            if (ChainMakerConstant.CHAINMAKER_HUB_NAME.equals(contract.getName())
                    || CryptoUtils.generateAddress(ChainMakerConstant.CHAINMAKER_HUB_NAME).equals(contract.getName())) {
                connection.addProperty(ChainMakerConstant.CHAINMAKER_HUB_NAME, contract.getAddress());
            }
            if (ChainMakerConstant.CHAINMAKER_PROXY_NAME.equals(contract.getName())
                    || CryptoUtils.generateAddress(ChainMakerConstant.CHAINMAKER_PROXY_NAME).equals(contract.getName())) {
                connection.addProperty(ChainMakerConstant.CHAINMAKER_PROXY_NAME, contract.getAddress());
            }
        }
    }

    public static ChainMakerConnection build(String stubConfigPath, String configName) throws Exception {
        ChainClient chainClient = ChainMakerClient.createChainClient(stubConfigPath, configName);
        ChainMakerConnection connection = new ChainMakerConnection(chainClient);
        addProperties(connection, stubConfigPath);
        return  connection;
    }

    private static String sha256(byte[] data) {
        Keccak.DigestKeccak kecc = new Keccak.Digest256();
        kecc.update(data, 0, data.length);
        byte[] address = kecc.digest();
        String addr = Hex.toHexString(address);
        return "0x" + addr.substring(24);
    }

    public synchronized static ChainMakerConnection build(String stubConfigPath, Account account) throws Exception {
        ChainMakerAccount chainMakerAccount = (ChainMakerAccount) account;
        ChainMakerConnection connection = connectionMap.get(chainMakerAccount.getIdentity());
        if(connection != null) {
            return connection;
        }
        SdkConfig sdkConfig = ChainMakerClient.loadConfig(stubConfigPath, "sdk_config.yml");
        ChainClient chainClient = ChainManager.getInstance().createChainClientWithoutPool(
                sdkConfig,
                chainMakerAccount.getUser());
        connection = new ChainMakerConnection(chainClient);
        connection.setConfigPath(stubConfigPath);

        User clientUser = connection.getChainClient().getClientUser();
        String signCrtHash = sha256(clientUser.getCertBytes());
        String signKeyHash = sha256(clientUser.getPrivateKey().getEncoded());
        String tlsCrtHash = sha256(clientUser.getTlsCertificate().getEncoded());
        String tlsKeyHash = sha256(clientUser.getTlsPrivateKey().getEncoded());
        String orgId = clientUser.getOrgId();
        String authType = clientUser.getAuthType();
        Boolean isEnableTxResult = clientUser.getEnableTxResultDispatcher();
        logger.info("ChainMakerConnection build id: {}, org: {}, authType: {}, isEnableTxResult: {}, sign: [{}, {}], tls: [{}, {}]",
                chainMakerAccount.getIdentity(), orgId, authType, isEnableTxResult, signCrtHash, signKeyHash, tlsCrtHash, tlsKeyHash);
        addProperties(connection, stubConfigPath);
        connectionMap.put(chainMakerAccount.getIdentity(), connection);
        return connection;
    }
}
