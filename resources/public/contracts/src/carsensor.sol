pragma solidity ^0.4.6;

contract carsensor {
    struct Enquiry {
        address from;
        address to;
        string encryptedMessage;
        uint registerDate;
    }
    struct Dealer {
        Enquiry[] enquiries;
        uint enquiryCount;
        string name ;
        address ethAccount;
        bool isPayed;
        uint payedAmount;
    }
    Dealer templateDealer;
    
    mapping (address => Dealer) public dealers;
    
    address rAddress = 0x3c26ab7c9795d0d0507c05f7d6b1f13fe1f56827;
    
    function carsensor() {
        putDealer(rAddress, "Recruit");
    }
    
    function putDealer(address dealerAddress, string name) {
        Dealer d = templateDealer;
        d.enquiryCount = 0;
        d.name = name;
        d.ethAccount = dealerAddress;
        d.isPayed = false;
        d.payedAmount = 0;
        dealers[dealerAddress] = d;
    }
    
    function addEnquiries (address from, address to, string message, uint registerDate) {
        Dealer dealer = dealers[to];
        Enquiry memory e;
        if (dealer.isPayed == false) {
            dealer = dealers[rAddress];
            e = Enquiry({from: from, to: to, encryptedMessage: message, registerDate: registerDate});
            dealer.enquiries.push(e);
            dealer.enquiryCount = dealer.enquiries.length;
            dealers[rAddress] = dealer;
        } else {
            e = Enquiry({from: from, to: to, encryptedMessage: message, registerDate: registerDate});
            dealer.enquiries.push(e);
            dealer.enquiryCount = dealer.enquiries.length;
            dealers[to] = dealer;
        }
    }
    
    function () payable {
        Dealer dealer = dealers[msg.sender];
        dealer.isPayed = true;
        dealers[msg.sender] = dealer;
    }
    
    function getDealerEnquiry(address addr, uint index) constant returns (address, address, string, uint) {
        return (dealers[addr].enquiries[index].from, dealers[addr].enquiries[index].to, dealers[addr].enquiries[index].encryptedMessage, dealers[addr].enquiries[index].registerDate);
    }
    
    function getDealer(address addr) constant returns (uint, string, address, bool, uint) {
        return (dealers[addr].enquiryCount, dealers[addr].name, dealers[addr].ethAccount, dealers[addr].isPayed, dealers[addr].payedAmount);
    }
    
    function kill() {
        suicide(msg.sender);
    }

}
