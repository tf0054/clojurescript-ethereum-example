(ns clojurescript-ethereum-example.h-dev
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
   [clojure.set :refer [rename-keys]]
   )
  (:import goog.History
           goog.Uri
           goog.net.Jsonp)
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
   (assoc-in db [:contract :address] x)
   ))

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


(defn- success-handler [x]
  ;;(console :log "CS:" (pr-str x))
  (let [res (js->clj x :keywordize-keys true)]
    (let [resdb (into [] (map #(merge (-> %
                                          (select-keys [:id :model :price])
                                          (rename-keys {:model :name}))
                                      {:image (get-in % [:photo :main :s])}
                                      {:dealer (str/lower-case
                                                (rand-nth
                                                 ["0x043b8174e15217f187De5629d219e78207f63DCE"
                                                  "0x81e94fBd99290EF5d5E9df9A041a8B8DebdA13E3"]
                                                 ))}
                                      {:dealer_name (rand-nth ;; DEBUG
                                                     ["A-SHOP"
                                                      "D-SHOP"]
                                                     )})
                              (get-in res [:results :usedcar])))]
      ;;(console :log "cardb:" (pr-str resdb))
      (dispatch [:dev/update-cars resdb])
      )))

(defn- error-handler [res]
  (console :log (str "API error occured: " res)))

(reg-event-db
 :dev/fetch-cars
 interceptors
 (fn [db _]
   (console :log "dev/fetch-cars:")
   (let [url   (str "https://webservice.recruit.co.jp/carsensor/usedcar/v1/"
                    "?key=916f3b97bf003394"
                    "&pref=" (+ (rand-int 10) 10)
                    "&price_min=100000&order=5"
                    "&count=5&format=jsonp")
         jsonp (goog.net.Jsonp. (Uri. url))]
     (.setRequestTimeout jsonp (* 1000 10))
     (.send jsonp nil success-handler error-handler))
   db
   ))

(reg-event-db
 :dev/update-cars
 interceptors
 (fn [db [x]]
   (assoc-in db [:cars] x)
   ))

