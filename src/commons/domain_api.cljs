(ns commons.domain-api
  (:require-macros [commons.domain-api]))


(defn doc-id [doc]
  (let [id (get doc :firestore/id)]
    (when-not id (throw (ex-info "Missing :firestore/id in document."
                                 {:doc doc})))
    id))
