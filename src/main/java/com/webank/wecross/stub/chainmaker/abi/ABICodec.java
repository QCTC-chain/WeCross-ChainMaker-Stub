package com.webank.wecross.stub.chainmaker.abi;

import com.webank.wecross.stub.chainmaker.abi.wrapper.*;
import org.chainmaker.pb.common.ResultOuterClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ABICodec {

    private Logger logger = LoggerFactory.getLogger(ABICodec.class);
    private final ABIDefinitionFactory abiDefinitionFactory = new ABIDefinitionFactory();
    private final ABIObjectFactory abiObjectFactory = new ABIObjectFactory();

    public Map<String, Object> decodeEvent(String ABI, ResultOuterClass.ContractEventInfo event) {
        ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(ABI);
        ABIDefinition abiDefinition = contractABIDefinition.getABIDefinitionByEventTopic(
                event.getTopic().substring(0, 8));
        ABIObject inputObject = abiObjectFactory.createEventInputObject(abiDefinition);
        ABICodecObject abiCodecObject = new ABICodecObject();
        Map<String, Object> params = new HashMap<>();

        int indexedCount = 0;
        for(ABIDefinition.NamedType namedType: abiDefinition.getInputs()) {
            if (namedType.isIndexed()) {
                String dataEvent = event.getEventData(indexedCount++);
                params.put(namedType.getName(), dataEvent);
            }
        }

        String dataEvent = event.getEventData(indexedCount);
        if(!dataEvent.equals("0x")) {
            List<Object> decodeObject = abiCodecObject.decodeJavaObject(inputObject, dataEvent);
            int index = 0;
            for(ABIDefinition.NamedType namedType: abiDefinition.getInputs()) {
                if (!namedType.isIndexed()) {
                    Object object = decodeObject.get(index++);
                    params.put(namedType.getName(), object);
                }
            }
        }
        return params;
    }

    public List<Object> decodeEvent(String ABI, String topic, String event_data) {
        ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(ABI);
        ABIDefinition abiDefinition = contractABIDefinition.getABIDefinitionByEventTopic(topic.substring(0,8));
        ABIObject inputObject = abiObjectFactory.createEventInputObject(abiDefinition);
        ABICodecObject abiCodecObject = new ABICodecObject();
        List<Object> params = new ArrayList<>();
        List<Object> decodeObject = abiCodecObject.decodeJavaObject(inputObject, event_data);
        params.add(decodeObject);
        return params;
    }
}
