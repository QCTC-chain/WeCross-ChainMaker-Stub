package com.webank.wecross.stub.chainmaker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stub.WeCrossContext;
import com.webank.wecross.stub.chainmaker.account.ChainMakerAccount;
import com.webank.wecross.stub.chainmaker.account.ChainMakerAccountFactory;

import com.webank.wecross.stub.chainmaker.common.ChainMakerConstant;
import com.webank.wecross.stub.chainmaker.config.AddChainStubConfig;
import com.webank.wecross.stub.chainmaker.custom.CommandHandlerDispatcher;
import com.webank.wecross.stub.chainmaker.custom.DeployContractHandler;
import com.webank.wecross.stub.chainmaker.custom.RegisterContractHandler;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.StringJoiner;


public class ChainMakerStubFactory implements StubFactory {
    private Logger logger = LoggerFactory.getLogger(ChainMakerStubFactory.class);

    private String stubConfigPath = "";
    private String stubType = "";

    public ChainMakerStubFactory(String subType) {
        this.stubType = subType;
    }

    public String getStubType() {
        return stubType;
    }

    @Override
    public void init(WeCrossContext context) {
        logger.info("init ChainMaker stub factory");
    }

    @Override
    public Driver newDriver() {
        logger.info("New Driver");

        if (stubConfigPath.isEmpty()) {
            logger.warn("stubConfigPath is empty.");
            return null;
        }

        CommandHandlerDispatcher commandHandlerDispatcher = new CommandHandlerDispatcher();
        ChainMakerDriver driver = new ChainMakerDriver();
        DeployContractHandler deployContractHandler = DeployContractHandler.build(stubConfigPath, "stub.toml");
        deployContractHandler.setDriver(driver);

        commandHandlerDispatcher.registerCommandHandler(
                ChainMakerConstant.CUSTOM_COMMAND_DEPLOY_CONTRACT,
                deployContractHandler);
        commandHandlerDispatcher.registerCommandHandler(
                ChainMakerConstant.CUSTOM_COMMAND_UPGRADE_CONTRACT,
                deployContractHandler);

        RegisterContractHandler registerContractHandler = new RegisterContractHandler();
        commandHandlerDispatcher.registerCommandHandler(
                ChainMakerConstant.CUSTOM_COMMAND_REGISTER_CONTRACT,
                registerContractHandler);

        driver.setCommandHandlerDispatcher(commandHandlerDispatcher);

        return driver;
    }

    @Override
    public Connection newConnection(String path) {
        try {
            logger.info("New connection: {}", path);
            stubConfigPath = path;
            ChainMakerConnection connection = ChainMakerConnectionFactory.build(stubConfigPath, "sdk_config.yml");
            connection.setConfigPath(stubConfigPath);

            // check proxy contract
            if(connection.hasProxyDeployed() == false) {
                String errorMsg = "WeCrossProxy error: WeCrossProxy contract has not been deployed!";
                throw new WeCrossException(WeCrossException.ErrorCode.INTER_CHAIN_ERROR, errorMsg);
            }

            // check hub contract
            if (!connection.hasHubDeployed()) {
                String errorMsg = "WeCrossHub error: WeCrossHub contract has not been deployed!";
                throw new WeCrossException(WeCrossException.ErrorCode.INTER_CHAIN_ERROR, errorMsg);
            }

            return connection;
        } catch (Exception ec) {
            logger.error("New connection, e: ", ec);
            return null;
        }
    }

    @Override
    public Account newAccount(Map<String, Object> properties) {
        try {
            String ext = (String) properties.get("ext0");
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> chainMakerProperties =
                    objectMapper.readValue(ext, new TypeReference<Map<String, Object>>(){});

            chainMakerProperties.put("userKey", properties.get("secKey"));
            chainMakerProperties.put("userCert", properties.get("pubKey"));
            chainMakerProperties.put("username", properties.get("username"));
            chainMakerProperties.put("type", properties.get("type"));
            chainMakerProperties.put("isDefault", properties.get("isDefault"));
            chainMakerProperties.put("keyID", properties.get("keyID"));

            assert(chainMakerProperties.get("type").equals(this.stubType));

            ChainMakerAccount account = ChainMakerAccountFactory.build(chainMakerProperties);
            logger.info("newAccount, id: {}, properties: {}",account.getIdentity(), chainMakerProperties);
            return account;
        } catch (JsonProcessingException e) {
            logger.warn("newAccount was failure. e: ", e);
            return null;
        }
    }

