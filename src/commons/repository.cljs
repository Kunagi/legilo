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


(defn update-doc> [doc values]
  (let [values (assoc values
                      :ts-updated [:db/timestamp])]
    (firestore/update-fields> doc values)))


(defn update-doc-child> [doc inner-path child-id child-values]
  (let [inner-path-as-string (reduce (fn [s path-element]
                                       (if s
                                         (str s "." (name path-element))
                                         (name path-element)))
                                     nil inner-path)
        values (reduce (fn [values [k v]]
                         (assoc values
                                (str inner-path-as-string
                                     "." child-id
                                     "." (name k))
                                v))
                       {} child-values )]
    (update-doc> doc values)))
