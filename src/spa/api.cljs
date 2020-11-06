(ns spa.api
  (:require
   [cljs.pprint :refer [pprint]]
   [helix.core :refer [defnc $]]
   [helix.hooks :as hooks]
   [helix.dom :as d]

   ["react-router-dom" :as router]

   ["@material-ui/core" :as mui]
   ["@material-ui/core/styles" :as mui-styles]

   [spa.impl.logging :as logging]

   [spa.impl.firestore-hooks :as firestore-hooks]
   [spa.impl.firestore :as fs]
   ))


(defn dev-mode? []
  ^boolean js/goog.DEBUG)


(def log logging/log)

;;;
;;; Store
;;;

(def doc-id fs/doc-id)
(def update-doc> fs/update-doc>)
(def update--array-remove fs/update--array-remove)
(def update--array-union fs/update--array-union)
(def update--timestamp fs/update--timestamp)


;;;
;;; Formats
;;;

(def ^js eur-number-format
  (-> js/Intl (.NumberFormat "de-DE"
                             (clj->js {:style "currency"
                                       :currency "EUR"}))))

(defn eur [v]
  (when v
    (-> eur-number-format (.format v))))