    @Override
    public void generateAccount(String path, String[] args) {
        logger.info("generateAccount, path: {}, args: {}", path, args);
    }

    private String[] getOrgIds(String path) {
        String[] orgIds = null;
        File sdkConfigPath = new File(path + File.separator + "crypto-config");
        if (sdkConfigPath.isDirectory()) {
            orgIds = sdkConfigPath.list();
        }
        return orgIds;
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
        File signPath = new File(path, "sign");
        return signPath.listFiles((dir, name) -> name.contains(".crt"))[0];
    }

    private File getSignKeyFilePath(File path) {
        File signPath = new File(path, "sign");
        return signPath.listFiles((dir, name) -> name.contains(".key"))[0];
    }

    private File getTLSCertFilePath(File path) {
        File tlsPath = new File(path, "tls");
        return tlsPath.listFiles((dir, name) -> name.contains(".crt"))[0];
    }

    private File getTLSKeyFilePath(File path) {
        File tlsPath = new File(path, "tls");
        return tlsPath.listFiles((dir, name) -> name.contains(".key"))[0];
    }

    private String generateStubTomlConfig(String path) {
        File basePath = new File(path);
        String chainName = basePath.getName();
        StringJoiner entries = new StringJoiner(",");
        String endorsement = "";
        String accountTemplate =
                "[common]\n"
                        + "    name = '"
                        + chainName
                        + "'\n"
                        + "    type = '"
                        + getStubType()
                        + "'\n"
                        + "[endorsement]\n"
                        + "    entries = [%s]\n"
                        + "\n"
                        + "%s\n"
                        + "\n";
        File sdkConfigPath = new File(
                basePath + File.separator + "certs");
        File[] orgIds = getOrgIdPaths(sdkConfigPath);
        if(orgIds == null) {
            logger.warn("orgIds are empty. {}", path);
            return String.format(accountTemplate, "", "");
        }

        for(File orgId: orgIds) {
            if(orgId.isHidden()) {
                continue;
            }

            entries.add(String.format("'%s'", orgId.getName()));

            File[] users = getUserPaths(orgId);
            if(users == null) {
                continue;
            }
            for(File user: users) {
                endorsement += "[[" + orgId.getName() + "]]\n";
                String userName = user.getName();
                File signCertFilePath = getSignCertFilePath(user);
                File signKeyFilePath = getSignKeyFilePath(user);
                File tlsCertFilePath = getTLSCertFilePath(user);
                File tlsKeyFilePath = getTLSKeyFilePath(user);
                endorsement +=
                        "    userName = '" + userName + "'\n"
                                + "    user_key_file_path = '" + tlsKeyFilePath.getPath().substring(basePath.getPath().length() + 1) + "'\n"
                                + "    user_crt_file_path = '" + tlsCertFilePath.getPath().substring(basePath.getPath().length() + 1) + "'\n"
                                + "    user_sign_key_file_path = '" + signKeyFilePath.getPath().substring(basePath.getPath().length() + 1) + "'\n"
                                + "    user_sign_crt_file_path = '" + signCertFilePath.getPath().substring(basePath.getPath().length() + 1) + "'\n"
                                + "\n";
            }
        }
        String stubContent = String.format(accountTemplate, entries.toString(), endorsement);
        return stubContent;
    }

    private void generateStubToml(String path) throws IOException {
        String stubContent = generateStubTomlConfig(path);
        String confFilePath = path + File.separator + "stub.toml";
        File confFile = new File(confFilePath);
        if (!confFile.createNewFile()) {
            logger.error("Conf file exists! {}", confFile);
            return;
        }

        FileWriter fileWriter = new FileWriter(confFile);
        try {
            fileWriter.write(stubContent);
        } finally {
            fileWriter.close();
        }
    }

