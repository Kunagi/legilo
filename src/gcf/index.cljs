(ns gcf.index
  (:require
   [clojure.spec.alpha :as s]
   ["firebase-admin" :as admin]

   [spark.loggin-init-gcf]
   [spark.logging :as logging]
   [spark.firestore-init-gcf]

   [spark.firebase.backup :as backup]
   ;; [gcf.backup :as backup]
   ))



(when goog.DEBUG
  (s/check-asserts true)
  (logging/install-tap))


(def exports
  (clj->js
   (merge
    {}

    (backup/exports "legilo-backups")

    )))
