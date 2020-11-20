(ns spa.main
  (:require
   ["react-dom" :as rdom]
   [helix.core :refer [$]]

   [commons.firestore-init-spa]

   [spa.api :refer [log]]
   [spa.desktop :as desktop]

   [spa.user]))


(defn main! []
  (log ::main!)
  (rdom/render ($ desktop/Desktop) (js/document.getElementById "app")))
