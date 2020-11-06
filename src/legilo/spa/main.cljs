(ns legilo.spa.main
  (:require
   ["react-dom" :as rdom]
   [helix.core :refer [$]]

   [legilo.spa.api :refer [log]]
   [legilo.spa.desktop :as desktop]

   [legilo.spa.user]))


(defn main! []
  (log ::main!)
  (rdom/render ($ desktop/Desktop) (js/document.getElementById "app")))
