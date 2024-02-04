package com.webank.wecross.stub.chainmaker.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.stub.chainmaker.common.ChainMakerToml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Load and parser stub.toml configuration file for ChainMaker */
public class ChainMakerStubConfigParser extends AbstractConfigParser {

    private static final Logger logger = LoggerFactory.getLogger(ChainMakerStubConfigParser.class);

    private final String stubDir;

    public ChainMakerStubConfigParser(String configPath, String configName) {
        super(configPath + File.separator + configName);
        this.stubDir = configPath;
    }

    /**
     * parser configPath file and return ChainaMakerConfig object
     *
     * @return
     * @throws IOException
     */
    public List<EndorsementEntry> loadEndorsementEntry() throws IOException {
        List<EndorsementEntry> endorsementEntries = new ArrayList<>();

        ChainMakerToml chainMakerToml = new ChainMakerToml(getConfigPath());
        Toml toml = chainMakerToml.getToml();

        Map<String, Object> stubConfig = toml.toMap();
        Map<String, Object> endorsement = (Map<String, Object>)stubConfig.get("endorsement");
        if(endorsement == null) {
            return endorsementEntries;
        }

        List<String> entries = (List<String>) endorsement.get("entries");
        for(String orgId: entries) {
            Map<String, Object> paths = (Map<String, Object>)stubConfig.get(orgId);
            EndorsementEntry entry = new EndorsementEntry();
            entry.setOrgId(orgId);
            entry.setUserKeyFilePath((String)paths.get("user_key_file_path"));
            entry.setUserCrtFilePath((String)paths.get("user_crt_file_path"));
            entry.setUserSignKeyFilePath((String)paths.get("user_sign_key_file_path"));
            entry.setUserSignCrtFilePath((String)paths.get("user_sign_crt_file_path"));
            endorsementEntries.add(entry);
        }
        return endorsementEntries;
    }

    public String getStubType() throws IOException {
        Map<String, Object> commonConfig = getCommonConfig();
        return (String) commonConfig.get("type");
    }

    public String getStubName() throws IOException {
        Map<String, Object> commonConfig = getCommonConfig();
        return (String) commonConfig.get("name");
    }

    private Map<String, Object> getCommonConfig() throws IOException {
        ChainMakerToml chainMakerToml = new ChainMakerToml(getConfigPath());
        Toml toml = chainMakerToml.getToml();
        Map<String, Object> stubConfig = toml.toMap();
        return (Map<String, Object>)stubConfig.get("common");
    }
}
