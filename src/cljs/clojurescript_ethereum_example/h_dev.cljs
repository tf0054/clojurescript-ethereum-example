(ns clojurescript-ethereum-example.h-dev
  (:require
   [clojure.string :as str]
   [cljs.reader :as reader]
   [ajax.core :as ajax]
   [cljs-web3.core :as web3]
   [cljs-web3.eth :as web3-eth]
   [cljs-web3.personal :as web3-personal]
   [cljsjs.web3]
   [ajax.core :refer [GET POST url-request-format]]
   [clojurescript-ethereum-example.db :as db]
   [day8.re-frame.http-fx]
   [re-frame.core :refer [dispatch subscribe]]
   [cljs-react-material-ui.reagent :as ui]
   [goog.string :as gstring]
   [goog.string.format]
   [madvas.re-frame.web3-fx]
   [re-frame.core :refer [reg-event-db reg-event-fx path trim-v after debug reg-fx console dispatch]]
   [clojurescript-ethereum-example.utils :as u]
   )
  )

(def interceptors [#_(when ^boolean js/goog.DEBUG debug)
                   trim-v])

(reg-event-fx
 :tf0054/getTweetsNum
 interceptors
 (fn [{:keys [db]} []]
   (let []
     (console :log "hendler:ui/getTweetsNum")
     {:web3-fx.contract/constant-fns
      {:instance (:instance (:contract db))
       :fns      [[:get-tweets-num ;; have to call with kebab-case (-> GetTweetsNum)
                   :ui/tweetsNum :log-error]]}})))

(reg-event-db
 :ui/tweetsNum
 interceptors
 (fn [db [x]]
   (console :log "hendler:ui/tweetsNum" x)
   (assoc db :tweetsNum (.toNumber x))))

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
 (fn [db]
   (let [page (subscribe [:db/page])]
     (.log js/console db)
     (.log js/console "email")
     (.log js/console (get-in db [:login :email]))
     (.log js/console "password")
     (.log js/console (get-in db [:login :password]))
     (.log js/console "ログイン失敗")
     (POST "/login"
           {:params          {:email    (get-in db [:login :email])
                              :password (get-in db [:login :password])}
            :handler         (fn [res]
                               (assoc db :page 0)
                               (.log js/console res))
            :response-format :json
            :keywords?       true
            :format          (url-request-format)}))))


(reg-event-fx
 :ui/cInstUpdate
 interceptors
 (fn [{:keys [db]} []]
   (console :log "hendler:ui/cInstUpdate" (get-in db [:contract :address]))
   (let [abi       (get-in db [:contract :abi])
         web3      (:web3 db)
         addr      (get-in db [:contract :address])
         cinstance (web3-eth/contract-at web3 abi addr)]
     (println cinstance)
     {:db (assoc-in db [:contract :instance] cinstance)}
     )
   ))

(reg-event-db
 :ui/AAupdate
 interceptors
 (fn [db [value]]
   (let [val (str/lower-case (str/lower-case value))
         ;; encStr (u/getEncrypted (get-in db [:new-tweet :address]) value)
         ]
     (-> db
         (assoc-in [:dev :address] val)
         (assoc-in [:dev :enc]
                   val
                   ;; (str encStr "^ "
                   ;;      (u/getDecrypted
                   ;;       ;;(get-in db [:new-tweet :address])
                   ;;       "tf0054"
                   ;;       encStr) "^ "
                   ;;      (get-in db [:new-tweet :address])
                   ;;      )
                   )))))

(reg-event-db
 :ui/amountNum
 interceptors
 (fn [db [x]]
   (console :log "hendler:ui/amountNum" x)
   (assoc-in db [:dev :amount] (.toNumber x))))

(reg-event-fx
 :tf0054/getAmount
 interceptors
 (fn [{:keys [db]} [x]]
   (let []
     (console :log "hendler:ui/getAmount")
     {:web3-fx.contract/constant-fns
      {:instance (:instance (:contract db))
       :fns      [[:get-Balance x
                   :ui/amountNum :log-error]]}})))
