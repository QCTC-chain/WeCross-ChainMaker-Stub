// SPDX-License-Identifier: GPL-3.0
pragma solidity >=0.5.0 <0.6.0;
pragma experimental ABIEncoderV2;

contract HelloWeCross {
    string name = "hello wecross!";

    function get() public view returns (string memory) {
        return name;
    }

    function set(string memory n) public {
        name = n;
    }
}