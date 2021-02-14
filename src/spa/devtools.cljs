(ns spa.devtools
  (:require

   ["@material-ui/core" :as mui]

   [spark.ui :as ui :refer [def-ui $ ]]
    
))


(def-ui DevTools []
  ($ :div
   {:style {:padding-top "2rem"}}
   ($ ui/Stack
      ($ mui/Divider)
      ($ :div {:style {:color "grey"}} "DevTools")


      )))
