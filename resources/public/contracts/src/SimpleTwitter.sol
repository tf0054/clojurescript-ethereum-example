pragma solidity ^0.4.1;

import "utils.sol";
import "strings.sol";

contract SimpleTwitter {
    using strings for *;
    
    mapping (string => uint) private balances;
    
    // Needed for a contract which can receive ETH.
    // http://ethereum.stackexchange.com/questions/9545/forwarding-contract
    string tmp;
    function () payable {
        tmp = utils.toString(msg.sender);
        addTweet("tf0054(Default)", concat("Funded: ", utils.uintToString(msg.value)));
        balances[concat("0x", tmp)] += msg.value;
    }
    //
    //

    address public developer;
    uint16 public maxTweetLength;
    uint16 public maxNameLength;
    uint16 idxTweets = 0;

    struct Tweet {
        address authorAddress;
        uint date;
    }

    //Tweet[] public tweets;

    event onTweetAdded(address authorAddress, string name, string text, uint date, uint tweetKey);

    modifier onlyDeveloper() {
        if (msg.sender != developer) throw;
        _;
    }

    function SimpleTwitter() {
        maxTweetLength = 140;
        maxNameLength = 20;
        developer = msg.sender;
    }
    
    // using strings.sol
    function concat(string s1, string s2) returns(string) {
        return(s1.toSlice().concat(s2.toSlice()));
    }
    
    function chkBalance(address x, string y) returns(string){
        tmp = utils.toString(msg.sender);
        if(getBalance(concat("0x", tmp)) > 0){
                return (concat(concat(concat(y," (PAYED: "), tmp), " )"));
            }else{
                return (y);
            }
    }
    
    string ctweet;
    function addTweet(string name, string text) {
        if (name.toSlice().len() > maxNameLength) throw;
        if (text.toSlice().len() > maxTweetLength) throw;
        
        ctweet = chkBalance(msg.sender, text);
        
        //tweets.push(Tweet(msg.sender, now));
        onTweetAdded(msg.sender, name, ctweet, now, idxTweets++);
    }

    function getSettings() constant returns(uint16, uint16) {
        return (maxNameLength, maxTweetLength);
    }

    function setSettings(uint16 _maxNameLength, uint16 _maxTweetLength) onlyDeveloper {
        maxNameLength = _maxNameLength;
        maxTweetLength = _maxTweetLength;
    }

    function getTweetsNum() constant returns(uint16) {
        return //uint16(tweets.length);
        idxTweets;
    }
    
    function getBalance(string x) constant returns(uint) {
        return balances[x];
    }
    
}
