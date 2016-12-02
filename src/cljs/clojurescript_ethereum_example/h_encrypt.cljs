(ns clojurescript-ethereum-example.h-encrypt
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
 :server/fetch-key
 interceptors
 (fn [{:keys [db]} [dealer id customer?]]
   (console :log "fetch:" dealer id)
   {:http-xhrio {:method          :get
                 :uri             (str "/key/" dealer "/" id)
                 :timeout         6000
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      (if customer?
                                    [:customer-key-result [:enquiry :key]]
                                    [:dealer-key-result])
                 :on-failure      [:log-error]}}))

(reg-event-db
 :customer-key-result
 (fn [db [_ x result]]
   (console :log "http-c-result(KEY):" (if-let [y (:key result)]
                                         y
                                         "cannot find!"))
   (assoc-in db x (:key result))))

(reg-event-db
 :dealer-key-result
 (fn [db [_ result]]
   (console :log "http-d-result(KEY):" (if-let [x (:key result)]
                                         x
                                         "cannot find!")) 
   (assoc-in db [:tweets] (into [] (map (fn [x]
                                          ;; (console :log "mapped:" x)
                                          (merge (dissoc x :text)
                                                 (if (> (.-length (:text x)) 40) ;; NOT GOOD
                                                   (let [rawHash
                                                         (reader/read-string
                                                          (u/getDecrypted (:key result) (:text x)))]
                                                     (console :log "DECODED:" rawHash)
                                                     {:text [ui/paper {:style {:padding "5px 10px 10px"}}
                                                             [:div "CAR_NAME: " (:name rawHash)]
                                                             [:div "PRICE: " (:price rawHash)]
                                                             [:div "MESSAGE: "  (:text rawHash)]
                                                             ]})
                                                   {:text (:text x)}
                                                   )
                                                 ))
                                        (:tweets db) )))

   ))
