/*
*   v1.0.0
*   proxy contract for WeCross
*   main entrance of all contract call
*/

// SPDX-License-Identifier: MIT

pragma solidity >=0.5.0 <0.6.0;
pragma experimental ABIEncoderV2;

contract WeCrossProxy {
    string constant version = "v1.0.0";
    // per step of xa transaction
    struct XATransactionStep {
        string accountIdentity;
        uint256 timestamp;
        string path;
        address contractAddress;
        string func;
        bytes args;
    }

    // information of xa transaction
    struct XATransaction {
        string accountIdentity;
        string[] paths;     // all paths related to this transaction
        address[] contractAddresses; // locked addressed in current chain
        string status;      // processing | committed |  rolledback
        uint256 startTimestamp;
        uint256 commitTimestamp;
        uint256 rollbackTimestamp;
        uint256[] seqs;    // sequence of each step
        uint256 stepNum;   // step number
    }

    struct ContractStatus {
        bool locked;     // isolation control, read-committed
        string xaTransactionID;
    }

    struct Transaction {
        bool existed;
        bytes result;
    }

    mapping(string => Transaction) transactions; // key: uniqueID

    mapping(address => ContractStatus) lockedContracts;
    mapping(string => XATransaction) xaTransactions;      // key: xaTransactionID
    mapping(string => XATransactionStep) xaTransactionSteps;  // key: xaTransactionID || xaTransactionSeq

    byte   constant SEPARATOR = '.';
    string constant SUCCESS_FLAG = "success";
    string constant REVERT_FLAG = "_revert";
    string constant NULL_FLAG = "null";

    string constant XA_STATUS_PROCESSING = "processing";
    string constant XA_STATUS_COMMITTED = "committed";
    string constant XA_STATUS_ROLLEDBACK = "rolledback";
    uint256 constant MAX_SETP = 1024;

    /*
    * record all xa transactionIDs
    * head: point to the current xa transaction to be checked
    * tail: point to the next position for added xa transaction
    */
    uint256 tail = 0;
    string[] xaTransactionIDs;

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

    // internal call
    function callContract(address _contractAddress, bytes memory _argsWithMethodId) internal
    returns(bytes memory result)
    {
        bool success;
        (success, result) = address(_contractAddress).call(_argsWithMethodId);
        if(!success) {
            //(string memory error) = abi.decode(result, (string));
            revert(string(result));
        }
    }

    // constant call with xaTransactionID
    function constantCall(
        string memory _XATransactionID,
        string memory _path,
        string memory _func,
        bytes memory _args
    ) public returns(bytes memory) {
        address addr = makeAddressByPath(_path);

        if(!isExistedXATransaction(_XATransactionID)) {
            revert("xa transaction not found");
        }

        if(!sameString(lockedContracts[addr].xaTransactionID, _XATransactionID)) {
            revert(string(abi.encodePacked(_path, " is unregistered in xa transaction: ", _XATransactionID)));
        }

        return callContract(addr, _func, _args);
    }

    // constant call without xaTransactionID
    function constantCall(string memory _name, bytes memory _argsWithMethodId) public
    returns(bytes memory)
    {
        // find address from abi cache first
        address addr = makeAddressByName(_name);

        if(lockedContracts[addr].locked) {
            revert(string(abi.encodePacked("resource is locked by unfinished xa transaction: ", lockedContracts[addr].xaTransactionID)));
        }

        return callContract(addr, _argsWithMethodId);
    }

    // non-constant call with xaTransactionID
    function sendTransaction(
        string memory _uid,
        string memory _XATransactionID,
        uint256 _XATransactionSeq,
        string memory _path,
        string memory _func,
        bytes memory _args
    ) public returns(bytes memory) {
        if(transactions[_uid].existed) {
            return transactions[_uid].result;
        }

        address addr = makeAddressByPath(_path);

        if(!isExistedXATransaction(_XATransactionID)) {
            revert("xa transaction not found");
        }

        if(sameString(xaTransactions[_XATransactionID].status, XA_STATUS_COMMITTED)) {
            revert("xa transaction has been committed");
        }

        if(sameString(xaTransactions[_XATransactionID].status, XA_STATUS_ROLLEDBACK)) {
            revert("xa transaction has been rolledback");
        }

        if(!sameString(lockedContracts[addr].xaTransactionID, _XATransactionID)) {
            revert(string(abi.encodePacked(_path, " is unregistered in xa transaction ", _XATransactionID)));
        }

        if(!isValidXATransactionSep(_XATransactionID, _XATransactionSeq)) {
            revert("seq should be greater than before");
        }

        // recode step
        xaTransactionSteps[getXATransactionStepKey(_XATransactionID, _XATransactionSeq)] = XATransactionStep(
            addressToString(tx.origin),
            block.timestamp / 1000,
            _path,
            addr,
            _func,
            _args
        );

        // recode seq
        uint256 num = xaTransactions[_XATransactionID].stepNum;
        xaTransactions[_XATransactionID].seqs[num] = _XATransactionSeq;
        xaTransactions[_XATransactionID].stepNum = num + 1;

        bytes memory result =  callContract(addr, _func, _args);

        // recode transaction
        transactions[_uid] = Transaction(true, result);
        return result;

    }

    // non-constant call without xaTransactionID
    function sendTransaction(string memory _uid, string memory _name, bytes memory _argsWithMethodId) public returns(bytes memory) {
        if(transactions[_uid].existed) {
            return transactions[_uid].result;
        }

        // find address from abi cache first
        address addr = makeAddressByName(_name);

        if(lockedContracts[addr].locked) {
            revert(string(abi.encodePacked(_name, " is locked by unfinished xa transaction: ", lockedContracts[addr].xaTransactionID)));
        }

        bytes memory result = callContract(addr, _argsWithMethodId);

        // recode transaction
        transactions[_uid] = Transaction(true, result);
        return result;
    }

    /*
    * @param xaTransactionID
    * @param selfPaths are related to current chain
    * result: success
    */
    function startXATransaction(
        string memory _xaTransactionID,
        string[] memory _selfPaths,
        string[] memory _otherPaths
    ) public returns(string memory) {

        if(isExistedXATransaction(_xaTransactionID)) {
            revert(string(abi.encodePacked("xa transaction ", _xaTransactionID, " already exists")));
        }

        uint256 selfLen = _selfPaths.length;
        uint256 otherLen = _otherPaths.length;

        address[] memory contracts = new address[](selfLen);
        string[] memory allPaths = new string[](selfLen + otherLen);

        // recode ACL
        for(uint256 i = 0; i < selfLen; i++) {
            address addr = makeAddressByPath(_selfPaths[i]);
            contracts[i] = addr;
            if(lockedContracts[addr].locked) {
                revert(string(abi.encodePacked(_selfPaths[i], " is locked by unfinished xa transaction: ", lockedContracts[addr].xaTransactionID)));
            }
            lockedContracts[addr].locked = true;
            lockedContracts[addr].xaTransactionID = _xaTransactionID;
            allPaths[i] = _selfPaths[i];
        }

        for(uint256 i = 0; i < otherLen; i++)
        {
            allPaths[selfLen+i] = _otherPaths[i];
        }

        uint256[] memory seqs = new uint256[](MAX_SETP);
        // recode xa transaction
        xaTransactions[_xaTransactionID] = XATransaction(
            addressToString(tx.origin),
            allPaths,
            contracts,
            XA_STATUS_PROCESSING,
            block.timestamp / 1000,
            0,
            0,
            seqs,
            0
        );

        addXATransaction(_xaTransactionID);

        return SUCCESS_FLAG;
    }

    /*
    *  @param xaTransactionID
    * result: success
    */
    function commitXATransaction(string memory _xaTransactionID) public returns(string memory) {
        if(!isExistedXATransaction(_xaTransactionID)) {
            revert("xa transaction not found");
        }

        // has committed
        if(sameString(xaTransactions[_xaTransactionID].status, XA_STATUS_COMMITTED)) {
            revert("xa transaction has been committed");
        }

        // has rolledback
        if(sameString(xaTransactions[_xaTransactionID].status, XA_STATUS_ROLLEDBACK)) {
            revert("xa transaction has been rolledback");
        }

        xaTransactions[_xaTransactionID].commitTimestamp = block.timestamp / 1000;
        xaTransactions[_xaTransactionID].status = XA_STATUS_COMMITTED;
        deleteLockedContracts(_xaTransactionID);

        return SUCCESS_FLAG;
    }

    /*
    *  @param xaTransactionID
    * result: success | message
    */
    function rollbackXATransaction(string memory _xaTransactionID) public returns(string memory) {
        string memory result = SUCCESS_FLAG;
        if(!isExistedXATransaction(_xaTransactionID)) {
            revert("xa transaction not found");
        }

        // has committed
        if(sameString(xaTransactions[_xaTransactionID].status, XA_STATUS_COMMITTED)) {
            revert("xa transaction has been committed");
        }

        // has rolledback
        if(sameString(xaTransactions[_xaTransactionID].status, XA_STATUS_ROLLEDBACK)) {
            revert("xa transaction has been rolledback");
        }

        string memory message = 'warning:';
        uint256 stepNum = xaTransactions[_xaTransactionID].stepNum;
        for(uint256 i = stepNum; i > 0; i--) {
            uint256 seq = xaTransactions[_xaTransactionID].seqs[i-1];
            string memory key = getXATransactionStepKey(_xaTransactionID, seq);

            string memory func = xaTransactionSteps[key].func;
            address contractAddress = xaTransactionSteps[key].contractAddress;
            bytes memory args = xaTransactionSteps[key].args;

            // call revert function
            bytes memory sig = abi.encodeWithSignature(getRevertFunc(func, REVERT_FLAG));
            bool success;
            (success, ) = address(contractAddress).call(abi.encodePacked(sig, args));
            if(!success) {
                message = string(abi.encodePacked(message, ' revert "', func, '" failed.'));
                result = message;
            }
        }

        xaTransactions[_xaTransactionID].rollbackTimestamp = block.timestamp / 1000;
        xaTransactions[_xaTransactionID].status = XA_STATUS_ROLLEDBACK;
        deleteLockedContracts(_xaTransactionID);
        return result;
    }

    /*
    * traverse in reverse order
    * outputs:
    {
        "total": 100,
        "xaTransactions":
        [
            {
            	"xaTransactionID": "001",
        		"accountIdentity": "0x11",
        		"status": "processing",
        		"timestamp": 123,
        		"paths": ["a.b.1","a.b.2"]
        	},
        	{
            	"xaTransactionID": "002",
        		"accountIdentity": "0x11",
        		"status": "committed",
        		"timestamp": 123,
        		"paths": ["a.b.1","a.b.2"]
        	}
        ]
    }
    */
    function listXATransactions(
        string memory _index,
        uint256 _size
    ) public view returns (string memory) {
        uint256 len = xaTransactionIDs.length;
        uint256 index = sameString("-1", _index) ? (len - 1) : stringToUint256(_index);

        if(len == 0 || len <= index) {
            return '{"total":0,"xaTransactions":[]}';
        }

        string memory jsonStr = '[';
        for(uint256 i = 0; i < (_size - 1) && (index - i) > 0; i++) {
            string memory xaTransactionID = xaTransactionIDs[index-i];
            jsonStr = string(abi.encodePacked(jsonStr, '{"xaTransactionID":"', xaTransactionID, '",',
                '"accountIdentity":"', xaTransactions[xaTransactionID].accountIdentity, '",',
                '"status":"', xaTransactions[xaTransactionID].status, '",',
                '"paths":', pathsToJson(xaTransactionID), ',',
                '"timestamp":', uint256ToString(xaTransactions[xaTransactionID].startTimestamp), '},')
            );
        }

        uint256 lastIndex = (index + 1) >= _size ? (index + 1 - _size) : 0;
        string memory xaTransactionID = xaTransactionIDs[lastIndex];
        jsonStr = string(abi.encodePacked(jsonStr, '{"xaTransactionID":"', xaTransactionID, '",',
            '"accountIdentity":"', xaTransactions[xaTransactionID].accountIdentity, '",',
            '"status":"', xaTransactions[xaTransactionID].status, '",',
            '"paths":', pathsToJson(xaTransactionID), ',',
            '"timestamp":', uint256ToString(xaTransactions[xaTransactionID].startTimestamp), '}]')
        );

        return string(abi.encodePacked('{"total":', uint256ToString(len),',"xaTransactions":', jsonStr, '}'));
    }

    /*
    *  @param xaTransactionID
    * result with json form
    * example:
    {
    	"xaTransactionID": "1",
    	"accountIdentity": "0x88",
    	"status": "processing",
    	"paths":["a.b.c1","a.b.c2","a.b1.c3"],
    	"startTimestamp": 123,
    	"commitTimestamp": 456,
    	"rollbackTimestamp": 0,
    	"xaTransactionSteps": [{
    	        "accountIdentity":"0x12",
            	"xaTransactionSeq": 233,
    			"path": "a.b.c1",
    			"timestamp": 233,
    			"method": "set",
    			"args": "0010101"
    		},
    		{
    		    "accountIdentity":"0x12",
    		    "xaTransactionSeq": 244,
    			"path": "a.b.c2",
    			"timestamp": 244,
    			"method": "set",
    			"args": "0010101"
    		}
    	]
    }
    */
    function getXATransaction(
        string memory _xaTransactionID
    ) public view returns(string memory) {
        if(!isExistedXATransaction(_xaTransactionID)) {
            revert("xa transaction not found");
        }

        return string(abi.encodePacked('{"xaTransactionID":"', _xaTransactionID, '",',
            '"accountIdentity":"', xaTransactions[_xaTransactionID].accountIdentity, '",',
            '"status":"', xaTransactions[_xaTransactionID].status, '",',
            '"paths":', pathsToJson(_xaTransactionID), ',',
            '"startTimestamp":', uint256ToString(xaTransactions[_xaTransactionID].startTimestamp), ',',
            '"commitTimestamp":', uint256ToString(xaTransactions[_xaTransactionID].commitTimestamp), ',',
            '"rollbackTimestamp":', uint256ToString(xaTransactions[_xaTransactionID].rollbackTimestamp), ',',
            '"xaTransactionSteps":', xaTransactionStepArrayToJson(_xaTransactionID, xaTransactions[_xaTransactionID].seqs, xaTransactions[_xaTransactionID].stepNum), "}")
        );
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

    function addressToString(address _addr) internal pure
    returns (string memory)
    {
        bytes memory result = new bytes(40);
        for (uint i = 0; i < 20; i++) {
            byte temp = byte(uint8(uint(_addr) / (2 ** (8 * (19 - i)))));
            byte b1 = byte(uint8(temp) / 16);
            byte b2 = byte(uint8(temp) - 16 * uint8(b1));
            result[2 * i] = convert(b1);
            result[2 * i + 1] = convert(b2);
        }
        return string(abi.encodePacked("0x", string(result)));
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

    function uint256ToString(uint256 _value) internal pure
    returns (string memory)
    {
        bytes32 result;
        if (_value == 0) {
            return "0";
        } else {
            while (_value > 0) {
                result = bytes32(uint(result) / (2 ** 8));
                result |= bytes32(((_value % 10) + 48) * 2 ** (8 * 31));
                _value /= 10;
            }
        }
        return bytes32ToString(result);
    }

    function stringToUint256(string memory _str) public pure
    returns (uint256)
    {
        bytes memory bts = bytes(_str);
        uint256 result = 0;
        uint256 len = bts.length;
        for (uint256 i = 0; i < len; i++) {
            if (uint8(bts[i]) >= 48 && uint8(bts[i]) <= 57) {
                result = result * 10 + (uint8(bts[i]) - 48);
            }
        }
        return result;
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

    function makeAddressByPath(string memory path) internal returns (address) {
        string memory name = getNameByPath(path);
        return makeAddressByName(name);
    }

    function makeAddressByName(string memory name) internal returns (address) {
        string memory s = bytes32ToString(keccak256(bytes(name)));
        string memory s2  = bytes32ToString(keccak256(bytes(s)));
        string memory address_str = bytesToHexString(bytes(s2));
        address_str = substring(address_str, 24, 64);
        bytes memory _bytes = bytes(address_str);
        return bytesToAddress(_bytes);
    }

    function isExistedXATransaction(string memory _xaTransactionID) internal view
    returns (bool)
    {
        return xaTransactions[_xaTransactionID].startTimestamp != 0;
    }

    function addXATransaction(string memory _xaTransactionID) internal
    {
        tail++;
        xaTransactionIDs.push(_xaTransactionID);
    }

    function deleteLockedContracts(string memory _xaTransactionID) internal
    {
        uint256 len = xaTransactions[_xaTransactionID].contractAddresses.length;
        for(uint256 i = 0; i < len; i++) {
            address contractAddress = xaTransactions[_xaTransactionID].contractAddresses[i];
            delete lockedContracts[contractAddress];
        }
    }

    function getXATransactionStepKey(string memory _transactionID, uint256 _transactionSeq) internal pure
    returns(string memory)
    {
        return string(abi.encodePacked(_transactionID, uint256ToString(_transactionSeq)));
    }

    function sameString(string memory _str1, string memory _str2) internal pure
    returns (bool)
    {
        return keccak256(bytes(_str1)) == keccak256(bytes(_str2));
    }

// func(string,uint256) => func_flag(string,uint256)
    function getRevertFunc(string memory _func, string memory _revertFlag) internal pure
    returns(string memory)
    {
        bytes memory funcBytes = bytes(_func);
        bytes memory flagBytes = bytes(_revertFlag);
        uint256 funcLen = funcBytes.length;
        uint256 flagLen = flagBytes.length;
        bytes memory newFunc = new bytes(funcLen + flagLen);

        byte c = byte('(');
        uint256 index = 0;
        uint256 point = 0;

        for(uint256 i = 0; i < funcLen; i++) {
            if(funcBytes[i] != c) {
                newFunc[index++] = funcBytes[i];
            } else {
                point = i;
                break;
            }
        }

        for(uint256 i = 0; i < flagLen; i++) {
            newFunc[index++] = flagBytes[i];
        }

        for(uint256 i = point; i < funcLen; i++) {
            newFunc[index++] = funcBytes[i];
        }

        return string(newFunc);
    }

    // func(string,uint256) => func
    function getMethodFromFunc(string memory _func) internal pure
    returns(string memory)
    {
        bytes memory funcBytes = bytes(_func);
        uint256 funcLen = funcBytes.length;
        bytes memory temp = new bytes(funcLen);

        byte c = byte('(');
        uint256 index = 0;

        for(uint256 i = 0; i < funcLen; i++) {
            if(funcBytes[i] != c) {
                temp[index++] = funcBytes[i];
            } else {
                break;
            }
        }

        bytes memory result = new bytes(index);
        for(uint256 i = 0; i < index; i++) {
            result[i] = temp[i];
        }

        return string(result);
    }

    /*
        ["a.b.c1", "a.b.c2"]
    */
    function pathsToJson(string memory _transactionID) internal view
    returns(string memory)
    {
        uint256 len = xaTransactions[_transactionID].paths.length;
        string memory paths = string(abi.encodePacked('["', xaTransactions[_transactionID].paths[0], '"'));
        for(uint256 i = 1; i < len; i++) {
            paths = string(abi.encodePacked(paths, ',"', xaTransactions[_transactionID].paths[i], '"'));
        }
        return string(abi.encodePacked(paths, ']'));
    }

   /*
    [
        {
    	    "accountIdentity":"0x12",
            "xaTransactionSeq": 233,
    		"path": "a.b.c1",
    		"timestamp": 233,
    		"method": "set",
    		"args": "0010101"
    	},
        {
    	    "accountIdentity":"0x12",
            "xaTransactionSeq": 233,
    		"path": "a.b.c1",
    		"timestamp": 233,
    		"method": "set",
    		"args": "0010101"
    	}
    ]
    */
    function xaTransactionStepArrayToJson(string memory _transactionID, uint256[] memory _seqs, uint256 _len) internal view
    returns(string memory result)
    {
        if(_len == 0) {
            return '[]';
        }

        result = string(abi.encodePacked('[', xatransactionStepToJson(xaTransactionSteps[getXATransactionStepKey(_transactionID, _seqs[0])], _seqs[0])));
        for(uint256 i = 1; i < _len; i++) {
            result = string(abi.encodePacked(result, ',', xatransactionStepToJson(xaTransactionSteps[getXATransactionStepKey(_transactionID, _seqs[i])], _seqs[i])));
        }

        return string(abi.encodePacked(result, ']'));
    }

    /*
    {
        "xaTransactionSeq": 233,
        "accountIdentity":"0x12",
		"path": "a.b.c1",
		"timestamp": 233,
		"method": "set",
		"args": "0010101"
	}
    */
    function xatransactionStepToJson(XATransactionStep memory _xaTransactionStep, uint256 _XATransactionSeq) internal pure
    returns(string memory)
    {
        return string(abi.encodePacked('{"xaTransactionSeq":', uint256ToString(_XATransactionSeq), ',',
            '"accountIdentity":"', _xaTransactionStep.accountIdentity, '",',
            '"path":"', _xaTransactionStep.path, '",',
            '"timestamp":', uint256ToString(_xaTransactionStep.timestamp), ',',
            '"method":"', getMethodFromFunc(_xaTransactionStep.func), '",',
            '"args":"', bytesToHexString(_xaTransactionStep.args), '"}')
        );
    }

    function isValidXATransactionSep(string memory _xaTransactionID, uint256 _XATransactionSeq) internal view
    returns(bool)
    {
        uint256 index = xaTransactions[_xaTransactionID].stepNum;
        return (index == 0) || (_XATransactionSeq > xaTransactions[_xaTransactionID].seqs[index-1]);
    }
}
