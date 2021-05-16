(ns radar.relation
  (:require
   [spark.utils :as u]
   [spark.core :as spark :refer [def-field def-subdoc]]

   ))


(def-subdoc Relation
  [{}
   [:id :string]
   [:type :keyword]])


(def relation-types
  {:similar-to {:label      "similar to"
                :back-label "similar to"}})


(defn label [this]
  (get-in relation-types [(-> this :type) :label]))
