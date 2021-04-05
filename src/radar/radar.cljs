(ns radar.radar
  (:require
   [spark.core :as spark :refer [def-field def-doc]]

   [radar.book :as book]
   ))


(def-field title
  [:string 
   {:label "Name"
    :required? true}])


(def-field allow-domain
  [:string
   {:label    "Allow Domain"
    :helptext "Here you can specify a domain, like example.com or your-org.com.
All users from this domain will have access to this Radar."}])

(def-field allow-emails
  [:vector
   {:label    "Allow E-Mails"
    :type     "chips"
    :helptext "Here you can specify e-mail addresses of users which get access to this radar."}
   :string])


(def-doc Radar
  [{:firestore/collection "radars"}
   [:books {:optional true} [:map-of :string book/Book]]])


(defn all-tags [radar]
  (->> radar :books vals (mapcat :tags) set))


(def sections
  [{:key  :must-read
    :name "Must Read"
    :idx  0}
   {:key  :should-read
    :name "Should Read"
    :idx  1}
   {:key  :trial
    :name "Trial"
    :idx  2}
   {:key  :assess
    :name "Assess"
    :idx  3}
   ])


(defn books-by-section-key [books]
  (->> books
       (group-by #(let [c (-> % :recommendations count)]
                    (cond
                      (>= c 5) :must-read
                      (>= c 3) :should-read
                      (>= c 1) :trial
                      :else    :assess)))))


(defn title-in-lowercase [radar]
  (when-let [title (-> radar :title)]
    (-> title .toLowerCase)))

(defn books [radar]
  (->> radar :books vals (remove #(-> % :hidden))))

(defn book-by-id [radar book-id]
  (get-in radar [:books book-id]))
