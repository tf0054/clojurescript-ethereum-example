(ns clojurescript-ethereum-example.views
  (:require
   [re-frame.core :refer [dispatch subscribe]]
   [reagent.core :as r]
   [cljs-react-material-ui.reagent :as ui]
   [cljs-react-material-ui.core :refer [get-mui-theme color]]
   [clojurescript-ethereum-example.v-twitter :as v_twitter]
   [clojurescript-ethereum-example.v-dev :as v_dev]
   [clojurescript-ethereum-example.v-list :as v_list]
   [clojurescript-ethereum-example.v-monitor :as v_monitor]
   [clojurescript-ethereum-example.utils :as u]))

(def col (r/adapt-react-class js/ReactFlexboxGrid.Col))
(def row (r/adapt-react-class js/ReactFlexboxGrid.Row))

(defn- drawer-component []
  (let [drawer (subscribe [:db/drawer])]
    (fn []
      [ui/drawer {:docked false
                  :open   (:open @drawer)
                  }
       [ui/menu-item {:onTouchTap #(dispatch [:ui/drawer])} "-CLOSE-"]
       [ui/menu-item {:onTouchTap #(do
                                     (dispatch [:ui/page 0])
                                     (dispatch [:ui/drawer]))} "Messages (Dealer)"]
       [ui/menu-item {:onTouchTap #(do
                                     (dispatch [:ui/page 2])
                                     (dispatch [:ui/drawer]))} "List (Customer)"]
       [ui/menu-item {:onTouchTap #(do
                                     (dispatch [:ui/page 3])
                                     (dispatch [:ui/drawer])
                                     (dispatch [:dev/start-test-filter]))} "Monitor"]
       [ui/menu-item {:onTouchTap #(do
                                     (dispatch [:ui/page 1])
                                     (dispatch [:ui/drawer]))} "development"]
       ]
      )
    )
  )

(defn- display [x y]
  (if (== x y)
    {:style {:display "block"}}
    {:style {:display "none"}}
    )
  )

(defn main-panel []
  (let [page (subscribe [:db/page])]
    (fn []
      [ui/mui-theme-provider
       {:mui-theme (get-mui-theme {:palette {:primary1-color (color :light-blue500)
                                             :accent1-color  (color :amber700)}})}
       [:div
        (if (not= 3 @page)
          [:div
           [ui/app-bar {:title                    "Blockchain DEMO: \"carsensor.net\""
                        :onLeftIconButtonTouchTap #(dispatch [:ui/drawer])}]          
           [drawer-component] ]
          [:div
           [ui/app-bar {:title                    "Ethereum Tx monitor for ROPSTEN TESTNET"}]
           
           ]
          )
        
        ;; default
        [:div (display @page 0)
         [v_twitter/new-tweet-component]
         [v_twitter/tweets-component]
         ]
        ;; development
        [:div (display @page 1)
         [v_dev/dev-component0]
         ;;[v_dev/dev-component1]
         [v_dev/dev-component2]
         ;;[v_dev/dev-component3]
         ]
        ;; list
        [:div (display @page 2)
         [v_list/enquiry-component]
         [v_list/list-component] 
         ]
        ;; list
        [:div (display @page 3)
         [v_monitor/component0]
         ;;[v_monitor/component1]
         ]

        ]])))

