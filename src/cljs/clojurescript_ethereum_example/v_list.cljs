(ns clojurescript-ethereum-example.v-list
  (:require
   [re-frame.core :refer [dispatch subscribe]]
   [reagent.core :as r]
   [reagent.core :as r]
   [clojurescript-ethereum-example.address-select-field :refer [address-select-field]]
   [cljs-react-material-ui.reagent :as ui]
   [cljs-react-material-ui.core :refer [get-mui-theme color]]
   [clojurescript-ethereum-example.utils :as u]))

(def col (r/adapt-react-class js/ReactFlexboxGrid.Col))
(def row (r/adapt-react-class js/ReactFlexboxGrid.Row))

(defn enquery-component []
  (let [enquery (subscribe [:db/enquery])]
    (fn []
      [ui/dialog {;; :title "test dialog"
                  :open  (:open @enquery)
                  :modal true}
       (:lead-text @enquery)
       [ui/text-field {:default-value       "test" ;;(:name @new-tweet)
                       :on-change           #(dispatch [:enquery/update (u/evt-val %)])
                       :name                "name"
                       ;; :max-length          (:max-name-length @settings)
                       :floating-label-text "Message to dealer"
                       :style               {:width "100%"}}]
       [:div {:style {:text-align "right"}}
        [ui/flat-button {:label        "Submit"
                         :primary      true
                         :on-touch-tap #(dispatch [:enquery/send])}] ]
       ]
      ))
  )

(defn list-component []
  (let [cars (subscribe [:db/cars])]
    (fn []
      [row
       [col {:xs 12 :sm 12 :md 10 :lg 6 :md-offset 1 :lg-offset 3}
        [ui/paper {:style {:padding 20 :margin-top 15}}
         [:h1 "cars"]
         (for [{:keys [id name price image dealer]} @cars]
           [:div {:style {:margin-top 20
                          :height     120}
                  :key   id}
            [:img {:src    image
                   :height 110}]
            [ui/raised-button
             {:secondary    true
              :label        "Enquery"
              :style        {:margin-top 15
                             :float      "right"}
              :on-touch-tap #(do
                               ;;(println ":;::" id)
                               (dispatch [:ui/enquery id name price dealer]))
              }]
            [:div {:style {:float         "right"
                           :padding-right 80}}
             [:h3 "CAR_ID: " id]
             [:h3 "CAR_NAME: " name]
             [:h3 "PRICE: " price]
             [:h3 "DEALER: " dealer]]
            [ui/divider]])]]])))
