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


(defn- initialize-field [idx field]
  (assoc field
         :auto-focus? (= 0 idx)
         :name (or (-> field :name)
                   (-> field :id name))
         :label (or (-> field :label)
                    (-> field :name)
                    (-> field :id name))
         :auto-complete (get field :auto-complete "off")))


(defn initialize [form]
  (s/assert ::form form)
  (-> form
      (assoc :fields (map-indexed initialize-field (-> form :fields)))
      (assoc :values
             (reduce (fn [values field]
                       (assoc values
                              (-> field :id)
                              (-> field :value)))
                     {} (-> form :fields)))))


(defn field-by-id [form field-id]
  (s/assert ::form form)
  (some #(when (= field-id (-> % :id)) %)
        (-> form :fields)))


(defn field-type [form field-id]
  (-> form
      (field-by-id field-id)
      :type
      (or "text")))


(defn values [form]
  (-> form :values))


(defn field-value [form field-id]
  (-> (values form)
      (get field-id)))


(defn validate-field [form field-id]
  (let [field (field-by-id form field-id)
        value (field-value form field-id)
        error (when (and  (-> field :required?) (nil? value))
                "Input required.")]
    (if error
      (assoc-in form [:errors field-id] error)
      (update form :errors dissoc field-id))))


(defn adopt-value [value form field-id]
  (if (or (nil? value)
          (= "" value))
    nil
    (case (field-type form field-id)
      "number" (js/parseInt value)
      value)))


(defn on-field-value-change [form field-id new-value]
  (-> form
      (assoc-in [:values field-id]
                (adopt-value new-value form field-id))
      (validate-field field-id)))


(defn contains-errors? [form]
  (-> form :errors seq boolean))


(defn field-error [form field-id]
  (get-in form [:errors field-id]))


(defn on-submit [form]
  (s/assert ::form form)
  (reduce validate-field
          form (->> form :fields (map :id))))
