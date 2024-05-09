package com.webank.wecross.stub.chainmaker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stub.WeCrossContext;
import com.webank.wecross.stub.chainmaker.account.ChainMakerAccountFactory;

import com.webank.wecross.stub.chainmaker.common.ChainMakerConstant;
import com.webank.wecross.stub.chainmaker.custom.CommandHandlerDispatcher;
import com.webank.wecross.stub.chainmaker.custom.DeployContractHandler;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.Map;


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
        ChainMakerDriver driver = new ChainMakerDriver();

        DeployContractHandler deployContractHandler = DeployContractHandler.build(stubConfigPath, "stub.toml");
        deployContractHandler.setDriver(driver);

        CommandHandlerDispatcher commandHandlerDispatcher = new CommandHandlerDispatcher();
        commandHandlerDispatcher.registerCommandHandler(
                ChainMakerConstant.CUSTOM_COMMAND_DEPLOY_CONTRACT,
                deployContractHandler);
        commandHandlerDispatcher.registerCommandHandler(
                ChainMakerConstant.CUSTOM_COMMAND_UPGRADE_CONTRACT,
                deployContractHandler);

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
                throw new Exception(errorMsg);
            }

            // check hub contract
            if (!connection.hasHubDeployed()) {
                String errorMsg = "WeCrossHub error: WeCrossHub contract has not been deployed!";
                throw new Exception(errorMsg);
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
            Map<String, String> chainMakerProperties =
                    objectMapper.readValue(ext, new TypeReference<Map<String, String>>(){});

            chainMakerProperties.put("userKey", (String) properties.get("secKey"));
            chainMakerProperties.put("userCert", (String) properties.get("pubKey"));
            chainMakerProperties.put("username", (String) properties.get("username"));
            chainMakerProperties.put("type", (String) properties.get("type"));

            assert(chainMakerProperties.get("type").equals(this.stubType));

            logger.info("newAccount, properties: {}", chainMakerProperties);

            return ChainMakerAccountFactory.build(chainMakerProperties);
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

    private String generateStubTomlConfig(String path) {
        File basePath = new File(path);
        String chainName = basePath.getName();
        String entries = "";
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
                basePath + File.separator + "crypto-config");
        File[] orgIds = getOrgIdPaths(sdkConfigPath);
        if(orgIds == null) {
            logger.warn("orgIds are empty. {}", path);
            return String.format(accountTemplate, "", "");
        }

        int lastIndex = 0;
        for(File orgId: orgIds) {
            if(orgId.isHidden()) {
                continue;
            }

            entries += "'" + orgId.getName() + "'";
            if(++lastIndex != orgIds.length) {
                entries += ",";
            }

            endorsement += "[[" + orgId.getName() + "]]\n";
            File[] users = getUserPaths(orgId);
            if(users == null) {
                continue;
            }
            for(File user: users) {
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
        String stubContent = String.format(accountTemplate, entries, endorsement);
        return stubContent;
    }

    @Override
    public void generateConnection(String path, String[] args) {
        try {
            File basePath = new File(path);
            String chainName = basePath.getName();
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
                    new File(path + File.separator + "WeCrossProxy" + File.separator + proxySourcePath);
            FileUtils.copyURLToFile(proxyDir, dest);

            String proxyBinPath = "WeCrossProxy.bin";
            proxyDir = getClass().getResource(
                    File.separator + "contract/WeCrossProxy" + File.separator + proxyBinPath);
            dest =
                    new File(path + File.separator + "WeCrossProxy" + File.separator + proxyBinPath);
            FileUtils.copyURLToFile(proxyDir, dest);

            String proxyABIPath = "WeCrossProxy.abi";
            proxyDir = getClass().getResource(
                    File.separator + "contract/WeCrossProxy" + File.separator + proxyABIPath);
            dest =
                    new File(path + File.separator + "WeCrossProxy" + File.separator + proxyABIPath);
            FileUtils.copyURLToFile(proxyDir, dest);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void generateHubContract(String path) {
        try {
            String hubSourcePath = "WeCrossHub.sol";
            URL hubDir = getClass().getResource(File.separator + "contract/WeCrossHub" + File.separator + hubSourcePath);
            File dest = new File(path + File.separator + "WeCrossHub" + File.separator + hubSourcePath);
            FileUtils.copyURLToFile(hubDir, dest);

            String hubBinPath = "WeCrossHub.bin";
            hubDir = getClass().getResource(File.separator + "contract/WeCrossHub" + File.separator + hubBinPath);
            dest = new File(path + File.separator + "WeCrossHub" + File.separator + hubBinPath);
            FileUtils.copyURLToFile(hubDir, dest);

            String hubABIPath = "WeCrossHub.abi";
            hubDir = getClass().getResource(File.separator + "contract/WeCrossHub" + File.separator + hubABIPath);
            dest = new File(path + File.separator + "WeCrossHub" + File.separator + hubABIPath);
            FileUtils.copyURLToFile(hubDir, dest);

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
