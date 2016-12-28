(ns clojurescript-ethereum-example.core
  (:require [clojure.java.io :as io]
            [compojure.core :refer [ANY GET PUT POST DELETE defroutes]]
            [compojure.route :refer [resources]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [environ.core :refer [env]]
            [cheshire.core :as json]
            [org.httpkit.server :refer [run-server]])
  (:gen-class))

(def ^:dynamic *server*)

;; For browser
(def users {(clojure.string/lower-case "0x39c4B70174041AB054f7CDb188d270Cc56D90da8")
            {:role "RTC"}
            (clojure.string/lower-case "0x043b8174e15217f187De5629d219e78207f63DCE")
            {:role "DEALER"
             :name "DEALER_NAME_01"
             :key  "key01dealer"}
            (clojure.string/lower-case "0x78348AA884Cb4b4619514e728631742AE8Dd9927")
            {:role "DEALER"
             :name "DEALER_NAME_02"
             :key  "key02dealer"}
            (clojure.string/lower-case "0x81e94fBd99290EF5d5E9df9A041a8B8DebdA13E3")
            {:role "CUSTOMER"
             :name "CUSTOMER_NAME_02"}
            })

(defroutes routes

  (resources "/browser-solidity/" {:root "browser-solidity"})
  
  (resources "/images/" {:root "images"})

  ;; DEALER KEY
  (GET "/key/:id/:num" [raw_id num];; "/dealers/" isnt dealt with.
       (let [id (clojure.string/lower-case raw_id)]
         {:status  200
          :headers {"Content-Type" "application/json"}
          :body    (json/generate-string
                    (if (nil? (and id (get users id)))
                      {}
                      (dissoc (get users id) :role :name)))
          }))
  
  ;; DEALER/CUSTOMER INFO
  (GET "/users/:raw_id" [raw_id];; "/dealers/" isnt dealt with.
       (let [id (clojure.string/lower-case raw_id)]
         (println "dealers: " id)
         {:status  200
          :headers {"Content-Type" "application/json"}
          :body    (json/generate-string
                    (if (nil? (and id (get users id)))
                      {}
                      (dissoc (get users id) :key)))
          }))

  (GET "/js/*" _
       {:status 404})

  ;; FOR GETTING ENV MAINLY FOR CLJS
  (GET "/env/" _
       (if-let [key (env :recruit)]
         {:status  200
          :headers {"Content-Type" "application/json"}
          :body    (json/generate-string {:recruit   (env :recruit)
                                          :etherscan (env :etherscan)
                                          })}
         {:status 404})) ;; DEBUG

  ;; FOR DEV
  (GET "/cors" _
       {:status  200
        :headers {"Content-Type" "text/html; charset=utf-9"
                  "Access-Control-Allow-Origin" "*"}
        :body    (io/input-stream (io/resource "public/index.html"))
        })

  ;; not used while using figwheel
  (GET "/" _
       {:status  200
        :headers {"Content-Type" "text/html; charset=utf-8"
                  "Access-Control-Allow-Origin" "*"}
        :body    (io/input-stream (io/resource "public/index.html"))
        }) 
  )

(def http-handler
  (-> routes
      (wrap-defaults site-defaults)
      wrap-with-logger
      wrap-gzip
      ))

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
