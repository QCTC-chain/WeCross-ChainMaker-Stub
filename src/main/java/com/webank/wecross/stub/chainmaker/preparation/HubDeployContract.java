package com.webank.wecross.stub.chainmaker.preparation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HubDeployContract {
    private static final Logger logger = LoggerFactory.getLogger(HubDeployContract.class);

    public static String getUsage(String chainPath) {
        String pureChainPath = chainPath.replace("classpath:/", "").replace("classpath:", "");
        return "Usage:\n"
                + "         java -cp 'conf/:lib/*:plugin/*' "
                + HubDeployContract.class.getName()
                + " deploy [chainName] [accountName(optional)]\n"
                + "         java -cp 'conf/:lib/*:plugin/*' "
                + HubDeployContract.class.getName()
                + " upgrade [chainName] [accountName(optional)]\n"
                + "Example:\n"
                + "         java -cp 'conf/:lib/*:plugin/*' "
                + HubDeployContract.class.getName()
                + " deploy "
                + pureChainPath
                + " \n"
                + "         java -cp 'conf/:lib/*:plugin/*' "
                + HubDeployContract.class.getName()
                + " deploy "
                + pureChainPath
                + " admin\n"
                + "         java -cp 'conf/:lib/*:plugin/*' "
                + HubDeployContract.class.getName()
                + " upgrade "
                + pureChainPath
                + " \n"
                + "         java -cp 'conf/:lib/*:plugin/*' "
                + HubDeployContract.class.getName()
                + " upgrade "
                + pureChainPath
                + " admin";
    }

    public static void main(String[] args) {
        try {
            switch (args.length) {
                case 2:
                    handle2Args(args);
                    break;
                default:
                    usage();
            }
        } catch (Exception e) {
            System.out.println("Failed, please check account or contract. " + e);
            logger.warn("Error: ", e);
        } finally {
            exit();
        }
    }

    private static void usage() {
        System.out.println(getUsage("chains/chaimaker"));
        exit();
    }

    private static void exit() {
        System.exit(0);
    }

    private static void handle2Args(String[] args) {
        if (args.length != 2) {
            usage();
        }

        String cmd = args[0];
        String chainPath = args[1];

        switch (cmd) {
            case "deploy":
                deploy(chainPath);
                break;
            case "upgrade":
                upgrade(chainPath);
                break;
            default:
                usage();
        }
    }

    private static void deploy(String chainPath) {
        try {
            HubContract hubContract = new HubContract(chainPath);
            hubContract.deploy();
        } catch (Exception e) {
            logger.error("deploy, e: ", e);
            System.out.println("Failed, please check contract or account. Exception details:");
            e.printStackTrace();
        }
    }

    private static void upgrade(String chainPath) {
        try {
            HubContract hubContract = new HubContract(chainPath);
            hubContract.upgrade();
        } catch (Exception e) {
            logger.error("upgrade, e: ", e);
            System.out.println("Failed, please check contract or account. Exception details:");
            e.printStackTrace();
        }
    }
}
