(ns spa.api
  (:require
   [cljs.pprint :refer [pprint]]
   [helix.core :refer [defnc $]]
   [helix.hooks :as hooks]
   [helix.dom :as d]

   ["react-router-dom" :as router]

   ["@material-ui/core" :as mui]
   ["@material-ui/core/styles" :as mui-styles]

   [commons.logging :as logging]
   [commons.firestore-hooks :as firestore-hooks]
   [commons.firestore :as fs]
   ))


(defn dev-mode? []
  ^boolean js/goog.DEBUG)


(def log logging/log)

;;;
;;; Store
;;;

(def doc-id fs/doc-id)


