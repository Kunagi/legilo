(ns radar.book
  (:require
   [spark.utils :as u]
   [spark.core :as spark :refer [def-field def-subdoc]]

   [radar.review :as review]
    ))


(def-field title
  [:string  
   {:label "Title"
    :required? true}])

(def-field subtitle
  [:string
   {:label "Subtitle"}])

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
    :helptext "International Standard Book Number. Provide this to lookup data."
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
  [:set
   {:label    "Tags"
    :type     "chips"
    :sort?    true
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



(def-subdoc Book
  [{}
   [:tags {:optional true} tags]
   [:reviews {:optional true} [:map-of :string review/Review]]
   [:recommendations {:optional true} [:set :string]]
   [:recommendations-times
    {:optional true}
    [:map-of :string any?]]])



(defn review-by-uid [book uid]
  (get-in book [:reviews uid]))

(defn recommendation-count [book]
  (-> book :recommendations count))

(defn recommended-by-user? [book uid]
  (-> book :recommendations (u/v-contains? uid)))

(defn cover-url [book]
  (let [isbn (-> book :isbn)
        ;; asin (-> book :asin)
        ]
    (cond
      isbn  (str "https://covers.openlibrary.org/b/isbn/" isbn "-M.jpg")
      ;; asin (amazon-service/cover-url-by-asin asin)
      :else nil)))
