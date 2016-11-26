(ns clojurescript-ethereum-example.subs
  (:require [re-frame.core :refer [reg-sub]]))

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
   ;; (println "contractAddr" (:address (:contract db)))
   ;; (:address (:contract db))
   (get-in db [:contract :address])
   ))
