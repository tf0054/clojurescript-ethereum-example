(ns clojurescript-ethereum-example.v-dev
  (:require
   [clojure.string :as str]
   [re-frame.core :refer [dispatch subscribe]]
   [reagent.core :as r]
   [cljs-react-material-ui.reagent :as ui]
   [cljs-react-material-ui.core :refer [get-mui-theme color]]
   [io.github.theasp.simple-encryption :as se]
   [clojurescript-ethereum-example.utils :as u]))

(def col (r/adapt-react-class js/ReactFlexboxGrid.Col))
(def row (r/adapt-react-class js/ReactFlexboxGrid.Row))

(defn dev-component0 []
  (let [num (subscribe [:db/tweetsNum])]
    (fn []
      [row
       [col {:xs 12 :sm 12 :md 10 :lg 6 :md-offset 1 :lg-offset 3}
        [ui/paper {:style {:padding "0 20px 20px"}}
         [:br]
         @num
         [:br]
         [ui/raised-button
          {:secondary    true
           :label        "Get Num"
           :style        {:margin-top 15}
           :on-touch-tap #(dispatch [:tf0054/getTweetsNum])
           }]
         ]]]
      )))

(comment
  (defn dev-component1 []
   (let [aaddr   (subscribe [:db/devAddr])
         aamount (subscribe [:db/devAmount])
         enc     (subscribe [:db/devEnc])
         ]
     (fn []
       [row
        [col {:xs 12 :sm 12 :md 10 :lg 6 :md-offset 1 :lg-offset 3}
         [ui/paper {:style {:padding "0 20px 20px"}}
          [ui/text-field {:default-value       (:name @aaddr)
                          :on-change           #(dispatch [:ui/AAupdate (u/evt-val %)])
                          :name                "addr"
                          :floating-label-text "Who's amount is interested?"
                          :style               {:width "70%"}}]
          [:br]
          [:h3 "ENC/OUT: \"" @enc "\""]
          [:br]
          [ui/raised-button
           {:secondary    true
            :disabled     (empty? @aaddr)
            :label        "Get ammount"
            :style        {:margin-top 15}
            :on-touch-tap #(dispatch [:tf0054/getAmount @aaddr])
            }]
          [:br]
          [:h3 "Balance: " @aamount] 
          ]]]))))

(defn dev-component2 []
  (let [cars (subscribe [:db/cars])]
    (fn []
      [row
       [col {:xs 12 :sm 12 :md 10 :lg 6 :md-offset 1 :lg-offset 3}
        [ui/paper {:style {:padding "0 20px 20px"}}
         ;;(map #([:div (:id %)]) @cars)
         (pr-str @cars)
         [:br]
         [ui/raised-button
          {:secondary    true
           :label        "Update cars"
           :style        {:margin-top 15}
           :on-touch-tap #(dispatch [:dev/fetch-cars])
           }]
         [:br]         
         ]]])))

(defn- mkAHref [uri x]
  [:a {:href (str uri x)
       :target "_blank"} x] )

(defn dev-component3 []
  (let [monitor (subscribe [:db/monitor])]
    (fn []
      [row
       [col {:xs 12 :sm 12 :md 10 :lg 6 :md-offset 1 :lg-offset 3}
        [ui/paper {:style {:padding "0 20px 20px"}}
         ;;(pr-str (:found @monitor))
         [:h3 (:latest-block @monitor)]
         (doall (map (fn [key]
                       [:div {:key (name key)}
                        [:h4 {:style {:padding 10}}
                         [:i "TO: "
                          (mkAHref "https://testnet.etherscan.io/address/" (name key))]]

                        [ui/table
                         ;; HEADER
                         [ui/table-header {:display-select-all false}
                          [ui/table-row
                           [ui/table-header-column "From"]
                           [ui/table-header-column "TxHash"]
                           [ui/table-header-column "Block"]
                           [ui/table-header-column "Time"]
                           [ui/table-header-column "Value"]
                           ]]
                         ;; BODY
                         [ui/table-body {:display-row-checkbox false}
                          (doall (map (fn [tx]
                                        (let [{:keys [num from to hash value data time]} tx]
                                          [ui/table-row {:key hash}
                                           [ui/table-row-column
                                            (mkAHref "https://testnet.etherscan.io/address/" from)]
                                           [ui/table-row-column
                                            (mkAHref "https://testnet.etherscan.io/tx/" hash)]
                                           [ui/table-row-column {:style {:text-align "center"}}
                                            (mkAHref "https://testnet.etherscan.io/block/" num)]
                                           [ui/table-row-column (u/date4small (u/epochSecToDate time))
                                            ]
                                           [ui/table-row-column {:style {:text-align "right"}}
                                            value]
                                           ]
                                        ))
                                    (get (:found @monitor) key)
                                    ) ) ]
                         ]
                        [ui/divider {:style {:margin-top 5}}] 
                        ])
                     (keys (:found @monitor)) ))
         [:br]
         [ui/text-field {:value               (:tmp @monitor)
                         :on-change           #(dispatch [:dev/tmp-target (u/evt-val %)])
                         :name                "addr"
                         :floating-label-text "Address for monitoring"
                         :style               {:width "70%"
                                               }}]
         [ui/raised-button
          {:secondary    true
           :label        "Add"
           :style        {:margin-top 15}
           :on-touch-tap #(dispatch [:dev/add-target])
           }]
                  [:br]
         [ui/raised-button
          {:secondary    true
           :label        "Connect"
           :style        {:margin-top 15}
           :on-touch-tap #(dispatch [:dev/start-test-filter])
           }]
         [ui/raised-button
          {:secondary    true
           :label        "Disonnect"
           :style        {:margin-left 5
                          :margin-top  15}
           :on-touch-tap #(dispatch [:dev/etherscan-disconnect])
           }]
         [:br]         
         ]]])))

(comment
  (defn tweets-component []
    (let [tweets (subscribe [:db/tweets])]
      (fn []
        [row
         [col {:xs 12 :sm 12 :md 10 :lg 6 :md-offset 1 :lg-offset 3}
          [ui/paper {:style {:padding 20 :margin-top 20}}
           [:h1 "Tweets"]
           (for [{:keys [tweet-key name text date author-address]} @tweets]
             [:div {:style {:margin-top 20}
                    :key   tweet-key}
              [:h3 name]
              [:h5 (u/format-date date)]
              [:div {:style {:margin-top 5}}
               text]
              [:h3 {:style {:margin "5px 0 10px"}}
               author-address]
              [ui/divider]])]]]))))
