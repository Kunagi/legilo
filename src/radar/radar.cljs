(ns radar.radar
  (:require
   [clojure.spec.alpha :as s]
   [commons.models :as m :refer [def-model]]

   [radar.book :as book]
   ))


(def-model title
  [m/Attr
   {:label "Name"
    :required? true}])


(def-model allow-domain
  [m/Attr
   {:label "Allow Domain"
    :helptext "Here you can specify a domain, like example.com or your-org.com.
All users from this domain will have access to this Radar."}])


(def-model Radar
  [m/Doc
   {}])


(def-model Radars
  [m/Col
   {:doc Radar}])



(defn all-tags [radar]
  (->> radar :books vals (mapcat :tags) set))


(def sections
  [{:key :must-read
    :name "Must Read"
    :idx 0}
   {:key :should-read
    :name "Should Read"
    :idx 1}
   {:key :trial
    :name "Trial"
    :idx 2}
   {:key :assess
    :name "Assess"
    :idx 3}
   ])


(defn books-by-section-key [books]
  (->> books
       (group-by #(let [c (-> % :recommendations count)]
                    (cond
                      (>= c 5) :must-read
                      (>= c 3) :should-read
                      (>= c 1) :trial
                      :else :assess)))))


(defn title-in-lowercase [radar]
  (when-let [title (-> radar :title)]
    (-> title .toLowerCase)))

(defn books [radar]
  (-> radar :books vals))

(defn book-by-id [radar book-id]
  (get-in radar [:books (keyword book-id)]))
