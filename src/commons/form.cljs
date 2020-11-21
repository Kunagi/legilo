(ns commons.form
  (:require
   [clojure.spec.alpha :as s]
   ))

(s/def ::fields vector?)
(s/def ::form (s/keys :req-un [::fields]))


(defn field-by-id [form field-id]
  (s/assert ::form form)
  (some #(when (= field-id (-> % :id)) %)
        (-> form :fields)))

(comment
  (-> {:fields [{:id :name
                 :label "Name"}
                {:id :city
                 :label "City"}]}
      (field-by-id :name)))
