(ns radar.repository
  (:require
   [clojure.spec.alpha :as s]

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


(defn update-radar> [radar fields]
  (s/assert ::firestore/doc radar)
  (firestore/update-fields> radar fields))
