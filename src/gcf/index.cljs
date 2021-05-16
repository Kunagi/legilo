(ns gcf.index
  (:require
   [clojure.string :as str]
   [clojure.spec.alpha :as s]
   [shadow.resource :as resource]

   [spark.loggin-init-gcf]
   [spark.logging :as logging]
   [spark.firestore-init-gcf]
   [spark.firebase.backup :as backup]
   [spark.gcf.upgrade :as upgrade]
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
    (upgrade/exports (str/trim (str (resource/inline "../spa/version.txt"))))

    )))
