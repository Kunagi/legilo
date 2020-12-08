(ns radar.repository
  (:require

   [commons.firestore :as firestore]))


;;; Radars

(defn radars-col-path []
  ["radars"])

(defn radars-by-uid-col-path [uid]
  [{:id "radars"
    :where ["uids" "array-contains" uid]}])

(defn radars-by-domain-col-path [domain]
  [{:id "radars"
    :where ["allow-domain" "==" domain]}])

(defn radar-path [radar-id]
  (conj (radars-col-path) radar-id))

(defn create-radar> [fields]
  (firestore/create-doc> (radars-col-path) fields))

(defn update-radar> [radar-id fields]
  (firestore/update-fields> (radar-path radar-id) fields))
