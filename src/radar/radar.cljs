(ns radar.radar
  (:require
   [commons.domain-api :refer [def-attr def-command]]

   [radar.book :as book]
   ))


(def-attr title
  {:label "Name"
   :required? true})


(def-attr allow-domain
  {:label "Allow Domain"})



(def-command add-book
  {:label "Add Book"
   :icon "add"
   :form {:fields [book/title book/author book/isbn book/asin book/tags]}})

(def-command add-example-books
  {:label "Add example Books"})


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
   {:key :to-read
    :name "To Read"
    :idx 3}
   ])


(defn books-by-section-key [books]
  (->> books
       (group-by #(let [c (-> % :recommendations count)]
                    (cond
                      (>= c 5) :must-read
                      (>= c 2) :should-read
                      (>= c 1) :trial
                      :else :to-read)))))


(defn title-in-lowercase [radar]
  (when-let [title (-> radar :title)]
    (-> title .toLowerCase)))
