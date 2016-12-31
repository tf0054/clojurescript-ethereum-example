(ns clojurescript-ethereum-example.v-twitter
  (:require
   [re-frame.core :refer [dispatch subscribe]]
   [reagent.core :as r]
   ;; [clojurescript-ethereum-example.address-select-field :refer [address-select-field]]
   [cljs-react-material-ui.reagent :as ui]
   [cljs-react-material-ui.core :refer [get-mui-theme color]]
   [clojurescript-ethereum-example.utils :as u]))

(def col (r/adapt-react-class js/ReactFlexboxGrid.Col))
(def row (r/adapt-react-class js/ReactFlexboxGrid.Row))

(defn new-tweet-component []
  (let [abi          (subscribe [:db/contractAbi])
        caddr        (subscribe [:db/contractAddr])
        settings     (subscribe [:db/settings])
        new-tweet    (subscribe [:db/new-tweet])
        my-addresses (subscribe [:db/my-addresses])
        balance      (subscribe [:new-tweet/selected-address-balance])]
    (fn []
      [row
       [col {:xs 12 :sm 12 :md 10 :lg 6 :md-offset 1 :lg-offset 3}
        [ui/paper {:style {:margin-top "15px"
                           :padding    20
                           ;;:padding    "0 20px 20px"
                           }}
         [:h2 "Connection"]
         ;; [ui/text-field {:default-value       (:name @new-tweet)
         ;;                 :on-change           #(dispatch [:new-tweet/update :name (u/evt-val %)])
         ;;                 :name                "name"
         ;;                 ;; :max-length          (:max-name-length @settings)
         ;;                 :floating-label-text "Recipient address (0x...)"
         ;;                 :style               {:width "90%"}}]
         ;; [:br]
         ;; [ui/text-field {:default-value       (:text @new-tweet)
         ;;                 :on-change           #(dispatch [:new-tweet/update :text (u/evt-val %)])
         ;;                 :name                "tweet"
         ;;                 :max-length          (:max-tweet-length @settings)
         ;;                 :floating-label-text "What's happening?"
         ;;                 :style               {:width "100%"}}]
         ;; [:br]
         ;;
         ;; [address-select-field
         ;;  @my-addresses
         ;;  (:address @new-tweet)
         ;;  [:new-tweet/update :address]]
         [ui/text-field {:default-value       (:address @new-tweet)
                         :on-change           #(dispatch [:ui/cAddrUpdate (u/evt-val %)])
                         :name                "MyAddr"
                         ;; :max-length       (:max-tweet-length @settings)
                         :floating-label-text "Your account (address)"
                         :style               {:width "100%"}}]
         [:br]
         [:h3 "Balance: " (u/eth @balance)]
         ;;[:br]

         [ui/text-field {:default-value       @caddr
                         :on-change           #(dispatch [:ui/cAddrUpdate (u/evt-val %)])
                         :name                "ContractAddr"
                         ;; :max-length       (:max-tweet-length @settings)
                         :floating-label-text "Where is your contract at?"
                         :style               {:width "100%"}}]
         [:br]
         ;; [:h3 "Current contract address: " @caddr]
         [ui/raised-button
          {:secondary    true
           ;; :disabled     (or (empty? (:text @new-tweet))
           ;;                   (empty? (:name @new-tweet))
           ;;                   (empty? (:address @new-tweet))
           ;;                   (:sending? @new-tweet))
           :label        "Update addr"
           :style        {:margin-top 15}
           :on-touch-tap ;; # (dispatch [:ui/cInstUpdate])
           #(dispatch [:contract/abi-loaded @abi])
           }]
         ;; [ui/raised-button
         ;;  {:secondary    true
         ;;   :disabled     (or (empty? (:text @new-tweet))
         ;;                     (empty? (:name @new-tweet))
         ;;                     (empty? (:address @new-tweet))
         ;;                     (:sending? @new-tweet))
         ;;   :label        "Tweet"
         ;;   :style        {:margin-top 15}
         ;;   :on-touch-tap #(dispatch [:new-tweet/send])
         ;;   }]]
         ]]])))

(defn tweets-component []
  (let [tweets  (subscribe [:db/tweets])
        myaddrs (subscribe [:db/my-addresses])]
    (fn []
      [row
       [col {:xs 12 :sm 12 :md 10 :lg 6 :md-offset 1 :lg-offset 3}
        [ui/paper {:style {:padding 20 :margin-top 20}}
         [:h2 "Messages"]
         (for [{:keys [tweet-key name text date author-address]} @tweets]
           [:div {:style {:margin-top 20}
                  :key   tweet-key}
            [:h5 [:i "Date: "(u/format-date date)]]
            [:h5 [:i "Tx: " author-address " -> " name]]
            [:div {:style {:margin-top 5
                           :word-break "break-all"}
                   :width 500}
             text]
            [ui/divider {:style {:margin-top 5}}]])
         [ui/raised-button
          {:secondary    true
           :label        "decode msg"
           :style        {:margin-top 15}
           :on-touch-tap #(dispatch [:server/fetch-key (first @myaddrs) "xx" false])
           }]
         ]]])))
