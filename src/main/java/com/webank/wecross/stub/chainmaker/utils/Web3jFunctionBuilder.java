package com.webank.wecross.stub.chainmaker.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Int;
import org.web3j.abi.datatypes.StaticStruct;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes1;
import org.web3j.abi.datatypes.generated.Bytes10;
import org.web3j.abi.datatypes.generated.Bytes11;
import org.web3j.abi.datatypes.generated.Bytes12;
import org.web3j.abi.datatypes.generated.Bytes13;
import org.web3j.abi.datatypes.generated.Bytes14;
import org.web3j.abi.datatypes.generated.Bytes15;
import org.web3j.abi.datatypes.generated.Bytes16;
import org.web3j.abi.datatypes.generated.Bytes17;
import org.web3j.abi.datatypes.generated.Bytes18;
import org.web3j.abi.datatypes.generated.Bytes19;
import org.web3j.abi.datatypes.generated.Bytes2;
import org.web3j.abi.datatypes.generated.Bytes20;
import org.web3j.abi.datatypes.generated.Bytes21;
import org.web3j.abi.datatypes.generated.Bytes22;
import org.web3j.abi.datatypes.generated.Bytes23;
import org.web3j.abi.datatypes.generated.Bytes24;
import org.web3j.abi.datatypes.generated.Bytes25;
import org.web3j.abi.datatypes.generated.Bytes26;
import org.web3j.abi.datatypes.generated.Bytes27;
import org.web3j.abi.datatypes.generated.Bytes28;
import org.web3j.abi.datatypes.generated.Bytes29;
import org.web3j.abi.datatypes.generated.Bytes3;
import org.web3j.abi.datatypes.generated.Bytes30;
import org.web3j.abi.datatypes.generated.Bytes31;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Bytes4;
import org.web3j.abi.datatypes.generated.Bytes5;
import org.web3j.abi.datatypes.generated.Bytes6;
import org.web3j.abi.datatypes.generated.Bytes7;
import org.web3j.abi.datatypes.generated.Bytes8;
import org.web3j.abi.datatypes.generated.Bytes9;
import org.web3j.abi.datatypes.generated.Int104;
import org.web3j.abi.datatypes.generated.Int112;
import org.web3j.abi.datatypes.generated.Int120;
import org.web3j.abi.datatypes.generated.Int128;
import org.web3j.abi.datatypes.generated.Int136;
import org.web3j.abi.datatypes.generated.Int144;
import org.web3j.abi.datatypes.generated.Int152;
import org.web3j.abi.datatypes.generated.Int16;
import org.web3j.abi.datatypes.generated.Int160;
import org.web3j.abi.datatypes.generated.Int168;
import org.web3j.abi.datatypes.generated.Int176;
import org.web3j.abi.datatypes.generated.Int184;
import org.web3j.abi.datatypes.generated.Int192;
import org.web3j.abi.datatypes.generated.Int200;
import org.web3j.abi.datatypes.generated.Int208;
import org.web3j.abi.datatypes.generated.Int216;
import org.web3j.abi.datatypes.generated.Int224;
import org.web3j.abi.datatypes.generated.Int232;
import org.web3j.abi.datatypes.generated.Int24;
import org.web3j.abi.datatypes.generated.Int240;
import org.web3j.abi.datatypes.generated.Int248;
import org.web3j.abi.datatypes.generated.Int256;
import org.web3j.abi.datatypes.generated.Int32;
import org.web3j.abi.datatypes.generated.Int40;
import org.web3j.abi.datatypes.generated.Int48;
import org.web3j.abi.datatypes.generated.Int56;
import org.web3j.abi.datatypes.generated.Int64;
import org.web3j.abi.datatypes.generated.Int72;
import org.web3j.abi.datatypes.generated.Int8;
import org.web3j.abi.datatypes.generated.Int80;
import org.web3j.abi.datatypes.generated.Int88;
import org.web3j.abi.datatypes.generated.Int96;
import org.web3j.abi.datatypes.generated.Uint104;
import org.web3j.abi.datatypes.generated.Uint112;
import org.web3j.abi.datatypes.generated.Uint120;
import org.web3j.abi.datatypes.generated.Uint128;
import org.web3j.abi.datatypes.generated.Uint136;
import org.web3j.abi.datatypes.generated.Uint144;
import org.web3j.abi.datatypes.generated.Uint152;
import org.web3j.abi.datatypes.generated.Uint16;
import org.web3j.abi.datatypes.generated.Uint160;
import org.web3j.abi.datatypes.generated.Uint168;
import org.web3j.abi.datatypes.generated.Uint176;
import org.web3j.abi.datatypes.generated.Uint184;
import org.web3j.abi.datatypes.generated.Uint192;
import org.web3j.abi.datatypes.generated.Uint200;
import org.web3j.abi.datatypes.generated.Uint208;
import org.web3j.abi.datatypes.generated.Uint216;
import org.web3j.abi.datatypes.generated.Uint224;
import org.web3j.abi.datatypes.generated.Uint232;
import org.web3j.abi.datatypes.generated.Uint24;
import org.web3j.abi.datatypes.generated.Uint240;
import org.web3j.abi.datatypes.generated.Uint248;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint32;
import org.web3j.abi.datatypes.generated.Uint40;
import org.web3j.abi.datatypes.generated.Uint48;
import org.web3j.abi.datatypes.generated.Uint56;
import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.abi.datatypes.generated.Uint72;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.abi.datatypes.generated.Uint80;
import org.web3j.abi.datatypes.generated.Uint88;
import org.web3j.abi.datatypes.generated.Uint96;
import org.web3j.protocol.core.methods.response.AbiDefinition;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Web3jFunctionBuilder {

    private static final Logger logger = LoggerFactory.getLogger(Web3jFunctionBuilder.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Dynamically builds a Web3j Function object based on ABI, function name, and input values.
     *
     * @param abiJson The contract ABI as a JSON string.
     * @param functionName The name of the function to call.
     * @param inputValues An array of strings where even indices are parameter names and odd indices are their JSON string values.
     * Example: ["id", "doc001", "info", "{\"name\":\"qctc_doc\", \"size\": 2045}"]
     * @return A Web3j Function object.
     * @throws IOException If there's an error parsing JSON or ABI.
     * @throws IllegalArgumentException If the function is not found, or parameters are missing/invalid.
     */
    public Function buildFunctionFromAbi(String abiJson, String functionName, String[] inputValues)
            throws IOException {

        // 1. Parse user-provided inputValues string array into a Map for easier access.
        Map<String, String> userInputsMap = new HashMap<>();
        if (inputValues != null) {
            for (int i = 0; i < inputValues.length; i += 2) {
                if (i + 1 < inputValues.length) {
                    userInputsMap.put(inputValues[i], inputValues[i + 1]);
                } else {
                    // Or throw an IllegalArgumentException for malformed inputValues
                    logger.error("Warning: inputValues format is incorrect. Parameter {} is missing a value.", inputValues[i]);
                }
            }
        }

        // 2. Parse the ABI JSON string into a list of AbiDefinition objects.
        List<AbiDefinition> abiDefinitions = objectMapper.readValue(abiJson,
                new TypeReference<List<AbiDefinition>>() {});

        // 3. Find the AbiDefinition for the target function.
        AbiDefinition targetFunctionAbi = null;
        for (AbiDefinition ad : abiDefinitions) {
            if ("function".equals(ad.getType()) && functionName.equals(ad.getName())) {
                targetFunctionAbi = ad;
                break;
            } else if("constructor".equals(ad.getType()) && functionName.equals("constructor")) {
                targetFunctionAbi = ad;
                break;
            }
        }

        if (targetFunctionAbi == null) {
            throw new IllegalArgumentException("Function '" + functionName + "' not found in the provided ABI.");
        }

        // 4. Build the list of Web3j input parameters (List<Type>) based on ABI and user inputs.
        List<Type> web3jInputParameters = new ArrayList<>();
        if (targetFunctionAbi.getInputs() != null) {
            for (AbiDefinition.NamedType abiInputParam : targetFunctionAbi.getInputs()) {
                String paramName = abiInputParam.getName();
                String paramType = abiInputParam.getType();
                String paramValueStr = userInputsMap.get(paramName);

                if (paramValueStr == null) {
                    throw new IllegalArgumentException("Missing value for parameter '" + paramName + "' in function '" + functionName + "'.");
                }
                // The 'components' are needed if paramType is "tuple" or "tuple[]"
                web3jInputParameters.add(mapToWeb3jType(paramType, paramValueStr, abiInputParam.getComponents()));
            }
        }

        // 5. Build the list of Web3j output parameter types (List<org.web3j.abi.TypeReference<?>>).
        List<org.web3j.abi.TypeReference<?>> web3jOutputParameters = new ArrayList<>();
        if (targetFunctionAbi.getOutputs() != null) {
            for (AbiDefinition.NamedType abiOutputParam : targetFunctionAbi.getOutputs()) {
                web3jOutputParameters.add(createWeb3jTypeReference(abiOutputParam));
            }
        }

        // 6. Create and return the Web3j Function object.
        return new Function(
                functionName,
                web3jInputParameters,
                web3jOutputParameters);
    }

    /**
     * Maps a Solidity type string and its string value to a Web3j Type object.
     *
     * @param solidityType The Solidity type (e.g., "string", "uint256", "tuple").
     * @param valueStr The string representation of the value (can be JSON for structs/arrays).
     * @param components For "tuple" or "tuple[]" types, this is the list of component definitions from the ABI.
     * @return The corresponding Web3j Type object.
     * @throws IOException If JSON parsing fails for structs/arrays.
     * @throws IllegalArgumentException For unsupported types or invalid values.
     */
    @SuppressWarnings("rawtypes")
    private Type mapToWeb3jType(String solidityType, String valueStr, List<AbiDefinition.NamedType> components) throws IOException {
        // Handle basic types
        if (solidityType.startsWith("uint")) {
            Class<? extends Uint> uintClass = (Class<? extends Uint>) getWeb3jTypeClass(solidityType);
            try {
                return uintClass.getConstructor(BigInteger.class).newInstance(new BigInteger(valueStr));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid value for " + solidityType + ": " + valueStr, e);
            }
        } else if (solidityType.startsWith("int")) {
            Class<? extends Int> intClass = (Class<? extends Int>) getWeb3jTypeClass(solidityType);
            try {
                return intClass.getConstructor(BigInteger.class).newInstance(new BigInteger(valueStr));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid value for " + solidityType + ": " + valueStr, e);
            }
        } else if ("address".equals(solidityType)) {
            return new Address(valueStr);
        } else if ("bool".equals(solidityType)) {
            return new Bool(Boolean.parseBoolean(valueStr));
        } else if ("string".equals(solidityType)) {
            return new Utf8String(valueStr);
        } else if ("bytes".equals(solidityType)) { // Dynamic bytes
            return new DynamicBytes(Numeric.hexStringToByteArray(valueStr));
        } else if (solidityType.matches("bytes([1-9]|[12][0-9]|3[0-2])")) { // Fixed-size bytes (bytes1 to bytes32)
            Class<? extends org.web3j.abi.datatypes.Bytes> bytesClass =
                    (Class<? extends org.web3j.abi.datatypes.Bytes>) getWeb3jTypeClass(solidityType);
            try {
                return bytesClass.getConstructor(byte[].class).newInstance(Numeric.hexStringToByteArray(valueStr));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid value for " + solidityType + ": " + valueStr, e);
            }
        }
        // Handle "tuple" (struct) type
        else if ("tuple".equals(solidityType)) {
            if (components == null || components.isEmpty()) {
                throw new IllegalArgumentException("Tuple type '" + solidityType + "' requires component definitions from ABI.");
            }
            // Parse the JSON string value of the struct into a Map
            Map<String, Object> structValues = objectMapper.readValue(valueStr,
                    new TypeReference<Map<String, Object>>() {});

            List<Type> structFields = new ArrayList<>();
            boolean isDynamicStruct = false;
            for (AbiDefinition.NamedType component : components) {
                Object componentValueObj = structValues.get(component.getName());
                if (componentValueObj == null) {
                    throw new IllegalArgumentException("Missing field '" + component.getName() + "' in JSON for tuple parameter.");
                }
                String objectStr = componentValueObj.toString();
                if (component.getType().equals("tuple")) {
                    objectStr = objectMapper.writeValueAsString(componentValueObj);
                }
                // Recursively map the component type.
                // Pass component.getComponents() for nested structs/arrays within the struct.
                Type componentType = mapToWeb3jType(component.getType(), objectStr, component.getComponents());
                structFields.add(componentType);
                // Corrected check for dynamic type
                if (componentType instanceof DynamicBytes ||
                        componentType instanceof Utf8String ||
                        componentType instanceof DynamicArray ||
                        componentType instanceof DynamicStruct
                ) {
                    isDynamicStruct = true;
                }
            }

            if (isDynamicStruct) {
                return new DynamicStruct(structFields);
            } else {
                return new StaticStruct(structFields);
            }
        }
        // Handle array types (e.g., "string[]", "uint256[]", "tuple[]")
        else if (solidityType.endsWith("[]")) {
            String baseType = solidityType.substring(0, solidityType.length() - 2);
            // Parse the JSON array string into a List of Objects
            List<Object> arrayJsonValues = objectMapper.readValue(valueStr, new TypeReference<List<Object>>() {});
            List<Type> web3jArrayElements = new ArrayList<>();

            Class<? extends Type> arrayElementTypeClass = getWeb3jTypeClass(baseType);
            if (arrayElementTypeClass == null && !"tuple".equals(baseType)) { // tuple base type class will be DynamicStruct/StaticStruct
                throw new IllegalArgumentException("Unsupported base type for array: " + baseType);
            }

            for (Object itemJsonValue : arrayJsonValues) {
                // For "tuple[]", the 'components' for the tuple elements are the same 'components'
                // passed to this call if the ABI describes the tuple structure at this level.
                // If 'components' is null here, it means the baseType is not a tuple or ABI is missing info.
                List<AbiDefinition.NamedType> itemComponents = null;
                if ("tuple".equals(baseType)) {
                    itemComponents = components; // These are components of the tuple *element*
                }
                web3jArrayElements.add(mapToWeb3jType(baseType, itemJsonValue.toString(), itemComponents));
            }

            if (web3jArrayElements.isEmpty()) {
                if ("tuple".equals(baseType)) { // Special handling for empty array of structs
                    // For an empty array of structs, we need to decide if it's DynamicStruct or StaticStruct.
                    // This depends on the definition of the struct itself.
                    // If components for the struct indicate it would be dynamic, use DynamicStruct.
                    // For simplicity, if 'components' implies a dynamic nature, use DynamicStruct.
                    boolean isElementDynamic = false;
                    if (components != null) {
                        for (AbiDefinition.NamedType comp : components) {
                            // Simplified check: if any component could be dynamic
                            if (comp.getType().equals("string") || comp.getType().equals("bytes") || comp.getType().endsWith("[]") || comp.getType().equals("tuple")) {
                                // A more thorough check would involve looking up comp.getComponents() for nested tuples
                                isElementDynamic = true;
                                break;
                            }
                        }
                    }
                    if(isElementDynamic) {
                        return new DynamicArray<>(DynamicStruct.class);
                    } else {
                        return new DynamicArray<>(StaticStruct.class);
                    }
                }
                // Ensure arrayElementTypeClass is not null before creating DynamicArray for non-tuple base types
                if (arrayElementTypeClass == null) {
                    throw new IllegalArgumentException("Cannot create DynamicArray with null element type class for base type: " + baseType);
                }
                return new DynamicArray<>(arrayElementTypeClass); // Empty array of specific type
            } else {
                // All elements in DynamicArray constructor must be of the same class.
                // For mixed types (e.g. DynamicStruct and StaticStruct if logic was different), this would fail.
                // Our mapToWeb3jType should consistently return either Dynamic or Static for a given tuple definition.
                // For arrays of simple types, this is straightforward.
                // For arrays of tuples, it will be DynamicArray<DynamicStruct> or DynamicArray<StaticStruct>.
                if ("tuple".equals(baseType)) {
                    // Determine if elements are DynamicStruct or StaticStruct based on the first element
                    if (web3jArrayElements.get(0) instanceof DynamicStruct) {
                        return new DynamicArray<>(DynamicStruct.class, web3jArrayElements.stream().map(t -> (DynamicStruct)t).collect(Collectors.toList()));
                    } else { // Assuming it must be StaticStruct if not DynamicStruct
                        return new DynamicArray<>(StaticStruct.class, web3jArrayElements.stream().map(t -> (StaticStruct)t).collect(Collectors.toList()));
                    }
                }
                // Ensure arrayElementTypeClass is not null
//                if (arrayElementTypeClass == null) {
//                    throw new IllegalArgumentException("Cannot create DynamicArray with null element type class for populated array of base type: " + baseType);
//                }
//                return new DynamicArray<>(arrayElementTypeClass, web3jArrayElements);
            }
        }

        throw new IllegalArgumentException("Unsupported Solidity type for mapping: " + solidityType);
    }

    /**
     * Creates a Web3j TypeReference for a given ABI output parameter definition.
     * This is used for decoding function return values.
     *
     * @param abiOutputParam The ABI definition of the output parameter.
     * @return A Web3j TypeReference.
     * @throws IllegalArgumentException If the type is unsupported.
     */
    @SuppressWarnings("rawtypes")
    private org.web3j.abi.TypeReference<?> createWeb3jTypeReference(AbiDefinition.NamedType abiOutputParam) {
        String solidityType = abiOutputParam.getType();
        boolean isIndexed = abiOutputParam.isIndexed(); // Relevant for events, less so for function outputs directly but good to have

        // Handle "tuple" (struct) output
        if ("tuple".equals(solidityType)) {
            // For dynamic creation, DynamicStruct is often the easiest for decoding arbitrary structs.
            // If you had pre-generated Java classes for your structs, you could use them here.
            // To be more precise, one could recursively build a TypeReference for a StaticStruct
            // if all components are static, but that adds complexity.
            // We need to inspect components to decide if it's Static or Dynamic
            boolean isDynamic = false;
            if (abiOutputParam.getComponents() != null) {
                for (AbiDefinition.NamedType component : abiOutputParam.getComponents()) {
                    // Recursive check or simplified check
                    if (component.getType().equals("string") || component.getType().equals("bytes") ||
                            component.getType().endsWith("[]") ||
                            (component.getType().equals("tuple") && isTupleDynamic(component))) { // Recursive call for nested tuples
                        isDynamic = true;
                        break;
                    }
                }
            }
            if (isDynamic) {
                return org.web3j.abi.TypeReference.create(DynamicStruct.class, isIndexed);
            } else {
                return org.web3j.abi.TypeReference.create(StaticStruct.class, isIndexed);
            }
        }
        // Handle array outputs (e.g., "string[]", "tuple[]")
        else if (solidityType.endsWith("[]")) {
            // Similar to tuples, DynamicArray is a common choice for dynamic decoding.
            // String baseType = solidityType.substring(0, solidityType.length() - 2);
            // Class<? extends Type> elementClass = getWeb3jTypeClass(baseType);
            // Example: new TypeReference<DynamicArray<Utf8String>>() {} - hard to do dynamically.
            // So, we use TypeReference.create(DynamicArray.class).
            // The caller will get a List<Object> or List<Type> and may need to cast/process elements.
            // For arrays of tuples, the TypeReference should ideally be more specific,
            // e.g., TypeReference<DynamicArray<DynamicStruct>> or TypeReference<DynamicArray<StaticStruct>>
            // This simplified version uses DynamicArray.class, which is okay for encoding,
            // but decoding might require more specific handling by the caller.
            // To improve, one could try to determine the element type for the TypeReference more precisely.
            return org.web3j.abi.TypeReference.create(DynamicArray.class, isIndexed);
        }
        // Handle basic types
        else {
            Class<? extends Type> typeClass = getWeb3jTypeClass(solidityType);
            if (typeClass != null) {
                return org.web3j.abi.TypeReference.create(typeClass, isIndexed);
            }
        }
        throw new IllegalArgumentException("Unsupported Solidity type for creating TypeReference: " + solidityType);
    }

    /**
     * Helper to check if a tuple (struct) defined by its components is dynamic.
     * @param tupleDefinition The AbiDefinition.NamedType for the tuple.
     * @return true if the tuple is dynamic, false otherwise.
     */
    private boolean isTupleDynamic(AbiDefinition.NamedType tupleDefinition) {
        if (tupleDefinition.getComponents() != null) {
            for (AbiDefinition.NamedType component : tupleDefinition.getComponents()) {
                String compType = component.getType();
                if (compType.equals("string") || compType.equals("bytes") || compType.endsWith("[]") ||
                        (compType.equals("tuple") && isTupleDynamic(component))) { // Recursive check
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Gets the corresponding Web3j Java Class for a given Solidity type string.
     *
     * @param solidityType The Solidity type string (e.g., "uint256", "string", "bytes32", "tuple").
     * @return The Class object for the Web3j type, or null if it's a tuple (handled separately).
     */
    private Class<? extends Type> getWeb3jTypeClass(String solidityType) {
        // Uint types
        if (solidityType.equals("uint8")) return Uint8.class;
        if (solidityType.equals("uint16")) return Uint16.class;
        if (solidityType.equals("uint24")) return Uint24.class;
        if (solidityType.equals("uint32")) return Uint32.class;
        if (solidityType.equals("uint40")) return Uint40.class;
        if (solidityType.equals("uint48")) return Uint48.class;
        if (solidityType.equals("uint56")) return Uint56.class;
        if (solidityType.equals("uint64")) return Uint64.class;
        if (solidityType.equals("uint72")) return Uint72.class;
        if (solidityType.equals("uint80")) return Uint80.class;
        if (solidityType.equals("uint88")) return Uint88.class;
        if (solidityType.equals("uint96")) return Uint96.class;
        if (solidityType.equals("uint104")) return Uint104.class;
        if (solidityType.equals("uint112")) return Uint112.class;
        if (solidityType.equals("uint120")) return Uint120.class;
        if (solidityType.equals("uint128")) return Uint128.class; // As per user's contract
        if (solidityType.equals("uint136")) return Uint136.class;
        if (solidityType.equals("uint144")) return Uint144.class;
        if (solidityType.equals("uint152")) return Uint152.class;
        if (solidityType.equals("uint160")) return Uint160.class;
        if (solidityType.equals("uint168")) return Uint168.class;
        if (solidityType.equals("uint176")) return Uint176.class;
        if (solidityType.equals("uint184")) return Uint184.class;
        if (solidityType.equals("uint192")) return Uint192.class;
        if (solidityType.equals("uint200")) return Uint200.class;
        if (solidityType.equals("uint208")) return Uint208.class;
        if (solidityType.equals("uint216")) return Uint216.class;
        if (solidityType.equals("uint224")) return Uint224.class;
        if (solidityType.equals("uint232")) return Uint232.class;
        if (solidityType.equals("uint240")) return Uint240.class;
        if (solidityType.equals("uint248")) return Uint248.class;
        if (solidityType.equals("uint256") || solidityType.equals("uint")) return Uint256.class; // Default uint is uint256

        // Int types
        if (solidityType.equals("int8")) return Int8.class;
        if (solidityType.equals("int16")) return Int16.class;
        if (solidityType.equals("int24")) return Int24.class;
        if (solidityType.equals("int32")) return Int32.class;
        if (solidityType.equals("int40")) return Int40.class;
        if (solidityType.equals("int48")) return Int48.class;
        if (solidityType.equals("int56")) return Int56.class;
        if (solidityType.equals("int64")) return Int64.class;
        if (solidityType.equals("int72")) return Int72.class;
        if (solidityType.equals("int80")) return Int80.class;
        if (solidityType.equals("int88")) return Int88.class;
        if (solidityType.equals("int96")) return Int96.class;
        if (solidityType.equals("int104")) return Int104.class;
        if (solidityType.equals("int112")) return Int112.class;
        if (solidityType.equals("int120")) return Int120.class;
        if (solidityType.equals("int128")) return Int128.class;
        if (solidityType.equals("int136")) return Int136.class;
        if (solidityType.equals("int144")) return Int144.class;
        if (solidityType.equals("int152")) return Int152.class;
        if (solidityType.equals("int160")) return Int160.class;
        if (solidityType.equals("int168")) return Int168.class;
        if (solidityType.equals("int176")) return Int176.class;
        if (solidityType.equals("int184")) return Int184.class;
        if (solidityType.equals("int192")) return Int192.class;
        if (solidityType.equals("int200")) return Int200.class;
        if (solidityType.equals("int208")) return Int208.class;
        if (solidityType.equals("int216")) return Int216.class;
        if (solidityType.equals("int224")) return Int224.class;
        if (solidityType.equals("int232")) return Int232.class;
        if (solidityType.equals("int240")) return Int240.class;
        if (solidityType.equals("int248")) return Int248.class;
        if (solidityType.equals("int256") || solidityType.equals("int")) return Int256.class; // Default int is int256

        // Other types
        if ("address".equals(solidityType)) return Address.class;
        if ("bool".equals(solidityType)) return Bool.class;
        if ("string".equals(solidityType)) return Utf8String.class;
        if ("bytes".equals(solidityType)) return DynamicBytes.class; // Dynamic-size bytes

        // Fixed-size bytes (bytes1 to bytes32)
        if (solidityType.equals("bytes1")) return Bytes1.class;
        if (solidityType.equals("bytes2")) return Bytes2.class;
        if (solidityType.equals("bytes3")) return Bytes3.class;
        if (solidityType.equals("bytes4")) return Bytes4.class;
        if (solidityType.equals("bytes5")) return Bytes5.class;
        if (solidityType.equals("bytes6")) return Bytes6.class;
        if (solidityType.equals("bytes7")) return Bytes7.class;
        if (solidityType.equals("bytes8")) return Bytes8.class;
        if (solidityType.equals("bytes9")) return Bytes9.class;
        if (solidityType.equals("bytes10")) return Bytes10.class;
        if (solidityType.equals("bytes11")) return Bytes11.class;
        if (solidityType.equals("bytes12")) return Bytes12.class;
        if (solidityType.equals("bytes13")) return Bytes13.class;
        if (solidityType.equals("bytes14")) return Bytes14.class;
        if (solidityType.equals("bytes15")) return Bytes15.class;
        if (solidityType.equals("bytes16")) return Bytes16.class;
        if (solidityType.equals("bytes17")) return Bytes17.class;
        if (solidityType.equals("bytes18")) return Bytes18.class;
        if (solidityType.equals("bytes19")) return Bytes19.class;
        if (solidityType.equals("bytes20")) return Bytes20.class;
        if (solidityType.equals("bytes21")) return Bytes21.class;
        if (solidityType.equals("bytes22")) return Bytes22.class;
        if (solidityType.equals("bytes23")) return Bytes23.class;
        if (solidityType.equals("bytes24")) return Bytes24.class;
        if (solidityType.equals("bytes25")) return Bytes25.class;
        if (solidityType.equals("bytes26")) return Bytes26.class;
        if (solidityType.equals("bytes27")) return Bytes27.class;
        if (solidityType.equals("bytes28")) return Bytes28.class;
        if (solidityType.equals("bytes29")) return Bytes29.class;
        if (solidityType.equals("bytes30")) return Bytes30.class;
        if (solidityType.equals("bytes31")) return Bytes31.class;
        if (solidityType.equals("bytes32")) return Bytes32.class;

        // For tuples, we don't return a single class here as it depends on its components (StaticStruct vs DynamicStruct)
        // The mapToWeb3jType method handles struct creation.
        // For array element type resolution, if the base type is "tuple", mapToWeb3jType will construct the struct.
        // Returning null here forces mapToWeb3jType to handle tuple arrays specifically.
        if ("tuple".equals(solidityType)) {
            return null;
        }

        logger.error("Warning: No specific Web3j class found for Solidity type: {}. Falling back if possible or will fail.", solidityType);
        return null; // Fallback or unsupported
    }
}
