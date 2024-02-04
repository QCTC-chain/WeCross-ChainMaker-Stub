package com.webank.wecross.stub.chainmaker.preparation;

import com.webank.wecross.stub.chainmaker.ChainMakerConnection;
import com.webank.wecross.stub.chainmaker.utils.ConfigUtils;
import org.chainmaker.pb.common.ContractOuterClass;
import org.chainmaker.pb.common.Request;
import org.chainmaker.pb.common.ResultOuterClass;
import org.chainmaker.sdk.User;
import org.chainmaker.sdk.utils.FileUtils;
import org.chainmaker.sdk.utils.SdkUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DeployWeCrossContract {
    private final String WECROSS_PROXY = "WeCrossProxy";
    private final String WECROSS_HUB = "WeCrossHub";

    private ChainMakerConnection chainMakerConnection;
    private String chainPath;
    private List<User> endorsementUsers = new ArrayList<>();

    public DeployWeCrossContract(
            ChainMakerConnection chainMakerConnection,
            List<User> endorsementUsers,
            String chainPath) {
        this.chainMakerConnection = chainMakerConnection;
        this.endorsementUsers = endorsementUsers;
        this.chainPath = chainPath;
    }

    private void deployOrUpgradeProxyContract(boolean deploy, String contractName) throws Exception {
        String contractBinFile = this.chainPath
                + File.separator
                + contractName
                + File.separator
                + contractName + ".bin";

        contractBinFile = ConfigUtils.classpath2Absolute(contractBinFile);
        String version = String.valueOf(System.currentTimeMillis() / 1000);
        byte[] byteCode = FileUtils.getFileBytes(contractBinFile);
        Request.Payload payload = null;
        if(deploy) {
            payload = chainMakerConnection.getChainClient().createContractCreatePayload(
                    contractName,
                    version,
                    byteCode, ContractOuterClass.RuntimeType.EVM, null);
        } else {
            payload = chainMakerConnection.getChainClient().createContractUpgradePayload(
                    contractName,
                    version,
                    byteCode, ContractOuterClass.RuntimeType.EVM, null);
        }

        Request.EndorsementEntry[] endorsementEntries = SdkUtils.getEndorsers(
                payload,
                endorsementUsers.stream().toArray(User[]::new));

        ResultOuterClass.TxResponse response = chainMakerConnection.getChainClient().sendContractManageRequest(
                payload, endorsementEntries, 10000, 10000);
        if(response.getCode() == ResultOuterClass.TxStatusCode.SUCCESS) {
            System.out.println(
                    "SUCCESS: " + contractName + ": " + version + " has been " + (deploy ? "deployed!" : "upgraded") + " chain: " + chainPath);
        } else {
            throw new RuntimeException((deploy ? " deploy" : " upgrade") + " contract failed, error code: " + response.getCode());
        }
    }

    public void deployWeCrossProxy(boolean deploy) throws Exception {
        deployOrUpgradeProxyContract(deploy, WECROSS_PROXY);
    }

    public void deployWeCrossHub(boolean deploy) throws Exception {
        deployOrUpgradeProxyContract(deploy, WECROSS_HUB);
    }
}
