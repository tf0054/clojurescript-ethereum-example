(ns clojurescript-ethereum-example.h-login
  (:require
   [clojure.string :as str]
   [cljs.reader :as reader]
   [ajax.core :as ajax]
   [cljs-web3.core :as web3]
   [cljs-web3.eth :as web3-eth]
   [cljs-web3.personal :as web3-personal]
   [cljsjs.web3]
   [clojurescript-ethereum-example.db :as db]
   [day8.re-frame.http-fx]
   [re-frame.core :refer [dispatch subscribe]]
   [cljs-react-material-ui.reagent :as ui]
   [goog.string :as gstring]
   [goog.string.format]
   [madvas.re-frame.web3-fx]
   [hodgepodge.core :refer [session-storage remove-item]]
   [re-frame.core :refer [reg-event-db reg-event-fx path trim-v after debug reg-fx console dispatch]]
   [clojurescript-ethereum-example.utils :as u]
   )
  )

(def interceptors [#_(when ^boolean js/goog.DEBUG debug)
                   trim-v])

(reg-event-db
 :ui/cAddrUpdate
 interceptors
 (fn [db [x]]
   (assoc-in db [:contract :address] x)))

(reg-event-db
 :ui/loginEmailUpdate
 interceptors
 (fn [db [x]]
   (assoc-in db [:login :email] x)))

(reg-event-db
 :ui/loginPasswordUpdate
 interceptors
 (fn [db [x]]
   (assoc-in db [:login :password] x)))

(reg-event-db
 :ui/login
 interceptors
 (fn [db [user]]
   (console :log "type:" (:type user))
   (-> db
       (assoc-in [:login :name] (:name user))
       (assoc :type (:type user))
       (assoc :page 0))))

(reg-event-db
 :ui/logout
 interceptors
 (fn [db]
   (remove-item session-storage "keystore")
   (-> db
       (assoc :type "customer")
       (dissoc :keystore)
       (assoc :page 3))))

(reg-event-db
 :ui/register-type
 interceptors
 (fn [db [type]]
   (assoc db :register-type type)))


(reg-event-db
 :ui/web3
 interceptors
 (fn [db [ks]]
   (let [ks        ks
         provider  (js/HookedWeb3Provider. (clj->js {:host db/rpc-url :transaction_signer ks}))
         web3      (js/Web3.)
         addresses (map #(str "0x" %) (js->clj (.getAddresses ks)))]
     (web3/set-provider web3 provider)
     (set! (.-accounts (.-eth web3)) (.getAddresses ks))
     (set! (.-getAccounts (.-eth web3)) #(.getAddresses ks))
     (-> db
         (assoc :keystore ks)
         (assoc :my-addresses addresses)
         (assoc :web3 web3)
         (assoc :provides-web3? true)))) )
