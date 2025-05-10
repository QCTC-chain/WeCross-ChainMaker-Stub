package com.webank.wecross.stub.chainmaker.common;

public class ChainMakerStatusCode {
    public static final int Success = 0;
    public static final int HandleGetBlockNumberFailed = 2023;
    public static final int HandleGetBlockFailed = 2024;
    public static final int HandleInvokeWeCrossProxyFailed = 2025;

    public static final int MethodNotExist = 2026;
    public static final int HandleDeployContract = 3000;
    public static final int HandleGetContracts = 3001;
    public static final int HandleSubscribeEvent = 3002;
    public static final int ContractResultFailed = 3003;
    public static final int InnerError = 8000;

    public static String getStatusMessage(int status) {
        String message = "";
        switch (status) {
            case Success:
                message = "success";
                break;
            case HandleGetBlockNumberFailed:
                message = "GetBlockNumberFailed";
                break;
            case HandleGetBlockFailed:
                message = "GetBlockFailed";
                break;
            case HandleInvokeWeCrossProxyFailed:
                message = "InvokeWeCrossProxyFailed";
                break;
            case MethodNotExist:
                message = "MethodNotExist";
                break;
            case HandleDeployContract:
                message = "DeployContractFailed";
                break;
            case HandleGetContracts:
                message = "GetContractsFailed";
                break;
            case HandleSubscribeEvent:
                message = "SubscribeEventFailed";
                break;
            case InnerError:
                message = "Inner error";
                break;
            case ContractResultFailed:
                message = "ContractResultFailed";
                break;
            default:
                message = "unrecognized status: " + status;
                break;
        }
        return message;
    }
}
