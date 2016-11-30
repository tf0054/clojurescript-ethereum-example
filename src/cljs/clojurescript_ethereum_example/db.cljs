(ns clojurescript-ethereum-example.db
  (:require [cljs-web3.core :as web3]))

(def default-db
  {:tweets         []
   :filteredTweets []
   :settings       {}
   :my-addresses   []
   :accounts       {}
   :new-tweet      {:text     ""
                    :name     ""
                    :address  nil
                    :sending? false}
   :web3           (or (aget js/window "web3")
                       (if goog.DEBUG
                         (web3/create-web3 "http://localhost:8545/")
                         (web3/create-web3 "https://morden.infura.io/metamask")))
   :provides-web3? (or (aget js/window "web3") goog.DEBUG)
   :contract       {:name     "SimpleTwitter"
                    :abi      nil
                    :bin      nil
                    :instance nil
                    ;; :address  "0x1a962b69f59b6879a0c25874aa86f8f2658aa368"
                    ;; :address  "0x7B51E82Cbeed5732845CFDFe58CFE9099a61d5De"
                    :address  "0x5dF19f207F18f86C7106Fe5835f4075f78A593A8"
                    }
   :drawer         {:open false}
   :page           0
   :tweetsNum      0
   :dev            {:address nil
                    :amount  0
                    :enc     nil
                    }
   ;;
   :cars           [{:id     "car001"
                     :name   "Fiat punto"
                     :price  1000000
                     :image  "/images/car001.jpg"
                     :dealer "0xaaDC052Ee37f62889064b44F40D271441e18Be6e"}
                    {:id     "car002"
                     :name   "Fiat punto"
                     :price  1000000
                     :image  "/images/car002.jpg"
                     :dealer "0xaaDC052Ee37f62889064b44F40D271441e18Be6e"}
                    {:id     "car003"
                     :name   "Fiat punto"
                     :price  1000000
                     :image  "/images/car003.jpg"
                     :dealer "0x81e94fBd99290EF5d5E9df9A041a8B8DebdA13E3 "}
                    {:id     "car004"
                     :name   "Fiat punto"
                     :price  1000000
                     :image  "/images/car004.jpg"
                     :dealer "0x81e94fBd99290EF5d5E9df9A041a8B8DebdA13E3 "}
                    ]
   :user           {:id       "0x"
                    :name     "Geroge"
                    :location "Tokyo"}
   :enquery        {:open      false
                    :lead-text "test text"
                    :id        nil
                    :name      nil
                    :price     nil
                    :dealer    nil
                    :text      nil}
   })
