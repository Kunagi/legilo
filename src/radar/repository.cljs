(ns radar.repository
  (:require

   [commons.firestore :as firestore]))


(defn book-col-path [radar-id]
  ["radars" radar-id "books"])

(defn book-path [radar-id book-id]
  (conj (book-col-path radar-id) book-id))


(defn add-book> [radar-id fields]
  (firestore/create-doc> (book-col-path radar-id) fields))


(defn update-book> [book fields]
  (firestore/update-fields> book fields))
