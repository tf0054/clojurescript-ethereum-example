(ns clojurescript-ethereum-example.h-monitor
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
     (go (let [pingObj (clj->js {:event "ping"})
               test (u/timeout (* 20 1000))]
           (>! ch pingObj)
           (<! test) )
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

(reg-event-db
 :dev/update-latest
 interceptors
 (fn [db [x]]
   (if (= (:num x) -1)
     (assoc-in db [:monitor :sec-old] 0)
     (-> db
         (assoc-in [:monitor :latest-block] (:num x))
         (assoc-in [:monitor :sec-old] 0)) )
   ))

(defn filterIds [db x]
   (not-every? false?
                 (map #(= x %) (get-in db [:monitor :targets]))) )

(reg-event-db
 :dev/etherscan-update
 interceptors
 (fn [db [id x]]
   (dispatch [:dev/update-latest x])
   (let [tmpCnt (+ 1 (get-in db [:monitor :graph :tmp-count]))]     
     (if (filterIds db id)
       (do (console :log "etherscan-update" id)
           (let [q (get-in db [:monitor :found (keyword id)])]
             (if (every? false?
                         (map #(= (:hash %) (:hash x)) q))
               (-> db
                   (assoc-in [:monitor :found (keyword id)] (if (< (count q) 3)
                                                              (conj q x)
                                                              (conj (pop q) x) ))
                   (assoc-in [:monitor :txhash] (:hash x))
                   (assoc-in [:monitor :graph :tmp-count] tmpCnt)
                   )
               (do
                 (console :log "dup:" (:hash x))
                 (assoc-in db [:monitor :graph :tmp-count] tmpCnt) ))))
       (do (console :log "etherscan-update" id "(Ignored)")
           (assoc-in db [:monitor :graph :tmp-count] tmpCnt) ))
     )))

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
       (assoc-in [:monitor :conn] nil))
   ))

(defn connectTx [strUrl regObj]
  (let [socket (new js/SockJS strUrl)
        stompClient (.over js/Stomp socket)]
    (.connect stompClient
              ;; headers for stomp
              (clj->js {})
              ;; success callback
              (fn [frame] (.subscribe stompClient (:uri regObj) (:callback regObj)) )
              ;; error callback
              (fn [] (go (<! (u/timeout 1500))
                         (console :log "(Reconnect in 3000 ms)")
                         (<! (u/timeout 1500))
                         (connectTx strUrl regObj))) )
    stompClient))

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
  (if (nil? (get-in db [:monitor :sec-old]))
    (do
      (dispatch [:dev/sec-tick])
      (let [data [{:x (get-in db [:monitor :graph :x])
                   :y (get-in db [:monitor :graph :y])
                   :type "scatter"}]]
        (.newPlot js/Plotly "myDiv" (clj->js data)) )
      ))
   (let [stompClient (connectTx "http://localhost:8080/websocket"
                                {:uri "/topic/tx" :callback callbackTx})]
     (-> db
        ;; reset monitor lists with target addresses
         (assoc-in [:monitor :found]
                   (reduce merge (map #(hash-map (keyword %)
                                                 cljs.core/PersistentQueue.EMPTY
                                                 ;;{:to %}
                                                 )
                                      (get-in db [:monitor :targets]) )) )
         (assoc-in [:monitor :conn] stompClient)) )))

(reg-event-db
 :dev/tmp-target
 interceptors
 (fn [db [value]]
   (let [val (str/lower-case (str/lower-case value))]
         (assoc-in db [:monitor :tmp] val))))

(reg-event-db
 :dev/sec-tick-g
 interceptors
 (fn [db _]
   (let [limit 30
         y (get-in db [:monitor :graph :tmp-count])
         x (u/date4plotly (new js/Date))
         old (get-in db [:monitor :graph])
         xx (if (< (count (:x old)) limit)
              (conj (:x old) x)
              (conj (pop (:x old)) x) )
         yy (if (< (count (:y old)) limit)
              (conj (:y old) y)
              (conj (pop (:y old)) y) )
         ]
     (console :log "tick-g:" x y)
     (let [data [{:x xx
                  :y yy
                  :type "scatter"}]
           layout {;;:title "TEST GRAPH"
                   :xaxis {;;:title "Time"
                           :rangemode "tozero"}
                   :yaxis {:title "Tx"
                           :rangemode "tozero"}
                   :margin {:t 15 :b 25}
                   }]
       (.newPlot js/Plotly "myDiv" (clj->js data) (clj->js layout)) )
     (-> db
         (assoc-in [:monitor :graph :x] xx)
         (assoc-in [:monitor :graph :y] yy)
         (assoc-in [:monitor :graph :tmp-count] 0))
     )))

(reg-event-db
 :dev/sec-tick
 interceptors
 (fn [db _]
   (go (<! (u/timeout 1000))
       (dispatch [:dev/sec-tick])
       (if (= 0 (rem (quot (new js/Date) 1000) 10))
         (do (console :log "10 grpah")
             (dispatch [:dev/sec-tick-g])) ))
   ;;
   (let [x (+ 1 (get-in db [:monitor :sec-old]))]
     (assoc-in db [:monitor :sec-old] x))
    ))

(reg-event-db
 :dev/add-target
 interceptors
 (fn [db _]
   (let [targets (get-in db [:monitor :targets])
         tmp (get-in db [:monitor :tmp])]
     (if (not (> (.indexOf targets tmp) -1))
       (-> db
           (assoc-in [:monitor :targets] (conj
                                          (get-in db [:monitor :targets])
                                          (get-in db [:monitor :tmp])))
           (assoc-in [:monitor :found (keyword tmp)]
                     cljs.core/PersistentQueue.EMPTY)
           (assoc-in [:monitor :tmp] "")) 
       db
       ))
   )) 

(reg-event-fx
 :dev/changeTab
 interceptors
 (fn [{:keys [db]} [x]]
   (console :log "tab:" x)
   {:db (assoc-in db [:monitor :tab-val] x)}
   ;;   
   ))

(reg-event-db
 :dev/changeView
 interceptors
 (fn [db _]
   (console :log "changeView")
   (case (get-in db [:monitor :display])
     0 (assoc-in db [:monitor :display] 1)
     1 (assoc-in db [:monitor :display] 0))
   ))
