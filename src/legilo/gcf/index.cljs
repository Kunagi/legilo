(ns legilo.gcf.index
  (:require

   ["firebase-admin" :as admin]


   [legilo.gcf.functions :as f]))


(-> admin .initializeApp)


(def exports
  #js
  {
   ;; :debugMywareLokationen (f/https-debug> mw-lok/handle-debug>)
   })
