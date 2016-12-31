(ns clojurescript-ethereum-example.h-init
  (:require [ajax.core :as ajax]
            [cljs-react-material-ui.reagent :as ui]
            [re-frame.core :refer [console reg-event-db reg-event-fx trim-v dispatch]]))

(def interceptors [#_(when ^boolean js/goog.DEBUG debug)
                   trim-v])

(def tweet-gas-limit 2000000)

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

(reg-event-fx
 :server/fetch-userrole
 interceptors
 (fn [{:keys [db]} [id]]
   (console :log "fetch(role):" id)
   {:http-xhrio {:method          :get
                 :uri             (str "/users/" id)
                 :timeout         6000
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:result-userrole]
                 :on-failure      [:log-error]}}))

(reg-event-db
 :result-userrole
 interceptors
 (fn [db [json]]
   (let [x (:role json)]
     (console :log "userrole:" x)
     (dispatch [:server/fetch-env])
     (assoc-in db [:new-tweet :role] x))
   ))
