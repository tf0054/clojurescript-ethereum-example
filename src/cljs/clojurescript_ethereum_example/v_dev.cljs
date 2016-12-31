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
         ]]])))

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
         ;; (pr-str @monitor)
         ;; [:br]
         (doall (map #(let [x (get (:found @monitor) %)
                            {:keys [num from to value data time]} x]
                  [:div {:style {:margin-top 20
                                 ;;:height     120
                                 }
                         :key   (rand-int 1000)}
                   [:h3 {:style {:padding 10}} "TO: "
                    (mkAHref "https://testnet.etherscan.io/address/" to)]
                   [:div {:style {:margin-left 20}}
                    [:h3 "FROM: "
                     (mkAHref "https://testnet.etherscan.io/address/" from)]
                    [:h3 "VALUE: " value]
                    [:h3 "DATA: " (if (= "" data)
                                    "-"
                                    (str (subs (str data) 0 16) "..."))]
                    ;; https://testnet.etherscan.io/block/169524
                    [:h3 "BLOCK_NUMBER: "
                     (mkAHref "https://testnet.etherscan.io/block/" num)]
                    [:h3 "TIME: "
                     (if (nil? time)
                       "-"
                       (.replace (.toISOString (u/epochSecToDate time)) "T" " "))]]
                   [ui/divider]]
                  ) (keys (:found @monitor))))
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
