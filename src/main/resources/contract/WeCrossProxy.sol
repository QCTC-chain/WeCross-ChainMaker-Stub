/*
*   v1.0.0
*   proxy contract for WeCross
*   main entrance of all contract call
*/

// SPDX-License-Identifier: MIT

pragma solidity >=0.5.0 <0.6.0;
pragma experimental ABIEncoderV2;

contract WeCrossProxy {
    byte   constant SEPARATOR = '.';

    // internal call
    function callContract(
        address _contractAddress,
        string memory _sig,
        bytes memory _args) internal returns(bytes memory result)
    {
        bytes memory sig = abi.encodeWithSignature(_sig);
        bool success;
        (success, result) = address(_contractAddress).call(abi.encodePacked(sig, _args));
        if(!success) {
            revert(string(result));
        }
    }

    function constantCall(
        string memory _XATransactionID,
        string memory _path,
        string memory _func,
        bytes memory _args
    ) public returns(bytes memory) {
        address contractAddress = makeAddress(_path);
        return callContract(contractAddress, _func, _args);
    }

    function sendTransaction(
        string memory _uid,
        string memory _XATransactionID,
        uint256 _XATransactionSeq,
        string memory _path,
        string memory _func,
        bytes memory _args
    ) public returns(bytes memory) {
        address contractAddress = makeAddress(_path);
        return callContract(contractAddress, _func, _args);
    }

    function startXATransaction(
        string memory _xaTransactionID,
        string[] memory _selfPaths,
        string[] memory _otherPaths
    ) public returns(string memory) {
        return "NOTIMPLEMENT";
    }

    function commitXATransaction(string memory _xaTransactionID) public returns(string memory) {
        return "NOTIMPLEMENT";
    }


    function rollbackXATransaction(string memory _xaTransactionID) public returns(string memory) {
        return "NOTIMPLEMENT";
    }

    function listXATransactions(
        string memory _index,
        uint256 _size
    ) public view returns (string memory) {
        return "NOTIMPLEMENT";
    }

    function getXATransaction(
        string memory _xaTransactionID
    ) public view returns(string memory) {
        return "NOTIMPLEMENT";
    }

    // input must be a valid path like "zone.chain.resource"
    function getNameByPath(string memory _path) internal pure
    returns (string memory)
    {
        bytes memory path = bytes(_path);
        uint256 len = path.length;
        uint256 nameLen = 0;
        uint256 index = 0;
        for(uint256 i = len - 1; i > 0; i--) {
            if(path[i] == SEPARATOR) {
                index = i + 1;
                break;
            } else {
                nameLen++;
            }
        }

        bytes memory name = new bytes(nameLen);
        for(uint256 i = 0; i < nameLen; i++) {
            name[i] = path[index++];
        }

        return string(name);
    }

    function bytes32ToString(bytes32 _bts32) internal pure
    returns (string memory)
    {
        bytes memory result = new bytes(_bts32.length);

        uint len = _bts32.length;
        for(uint i = 0; i < len; i++) {
            result[i] = _bts32[i];
        }

        return string(result);
    }

    function bytesToAddress(bytes memory _address) internal pure
    returns (address)
    {
        if(_address.length != 40) {
            revert(string(abi.encodePacked("cannot covert ", _address, " to chainmaker address.")));
        }

        uint160 result = 0;
        uint160 b1;
        uint160 b2;
        for (uint i = 0; i < 2 * 20; i += 2) {
            result *= 256;
            b1 = uint160(uint8(_address[i]));
            b2 = uint160(uint8(_address[i + 1]));
            if ((b1 >= 97) && (b1 <= 102)) {
                b1 -= 87;
            } else if ((b1 >= 65) && (b1 <= 70)) {
                b1 -= 55;
            } else if ((b1 >= 48) && (b1 <= 57)) {
                b1 -= 48;
            }

            if ((b2 >= 97) && (b2 <= 102)) {
                b2 -= 87;
            } else if ((b2 >= 65) && (b2 <= 70)) {
                b2 -= 55;
            } else if ((b2 >= 48) && (b2 <= 57)) {
                b2 -= 48;
            }
            result += (b1 * 16 + b2);
        }
        return address(result);
    }

    function bytesToHexString(bytes memory _bts) internal pure
    returns (string memory result)
    {
        uint256 len = _bts.length;
        bytes memory s = new bytes(len * 2);
        for (uint256 i = 0; i < len; i++) {
            byte befor = byte(_bts[i]);
            byte high = byte(uint8(befor) / 16);
            byte low = byte(uint8(befor) - 16 * uint8(high));
            s[i*2] = convert(high);
            s[i*2+1] = convert(low);
        }
        result = string(s);
    }

    function convert(byte _b) internal pure
    returns (byte)
    {
        if (uint8(_b) < 10) {
            return byte(uint8(_b) + 0x30);
        } else {
            return byte(uint8(_b) + 0x57);
        }
    }

    function substring(string memory str, uint startIndex, uint endIndex) public pure returns (string memory) {
        bytes memory strBytes = bytes(str);
        bytes memory result = new bytes(endIndex - startIndex);
        for (uint i = startIndex; i < endIndex; i++) {
            result[i - startIndex] = strBytes[i];
        }
        return string(result);
    }

    function makeAddress(string memory path) internal returns (address) {
        string memory name = getNameByPath(path);
        string memory s = bytes32ToString(keccak256(bytes(name)));
        string memory s2  = bytes32ToString(keccak256(bytes(s)));
        string memory address_str = bytesToHexString(bytes(s2));
        address_str = substring(address_str, 24, 64);
        bytes memory _bytes = bytes(address_str);
        return bytesToAddress(_bytes);
    }
}
