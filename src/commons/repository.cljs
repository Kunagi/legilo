(ns commons.repository
  (:require
   [commons.logging :refer [log]]
   [commons.runtime :as runtime]
   [commons.models :as models]
   [commons.firestore :as firestore]))


(defn create-doc> [Col values]
  (let [id (or (-> values :id)
               ((models/col-id-generator Col) {:values values}))
        values (assoc values
                      :id id
                      :ts-created [:db/timestamp]
                      :ts-updated [:db/timestamp])
        path [(models/col-path Col) id]]
    (firestore/create-doc> path values)))


(defn update-fields> [doc values]
  (let [values (assoc values
                      :ts-updated [:db/timestamp])]
    (firestore/update-fields> doc values)))
