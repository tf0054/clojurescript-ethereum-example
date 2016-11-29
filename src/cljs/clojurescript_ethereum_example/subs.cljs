(ns clojurescript-ethereum-example.subs
  (:require
   [clojure.string :as str]
   [re-frame.core :refer [reg-sub]]
   ) ) 

(reg-sub
 :db/my-addresses
 (fn [db]
   (:my-addresses db)))

(reg-sub
 :db/tweets
 (fn [db]
   (sort-by :date #(compare %2 %1) (:tweets db))))

(reg-sub
 :db/new-tweet
 (fn [db]
   (:new-tweet db)))

(reg-sub
  :db/settings
  (fn [db]
    (:settings db)))

(reg-sub
 :new-tweet/selected-address-balance
 (fn [db]
   (get-in db [:accounts (:address (:new-tweet db)) :balance])))

(reg-sub
 :db/drawer
 (fn [db]
   (:drawer db)))

(reg-sub
 :db/page
 (fn [db]
   (:page db)))

(reg-sub
 :db/tweetsNum
 (fn [db]
   (:tweetsNum db)))

(reg-sub
 :db/contractAddr
 (fn [db]
   (get-in db [:contract :address])
   ))

(reg-sub
 :db/contractAbi
 (fn [db]
   (get-in db [:contract :abi])
   ))

(reg-sub
 :db/devAddr
 (fn [db]
   (get-in db [:dev :address])
   ))

(reg-sub
 :db/devAmount
 (fn [db]
   (get-in db [:dev :amount]) 
   ))

(reg-sub
 :db/devEnc
 (fn [db]
   (get-in db [:dev :enc]) 
   ))
