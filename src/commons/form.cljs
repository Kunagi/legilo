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


(defn field-type [form field-id]
  (-> form
      (field-by-id field-id)
      :type
      (or "text")))


(defn convert-value-for-output [value form field-id]
  (if (or (nil? value)
          (= "" value))
    nil
    (case (field-type form field-id)
      "number" (js/parseInt value)
      value)))
