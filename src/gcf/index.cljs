(ns gcf.index
  (:require
   ["firebase-admin" :as admin]
   ))


(-> admin .initializeApp)


(def exports
  #js
  {
   ;; :debugMywareLokationen (f/https-debug> mw-lok/handle-debug>)
   })
