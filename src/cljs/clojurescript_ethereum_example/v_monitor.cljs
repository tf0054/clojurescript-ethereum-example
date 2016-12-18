(ns clojurescript-ethereum-example.v-monitor
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

(defn- mkAHref [uri x]
  [:a {:href (str uri x)
       :target "_blank"} x] )

(defn component0 []
  (let [monitor (subscribe [:db/monitor])]
    (fn []
      [row
       [col {:xs 12 :sm 12 :md 10 :lg 6 :md-offset 1 :lg-offset 3}
        [ui/paper {:style {:padding 20 :margin-top 15  :margin-bottom 15}}
         ;;{:style {:padding "0 20px 20px"}}
         ;;(pr-str (:graph @monitor) "***" (clj->js (:graph @monitor)) )
         [:h4 {:style {:text-align "right"}} "THE LATEST BLOCK: " (mkAHref
                                                                  "https://testnet.etherscan.io/tx/"
                                                                  (:latest-block @monitor))
          " (processed " (:sec-old @monitor) " sec ago)" [:br]
          (get-in @monitor [:graph :tmp-count]) " Tx found."]

         ;; Num of Tx GRAPH
         [:div {:style {:text-align "center"}}
          [:div {:id    "myDiv"
                 :style {;;:text-align "center"
                         :display "inline-block"
                         :width   800
                         :height  200}} ;;"GETTING DATA FROM TESTNET.."
           ]
          [:br]
          [ui/text-field {:value               (:tmp @monitor)
                          :on-change           #(dispatch [:dev/tmp-target (u/evt-val %)])
                          :name                "addr"
                          :floating-label-text "Add filters by address (ex. 0x39c4...)"
                          :style               {:display "inline-block"
                                                :width "100%"
                                                }}]
          [ui/raised-button
           {:secondary    true
            :label        "Add a filter"
            ;;:style        {:margin-top 15}
            :on-touch-tap #(dispatch [:dev/add-target])
            }] ]
         ;;
         [ui/divider {:style {:margin-top 10}}]
         (doall (map (fn [key]
                       [:div {:key (name key)}
                        [ui/table
                         ;; HEADER
                         [ui/table-header {:adjust-for-checkbox false
                                           :display-select-all false}
                          [ui/table-row
                           [ui/table-header-column {:col-span 5
                                                    :style {:padding-left 0
                                                            :font-size 14}}
                            [:span {:style {:font-family ["Lekton" "monospace"]}}
                             [:span {:style {:font-size "1.7em"}} "TO: "]
                             (let [x (name key)
                                   uri "https://testnet.etherscan.io/address/"]
                               [:a {:href   (str uri x)
                                    :target "_blank"
                                    }
                                (subs x 0 2)
                                [:span {:style {:font-size "1.7em"}} (subs x 2 6)]
                                (subs x 6)
                                ])]
                            ]
                           ]
                          [ui/table-row
                           [ui/table-header-column {:style {:text-align "center"}} "From"]
                           [ui/table-header-column {:style {:text-align "center"}} "TxHash"]
                           [ui/table-header-column {:style {:text-align "center"}} "Block"]
                           [ui/table-header-column {:style {:text-align "center"}} "Time"]
                           [ui/table-header-column {:style {:text-align "center"}} "Value (wei)"]

                           ]]
                         ;; BODY
                         [ui/table-body {:display-row-checkbox false}
                          (doall (map (fn [tx]
                                        (let [{:keys [num from to hash value data time]} tx]
                                          [ui/table-row {:key hash}
                                           [ui/table-row-column {:style {:font-family ["Lekton" "monospace"]}}
                                            (mkAHref "https://testnet.etherscan.io/address/" from)]
                                           [ui/table-row-column {:style {:font-family ["Lekton" "monospace"]}}
                                            (mkAHref "https://testnet.etherscan.io/tx/" hash)]
                                           [ui/table-row-column {:style {:text-align "center"
                                                                         :font-color "black"}}
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
         
         (comment
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
          [:br])

         [:div {:style {:text-align "right"}} "Curious Technologies GmbH."]

         ]]])))
