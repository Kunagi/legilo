(ns radar.service
  (:require

   [commons.firestore :as firestore]))


(defn book-recommendation-count [book]
  (-> book :recommendations count))


(defn recommend-book [uid book]
  (firestore/update-fields> book {:recommendations (firestore/array-union [uid])}))


(defn update-book [radar-id book changes]
  (firestore/load-and-save>
   (or book ["radars" radar-id "books" (str (random-uuid))])
   #(merge % changes)))
