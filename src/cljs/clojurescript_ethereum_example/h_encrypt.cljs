(ns clojurescript-ethereum-example.h-encrypt
  (:require [ajax.core :as ajax]
            [cljs-react-material-ui.reagent :as ui]
            [cljs.reader :as reader]
            [clojurescript-ethereum-example.utils :as u]
            [re-frame.core :refer [console reg-event-db reg-event-fx trim-v]]))

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
