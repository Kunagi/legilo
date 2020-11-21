(ns radar.repository
  (:require

   [commons.firestore :as firestore]))


(defn add-book> [radar-id fields]
  (firestore/create-doc> ["radars" radar-id "books"] fields))


(defn update-book> [book fields]
  (firestore/update-fields> book fields))
