(ns radar.context
  (:require
   ["@material-ui/core" :as mui]

   [commons.logging :refer [log]]
   [commons.mui :as cui]
   [commons.context :as commons-context]
   [commons.firestore-hooks :as fsh]

   [base.context :as base]

   [radar.repository :as repository]
   ))

(def use-uid base/use-uid)

(defn use-radar-id []
  (commons-context/use-param :radarId))

(defn use-radar []
  (fsh/use-doc (repository/radar-path (use-radar-id))))


(defn use-book-id []
  (commons-context/use-param :bookId))
