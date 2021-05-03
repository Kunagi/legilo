(ns radar.sysconf
  (:require
   [spark.core :as spark :refer [def-field def-doc]]

   [radar.book :as book]
   ))


(def-doc Sysconf
  [{:firestore/collection "sysconf"}
   [:spa-version {:optional true} :string]])


(defn spa-version [this]
  (-> this :spa-version))
