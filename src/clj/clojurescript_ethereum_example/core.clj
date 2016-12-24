(ns clojurescript-ethereum-example.core
  (:require [clojure.java.io :as io]
            [compojure.core :refer [ANY GET PUT POST DELETE defroutes]]
            [compojure.route :refer [resources]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [ring.middleware.json :refer [wrap-json-params]]
            [ring.middleware.transit :refer [wrap-transit-params]]
            [environ.core :refer [env]]
            [cheshire.core :as json]
            [org.httpkit.server :refer [run-server]])
  (:gen-class))

(def ^:dynamic *server*)

(def users (atom {"customer@a.a"          {:email    "customer@a.a"
                                           :password "password"
                                           :keystore "{\"encSeed\":{\"encStr\":\"4NpcO+IotM+5dPi6YK9OGD/Vpbo4sy34ubCn4yhtqQR5zUmdnWvbn26CpVenI8YcoO4SE3izPzTadV1UTuSlUBk/ur9dYYaNhBzq4nHmbCFC2EAnpvVWdQ824sDifI0wE239pYqLdZge456pfJe0KnrZdwo6SvYM/paNZIJu5ml66RUlkyHtKw==\",\"nonce\":\"WFbpwV/MC4v+fiTE7TiPFJzFbDizUk8y\"},\"ksData\":{\"m/0'/0'/0'\":{\"info\":{\"curve\":\"secp256k1\",\"purpose\":\"sign\"},\"encHdPathPriv\":{\"encStr\":\"7y8ylEzrs8t8QxSsNd3a3S6HevH1ame3XHhxfrUOcV7mK+/Wd1/9lIJB5rGTdSk4Iltkw91KWEWns6gpytHrNrULihLpVgBGnz3t3aOFUstbxiEbKpiFtrpYG1t+jvvpVbdPUylAKRqFu0oSVVqusfiTkFx+HvwPMT8HRav4mQ==\",\"nonce\":\"tw03WJgjbsQKNJPoIhBxhDcgpm0KsrP6\"},\"hdIndex\":3,\"encPrivKeys\":{\"abc24d2e0db6b3b1b548488d6d4d4264e652bc11\":{\"key\":\"P/NQcE5EutDiraAecLnMHOkf5PXzl0Gu0MpKGYSrTB5x/Pcwu/+9+9G/yJH7SLlR\",\"nonce\":\"vQtPZJOCBDut0i5V+FOkYOv3EEeytos7\"},\"6b9158c92762177b9e96b7b5fed96a56c9944ee5\":{\"key\":\"0jsABbLWtSMOS1bqU9qinFhVOH2djtEL0iki2S8eqqbh3T0vl5TUSHGRpaZia73a\",\"nonce\":\"QG1AEy1oowaRdAfNoMhfHeorK8kWQt4O\"},\"7321a26e48405abaf28960ecddebf719f86bb42c\":{\"key\":\"qdr38KjlH8EkbNA00H1IgQvrMxUusf5RtBCaf4toxIVSKktmwLlKnlEqejxlmmN2\",\"nonce\":\"nfPP/04TKguHJaUNm9FWp8I4GDw6A3FE\"}},\"addresses\":[\"abc24d2e0db6b3b1b548488d6d4d4264e652bc11\",\"6b9158c92762177b9e96b7b5fed96a56c9944ee5\",\"7321a26e48405abaf28960ecddebf719f86bb42c\"]}},\"encHdRootPriv\":{\"encStr\":\"ipFVGeR2sPReg+tvWN7e83n02TVt6YHWLAxzsCAf9jjbXWYDrbc/5+9pRHpNZbrNJF0F2KCWLXLGhVv9pdwQ4TdnbPanYKEBfcl/D7UDqyokFqU6xw7T38HaqDECs0PbYS+w8dSoRc3u2OcM33+dVkiSkRYSfJ0zLwpaSAzakg==\",\"nonce\":\"ftTmgO8DRCJdRixRBwHsgK5vV7w6X/9S\"},\"salt\":\"KxHMn3afRmyW7meTp+n0TmZEJWNeX/+SE3kAdHkAHvA=\",\"version\":2}"
                                           :type     "customer"
                                           :name     "customer"
                                           :address  "0xabc24d2e0db6b3b1b548488d6d4d4264e652bc11"}
                  "dealer@a.a"            {:email    "dealer@a.a"
                                           :password "password"
                                           :keystore "{\"encSeed\":{\"encStr\":\"MDqUK6lDWvw0Yv0Tf3QfeaZDNHa67qJDIsvZQ1GckCgtK8VzTPJCKQ4gZvB4RaCSx5QT8SrPH42fIQR5iuviYdQ2gJiAJvELaXyfui/I0uD7QTQAwzpiSFGUIOPCRIK+JGQnfPMZ6yS82GLj5kWfxuOTat9YQkh6Ws/PzgbYm/sbyqMErV64dw==\",\"nonce\":\"zHyVsyRh61kGxohXl0WmPTNzK7jXGSm/\"},\"ksData\":{\"m/0'/0'/0'\":{\"info\":{\"curve\":\"secp256k1\",\"purpose\":\"sign\"},\"encHdPathPriv\":{\"encStr\":\"BMibsmV2we66krLceOxYa2s1jVoiQG7vu7jnHbTepps9Wqi3WLtfYtXB3SGIV2v2BM9Q9dwvg7AdIXVwtCd9dq2lanUhxYEqLGH3ZGqjleJ67YUrk2tFSCFgqxPAuvuVtTvI+iS+Mtiq56rih49hwO6szs+y1RExWeP1Q1T2LQ==\",\"nonce\":\"1JdSBXjliJPEn3frIgONQMTIN/oYMtbO\"},\"hdIndex\":3,\"encPrivKeys\":{\"1c90b30e6fa89fe54801989b17dc8d985f2a2e81\":{\"key\":\"6MPmIsiIhljt1t7yghCq5o2qrtTC8JZ1a0NBC/gNd9933+kDXoUoNr6E+U12HIHk\",\"nonce\":\"UhTXDvEueCbIz2Q/dkYXsWErs4b1irzU\"},\"507592c93e91f303abbac855d7304ae6d3c9f4db\":{\"key\":\"EUgakK7JKPWxeXkxb3gIUIa5Dr8+FDfmIqMTlT4WDa1kjnvatp322XNjZLb/eJtg\",\"nonce\":\"HNGg1fc85o2hDgYgLJJ+Gt1qJXXhVkyv\"},\"e83f95da899682d06c0e5edeed721dabe6aa20dd\":{\"key\":\"gt31lVJughcJya/fp9msIZhqOgpw2dGpCNpvFzz85VuwsyJAxWGT1UqJJJHa/5IS\",\"nonce\":\"IWqhK42hpUR85kD2zzvEuIOVTWR7B3PM\"}},\"addresses\":[\"1c90b30e6fa89fe54801989b17dc8d985f2a2e81\",\"507592c93e91f303abbac855d7304ae6d3c9f4db\",\"e83f95da899682d06c0e5edeed721dabe6aa20dd\"]}},\"encHdRootPriv\":{\"encStr\":\"CKmVuaegyf/FwmhKnROOtaKN7xx/Aez7C96+rifCtEmYODX2M4b66YMMw17SxMrnvJYWrC2mgQX75aSDaSwHiNzMBK3cQ3DWLtzMGWoKDTjJiYyyU9i1HF59gGlQGJ+9b7OY+JltdDKrH7M+TbeEoLqO9dOkHMg4L7AYz2XPcw==\",\"nonce\":\"UijHwshv/oA7qeSXCi9fSXBpexL8mnN+\"},\"salt\":\"2brb6cfRnjrP8fONRSMUwUMYzuoZzJBF5nWKGn5h218=\",\"version\":2}"
                                           :type     "dealer"
                                           :name     "dealer"
                                           :address  "0x1c90b30e6fa89fe54801989b17dc8d985f2a2e81"}
                  "recruit@a.a"           {:email    "recruit@a.a"
                                           :password "password"
                                           :keystore "{\"encSeed\":{\"encStr\":\"RhrSVS5qu2uxfUvMBTxqLJoX7sKRKuoVl2eBV3f/RR9FmxSun+HeU0ajXCMQPNbrhdhUW20yIx3UAUkVlr61giSH0u7db5h/HAanR/KRnka3P6fI7v1FU2q1xZC8351zcJ0plWNHKbzOPZ8R6c9zfqUjwC/HNj2Pg8DX6iucnWLsd1A5VUDQeQ==\",\"nonce\":\"ia+B8B9ToW08+C3Dci2roAt11KKWdP8i\"},\"ksData\":{\"m/0'/0'/0'\":{\"info\":{\"curve\":\"secp256k1\",\"purpose\":\"sign\"},\"encHdPathPriv\":{\"encStr\":\"YzOqOmCgj5cRiZRAcQc302O3mNVm8rYffmoHXwuwta2d8PKYk/S0nmIA4PFgFdhtPX98a1lAEykJN84CVzV+XuYAQ31VhqpNm2VoTxuadTnfsP6bTkZZ5JtC3XzMZllN31idlCEJjzfeheJ5psbY+9G/N0wnVF9VRjaA0mfsig==\",\"nonce\":\"zrb5H2MNTqDH+NBkLLD8+qvqL4NpkvH7\"},\"hdIndex\":3,\"encPrivKeys\":{\"3c26ab7c9795d0d0507c05f7d6b1f13fe1f56827\":{\"key\":\"A+bDm1rB6EDxDiF//XysE+U6TmDVJ5R6zZ8436evxJ+gyzpf3tjaMg+ux7AXsnxc\",\"nonce\":\"2j84k1KF5EjGGmlVlUUIzheY0GNY/cXk\"},\"cc5b00bcf51090ed0c33d006e5206920a788ae73\":{\"key\":\"EmlTK6LzvTeX8fAJiosACr6S+MMZCq5dHMAHgaEBNucW8W7qjVn9oDz+QCxxbmlT\",\"nonce\":\"HIVe0SZzQZPGpyjOgpK2+Jco+iUVHd60\"},\"1ba2759e8920fe0d78825e1545b74ef00f13597b\":{\"key\":\"bSmMQtUczaTH3/qYmifsP1lfYJdb4jvfVkNmSKjk4C3O14T2Jx9gDyTEzpAqZeWn\",\"nonce\":\"C/XaYKpENeFBLsp1DcQMimDQ0rM6h0V8\"}},\"addresses\":[\"3c26ab7c9795d0d0507c05f7d6b1f13fe1f56827\",\"cc5b00bcf51090ed0c33d006e5206920a788ae73\",\"1ba2759e8920fe0d78825e1545b74ef00f13597b\"]}},\"encHdRootPriv\":{\"encStr\":\"d7M8Kdj5KSdrxl5Kh+HBuuIzRFCRmL0SOJJ/tRmLXYffEVRfHlfpwKQuFyhSf2sJQEG2oF5Ob/FBjGmKJxx8yEMVQDEOUTWU6DHOLJNHrN9tKME9HIb5aRuiJOJM/C6k2dEQGd7NjcQwkmosGyO2uKAXhAXonJRLAdFudIy7Tg==\",\"nonce\":\"CqHGj53xMv0uoMgr2j3y/PVQO/ovULVx\"},\"salt\":\"pdrHg9dFt1CTp663Nxo91yakJ3XqsjKiX7hRziQnaog=\",\"version\":2}"
                                           :type     "admin"}
                  "dealer2@a.a"           {:email    "dealer2@a.a"
                                           :password "password"
                                           :keystore "{\"encSeed\":{\"encStr\":\"cWGocXmv4yWnGwGaePUAIOl9tXZ7sebajwdEmVYsXSjT72QWxzPlfUIvtc2TebL2at+tlLWhOIhcGn0LBHGAI2yd+wPkLTu+v4Zkl3VjuuLt5oZ1NBzFHxXAjo08g6dHkjZ8Vtn6LanL5KNVxoatFbMhD3cFhCjnJP+IdajRtfYgTXJ5PyDvvA==\",\"nonce\":\"/Evn1Xqd3amfBPcatmhH4lB/Z7329ho4\"},\"ksData\":{\"m/0'/0'/0'\":{\"info\":{\"curve\":\"secp256k1\",\"purpose\":\"sign\"},\"encHdPathPriv\":{\"encStr\":\"ezC6sqi3I+DgNpYBx12PTZt5w5WLFTWGWSlVFHGMvy2jcn2bqjk7BG5uF2v0gcgdBESREmfzMsU9AlVhNB27LMP7iNXaxh/v0zjS7C24wYAMGXI4rAO4654Uzq09ddM+TcosW/sL4n/Qu+M59WNlXHRt+7JdPbmUzaLP5SEifg==\",\"nonce\":\"bU3Q7mH91W8xjmJ68AoMg/iX7zT3m1R5\"},\"hdIndex\":3,\"encPrivKeys\":{\"56fcc45350aaf6abbb555ee32cc4324f25485e32\":{\"key\":\"Nk3WqHdDXPCP6zUWnhaTNsuUN+QsgL5desg0Iite2yQL45xJ/kY5QbrowsRV15DC\",\"nonce\":\"Ow/Ye90jJsERVt69edGJlGXJw9hVwNBY\"},\"6a307448d696fd4a2acf60150e705f0f491a1911\":{\"key\":\"cbji1cQyyw6uGfkhg3Fvf7p8NbDZsbB0oeVn0TjVYMxLRkghVJCPawI/8p4ujfTx\",\"nonce\":\"W1Psx6h1QHZn+Z9A/FSXheeTwKQ5gz81\"},\"608000a6aecaa33d208808222e86f689fced5a65\":{\"key\":\"2kbxas4OEglQc9bYscNuPBaqsarCwaGQuxvWihHp/TI342z9A1A7cJZvARTW26UX\",\"nonce\":\"QvJbd8vXfHie1aIDIQw0TBIBx6dCYJuN\"}},\"addresses\":[\"56fcc45350aaf6abbb555ee32cc4324f25485e32\",\"6a307448d696fd4a2acf60150e705f0f491a1911\",\"608000a6aecaa33d208808222e86f689fced5a65\"]}},\"encHdRootPriv\":{\"encStr\":\"uxX1hhpu+jRUrqYRYMxcokbklzUesa5s8hTByJABzkLVgI+Qz4PY3/v/yFhzRu52z8PNmWSMnM4Kj7cJsVPZzrG/hS9ksnUv4Y1/vuO+rrUODkXNCerzHLJvB1XMRU6cFHxBGaCBSS/gE9eRSgAhP3IU57VFkPyUowZc0c8Bnw==\",\"nonce\":\"2gyfAPM3VwOgzWvUbphsQp1rqMuQCA4C\"},\"salt\":\"NLsikTP6H0ihTjt18y3PsKSZT/ViZggYn8si7fyAWWE=\",\"version\":2}"
                                           :type     "dealer"
                                           :name     "dealer2"
                                           :address  "0x56fcc45350aaf6abbb555ee32cc4324f25485e32"}
                  "customer-chubachi@a.a" {:email    "customer-chubachi@a.a"
                                           :password "password"
                                           :keystore "{\"encSeed\":{\"encStr\":\"KBGoHjdX6slMs5FYq1PPEiAZHolnjWy786eSKqL1f7bmo5imdZ9am5FPep+xzoOAgsQfGM9eVSWeHUiq5UyTtiM4czy0jqeB1doLBYPUa6T4URHNMGrqxcWcj/jmige2Ll92vlGsszxnf+2o2h7HXwB8xku4jD2kxIhVoASrfw8WpA15Tj5fpg==\",\"nonce\":\"6pebJdf9NDt+mCbryON+P+C2F2tALRhJ\"},\"ksData\":{\"m/0'/0'/0'\":{\"info\":{\"curve\":\"secp256k1\",\"purpose\":\"sign\"},\"encHdPathPriv\":{\"encStr\":\"FgyxQ4yW6tPxPkLg6B9lWM3C4u8R9T6PY0KQ2QOXH0wf5WAtBFyHxiVZ8T97EcTRcsX5XOqaW1WDzr6+tEoxEHuvtPHKUU/oAo2BLz0ClMw9p+ZhFOGOi0Ge1YRAXYQ/fGHkUBo0xmHLFEMFhqBXI8CLQNqsZRqBO6i5J/fgTQ==\",\"nonce\":\"Yg9HGaIM8NUkkHsK+Yl+Pb4n2TOaeXoM\"},\"hdIndex\":3,\"encPrivKeys\":{\"01a2ef00acc85e376a3d02a566878a47f5876570\":{\"key\":\"Oy1ziIdwOx6MZ191ibEeQ3jjL67mxoaPBxwrosVMvtJLka6/sbZBMZDHtLKezKVo\",\"nonce\":\"CJMOxZOWsM8ZzOHDNsZ4HXtdCpjhuNet\"},\"67c143715b5002bda3568880f879b5074a02fc25\":{\"key\":\"wB13Xy/5AQvCKl6lBDuwCWP3xuATDZEcnneCqU2qv2jC3n7Lyx3QK/5VPgvTPlzq\",\"nonce\":\"jJSW0Mltu2fscPSCoydXfbGtfw7GNK01\"},\"18da23b10f6bbeb06efef774b2ddce5040bb8a12\":{\"key\":\"LvvdJaAcxrVL/GWTTAAK7PKImxdlCRNOg4VQPNLb6kouv1hmI8MRhM7HQ/WwPP2d\",\"nonce\":\"dRqPFsUT34fCXlFLksPhxm9GdLjAH3x+\"}},\"addresses\":[\"01a2ef00acc85e376a3d02a566878a47f5876570\",\"67c143715b5002bda3568880f879b5074a02fc25\",\"18da23b10f6bbeb06efef774b2ddce5040bb8a12\"]},\"m/0'/0'/1'\":{\"info\":{\"curve\":\"curve25519\",\"purpose\":\"asymEncrypt\"},\"encHdPathPriv\":{\"encStr\":\"W6u5lKMAaxXwBtK0d01wtjFA1XxG8kMfWtUH7ybIQylaYGvvZItpqiT6nlbakbNLb2iJnlN1DfEYO3VY4ya8CRaEMU1Px+jh90wlNxGCNk103jLSZqf8+ETYd7ZW2ofrfDCn4wxrdpNYgrxoc0jWMZMiWpmDs743Zh22tcg3Xw==\",\"nonce\":\"uFZlJIHXkTTUyIW7eIHDAZ7Ve7UB3nAI\"},\"hdIndex\":1,\"encPrivKeys\":{\"76f561554ec4e417dce24118333b228b1b07348340b7510747a922dc458e762f\":{\"key\":\"Z8O4yIUNeQvftOEutXRKuZjikV/BBQiJfCaxd8domwxw9cuF9VZBK4uC44G7lknN\",\"nonce\":\"O9LMjH6ANx4mAJwK+PIcpssmJCDSHvC9\"}},\"pubKeys\":[\"76f561554ec4e417dce24118333b228b1b07348340b7510747a922dc458e762f\"]}},\"encHdRootPriv\":{\"encStr\":\"tqEVjY5Jd1sizL2z2qRsaYLwVfwbtz7/0kWUYRoCJi5JMO8sXS5dBYJGSfOFKtmSvJcw0qiqb0Kr4Y3YFFnAINmH0iSNYfGCsDqOWgaW5LiRziPBsFv0ERsahDM/2u8/ES22f7McGuEGnopul8OxiTtnLx5LIprgZDc7R4xgWA==\",\"nonce\":\"sV6bqqmG4Zqh9aWcA/RH0QBrWwqbnIJg\"},\"salt\":\"BUYmUloC6FIwPkBvnznOcn+7TdeGTj9jYq0+kZWEEf0=\",\"version\":2}"
                                           :pubkey   "76f561554ec4e417dce24118333b228b1b07348340b7510747a922dc458e762f"
                                           :type     "customer"
                                           :name     "customer-chubachi@a.a"
                                           :address  "0x01a2ef00acc85e376a3d02a566878a47f5876570"}
                  "dealer-chubachi@a.a"   {:email    "dealer-chubachi@a.a",
                                           :password "password",
                                           :keystore "{\"encSeed\":{\"encStr\":\"j+sLDog4ViJoDsxtXeOoVbfNV3eOQbXlxJj9DLzqt3Y7pSq9o1u8GANBDs0ZI3zdFaOTNQgVorNTuqI3X/Ypv59RSGYy7767clhUedcBOg1QHWGrwCqnQ7h3Lks94U8elsFPYKB0kpOGNX+GuyjnnJ3dxstkIE8Z2SvClZc1vtMJsLwgg5QQiA==\",\"nonce\":\"20eNTGxnsSRew6gmseaXqMRJDTB+cnqZ\"},\"ksData\":{\"m/0'/0'/0'\":{\"info\":{\"curve\":\"secp256k1\",\"purpose\":\"sign\"},\"encHdPathPriv\":{\"encStr\":\"6R0RKlzrzGTyqW0Het4dMGRCnnNu6N45jOHe4pPSYDZbh6TpUW6XADBeg2xlJZEvZ+ofjkR0f8TevKggsaOCelRB6Zo5PZUdtJiG+RDPxWZT2PJ0HNAMu5H38BgPZU8Vz5sdUkDDgu6wQQZYfL763DRRFsVMGATE+gNVsBg2hQ==\",\"nonce\":\"/Ggf96kuOuJqdfZEulkbz8nbcI0p9BOS\"},\"hdIndex\":3,\"encPrivKeys\":{\"fff3f282061c2add91d8850e4eb824a02f063f16\":{\"key\":\"nwUvbevEdBXd1yhGxXfEhw5u33RdScgntC4khy+u5q+FPgjIQ/+D8s2PsBdfRHHy\",\"nonce\":\"7us14bOjHQ7a1x3f520K+Z7OxsLQ9vdJ\"},\"b356d76573fe4af4a8440d6e535f8b486941a005\":{\"key\":\"5XdEBNpkMwDJI+POKBb1F2yflVfKg/vFbIkwDHUUMss5Avi1J1R87kDZvw14OzIU\",\"nonce\":\"l3U/e13Hk74PnqNQ1FytIYvr1sQ0Fs8v\"},\"c8fbe89e5b035183824c52007887a67ff0ec08dd\":{\"key\":\"G7nVTDoAt+qbluLr+IpfU/Ea3nCcJ+JRbeJLtb3t8ZiYKYZvPybUvU/1tOGsdntw\",\"nonce\":\"x6WhsZQ+mZPvRD+F+40y+pcFNyCM6G4S\"}},\"addresses\":[\"fff3f282061c2add91d8850e4eb824a02f063f16\",\"b356d76573fe4af4a8440d6e535f8b486941a005\",\"c8fbe89e5b035183824c52007887a67ff0ec08dd\"]},\"m/0'/0'/1'\":{\"info\":{\"curve\":\"curve25519\",\"purpose\":\"asymEncrypt\"},\"encHdPathPriv\":{\"encStr\":\"GXpeKXbKlmGvG2ADdr/rpP/z0CnNaLpKBlcffXHVHyscgbViKnIuI/athNo9oyqHr2vZA8h/ONYAMDzM3/Zz430YJz2DIoCFSqUEFwOAZQYLwvOZBt/hWCy4UYjzd5W4qsx57d5tb63f6biGwzg+ow9LJxHT604FfLR9P9lCUg==\",\"nonce\":\"R8cMyvGSVRrmFd1/INKSK231Ok6neSv7\"},\"hdIndex\":1,\"encPrivKeys\":{\"06b3f43652c7c282eda7653c22b5bcb35547f81ba80d072f2a7af0697128f551\":{\"key\":\"ahwzQPOsetc8+xSdNn2on0VcYPm54axnnyOvLSqzfKh6S+r+gFwZUF45f1trnUiR\",\"nonce\":\"RHikOmbkCP267hSIYKcgljmxHYHliqmp\"}},\"pubKeys\":[\"06b3f43652c7c282eda7653c22b5bcb35547f81ba80d072f2a7af0697128f551\"]}},\"encHdRootPriv\":{\"encStr\":\"WHsg71wEQhsoWMbkZliTS7shOKvQIMuYRlf1eUBZdHiKo1MmsWbCAwgoDoaCEOXrzotEfQkyZQSFfntdhxbwns6F9J9PqWhUKmvpR4XMwOdLqZbGfzc8ML9Y7N+4uSHjefISJkFxcvyjrowMSE9LfalFcaCmzgbPsVYJWJv7BA==\",\"nonce\":\"AfgWgU+tA26YIXyyhUCh8bQNDpASeto3\"},\"salt\":\"xyV60uwIQ4TuOCSSykT7s878S2ai3GctctGn8y0QC9Q=\",\"version\":2}",
                                           :pubkey   "06b3f43652c7c282eda7653c22b5bcb35547f81ba80d072f2a7af0697128f551",
                                           :type     "dealer"
                                           :name     "dealer-chubachi"
                                           :address  "0xfff3f282061c2add91d8850e4eb824a02f063f16"}}))

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

(def dealers {(clojure.string/lower-case "0x39da12ffc0b7209e6f473a19074e785e42eb7555")
              {:name "DEALER_A"
               ;; :address "0xaaDC052Ee37f62889064b44F40D271441e18Be6e"
               :key  "key01dealer"}
              (clojure.string/lower-case "0x043b8174e15217f187de5629d219e78207f63dce")
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
  (GET "/key/:address" [address];; "/dealers/" isnt dealt with.
       (println "id:" address)
       {:status  200
        :headers {"Content-Type" "text/html; charset=utf-8"}
        :body    (json/generate-string
                  (if (= 0 (count (filter #(= address (:address %)) (vals @users))))
                    {}
                    (dissoc (first (filter #(= address (:address %)) (vals @users))) :keystore)))})

  ;; DEALER INFO
  (GET "/dealers/:address" [address];; "/dealers/" isnt dealt with.
       (println "dealers: " address)
       {:status  200
        :headers {"Content-Type" "text/html; charset=utf-8"}
        :body    (json/generate-string
                  (if (= 0 (count (filter #(= address (:address %)) (vals @users))))
                    {}
                    (dissoc (first (filter #(= address (:address %)) (vals @users))) :keystore)))})

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
      (wrap-transit-params {:opts{}})
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
