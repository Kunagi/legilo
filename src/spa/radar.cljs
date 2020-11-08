(ns spa.radar
  (:require
   ["@material-ui/core" :as mui]

   [spa.api :as api :refer [log]]
   [spa.ui :as ui :refer [defnc $ <> div]]
   [spa.context :as context]))


(defn use-radar-id []
  (-> (ui/use-params) :radarId))


(defn use-radar []
  (ui/use-doc ["radars" (use-radar-id)]))


(defnc Radar []
  (let [radar (use-radar)]
    (div
     ($ mui/Typography
        {:variant "h4"
         :component "h2"}
        (-> radar :name))
     (ui/data (use-radar)))))


(defnc PageContent []
  ($ mui/Container
     {:maxWidth "sm"}
     ($ Radar)))
