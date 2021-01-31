(ns commons.effects
  (:require
   [commons.logging :refer [log]]
   [commons.runtime :as runtime]
   [commons.repository :as repository]))




(defmethod runtime/reify-effect> :db/create
  [[_ path values]]
  (let [values (assoc values
                      :ts-created [:db/timestamp])]
    (repository/create-doc> path values)))


(defmethod runtime/reify-effect> :db/update
  [[_ doc changes]]
  (repository/update-fields> doc changes))


(defmethod runtime/reify-effect> :fn
  [[_ f]]
  (f))

(defmethod runtime/reify-effect> :log
  [[ _ data]]
  (log ::reify-effect>--log
       :data data))
