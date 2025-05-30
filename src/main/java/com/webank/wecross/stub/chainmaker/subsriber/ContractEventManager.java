package com.webank.wecross.stub.chainmaker.subsriber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.chainmaker.ChainMakerConnection;
import com.webank.wecross.stub.chainmaker.abi.ABICodec;
import com.webank.wecross.stub.chainmaker.client.ChainMakerClient;
import com.webank.wecross.stub.chainmaker.utils.ConfigUtils;
import io.grpc.stub.StreamObserver;
import org.chainmaker.pb.common.ContractOuterClass;
import org.chainmaker.pb.common.ResultOuterClass;
import org.chainmaker.sdk.ChainClient;
import org.chainmaker.sdk.ChainClientException;
import org.chainmaker.sdk.ChainManager;
import org.chainmaker.sdk.config.SdkConfig;
import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ContractEventManager {
    private Logger logger = LoggerFactory.getLogger(ContractEventManager.class);
    public class ContractEventSubscriber {
        private long from;
        private long to;
        private String contract;
        private String topic;
        ChainClient subscriberClient;

        public ContractEventSubscriber(long from, long to, String contract, String topic) {
            this.from = from;
            this.to = to;
            this.contract = contract;
            this.topic = topic;
            this.subscriberClient = null;
        }

        public void setSubscriberClient(ChainClient chainClient) {
            this.subscriberClient = chainClient;
        }

        public String getContract() {
            return this.contract;
        }

        public String getTopic() {
            return this.topic;
        }

        @Override
        public boolean equals(Object o) {
            // 1. 地址相等快速返回
            if (this == o) return true;

            // 2. 类型检查（严格类匹配）
            if (o == null || getClass() != o.getClass()) return false;

            // 3. 类型转换
            ContractEventSubscriber that = (ContractEventSubscriber) o;

            // 4. 分字段比较
            return from == that.from &&
                    to == that.to &&
                    Objects.equals(contract, that.contract) &&
                    Objects.equals(topic, that.topic);
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, to, contract, topic, subscriberClient);
        }

        @Override
        public String toString() {
            return "ContractEventSubscriber{" +
                    "from=" + String.format("%d", from) +
                    "to=" + String.format("%d", to) +
                    "contract='" + contract + "'" +
                    "topic='" + topic + "'" +
                    "}";
        }
    }

    private ChainMakerConnection connection;

    private Map<String, ContractEventSubscriber> subscribers = new HashMap<>();

    public ContractEventManager(ChainMakerConnection connection) {
        this.connection = connection;
    }

    public String addSubscriber(
            TransactionContext context,
            String topic,
            long from,
            long to) throws ChainClientException, ChainMakerCryptoSuiteException {
        String subscriberId = null;
        ContractEventSubscriber subscriber = new ContractEventSubscriber(
                from, to, context.getPath().getResource(), topic);
        subscriberId = getContractSubscriberId(subscriber);
        if (subscriberId != null) {
            return subscriberId;
        } else {
            subscriberId = UUID.randomUUID().toString();
        }

        subscriber.setSubscriberClient(listenContractEvent(context, from, to, topic));
        subscribers.put(subscriberId, subscriber);
        return subscriberId;
    }

    public void cancelSubscriber(String subscriberId) {
        ContractEventSubscriber subscriber = subscribers.get(subscriberId);
        if(subscriber != null) {
            subscriber.subscriberClient.stop();
            subscribers.remove(subscriberId);
        } else {
            logger.warn("订阅事件不存在。{}", subscriberId);
        }
    }
    private ChainClient listenContractEvent(
            TransactionContext context,
            long from,
            long to,
            String topic) throws ChainClientException, ChainMakerCryptoSuiteException {
        ChainClient chainClient = null;
        try {
            SdkConfig sdkConfig = ChainMakerClient.loadConfig(connection.getConfigPath(), "sdk_config.yml");
            chainClient = ChainManager.getInstance().createChainClientWithoutPool(sdkConfig);
        } catch (Exception e) {
            logger.error("创建订阅节点失败。{}", e.getMessage());
            return null;
        }

        ChainClient finalChainClient = chainClient;
        StreamObserver<ResultOuterClass.SubscribeResult> observer = new StreamObserver<ResultOuterClass.SubscribeResult>() {
            // refer to:
            // https://git.chainmaker.org.cn/chainmaker/sdk-java/-/blob/master/src/test/java/org/chainmaker/sdk/TestSubscribe.java
            @Override
            public void onNext(ResultOuterClass.SubscribeResult value) {
                try {
                    ResultOuterClass.ContractEventInfoList contract = ResultOuterClass
                            .ContractEventInfoList
                            .parseFrom(value.getData());
                    int count = contract.getContractEventsCount();
                    for (int i = 0; i < count; i++) {
                        ResultOuterClass.ContractEventInfo eventInfo = contract.getContractEvents(i);

                        logger.info("contract event: {}", eventInfo);

                        Map<String, Object> result = new HashMap<>();
                        result.put("block_height", eventInfo.getBlockHeight());
                        result.put("chain_id", eventInfo.getChainId());
                        result.put("tx_id", eventInfo.getTxId());
                        result.put("path", context.getPath().toString());
                        result.put("topic", eventInfo.getTopic());
                        result.put("contract_name", eventInfo.getContractName());
                        result.put("contract_version", eventInfo.getContractVersion());

                        String finalTopic = topic;
                        String finalContractName = eventInfo.getContractName();
                        ContractOuterClass.Contract contractInfo = getContractInfo(
                                finalChainClient,
                                eventInfo.getContractName());
                        if (contractInfo.getRuntimeType().name().equals("DOCKER_GO")) {
                            result.put("event_data", eventInfo.getEventDataList());
                        } else if (contractInfo.getRuntimeType().name().equals("EVM")) {
                            String abiContent = "";
                            try {
                                abiContent = ConfigUtils.getContractABI(
                                        connection.getConfigPath(),
                                        eventInfo.getContractName());
                            } catch (Exception e) {
                                logger.error("获取 ABI 失败。 {}/{}",
                                        connection.getConfigPath(), eventInfo.getContractName());
                            }
                            logger.info("获取 ABI 数据: {}", abiContent.length());

                            if (!abiContent.isEmpty()) {
                                ABICodec abiCodec = new ABICodec();
                                Map<String, Object> decodedData = abiCodec.decodeEvent(abiContent, eventInfo);
                                result.put("event_data", decodedData);
                            }

                            finalTopic = (String) context.getResourceInfo().getProperties().get(eventInfo.getTopic());
                            finalContractName = (String) context.getResourceInfo().getProperties().get(contractInfo.getName());

                            result.put("topic", finalTopic);
                            result.put("contract_name", finalContractName);
                        }
                        ObjectMapper objectMapper = new ObjectMapper();
                        context.getCallback().onSubscribe(
                                finalContractName,
                                finalTopic,
                                objectMapper.writeValueAsString(result));
                    }
                } catch (InvalidProtocolBufferException e) {
                    logger.error("处理订阅事件 {}:{} 失败。{}", context.getPath().getResource(), topic, e.getMessage());
                } catch (JsonProcessingException e) {
                    logger.error("处理订阅事件结果失败 {}:{}。{}", context.getPath().getResource(), topic, e.getMessage());
                }
            }

            @Override
            public void onError(Throwable t) {
                // 长安链JAVA-SDK订阅合约事件后，过一段时间订阅失效
                // https://git.chainmaker.org.cn/chainmaker/issue/-/issues/1072
                try {
                    Thread.sleep(100);
                    finalChainClient.subscribeContractEvent(
                            -1, -1, topic, context.getPath().getResource(), this);
                } catch (Exception e) {
                    logger.error("处理订阅事件 {}:{} 失败。{}", context.getPath().getResource(), topic, e.getMessage());
                }
            }

            @Override
            public void onCompleted() {

            }
        };
        ContractOuterClass.Contract contractInfo = getContractInfo(chainClient, context.getPath().getResource());
        if(contractInfo == null) {
            throw new ChainClientException(String.format("合约 %s 不存在", context.getPath().getResource()));
        }
        chainClient.subscribeContractEvent(from, to, topic, contractInfo.getName(), observer);
        return chainClient;
    }

    private String getContractSubscriberId(ContractEventSubscriber subscriber) {
        for(Map.Entry<String, ContractEventSubscriber> entry: subscribers.entrySet()) {
            if (subscriber.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private ContractOuterClass.Contract getContractInfo(ChainClient chainClient, String contractName) {
        ContractOuterClass.Contract contractInfo;
        try {
            contractInfo = chainClient.getContractInfo(contractName, ChainMakerConnection.RPC_CALL_TIMEOUT);
        } catch(ChainMakerCryptoSuiteException | ChainClientException e) {
            // 如果是 EVM 合约，可能还需要计算一下合约名称
            if(e.getMessage().contains("contract not exist")) {
                contractName = com.webank.wecross.stub.chainmaker.utils.CryptoUtils.generateAddress(contractName);
                try {
                    contractInfo = chainClient.getContractInfo(contractName, ChainMakerConnection.RPC_CALL_TIMEOUT);
                } catch (Exception exception) {
                    contractInfo = null;
                }
            } else {
                contractInfo = null;
            }
        }
        return contractInfo;
    }
}
