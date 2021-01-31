(ns radar.service
  (:require
   [clojure.spec.alpha :as s]

   [commons.logging :refer [log]]
   [commons.utils :as u]


   [radar.radar :as radar]
   [radar.book :as book]
   [radar.repository :as repository]))




(defn update-review-text> [radar book uid changes]
  (s/assert ::book/book book)
  (repository/update-radar>
   radar
   {(str "books." (-> book :id) ".reviews." uid) changes}))
