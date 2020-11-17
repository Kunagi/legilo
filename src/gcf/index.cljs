(ns gcf.index
  (:require
   ["firebase-admin" :as admin]
   [gcf.functions :as f]
   [gcf.amazon :as amazon]
   ))


(-> admin .initializeApp)



(def exports
  #js
  {
   ;; :debugAmazon (f/https-debug> amazon/handle-debug>)
   })
