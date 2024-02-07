package com.webank.wecross.stub.chainmaker.common;

public class ChainMakerStatusCode {
    public static final int Success = 0;
    public static final int HandleGetBlockNumberFailed = 2023;
    public static final int HandleGetBlockFailed = 2024;
    public static final int HandleInvokeWeCrossProxyFailed = 2025;

    public static final int MethodNotExist = 2026;
    public static final int HandleDeployContract = 3000;
    public static final int HandleGetContracts = 3001;

    public static String getStatusMessage(int status) {
        String message = "";
        switch (status) {
            case Success:
                message = "success";
                break;
            case HandleGetBlockNumberFailed:
                break;
            default:
                message = "unrecognized status: " + status;
                break;
        }
        return message;
    }
}
