(ns radar.ui
  (:require
   [clojure.string :as str]
   [clojure.edn :as edn]
   [cljs.pprint :refer [pprint]]
   ["@material-ui/core" :as mui]

   [spark.utils :as u]
   [spark.logging :refer [log]]

   [spark.core :as spark :refer [def-page]]
   [spark.ui :as ui :refer [def-ui def-ui-test $]]


   [radar.radar :as radar]
   [radar.book :as book]
   [radar.commands :as commands]))


(def-ui MenuIcon [radar]
  {:from-context [radar]}
  (when radar
    ($ mui/IconButton
       {:component ui/Link
        :to (str "/ui/radars/" (-> radar :id) "/config")}
       ($ :div {:class "i material-icons"} "settings"))))


(def-ui RadarAppbarTitle [radar]
  {:from-context [radar]}
  (-> radar :title))