    private void writeContent(File file, String content) throws IOException {
        if (!file.createNewFile()) {
            logger.error("Conf file exists! {}", file);
            return;
        }

        FileWriter fileWriter = new FileWriter(file);
        try {
            fileWriter.write(content);
        } finally {
            fileWriter.close();
        }
    }

    private void saveCertsAndKeys(String path, AddChainStubConfig stubConfig) throws IOException {
        for(AddChainStubConfig.Organization organization: stubConfig.organizations) {
            File orgIdPath = new File(path
                    + File.separator + "certs"
                    + File.separator + organization.orgId);
            if(!orgIdPath.exists()) {
                orgIdPath.mkdirs();
            }
            File caPath = new File(orgIdPath + File.separator + "ca");
            if(!caPath.exists()) {
                caPath.mkdirs();
            }
            File caCert = new File(caPath + File.separator + "ca.crt");
            writeContent(caCert, organization.signCert);
            File caKey = new File(caPath + File.separator + "ca.key");
            writeContent(caKey, organization.signKey);

            for(AddChainStubConfig.User user: organization.users) {
                File userPath = new File(orgIdPath
                        + File.separator + "user"
                        + File.separator + user.id);
                if (!userPath.exists()) {
                    userPath.mkdirs();
                }

                File userSignPath = new File(userPath + File.separator + "sign");
                if(!userSignPath.exists()) {
                    userSignPath.mkdirs();
                }
                File userSignCertFile = new File(userSignPath
                        + File.separator + String.format("%s.sign.crt", user.id));
                writeContent(userSignCertFile, user.signCert);
                File userSignKeyFile = new File(userSignPath
                        + File.separator + String.format("%s.sign.key", user.id));
                writeContent(userSignKeyFile, user.signKey);

                File userTLSPath = new File(userPath + File.separator + "tls");
                if(!userTLSPath.exists()) {
                    userTLSPath.mkdirs();
                }

                File userTLSCertFile = new File(userTLSPath
                        + File.separator + String.format("%s.tls.crt", user.id));
                writeContent(userTLSCertFile, user.tlsCert);
                File userTLSKeyFile = new File(userTLSPath
                        + File.separator + String.format("%s.tls.key", user.id));
                writeContent(userTLSKeyFile, user.tlsKey);
            }
        }
    }

    private void generate_sdk_config(String path, AddChainStubConfig stubConfig) throws IOException {
        String sdk_config_template = "chain_client:\n" +
                "  chain_id: %s\n" +
                "  org_id: %s\n" +
                "  user_key_file_path: %s\n" +
                "  user_crt_file_path: %s\n" +
                "  user_sign_key_file_path: %s\n" +
                "  user_sign_crt_file_path: %s\n" +
                "  retry_limit: 10\n" +
                "  retry_interval: 500\n" +
                "  nodes:\n" +
                "  - node_addr: %s\n" +
                "    conn_cnt: 10\n" +
                "    enable_tls: %s\n" +
                "    trust_root_paths:\n" +
                "    - %s\n" +
                "    tls_host_name: chainmaker.org\n" +
                "  archive:\n" +
                "    type: mysql\n" +
                "    dest: root::127.0.0.1:3306\n" +
                "    secret_key: xxx\n" +
                "  rpc_client:\n" +
                "    max_receive_message_size: 16\n" +
                "  pkcs11:\n" +
                "    enabled: false";

        String chainId = stubConfig.chainClient.chainId;
        String orgId = stubConfig.chainClient.orgId;
        String nodeAddress = stubConfig.chainClient.nodes.address;
        String enableTLS = stubConfig.chainClient.nodes.enableTLS ? "true" : "false";

        File orgIdPath = new File(path + File.separator + "certs" + File.separator + orgId);
        File[] users = getUserPaths(orgIdPath);
        if(users == null) {
            logger.error("{} is empty.", orgIdPath);
            return;
        }
        File signCertFilePath = getSignCertFilePath(users[0]);
        File signKeyFilePath = getSignKeyFilePath(users[0]);
        File tlsCertFilePath = getTLSCertFilePath(users[0]);
        File tlsKeyFilePath = getTLSKeyFilePath(users[0]);

        String config_sdk_content = String.format(
                sdk_config_template,
                chainId, orgId,
                tlsKeyFilePath.getPath().substring(path.length() + 1),
                tlsCertFilePath.getPath().substring(path.length() + 1),
                signKeyFilePath.getPath().substring(path.length() + 1),
                signCertFilePath.getPath().substring(path.length() + 1),
                nodeAddress,
                enableTLS,
                "certs" + File.separator + orgId + File.separator + "ca");

        String sdkConfigFilePath = path + File.separator + "sdk_config.yml";
        File confFile = new File(sdkConfigFilePath);
        if (!confFile.createNewFile()) {
            logger.error("Conf file exists! {}", confFile);
            return;
        }

        FileWriter fileWriter = new FileWriter(confFile);
        try {
            fileWriter.write(config_sdk_content);
        } finally {
            fileWriter.close();
        }
    }

