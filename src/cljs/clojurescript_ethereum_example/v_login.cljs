(ns clojurescript-ethereum-example.v-login
  (:require
   [re-frame.core :refer [dispatch subscribe]]
   [reagent.core :as r]
   [cljs-react-material-ui.reagent :as ui]
   [cljs-react-material-ui.core :refer [get-mui-theme color]]
   [clojurescript-ethereum-example.utils :as u]))

(def col (r/adapt-react-class js/ReactFlexboxGrid.Col))
(def row (r/adapt-react-class js/ReactFlexboxGrid.Row))

(defn login-component []
  [row
   [col {:xs 12 :sm 12 :md 10 :lg 6 :md-offset 1 :lg-offset 3}
    [ui/paper {:style {:margin-top "15px"
                       :padding    20
                       }}
     [:h3 "Email"]
     [ui/text-field {:default-value       ""
                     :on-change           #(dispatch [:ui/loginEmailUpdate (u/evt-val %)])
                     :name                "email"
                     :floating-label-text "Email"
                     :style               {:width "100%"}}]
     [:br]
     [:h3 "Password"]
     [ui/text-field {:default-value       ""
                     :on-change           #(dispatch [:ui/loginPasswordUpdate (u/evt-val %)])
                     :name                "password"
                     :floating-label-text "Password"
                     :style               {:width "100%"}}]
     [:br]
     [ui/raised-button
      {:secondary    true
       :label        "Login"
       :style        {:margin-top 15}
       :on-touch-tap #(dispatch [:ui/login])}]]]])
