(ns gcf.upgrade
  (:require
   [clojure.string :as str]
   [shadow.resource :as resource]
   [tick.locale-en-us]
   [tick.alpha.api :as tick]
   [tick.format :as tick.format]
   ["firebase-admin" :as admin]
   [spark.firestore :as firestore]
   [spark.utils :as u]
   [spark.gcf :as gcf]))


(defn handle-set-spa-version> [^js _req]
  (let [current-version (str/trim (str (resource/inline "../spa/version.txt")))]
    (firestore/update-fields>
     ["sysconf" "singleton"]
     {:spa-version current-version})
    #_(firestore/transact> [{:firestore/id "sysconf/singleton"
                             :spa-version  current-version}])))


(defn exports []
  {

   :setSpaVersion
   (gcf/on-request--format-output> handle-set-spa-version>)

   })
