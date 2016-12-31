(ns clojurescript-ethereum-example.handlers
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

(def tweet-gas-limit 2000000)

(reg-event-fx
 :initialize
 (fn [_ _]
   (merge
    {:db         db/default-db
     :http-xhrio {:method          :get
                  :uri             (gstring/format "./contracts/build/%s.abi"
                                                   (get-in db/default-db [:contract :name]))
                  :timeout         6000
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:contract/abi-loaded]
                  :on-failure      [:log-error]}}
    (when (:provides-web3? db/default-db)
      {:web3-fx.blockchain/fns
       {:web3 (:web3 db/default-db)
        :fns  [[web3-eth/accounts :blockchain/my-addresses-loaded :log-error]]}}))))

(reg-event-fx
 :blockchain/my-addresses-loaded
 interceptors
 (fn [{:keys [db]} [addresses]]
   {:db (-> db
            (assoc :my-addresses addresses)
            (assoc-in [:new-tweet :address] (first addresses)))
    :web3-fx.blockchain/balances
    {:web3                   (:web3 db/default-db)
     :addresses              addresses
     :watch?                 true
     :blockchain-filter-opts "latest"
     :dispatches             [:blockchain/balance-loaded :log-error]}}))

(reg-event-fx
 :contract/abi-loaded
 interceptors
 (fn [{:keys [db]} [abi]]
   (console :log "abi-loaded:" (get-in db [:contract :address]))
   (let [web3              (:web3 db)
         contract-instance (web3-eth/contract-at web3 abi (:address (:contract db)))
         db                (-> db
                               (assoc-in [:contract :abi] abi)
                               (assoc-in [:contract :instance] contract-instance)
                               (assoc-in [:tweets] nil))]
     {:db db
      ;; :web3-fx.contract/events
      ;; {:instance contract-instance
      ;;  :db       db
      ;;  :db-path  [:contract :events]
      ;;  :events   [[:on-tweet-added
      ;;              {}
      ;;              {:from-block 0} :contract/on-tweet-loaded :log-error]]}

      :web3-fx.contract/events
      {:instance contract-instance
       :db       db
       :db-path  [:contract :events2]
       :events   [[:on-tweet-added ;; definition name
                   {:indexed-addr (get-in db [:new-tweet :address])}
                   ;;{}
                   {:from-block 0} :contract/on-tweet-loaded :log-error]]}

      :web3-fx.contract/constant-fns
      {:instance contract-instance
       :fns      [[:get-settings :contract/settings-loaded :log-error]]}})))

(reg-event-db
 :contract/on-tweet-loaded
 interceptors
 (fn [db [tweet]]
   (console :log "contract/on-tweet-loaded:" (.toNumber (:tweet-key tweet)) tweet)
   ;;(dispatch [:server/fetch-key (get-in db [:new-tweet :address]) "xx" false])
   (update db :tweets conj (merge (select-keys tweet [:author-address :text :name])
                                  {:date      (u/big-number->date-time (:date tweet))
                                   :tweet-key (.toNumber (:tweet-key tweet))}))
   ;; (update db :tweets conj (merge (select-keys tweet [:author-address :name])
   ;;                                {:text      (u/getDecrypted (:tmp-key db) (:text tweet))
   ;;                                 :date      (u/big-number->date-time (:date tweet))
   ;;                                 :tweet-key (.toNumber (:tweet-key tweet))}))
   ))

(reg-event-db
 :contract/settings-loaded
 interceptors
 (fn [db [[max-name-length max-tweet-length]]]
   (assoc db :settings {:max-name-length  (.toNumber max-name-length)
                        :max-tweet-length (.toNumber max-tweet-length)})))

(reg-event-db
 :blockchain/balance-loaded
 interceptors
 (fn [db [balance address]]
   (assoc-in db [:accounts address :balance] balance)))

(reg-event-db
 :new-tweet/update
 interceptors
 (fn [db [key value]]
   (assoc-in db [:new-tweet key] value)))

(reg-event-fx
 :new-tweet/send
 interceptors
 (fn [{:keys [db]} []]
   (console :log "Send tweet to a contract at"
            (get-in db [:contract :address]))
   (let [{:keys [name text address]} (:new-tweet db)]
     {:web3-fx.contract/state-fn
      {:instance (:instance (:contract db))
       :web3     (:web3 db)
       :db-path  [:contract :send-tweet]
       :fn       [:add-tweet (str/lower-case (str/lower-case name)) text
                  {;; :value (web3/to-wei 0.02 "ether")
                   :from address
                   :gas  tweet-gas-limit}
                  :new-tweet/confirmed
                  :log-error
                  :new-tweet/transaction-receipt-loaded]}})))

(reg-event-db
 :new-tweet/confirmed
 interceptors
 (fn [db [transaction-hash]]
   (assoc-in db [:new-tweet :sending?] true)
   (assoc-in db [:new-tweet :text] "")
   ))

(reg-event-db
 :new-tweet/transaction-receipt-loaded
 interceptors
 (fn [db [{:keys [gas-used] :as transaction-receipt}]]
   (console :log transaction-receipt)
   (when (= gas-used tweet-gas-limit)
     (console :error "All gas used"))
   (assoc-in db [:new-tweet :sending?] false)))

(reg-event-fx
 :contract/fetch-compiled-code
 interceptors
 (fn [{:keys [db]} [on-success]]
   {:http-xhrio {:method          :get
                 :uri             (gstring/format "/contracts/build/%s.json"
                                                  (get-in db [:contract :name]))
                 :timeout         6000
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      on-success
                 :on-failure      [:log-error]}}))

(reg-event-fx
 :contract/deploy-compiled-code
 interceptors
 (fn [{:keys [db]} [contracts]]
   (let [{:keys [abi bin]} (get-in contracts [:contracts (keyword (:name (:contract db)))])]
     {:web3-fx.blockchain/fns
      {:web3 (:web3 db)
       :fns  [[web3-eth/contract-new
               (js/JSON.parse abi)
               {:gas  4500000
                :data bin
                :from (first (:my-addresses db))}
               :contract/deployed
               :log-error]]}})))

(reg-event-fx
 :blockchain/unlock-account
 interceptors
 (fn [{:keys [db]} [address password]]
   {:web3-fx.blockchain/fns
    {:web3 (:web3 db)
     :fns  [[web3-personal/unlock-account address password 999999
             :blockchain/account-unlocked
             :log-error]]}}))

(reg-event-fx
 :blockchain/account-unlocked
 interceptors
 (fn [{:keys [db]}]
   (console :log "Account was unlocked.")
   {}))

(reg-event-fx
 :contract/deployed
 interceptors
 (fn [_ [contract-instance]]
   (when-let [address (aget contract-instance "address")]
     (console :log "Contract deployed at" address))))

(reg-event-fx
 :log-error
 interceptors
 (fn [_ [err]]
   (console :error err)
   {}))

;; - - - - -

(reg-event-db
 :ui/drawer
 (fn [db]
   (console :log "hendler:ui/drawer" (get-in db [:drawer :open]))
   (if (get-in db [:drawer :open])
     (assoc-in db [:drawer :open] false)
     (assoc-in db [:drawer :open] true) )   
   ))

(reg-event-db
 :ui/page
 interceptors
 (fn [db [x]]
   (console :log "hendler:ui/page" (get-in db [:page]) "->" x)
   (assoc-in db [:page] x)
   ))
