(ns clojurescript-ethereum-example.v-login
  (:require
   [re-frame.core :refer [dispatch subscribe]]
   [reagent.core :as r]
   [cljs-react-material-ui.reagent :as ui]
   [ajax.core :refer [GET POST url-request-format]]
   [cljs-react-material-ui.core :refer [get-mui-theme color]]
   [hodgepodge.core :refer [session-storage get-item set-item]]
   [clojurescript-ethereum-example.utils :as u]))

(def col (r/adapt-react-class js/ReactFlexboxGrid.Col))
(def row (r/adapt-react-class js/ReactFlexboxGrid.Row))


(defn enter-password
  [callback]
  (let [pw (js/prompt "Please Enter Password" "password")]
    (callback nil pw)))

(defn login-success-handler [res]
  (dispatch [:ui/login])
  (let [keystore (.-keystore js/lightwallet)]
    (.createVault keystore
                  (clj->js {:password "aaa"
                            ;; :sheedPhrase "else victory timber thought refuse erosion club oak enact turkey scan garment"
                            })
                  (fn[err ks]
                    (if-not (nil? err) (throw err))
                    (.keyFromPassword ks "aaa"
                                      (fn
                                        [err pw-derived-key]
                                        (if-not (nil? err)
                                          (throw err))
                                        (.generateNewAddress ks pw-derived-key 3)
                                        (.log js/console (.getAddresses ks))
                                        (.log js/console (.serialize ks))
                                        (set! (.-passwordProvider ks) enter-password)
                                        (set-item session-storage "keystore" (.serialize ks))
                                        (dispatch [:ui/keystore ks])
                                        (dispatch [:blockchain/my-addresses-loaded (.getAddresses ks)])
                                        (dispatch [:contract/on-tweet-loaded])
                                        ))))
    (.log js/console res)))

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
                     :type                "password"
                     :style               {:width "100%"}}]
     [:br]
     [ui/raised-button
      {:secondary    true
       :label        "Login"
       :style        {:margin-top 15}
       ;;:on-touch-tap #(dispatch [:ui/login])
       :on-touch-tap (fn []
                       (let [page     (subscribe [:db/page])
                             login    (subscribe [:db/login])]
                         (.log js/console (:email @login))
                         (.log js/console (:password @login))
                         (POST "/login"
                               {:params          {:email    (:email @login)
                                                  :password (:password @login)}
                                :handler         login-success-handler
                                :response-format :json
                                :keywords?       true
                                :format          (url-request-format)})))
       }]]]])
