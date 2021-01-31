(ns radar.repository
  (:require
   [clojure.spec.alpha :as s]

   [commons.firestore :as firestore]))


;;; Radars



(defn radars-by-uid-col-path [uid]
  [{:id "radars"
    :where ["uids" "array-contains" uid]}])

(defn radars-by-domain-col-path [domain]
  [{:id "radars"
    :where ["allow-domain" "==" domain]}])
