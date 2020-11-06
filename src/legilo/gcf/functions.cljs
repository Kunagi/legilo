(ns legilo.gcf.functions
  (:require
   [cljs.pprint :refer [pprint]]

   ["firebase-functions" :as f]))


(defn on-request [handler]
  (-> f
      (.region "europe-west3")
      .-https
      (.onRequest handler)))


(defn https [handler]
  (on-request
    (fn [req res]
      (handler req res))))


(defn https> [handler>]
  (on-request
   (fn [req res]
      (-> (handler> req)
          (.then #(-> res (.status 200) (.send %)))))))


(defn format-debug-response [val]
  (str
   "<pre>"
   (cond
     (or (nil? val) (seq? val) (map? val) (vector? val) (list? val))
     (with-out-str (pprint val))

     :else (js/JSON.stringify val))

   "</pre>"))


(defn https-debug> [handler>]
  (on-request
    (fn [req res]
      (-> (handler> req)
          (.then #(-> res
                      (.set "Access-Control-Allow-Origin" "*")
                      (.status 200)
                      (.send (format-debug-response %))))))))
