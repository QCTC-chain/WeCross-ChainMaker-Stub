package com.webank.wecross.stub.chainmaker.config;

import java.util.List;

public class AddChainStubConfig {
    public ChainClient chainClient;
    public List<Organization> organizations;

    public static class ChainClient {
        public String chainId;
        public String orgId;
        public Node nodes;
    }
    public static class Node {
        public String address;
        public boolean enableTLS;
    }

    public static class User {
        public String id;
        public String name;
        public String signCert;
        public String signKey;
        public String tlsCert;
        public String tlsKey;
    }

    public static class Organization {
        public String orgId;
        public String orgName;
        public String signCert;
        public String signKey;
        public List<User> users;
    }
}
