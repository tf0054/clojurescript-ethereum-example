(ns clojurescript-ethereum-example.h-dev
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-react-material-ui.reagent :as ui]
            [cljs-web3.eth :as web3-eth]
            [clojure.set :refer [rename-keys]]
            [clojure.string :as str]
            [re-frame.core :refer [console dispatch reg-event-db reg-event-fx trim-v]]
            [chord.client :refer [ws-ch]]
            [cljs.core.async :refer [chan <! >! put! close!]]
            [clojurescript-ethereum-example.utils :as u]
            )
  (:import goog.net.Jsonp
           goog.Uri))

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
         (assoc-in [:dev :enc] val)) )))

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
                    "?key=" (get-in db [:keys :recruit])
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

(reg-event-db
 :dev/etherscan-loop-listen
 interceptors
 (fn [db _]
   (if-let [ch (get-in db [:monitor :rtc :conn])]
     (go (let [raw (<! ch)
               x (:message (js->clj raw))]
           (console :log "etherscan update:" x "," raw)
           (if (= "subscribe-txlist" (:event x))
             (dispatch [:dev/etherscan-update "rtc" x]))
           (<! (u/timeout 500)) )
         (dispatch [:dev/etherscan-loop-listen]) ))
   db
   ))

(reg-event-db
 :dev/etherscan-loop-ping
 interceptors
 (fn [db _]
   (if-let [ch (get-in db [:monitor :rtc :conn])]
     (go (let [pingObj (clj->js {:event "ping"})]
           (>! ch pingObj)
           (<! (u/timeout (* 20 1000))) )
         (dispatch [:dev/etherscan-loop-ping]) ))
   db
   ))

(reg-event-db
 :dev/etherscan-init
 interceptors
   (fn [db [ch]]
     (go (let [regiObj (clj->js
                        {:event   "txlist"
                         :address (str/lower-case
                                   "0x78348AA884Cb4b4619514e728631742AE8Dd9927")})]
         (>! ch regiObj)) )
     (dispatch [:dev/etherscan-loop-listen])
     (dispatch [:dev/etherscan-loop-ping])
     (assoc-in db [:monitor :rtc :conn] ch)))

(defn filterIds [db x]
  (if (or
       (= x (get-in db [:contract :address]))
       (= x (get-in db [:new-tweet :address])) ;; Metamask
       (= x "0x39c4b70174041ab054f7cdb188d270cc56d90da8") ;; RTC
       (= x "0x043b8174e15217f187de5629d219e78207f63dce") ;; DEALER01
       (= x "0x78348aa884cb4b4619514e728631742ae8dd9927") ;; CUSTOMER01
       )
    true
    false) )

(reg-event-db
 :dev/etherscan-update
 interceptors
 (fn [db [id x]]
   (console :log "start-test-update" id)
   (if (filterIds db (str "0x" id))
     (assoc-in db [:monitor (keyword id) :amount] x)
     (do (console :log "Filtered Tx:" id)
         db))
   ))

(reg-event-db
 :dev/etherscan-connect
 interceptors
 (fn [db _]
   (go
     (console :log "dev/etherscan-connect:")
     (let [x     (<! (ws-ch "ws://socket.etherscan.io/wshandler"
                            ;;"ws://socket.testnet.etherscan.io/wshandler"
                            {:format :json-kw}))
           ch    (:ws-channel x)
           error (:error x)]
       (if-not error
         (dispatch [:dev/etherscan-init ch])
         (js/console.log "Error:" (pr-str error)))) )
   db))

(reg-event-db
 :dev/etherscan-disconnect
 interceptors
 (fn [db _]
   ;; stompClient
   (.disconnect (get-in db [:monitor :conn]))
   ;; maintain db
   (-> db
       (assoc-in [:monitor :conn] nil)
       ;;(assoc-in [:monitor :rtc :amount] nil)
       )
   ))

(defn callbackTx [msg]
  (let [w (.-body msg)
        y (js->clj (.parse js/JSON w)
                   :keywordize-keys true)]
    ;; cannot have filters here bc here is a inside callback and having no fresh db
    (dispatch [:dev/etherscan-update (:to y) y])) )

(reg-event-db
 :dev/start-test-filter
 interceptors
 (fn [db _]
   (console :log "start-test-filter")
   (let [socket (new js/SockJS "http://localhost:8080/websocket")
         stompClient (.over js/Stomp socket)]

     (set! (.-onopen socket) #(console :log "socketjs open."))
     (set! (.-onmessage socket) #(console :log (str "socketjs msg:" %)))
     (set! (.-onclose socket) #(console :log "socketjs close."))

     (.connect stompClient (clj->js {})
               (fn [frame]
                 (.subscribe stompClient "/topic/tx" callbackTx)) )
     ;;
     (assoc-in db [:monitor :conn] stompClient) )))
