(ns radar.repository
  (:require

   [commons.firestore :as firestore]))


;;; Radars

(defn radars-col-path []
  ["radars"])

;; FIXME filter by uid AND domain
(defn visible-radars-col-path [uid]
  ["radars"]
  #_[{:id "radars"
    :where ["uids" "array-contains" uid]}])

(defn radar-path [radar-id]
  (conj (radars-col-path) radar-id))

(defn create-radar> [fields]
  (firestore/create-doc> (radars-col-path) fields))

(defn update-radar> [radar-id fields]
  (firestore/update-fields> (radar-path radar-id) fields))

;;; Books

(defn books-col-path [radar-id]
  (conj (radar-path radar-id) "books"))

(defn book-path [radar-id book-id]
  (conj (books-col-path radar-id) book-id))


(defn add-book> [radar-id fields]
  (firestore/create-doc> (books-col-path radar-id) fields))


(defn update-book> [book fields]
  (firestore/update-fields> book fields))
