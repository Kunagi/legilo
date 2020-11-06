(ns legilo.gcf.http
  (:require
   ["https" :as http]

   [legilo.utils :as u]))


;; Usa agent to prevent creating new connections
(def agent (-> http (.Agent (clj->js {:keepAlive true}))))


(defn download-text> [url]
  ;; (log ::download :url url)
  (js/Promise.
   (fn [resolve reject]
     (let [BUFFER (atom "")
           req (.get http
                     url
                     (clj->js {:agent agent})
                     (fn [res]
                       (.setEncoding ^js res "utf-8")
                       (.on ^js res "data" #(swap! BUFFER str %))
                       (.on ^js res "end" #(resolve @BUFFER))))]

       (.on ^js req "error" reject)
       (.end req)))))


(defn download-json> [url]
  (u/transform>
   (download-text> url)
   #(-> %
        js/JSON.parse
        js->clj)))
