(ns clojurescript-ethereum-example.core
  (:require [clojure.java.io :as io]
            [compojure.core :refer [ANY GET PUT POST DELETE defroutes]]
            [compojure.route :refer [resources]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [ring.middleware.json :refer [wrap-json-params]]
            [environ.core :refer [env]]
            [cheshire.core :as json]
            [org.httpkit.server :refer [run-server]])
  (:gen-class))

(def ^:dynamic *server*)

(def users (atom {"a@a.a" {:email "a@a.a" :password "password" :key-store nil}
                  "b@b.b" {:email "b@b.b" :password "password" :key-store nil}}))

(defn login-ok?
  [email password]
  (if (and (not (nil? (@users email)))
           (= password (:password (@users email))))
    true
    false))

(defn login [session {email :email password :password  :as params}]
  (if (login-ok? email password)
    {:success true :user (@users email)}
    {:success false}))

(defn reg-keystore
  [session {email :email keystore :keystore}]
  (swap! users assoc-in [email :keystore] keystore))


(defn register
  [session {email :email password :password keystore :keystore :as params}]
  (swap! users assoc email params)
  {:success true :user params})

(defn json-response
  [body & more]
  (let [response {:status  200
                  :headers {"Content-Type" "text/html; charset=utf-8"}
                  :body    (json/generate-string body)}
        session  (first more)]
    (if-not (nil? session)
      (assoc response :session session)
      response)))

(def dealers {(clojure.string/lower-case "0x043b8174e15217f187De5629d219e78207f63DCE")
              {:name "DEALER_A"
               ;; :address "0xaaDC052Ee37f62889064b44F40D271441e18Be6e"
               :key  "key01dealer"}
              (clojure.string/lower-case "0x81e94fBd99290EF5d5E9df9A041a8B8DebdA13E3")
              {:name "DEALER_B"
               ;; :address "0x81e94fBd99290EF5d5E9df9A041a8B8DebdA13E3"
               :key  "key02dealer"}})

(defroutes routes

  (resources "/browser-solidity/" {:root "browser-solidity"})

  (resources "/images/" {:root "images"})

  (POST "/login" {session :session params :params} (json-response (login session params)))
  (POST "/register" {session :session params :params} (json-response (register session params)))

  ;; DEALER KEY
  (GET "/key/:id/:num" [id num];; "/dealers/" isnt dealt with.
       {:status  200
        :headers {"Content-Type" "text/html; charset=utf-8"}
        :body    (json/generate-string
                  (if (nil? (and id (get dealers id)))
                    {}
                    (dissoc (get dealers id) :name :address)))
        })

  ;; DEALER INFO
  (GET "/dealers/:id" [id];; "/dealers/" isnt dealt with.
       (println "dealers: " id)
       {:status  200
        :headers {"Content-Type" "text/html; charset=utf-8"}
        :body    (json/generate-string
                  (if (nil? (and id (get dealers id)))
                    {}
                    (get dealers id)))
        })

  (GET "/js/*" _
       {:status 404})
  (GET "/" _
       {:status  200
        :headers {"Content-Type" "text/html; charset=utf-8"}
        :body    (io/input-stream (io/resource "public/index.html"))}) )

(def http-handler
  (-> routes
      ;; (wrap-defaults site-defaults)
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))
      wrap-with-logger
      wrap-json-params
      wrap-gzip))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 6655))]
    (alter-var-root (var *server*)
                    (constantly (run-server http-handler {:port port :join? false})))))

(defn stop-server []
  (*server*)
  (alter-var-root (var *server*) (constantly nil)))

(defn restart-server []
  (stop-server)
  (-main))

(comment
  (restart-server)
  (-main)
  (stop-server))
