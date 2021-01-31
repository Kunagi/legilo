(ns radar.context
  (:require
   ["@material-ui/core" :as mui]

   [commons.logging :refer [log]]
   [commons.models :as models]
   [commons.context :as commons-context]
   [commons.firestore-hooks :as fsh]

   [base.context :as base]

   [radar.repository :as repository]
   [radar.radar :as radar]
   ))

(def use-uid base/use-uid)


(defn use-book-id []
  (commons-context/use-param :book-id))
