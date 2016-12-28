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

(defn- mkTxTable [monitor key header]
  [ui/table
   ;; HEADER
   [ui/table-header {:adjust-for-checkbox false
                     :display-select-all false}
    (if header
      [ui/table-row
       [ui/table-header-column {:col-span 5
                                :style {:padding-left 0
                                        :font-size 14}}
        [:span {:style {:font-family ["Lekton" "monospace"]}}
         [:span {:style {:font-size "1.7em" 
                         :color "black"}} "TO:"]
         (let [x (name key)
               uri "https://testnet.etherscan.io/address/"]
           [:a {:href   (str uri x)
                :target "_blank"}
            [:span {:style {:color "black"}} (subs x 0 2)]
            [:span {:style {:font-size "1.7em"
                            :color "black"}} (subs x 2 6)]
            [:span {:style {:color "black"}} (subs x 6)] 
            ])]
        ]
       ]
      )
    (if (empty? (get (:found @monitor) key))
      [ui/table-row
       [ui/table-header-column {:col-span 5
                                :style {:padding-left 0
                                        :font-size 14
                                        :text-align "center"}}
        "NO ITEM FOUND."] ]
      [ui/table-row
       [ui/table-header-column {:style {:text-align "center"}} "From"]
       [ui/table-header-column {:style {:text-align "center"}} "TxHash"]
       [ui/table-header-column {:style {:text-align "center"}} "Block"]
       [ui/table-header-column {:style {:text-align "center"}} "Time"]
       [ui/table-header-column {:style {:text-align "center"}} "Value (wei)"] ]
      )
    ]
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
  )

(defn component0 []
  (let [monitor (subscribe [:db/monitor])]
    (fn []
      [row 
       [col {:xs 12 :sm 12 :md 10 :lg 6 :md-offset 1 :lg-offset 3
             :style {:margin-left "10%"
                     :max-width "80%"
                     :flex-basis "80%"} }
        [ui/paper {:style {:padding 20 :margin-top 15  :margin-bottom 15}}
         ;;{:style {:padding "0 20px 20px"}}
         ;;(pr-str (:graph @monitor) "***" (clj->js (:graph @monitor)) )
         [:h4 {:style {:text-align "right"}}
          "THE LATEST BLOCK: " (mkAHref
                                "https://testnet.etherscan.io/tx/"
                                (:latest-block @monitor))
          " (processed " (:sec-old @monitor) " sec ago)" [:br]
          (get-in @monitor [:graph :tmp-count]) " Tx found in 10 sec."]

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
         [ui/divider {:style {:margin-top 30}}]
         ;; Views
         (case (:display @monitor)
           ;; ONE TABLE
           0 (doall (map (fn [key]
                           [:div {:key (name key)}
                            [ui/raised-button {:label key
                                               :primary true
                                               :style {:height 48}}]
                            (mkTxTable monitor key false)
                            [ui/divider {:style {:margin-top 5}}] ])
                         (keys (:found @monitor)) ))
           ;; TABS
           1 [ui/tabs {:value (:tab-val @monitor)
                       :onChange #(dispatch [:dev/changeTab %])}
              (doall (map (fn [key]
                            [ui/tab {:label (str "To: " (subs (name key) 0 6) "...") 
                                     :value (name key)
                                     :key (name key)}
                             (mkTxTable monitor key false)
                             ] )
                          (keys (:found @monitor)) )) ]
           )
         ;; FOOTER
         [:div {:style {:overflow "auto"}}
          [:div {:style {:float "left"}
                 :onClick #(dispatch [:dev/changeView])} "Change a view"]
          [:div " "]
          [:div {:style {:float "right"}} "Curious Technologies GmbH."]]
         
         ]]]
      )))
