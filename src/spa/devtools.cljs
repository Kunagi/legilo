(ns spa.devtools
  (:require

   ["@material-ui/core" :as mui]

   [spa.context :as context]
   [spa.ui :as ui :refer [defnc $ <> div]]))


(defnc DevTools []
  (div
   {:style {:padding-top "2rem"}}
   ($ ui/Stack
      ($ mui/Divider)
      (div {:style {:color "grey"}} "DevTools")

      (ui/data {:goog.DEBUG js/goog.DEBUG
                :uid (context/use-uid)})

      )))
