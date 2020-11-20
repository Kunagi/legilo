(ns spa.nav
  (:require

   ["@material-ui/core" :as mui]

   [commons.mui :as cmui]
   [commons.firestore :as firestore]

   [spa.api :refer [log]]
   [spa.auth :as auth]
   [spa.ui :as ui :refer [defnc $ <>]]
   ))


(defn create-example-radar> [])


(defnc DevCard []
  ($ cmui/SimpleCard
     {:title "Developer Tools"}
     ($ cmui/Button
        {:text "Create Example Radar"
         :onClick create-example-radar>})))


(defnc PageContent []
  ($ mui/Container
     {:maxWidth "sm"}
     ($ ui/Stack
        ($ auth/CurrentUserCard)
        (when js/goog.DEBUG
          ($ DevCard)))))