    @Override
    public void generateConnection(String path, String[] args) {
        try {
            File basePath = new File(path);
            String chainName = basePath.getName();
            ObjectMapper objectMapper = new ObjectMapper();
            AddChainStubConfig stubConfig = objectMapper.readValue(args[2], AddChainStubConfig.class);
            // 保存证书和私钥
            saveCertsAndKeys(path, stubConfig);
            // 生成 stub.toml 文件
            generateStubToml(path);
            // 生成 sdk_config.yml 文件
            generate_sdk_config(path, stubConfig);

            // 生成系统合约
            generateProxyContract(path);
            generateHubContract(path);

            System.out.println(
                    "SUCCESS: Chain \""
                            + chainName
                            + "\" config framework has been generated to \""
                            + path
                            + "\"");
        } catch (Exception e) {
            logger.error("Exception: ", e);
        }
    }
    private void generateProxyContract(String path) {
        try {
            String proxySourcePath = "WeCrossProxy.sol";
            URL proxyDir = getClass().getResource(
                    File.separator + "contract/WeCrossProxy" + File.separator + proxySourcePath);
            File dest =
                    new File(path + File.separator + "contract/WeCrossProxy" + File.separator + proxySourcePath);
            FileUtils.copyURLToFile(proxyDir, dest);

            String proxyBinPath = "WeCrossProxy.bin";
            proxyDir = getClass().getResource(
                    File.separator + "contract/WeCrossProxy" + File.separator + proxyBinPath);
            dest =
                    new File(path + File.separator + "contract/WeCrossProxy" + File.separator + proxyBinPath);
            FileUtils.copyURLToFile(proxyDir, dest);

            String proxyABIPath = "WeCrossProxy.abi";
            proxyDir = getClass().getResource(
                    File.separator + "contract/WeCrossProxy" + File.separator + proxyABIPath);
            dest =
                    new File(path + File.separator + "contract/WeCrossProxy" + File.separator + proxyABIPath);
            FileUtils.copyURLToFile(proxyDir, dest);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void generateHubContract(String path) {
        try {
            String hubSourcePath = "WeCrossHub.sol";
            URL hubDir = getClass().getResource(File.separator + "contract/WeCrossHub" + File.separator + hubSourcePath);
            File dest = new File(path + File.separator + "contract/WeCrossHub" + File.separator + hubSourcePath);
            FileUtils.copyURLToFile(hubDir, dest);

            String hubBinPath = "WeCrossHub.bin";
            hubDir = getClass().getResource(File.separator + "contract/WeCrossHub" + File.separator + hubBinPath);
            dest = new File(path + File.separator + "contract/WeCrossHub" + File.separator + hubBinPath);
            FileUtils.copyURLToFile(hubDir, dest);

            String hubABIPath = "WeCrossHub.abi";
            hubDir = getClass().getResource(File.separator + "contract/WeCrossHub" + File.separator + hubABIPath);
            dest = new File(path + File.separator + "contract/WeCrossHub" + File.separator + hubABIPath);
            FileUtils.copyURLToFile(hubDir, dest);

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
