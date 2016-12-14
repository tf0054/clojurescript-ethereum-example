(ns clojurescript-ethereum-example.h-list
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

(reg-event-db
 :ui/enquiry
 (fn [db [_ id name price dealer]]
   (console :log "hendler:ui/enquiry" (get-in db [:enquiry :open]) id name price dealer)
   (dispatch [:server/fetch-key dealer id true])
   (-> db
       (assoc-in [:enquiry :open] true)
       (assoc-in [:enquiry :id] id)
       (assoc-in [:enquiry :name] name)
       (assoc-in [:enquiry :price] price)
       (assoc-in [:enquiry :dealer] dealer))
   ))

(reg-event-db
 :enquiry/update
 interceptors
 (fn [db [value]]
   (assoc-in db [:enquiry :text] value)))

(reg-event-fx
 :enquiry/send
 interceptors
 (fn [{:keys [db]} []]
   (console :log "handler:enquiry/send"
            (get-in db [:enquiry :id]))
   (console :log "send db: " db)
   (let [address (get-in db [:new-tweet :address])
         strClj  (pr-str (dissoc (:enquiry db) :open :lead-text :dealer :key))
         strEnc  (u/getEncrypted (get-in db [:enquiry :key]) strClj)]
     ;; a json with id.. would be a encrypted sencente. 
     (console :log "sending data:" strClj "->"  strEnc)
     ;; after sending it as Tx, "(assoc-in [:enquery :text] nil)" should be done in confirmed callback.
     {:db (assoc-in db [:enquiry :open] false)
      :web3-fx.contract/state-fn
      {:instance (:instance (:contract db))
       :web3     (:web3 db)
       :db-path  [:contract :send-tweet]
       :fn       [:add-tweet (str/lower-case (get-in db [:enquiry :dealer])) strEnc
                  {:from address
                   :gas  tweet-gas-limit}
                  :enquiry/received
                  :log-error
                  :enquiry/transaction-receipt-loaded]}
      }
     )))

(reg-event-db
 :enquiry/received
 interceptors
 (fn [db [transaction-hash]]
   (console :log "Enquiry was confirmed! on" transaction-hash)
   (assoc-in db [:enquiry :text] "")
   ))

(reg-event-db
 :enquiry/transaction-receipt-loaded
 interceptors
 (fn [db [{:keys [gas-used] :as transaction-receipt}]]
   (console :log "Enquiry was mined! like" transaction-receipt)
   (when (= gas-used tweet-gas-limit)
     (console :error "All gas used"))
   db
   ))
