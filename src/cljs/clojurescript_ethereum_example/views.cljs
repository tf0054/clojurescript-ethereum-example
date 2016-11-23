(ns clojurescript-ethereum-example.views
  (:require
   [re-frame.core :refer [dispatch subscribe]]
   [reagent.core :as r]
   [clojurescript-ethereum-example.address-select-field :refer [address-select-field]]
   [cljs-react-material-ui.reagent :as ui]
   [cljs-react-material-ui.core :refer [get-mui-theme color]]
   [clojurescript-ethereum-example.v-twitter :as v_twitter]
   [clojurescript-ethereum-example.utils :as u]))

(def col (r/adapt-react-class js/ReactFlexboxGrid.Col))
(def row (r/adapt-react-class js/ReactFlexboxGrid.Row))

(defn- drawer-component []
  (let [drawer (subscribe [:db/drawer])]
    (fn []
      [ui/drawer {:docked false
                  :open   (:open @drawer)
                  }
       [ui/menu-item {:onTouchTap #(dispatch [:ui/drawer])} "A"]
       [ui/menu-item {:onTouchTap #(dispatch [:ui/page 1])} "B"]
       [ui/menu-item {:onTouchTap #(dispatch [:ui/page 0])} "C"]
       ]
      )
    )
  )

(defn- display [x y]
  (println "display" x y)
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
        [ui/app-bar {:title                    "Simple Decentralized Twitter"
                     :onLeftIconButtonTouchTap #(do (dispatch [:ui/drawer])
                                                    ;; (println "LeftNav")
                                                    )
                     }]
        [drawer-component]
        
        [:div (display @page 0)
         [v_twitter/new-tweet-component]
         [v_twitter/tweets-component]
         ]
        
        ]])))

