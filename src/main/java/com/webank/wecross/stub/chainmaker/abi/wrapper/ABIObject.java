package com.webank.wecross.stub.chainmaker.abi.wrapper;

import com.webank.wecross.stub.chainmaker.abi.TypeDecoder;

import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.datatypes.generated.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ABIObject {

    private static final Logger logger = LoggerFactory.getLogger(ABIObject.class);

    public enum ObjectType {
        VALUE, // uint, int, bool, address, bytes<M>, bytes, string
        STRUCT, // tuple
        LIST // T[], T[M]
    }

    public enum ValueType {
        BOOL, // bool
        UINT, // uint<M>
        INT, // int<M>
        BYTES, // byteN
        ADDRESS, // address
        STRING, // string
        DBYTES, // bytes
        FIXED, // fixed<M>x<N>
        UFIXED, // ufixed<M>x<N>
    }

    public enum ListType {
        DYNAMIC, // T[]
        FIXED, // T[M]
    }

    private String name; // field name

    private ObjectType type; // for value
    private ValueType valueType;

    private NumericType numericValue;

    private DynamicBytes bytesValue;
    private int bytesLength;
    private Address addressValue;
    private Bool boolValue;
    private DynamicBytes dynamicBytesValue;
    private Utf8String stringValue;

    private ListType listType;
    private List<ABIObject> listValues; // for list
    private int listLength; // for list
    private ABIObject listValueType; // for list

    private List<ABIObject> structFields; // for struct

    public ABIObject(ObjectType type) {
        this.type = type;

        switch (type) {
            case VALUE:
            {
                break;
            }
            case STRUCT:
            {
                structFields = new LinkedList<ABIObject>();
                break;
            }
            case LIST:
            {
                listValues = new LinkedList<ABIObject>();
                break;
            }
        }
    }

    public ABIObject(ValueType valueType) {
        this.type = ObjectType.VALUE;
        this.valueType = valueType;
    }

    public ABIObject(ValueType bytesValueType, int bytesLength) {
        this(bytesValueType);
        this.bytesLength = bytesLength;
    }

    public ABIObject(ListType listType) {
        this.type = ObjectType.LIST;
        this.listType = listType;
        this.listValues = new LinkedList<ABIObject>();
    }

    public ABIObject(Uint256 uintValue) {
        this(ValueType.UINT);
        this.numericValue = uintValue;
    }

    public ABIObject(Int256 intValue) {
        this(ValueType.INT);
        this.numericValue = intValue;
    }

    public ABIObject(Address addressValue) {
        this(ValueType.ADDRESS);
        this.addressValue = addressValue;
    }

    public ABIObject(Bool boolValue) {
        this(ValueType.BOOL);
        this.boolValue = boolValue;
    }

    public ABIObject(Utf8String stringValue) {
        this(ValueType.STRING);
        this.stringValue = stringValue;
    }

    public ABIObject(DynamicBytes dynamicBytesValue) {
        this(ValueType.DBYTES);
        this.dynamicBytesValue = dynamicBytesValue;
    }

    public ABIObject(Bytes bytesValue) {
        this(ValueType.BYTES);
        this.bytesValue = new DynamicBytes(bytesValue.getValue());
    }

    public ABIObject(Bytes bytesValue, int bytesLength) {
        this(bytesValue);
        this.bytesLength = bytesLength;
    }

    public ABIObject newObjectWithoutValue() {
        ABIObject abiObject = new ABIObject(this.type);
        // value
        abiObject.setValueType(this.getValueType());
        abiObject.setName(this.getName());

        // list
        abiObject.setListType(this.getListType());
        abiObject.setListLength(this.getListLength());

        if (this.getListValueType() != null) {
            abiObject.setListValueType(this.getListValueType().newObjectWithoutValue());
        }

        if (this.listValues != null) {
            for (ABIObject obj : this.listValues) {
                abiObject.listValues.add(obj.newObjectWithoutValue());
            }
        }

        // tuple
        if (this.structFields != null) {
            for (ABIObject obj : this.structFields) {
                abiObject.structFields.add(obj.newObjectWithoutValue());
            }
        }

        return abiObject;
    }

    // clone itself
    public ABIObject newObject() {

        ABIObject abiObject = new ABIObject(this.type);
        abiObject.setBytesLength(this.bytesLength);

        // value
        abiObject.setValueType(this.getValueType());
        abiObject.setName(this.getName());

        if (this.getNumericValue() != null) {
            abiObject.setNumericValue(
                    new NumericType(
                            this.getNumericValue().getTypeAsString(),
                            this.getNumericValue().getValue()) {
                        @Override
                        public int getBitSize() {
                            return this.getValue().bitLength();
                        }
                    });
        }

        if (this.getBoolValue() != null) {
            abiObject.setBoolValue(new Bool(this.getBoolValue().getValue()));
        }

        if (this.getStringValue() != null) {
            abiObject.setStringValue(new Utf8String(this.getStringValue().getValue()));
        }

        if (this.getDynamicBytesValue() != null) {
            abiObject.setDynamicBytesValue(
                    new DynamicBytes(this.getDynamicBytesValue().getValue()));
        }

        if (this.getAddressValue() != null) {
            abiObject.setAddressValue(new Address(this.getAddressValue().toUint()));
        }

        if (this.getBytesValue() != null) {
            abiObject.setBytesValue(
                    new DynamicBytes(this.getBytesValue().getValue()));
        }

        // list
        abiObject.setListType(this.getListType());
        abiObject.setListLength(this.getListLength());

        if (this.getListValueType() != null) {
            abiObject.setListValueType(this.getListValueType().newObject());
        }

        if (this.listValues != null) {
            for (ABIObject obj : this.listValues) {
                abiObject.listValues.add(obj.newObject());
            }
        }

        // tuple
        if (this.structFields != null) {
            for (ABIObject obj : this.structFields) {
                abiObject.structFields.add(obj.newObject());
            }
        }

        return abiObject;
    }

    /**
     * Checks to see if the current type is dynamic
     *
     * @return true/false
     */
    public boolean isDynamic() {
        switch (type) {
            case VALUE:
            {
                switch (valueType) {
                    case DBYTES: // bytes
                    case STRING: // string
                        return true;
                    default:
                        return false;
                }
                // break;
            }
            case LIST:
            {
                switch (listType) {
                    case FIXED: // T[M]
                    {
                        return listValueType.isDynamic();
                    }
                    case DYNAMIC: // T[]
                    {
                        return true;
                    }
                }
                break;
            }
            case STRUCT:
            {
                for (ABIObject abiObject : structFields) {
                    if (abiObject.isDynamic()) {
                        return true;
                    }
                }
                return false;
            }
        }

        return false;
    }

    /**
     * dynamic offset of this object
     *
     * @return the offset of the ABIObject
     */
    public int offset() {
        if (isDynamic()) { // dynamic
            return 1;
        }

        int offset = 0;
        if (type == ObjectType.VALUE) { // basic type
            offset = 1;
        } else if (type == ObjectType.STRUCT) { // tuple
            int l = 0;
            for (ABIObject abiObject : structFields) {
                l += abiObject.offset();
            }
            offset = l;
        } else { // T[M]
            int length = listLength;
            int basicOffset = listValueType.offset();
            offset = length * basicOffset;
        }

        return offset;
    }

    public int offsetAsByteLength() {
        return offset() * Type.MAX_BYTE_LENGTH;
    }

    public int offsetAsHexLength() {
        return offset() * (Type.MAX_BYTE_LENGTH << 1);
    }

    /**
     * encode this object
     *
     * @return the encoded object
     */
    public String encode() {

        StringBuffer stringBuffer = new StringBuffer();
        switch (type) {
            case VALUE:
            {
                switch (valueType) {
                    case UINT:
                    case INT:
                    {
                        stringBuffer.append(TypeEncoder.encode(numericValue));
                        break;
                    }
                    case BOOL:
                    {
                        stringBuffer.append(TypeEncoder.encode(boolValue));
                        break;
                    }
                    case FIXED:
                    case UFIXED:
                    {
                        throw new UnsupportedOperationException(
                                " Unsupported fixed/unfixed type. ");
                        // break;
                    }
                    case BYTES:
                    {
                        stringBuffer.append(TypeEncoder.encode(bytesValue));
                        break;
                    }
                    case ADDRESS:
                    {
                        stringBuffer.append(TypeEncoder.encode(addressValue));
                        break;
                    }
                    case DBYTES:
                    {
                        stringBuffer.append(TypeEncoder.encode(dynamicBytesValue));
                        break;
                    }
                    case STRING:
                    {
                        stringBuffer.append(TypeEncoder.encode(stringValue));
                        break;
                    }
                    default:
                    {
                        throw new UnsupportedOperationException(
                                " Unrecognized valueType: " + valueType);
                    }
                }
                break;
            }
            case STRUCT:
            {
                long dynamicOffset = 0;
                for (ABIObject abiObject : structFields) {
                    dynamicOffset += abiObject.offsetAsByteLength();
                }

                StringBuffer fixedBuffer = new StringBuffer();
                StringBuffer dynamicBuffer = new StringBuffer();

                for (ABIObject abiObject : structFields) {
                    String encodeValue = abiObject.encode();
                    if (abiObject.isDynamic()) {
                        fixedBuffer.append(TypeEncoder.encode(new Uint256(dynamicOffset)));
                        dynamicBuffer.append(encodeValue);
                        dynamicOffset += (encodeValue.length() >> 1);
                    } else {
                        fixedBuffer.append(encodeValue);
                    }
                }

                stringBuffer.append(fixedBuffer).append(dynamicBuffer);
                break;
            }
            case LIST:
            {
                StringBuffer lengthBuffer = new StringBuffer();
                StringBuffer listValueBuffer = new StringBuffer();
                StringBuffer offsetBuffer = new StringBuffer();

                if (listType == ListType.DYNAMIC) {
                    lengthBuffer.append(TypeEncoder.encode(new Uint256(listValues.size())));
                }

                int dynamicOffset = listValues.size() * Type.MAX_BYTE_LENGTH;

                for (ABIObject abiObject : listValues) {
                    String listValueEncode = abiObject.encode();
                    listValueBuffer.append(abiObject.encode());
                    if (abiObject.isDynamic()) {
                        offsetBuffer.append(TypeEncoder.encode(new Uint256(dynamicOffset)));
                        dynamicOffset += (listValueEncode.length() >> 1);
                    }
                }

                stringBuffer.append(lengthBuffer).append(offsetBuffer).append(listValueBuffer);
                break;
            }
        }

        if (logger.isTraceEnabled()) {
            logger.trace("ABI: {}", stringBuffer.toString());
        }

        return stringBuffer.toString();
    }

    /**
     * decode this object
     *
     * @param input the string to be decoded into ABIObject
     * @return the decoded ABIObject
     */
    public ABIObject decode(String input) {
        return decode(input, 0);
    }

    /**
     * decode this object
     *
     * @return the decoded ABIObject
     */
    private ABIObject decode(String input, int offset) {

        ABIObject abiObject = newObject();

        switch (type) {
            case VALUE:
            {
                switch (valueType) {
                    case BOOL:
                    {
                        abiObject.setBoolValue(
                                TypeDecoder.decode(input, offset, Bool.class));
                        break;
                    }
                    case UINT:
                    {
                        abiObject.setNumericValue(
                                TypeDecoder.decode(input, offset, Uint256.class));
                        break;
                    }
                    case INT:
                    {
                        abiObject.setNumericValue(
                                TypeDecoder.decode(input, offset, Int256.class));
                        break;
                    }
                    case FIXED:
                    case UFIXED:
                    {
                        throw new UnsupportedOperationException(
                                " Unsupported fixed/unfixed type. ");
                        // break;
                    }
                    case BYTES:
                    {
                        DynamicBytes dynamicBytes = decodeBytes(abiObject, input, offset);
                        abiObject.setBytesValue(dynamicBytes);
                        break;
                    }
                    case ADDRESS:
                    {
                        abiObject.setAddressValue(
                                TypeDecoder.decode(input, offset, Address.class));
                        break;
                    }
                    case DBYTES:
                    {
                        abiObject.setDynamicBytesValue(
                                TypeDecoder.decode(input, offset, DynamicBytes.class));
                        break;
                    }
                    case STRING:
                    {
                        abiObject.setStringValue(
                                TypeDecoder.decode(input, offset, Utf8String.class));
                        break;
                    }
                }
                break;
            }
            case STRUCT:
            {
                int structOffset = offset;
                int initialOffset = offset;

                for (int i = 0; i < structFields.size(); ++i) {
                    ABIObject structObject = abiObject.structFields.get(i);
                    ABIObject itemObject = null;
                    if (structObject.isDynamic()) {
                        int structValueOffset =
                                TypeDecoder.decode(input, structOffset, Uint256.class)
                                        .getValue()
                                        .intValue();
                        itemObject =
                                structObject.decode(
                                        input, initialOffset + (structValueOffset << 1));

                    } else {
                        itemObject = structObject.decode(input, structOffset);
                    }

                    abiObject.structFields.set(i, itemObject);
                    structOffset += structObject.offsetAsHexLength();
                }
                break;
            }
            case LIST:
            {
                int listOffset = offset;
                int initialOffset = offset;

                int listLength = 0;
                if (listType == ListType.DYNAMIC) {
                    // dynamic list length
                    listLength =
                            TypeDecoder.decode(input, listOffset, Uint256.class)
                                    .getValue()
                                    .intValue();
                    listOffset += (Type.MAX_BYTE_LENGTH << 1);
                    initialOffset += (Type.MAX_BYTE_LENGTH << 1);
                } else {
                    // fixed list length
                    listLength = abiObject.getListLength();
                }

                if (logger.isTraceEnabled()) {
                    logger.trace(" listType: {}, listLength: {}", listType, listLength);
                }

                ABIObject listValueObject = abiObject.getListValueType();

                for (int i = 0; i < listLength; i++) {
                    ABIObject itemABIObject = null;

                    if (listValueObject.isDynamic()) {
                        int listValueOffset =
                                TypeDecoder.decode(input, listOffset, Uint256.class)
                                        .getValue()
                                        .intValue();
                        itemABIObject =
                                abiObject
                                        .getListValueType()
                                        .decode(input, initialOffset + (listValueOffset << 1));
                    } else {
                        itemABIObject = abiObject.getListValueType().decode(input, listOffset);
                    }

                    listOffset += listValueObject.offsetAsHexLength();

                    abiObject.getListValues().add(itemABIObject);
                }
                break;
            }
        }

        return abiObject;
    }

    private DynamicBytes decodeBytes(ABIObject abiObject, String input, int offset) {
        DynamicBytes dynamicBytes = null;
        if (abiObject.getBytesLength() == 32) {
            Bytes32 bytes32 = TypeDecoder.decode(input, offset, Bytes32.class);
            dynamicBytes = new DynamicBytes(bytes32.getValue());
        } else if (abiObject.getBytesLength() == 31) {
            Bytes31 bytes31 = TypeDecoder.decode(input, offset, Bytes31.class);
            dynamicBytes = new DynamicBytes(bytes31.getValue());
        } else if (abiObject.getBytesLength() == 30) {
            Bytes30 bytes30 = TypeDecoder.decode(input, offset, Bytes30.class);
            dynamicBytes = new DynamicBytes(bytes30.getValue());
        } else if (abiObject.getBytesLength() == 29) {
            Bytes29 bytes29 = TypeDecoder.decode(input, offset, Bytes29.class);
            dynamicBytes = new DynamicBytes(bytes29.getValue());
        } else if (abiObject.getBytesLength() == 28) {
            Bytes28 bytes = TypeDecoder.decode(input, offset, Bytes28.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else if (abiObject.getBytesLength() == 27) {
            Bytes27 bytes = TypeDecoder.decode(input, offset, Bytes27.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else if (abiObject.getBytesLength() == 26) {
            Bytes26 bytes = TypeDecoder.decode(input, offset, Bytes26.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else if (abiObject.getBytesLength() == 25) {
            Bytes25 bytes = TypeDecoder.decode(input, offset, Bytes25.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else if (abiObject.getBytesLength() == 24) {
            Bytes24 bytes = TypeDecoder.decode(input, offset, Bytes24.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else if (abiObject.getBytesLength() == 23) {
            Bytes23 bytes = TypeDecoder.decode(input, offset, Bytes23.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else if (abiObject.getBytesLength() == 22) {
            Bytes22 bytes = TypeDecoder.decode(input, offset, Bytes22.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else if (abiObject.getBytesLength() == 21) {
            Bytes21 bytes = TypeDecoder.decode(input, offset, Bytes21.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else if (abiObject.getBytesLength() == 20) {
            Bytes20 bytes = TypeDecoder.decode(input, offset, Bytes20.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else if (abiObject.getBytesLength() == 19) {
            Bytes19 bytes = TypeDecoder.decode(input, offset, Bytes19.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else if (abiObject.getBytesLength() == 18) {
            Bytes18 bytes = TypeDecoder.decode(input, offset, Bytes18.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else if (abiObject.getBytesLength() == 17) {
            Bytes17 bytes = TypeDecoder.decode(input, offset, Bytes17.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else if (abiObject.getBytesLength() == 16) {
            Bytes16 bytes = TypeDecoder.decode(input, offset, Bytes16.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else if (abiObject.getBytesLength() == 15) {
            Bytes15 bytes = TypeDecoder.decode(input, offset, Bytes15.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else if (abiObject.getBytesLength() == 14) {
            Bytes14 bytes = TypeDecoder.decode(input, offset, Bytes14.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else if (abiObject.getBytesLength() == 13) {
            Bytes13 bytes = TypeDecoder.decode(input, offset, Bytes13.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else if (abiObject.getBytesLength() == 12) {
            Bytes12 bytes = TypeDecoder.decode(input, offset, Bytes12.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else if (abiObject.getBytesLength() == 11) {
            Bytes11 bytes = TypeDecoder.decode(input, offset, Bytes11.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else if (abiObject.getBytesLength() == 10) {
            Bytes10 bytes = TypeDecoder.decode(input, offset, Bytes10.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else if (abiObject.getBytesLength() == 9) {
            Bytes9 bytes = TypeDecoder.decode(input, offset, Bytes9.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else if (abiObject.getBytesLength() == 8) {
            Bytes8 bytes = TypeDecoder.decode(input, offset, Bytes8.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else if (abiObject.getBytesLength() == 7) {
            Bytes7 bytes = TypeDecoder.decode(input, offset, Bytes7.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else if (abiObject.getBytesLength() == 6) {
            Bytes6 bytes = TypeDecoder.decode(input, offset, Bytes6.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else if (abiObject.getBytesLength() == 5) {
            Bytes5 bytes = TypeDecoder.decode(input, offset, Bytes5.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else if (abiObject.getBytesLength() == 4) {
            Bytes4 bytes = TypeDecoder.decode(input, offset, Bytes4.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else if (abiObject.getBytesLength() == 3) {
            Bytes3 bytes = TypeDecoder.decode(input, offset, Bytes3.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else if (abiObject.getBytesLength() == 2) {
            Bytes2 bytes = TypeDecoder.decode(input, offset, Bytes2.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else if (abiObject.getBytesLength() == 1) {
            Bytes1 bytes = TypeDecoder.decode(input, offset, Bytes1.class);
            dynamicBytes = new DynamicBytes(bytes.getValue());
        } else {
            dynamicBytes = TypeDecoder.decode(input, offset, DynamicBytes.class);
        }
        return dynamicBytes;
    }

    public ObjectType getType() {
        return type;
    }

    public void setType(ObjectType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public NumericType getNumericValue() {
        return numericValue;
    }

    public Bool getBoolValue() {
        return boolValue;
    }

    public void setBoolValue(Bool boolValue) {
        this.type = ObjectType.VALUE;
        this.valueType = ValueType.BOOL;
        this.boolValue = boolValue;
    }

    public void setNumericValue(NumericType numericValue) {
        this.type = ObjectType.VALUE;
        if (numericValue.getTypeAsString().startsWith("int") || numericValue instanceof Int256) {
            this.valueType = ValueType.INT;
        } else {
            this.valueType = ValueType.UINT;
        }
        this.numericValue = numericValue;
    }

    public DynamicBytes getBytesValue() {
        return bytesValue;
    }

    public void setBytesValue(DynamicBytes bytesValue) {
        this.type = ObjectType.VALUE;
        this.valueType = ValueType.BYTES;
        this.bytesValue = bytesValue;
    }

    public Address getAddressValue() {
        return addressValue;
    }

    public void setAddressValue(Address addressValue) {
        this.type = ObjectType.VALUE;
        this.valueType = ValueType.ADDRESS;
        this.addressValue = addressValue;
    }

    public List<ABIObject> getStructFields() {
        return structFields;
    }

    public void setStructFields(List<ABIObject> structFields) {
        this.type = ObjectType.STRUCT;
        this.structFields = structFields;
    }

    public ListType getListType() {
        return listType;
    }

    public void setListType(ListType listType) {
        this.listType = listType;
    }

    public List<ABIObject> getListValues() {
        return listValues;
    }

    public void setListValues(List<ABIObject> listValues) {
        this.type = ObjectType.LIST;
        this.listValues = listValues;
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    public DynamicBytes getDynamicBytesValue() {
        return dynamicBytesValue;
    }

    public void setDynamicBytesValue(DynamicBytes dynamicBytesValue) {
        this.dynamicBytesValue = dynamicBytesValue;
    }

    public Utf8String getStringValue() {
        return stringValue;
    }

    public void setStringValue(Utf8String stringValue) {
        this.stringValue = stringValue;
    }

    public ABIObject getListValueType() {
        return listValueType;
    }

    public void setListValueType(ABIObject listValueType) {
        this.listValueType = listValueType;
    }

    public int getListLength() {
        return listLength;
    }

    public void setListLength(int listLength) {
        this.listLength = listLength;
    }

    public int getBytesLength() {
        return bytesLength;
    }

    public void setBytesLength(int bytesLength) {
        this.bytesLength = bytesLength;
    }

    @Override
    public String toString() {

        String str = "ABIObject{" + "name='" + name + '\'' + ", type=" + type;

        if (type == ObjectType.VALUE) {
            str += ", valueType=" + valueType;
            switch (valueType) {
                case BOOL:
                    str += ", booValueType=";
                    str += Objects.isNull(boolValue) ? "null" : boolValue.getValue();
                    break;
                case UINT:
                case INT:
                    str += ", numericValue=";
                    str += Objects.isNull(numericValue) ? "null" : numericValue.getValue();
                    break;
                case ADDRESS:
                    str += ", addressValue=";
                    str += Objects.isNull(addressValue) ? "null" : addressValue.getValue();
                    break;
                case BYTES:
                    str += ", bytesValue=";
                    str += Objects.isNull(bytesValue) ? "null" : bytesValue.getValue();
                    break;
                case DBYTES:
                    str += ", dynamicBytesValue=";
                    str +=
                            Objects.isNull(dynamicBytesValue)
                                    ? "null"
                                    : dynamicBytesValue.getValue();
                    // case STRING:
                default:
                    str += ", stringValue=";
                    str += Objects.isNull(stringValue) ? "null" : stringValue.getValue();
            }
        } else if (type == ObjectType.LIST) {
            str += ", listType=" + listType;
            str += ", listValues=" + listValues + ", listLength=" + listLength;
        } else if (type == ObjectType.STRUCT) {
            str += ", structFields=" + structFields;
        }

        str += '}';
        return str;
    }
}
