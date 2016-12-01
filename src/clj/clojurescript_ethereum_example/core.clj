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

(def dealers {"dealer01" {:name    "DEALER_A"
                          :address "0x1"
                          :key     "01dealer"}
              "dealer02" {:name    "DEALER_B"
                          :address "0x2"
                          :key     "02dealer"}
              })

(defroutes routes

  (resources "/browser-solidity/" {:root "browser-solidity"})
  
  (resources "/images/" {:root "images"})

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
      (wrap-defaults site-defaults)
      wrap-with-logger
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
