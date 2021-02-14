(ns radar.book
  (:require
   [clojure.spec.alpha :as s]
   [spark.utils :as u]
   [spark.form :as form]
   [spark.core :as spark :refer [def-field def-subdoc]]
    ))


(def-field title
  [:string  
   {:label "Title"
    :required? true}])

(def-field author
  [:string 
   {:label "Author"}])

(def-field asin
  [:string 
   {:label "ASIN"
    :helptext "Amazon Standard Identification Number."}])

(def-field isbn
  [:string  
   {:label "ISBN"
    :helptext "International Standard Book Number. If you provide this, a
picture of the book will be shown."
    ;; :action {:label "Load"
    ;;          :f (fn [form]
    ;;               (js/console.log "ACTION" form)
    ;;               (assoc-in form [:values :isbn] "DUMMY")
    ;;               #_(let [result (form/on-field-value-change form :isbn "abc")]
    ;;                 (js/console.log "RESULT" form)
    ;;                 result))}
    }
   ])

(def-field tags
  [:vector
   {:label "Tags"
    :type "chips"
    :helptext "Type in a tag and submit with RETURN."}
   :string])

(defn id [this]
  (-> this :id))

(defn contains-tag? [book tag]
  (-> book
      :tags
      set
      (contains? tag)))


(defn tags-in-order [book]
  (->> book :tags sort))


(s/def ::id string?)
(s/def ::book (s/keys :req-un [::id]))


(def Book
  [:map])



(defn review-by-uid [book uid]
  (get-in book [:reviews (keyword uid)]))

(defn recommendation-count [book]
  (-> book :recommendations count))

(defn recommended-by-user? [book uid]
  (-> book :recommendations (u/v-contains? uid)))

(defn cover-url [book]
  (let [isbn (-> book :isbn)
        asin (-> book :asin)]
    (cond
      isbn (str "https://covers.openlibrary.org/b/isbn/" isbn "-M.jpg")
      ;; asin (amazon-service/cover-url-by-asin asin)
      :else nil)))
