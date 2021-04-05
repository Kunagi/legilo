(ns radar.ui
  (:require
   ["@material-ui/core" :as mui]

   [spark.ui :as ui :refer [def-ui $]]

   ))

(def-ui MenuIcon [radar]
  {:from-context [radar]}
  (when radar
    ($ mui/IconButton
       {:component ui/Link
        :to        (str "/ui/radars/" (-> radar :id) "/config")}
       ($ :div {:class "i material-icons"} "settings"))))


(def-ui RadarAppbarTitle [radar]
  {:from-context [radar]}
  (-> radar :title))
