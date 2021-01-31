(ns radar.service
  (:require
   [clojure.spec.alpha :as s]

   [commons.logging :refer [log]]
   [commons.utils :as u]

   [openlibrary.service :as openlibrary-service]
   [amazon.service :as amazon-service]

   [radar.radar :as radar]
   [radar.book :as book]
   [radar.repository :as repository]))




(defn update-review-text> [radar book uid changes]
  (s/assert ::book/book book)
  (repository/update-radar>
   radar
   {(str "books." (-> book :id) ".reviews." uid) changes}))


(defn book-cover-url [book]
  (let [isbn (-> book :isbn)
        asin (-> book :asin)]
    (cond
      isbn (openlibrary-service/cover-url-by-isbn isbn)
      ;; asin (amazon-service/cover-url-by-asin asin)
      :else nil)))

