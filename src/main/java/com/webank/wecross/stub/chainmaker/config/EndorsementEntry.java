package com.webank.wecross.stub.chainmaker.config;

public class EndorsementEntry {
    private String orgId;
    private String userKeyFilePath;
    private String userCrtFilePath;
    private String userSignKeyFilePath;
    private String userSignCrtFilePath;

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getOrgId() {
        return this.orgId;
    }

    public void setUserKeyFilePath(String userKeyFilePath) {
        this.userKeyFilePath = userKeyFilePath;
    }

    public String getUserKeyFilePath() {
        return this.userKeyFilePath;
    }

    public void setUserCrtFilePath(String userCrtFilePath) {
        this.userCrtFilePath = userCrtFilePath;
    }

    public String getUserCrtFilePath() {
        return this.userCrtFilePath;
    }

    public void setUserSignKeyFilePath(String userSignKeyFilePath) {
        this.userSignKeyFilePath = userSignKeyFilePath;
    }

    public String getUserSignKeyFilePath() {
        return this.userSignKeyFilePath;
    }

    public void setUserSignCrtFilePath(String userSignCrtFilePath) {
        this.userSignCrtFilePath = userSignCrtFilePath;
    }

    public String getUserSignCrtFilePath() {
        return this.userSignCrtFilePath;
    }

    @Override
    public String toString() {
        return String.format("{\norgId: '%s',\nuserKeyFilePath: '%s',\nuserCrtFilePath: '%s',\nuserSignKeyFilePath: '%s',\nuserSignCrtFilePath: '%s'\n}",
                orgId, userKeyFilePath, userCrtFilePath, userSignKeyFilePath, userSignCrtFilePath);
    }
}
