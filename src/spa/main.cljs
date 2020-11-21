(ns spa.main
  (:require
   ["react-dom" :as rdom]
   [helix.core :refer [$]]

   [commons.logging :refer [log]]
   [commons.firestore-init-spa]

   [base.service]

   [spa.desktop :as desktop]
))




(defn main! []
  (log ::main!)
  (rdom/render ($ desktop/Desktop) (js/document.getElementById "app")))
