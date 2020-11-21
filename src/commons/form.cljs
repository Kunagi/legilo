(ns commons.form
  (:require
   [clojure.spec.alpha :as s]
   ))

(s/def ::id keyword?)
(s/def ::submit fn?)
(s/def ::field (s/keys :req-un [::id]))
(s/def ::fields (s/coll-of ::field
                           :min-count 1))
(s/def ::form (s/keys :req-un [::fields ::submit]))


(defn field-by-id [form field-id]
  ;; (s/assert ::form form)
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


(defn on-field-value-change [form field-id new-value]
  (js/console.log "on-field-value-change" form)
  (assoc-in form
            [:values field-id]
            (convert-value-for-output new-value form field-id)))


(defn values [form]
  (-> form :values))
