(ns radar.context
  (:require
   ["@material-ui/core" :as mui]

   [commons.logging :refer [log]]
   [commons.mui :as cui]
   [commons.firestore-hooks :as fsh]

   [base.context :as base]

   [radar.repository :as repository]
   ))

(def use-uid base/use-uid)

(defn use-radar-id []
  (-> (cui/use-params) :radarId))

(defn use-radar []
  (fsh/use-doc (repository/radar-path (use-radar-id))))


(defn use-book-id []
  (-> (cui/use-params) :bookId))

