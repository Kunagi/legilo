(ns spa.amazon
  (:require
   ["@material-ui/core" :as mui]

   [spa.api :as api :refer [log]]
   [spa.ui :as ui :refer [defnc $ <> div]]
   [spa.context :as context]))


(def partner-id "legilo-21")


(defn href [asin]
  (str "https://www.amazon.de/gp/product/" asin
       "?tag=" partner-id))


(defn ImageLink [asin]
  ($ :a
     {:target "_blank"
      :href (href asin)}
     ($ :img
        {:border 0
         :src (str "//ws-eu.amazon-adsystem.com/widgets/q?MarketPlace=DE&ASIN="
                   asin
                   "&ServiceVersion=20070822&ID=AsinImage&WS=1&Format=_SL160_&tag=" partner-id)}
        )))
