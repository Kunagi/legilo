(ns spa.devtools
  (:require

   ["@material-ui/core" :as mui]

   [spark.utils :as u]
   [spark.ui :as ui :refer [def-ui $ ]]
    
))


(def-ui DevTools []
  ($ :div
   {:style {:padding-top "2rem"}}
   ($ ui/Stack
      ($ mui/Divider)
      ($ :div {:style {:color "grey"}} "DevTools")

      (ui/data (u/conform-js-data
                (clj->js
                 {:hallo "welt"
                  :books {:b1 {:id "b1"
                               :recs {:r1 {:id "r1"}}
                               :uids ["a" "b" "c" 22]}}})
                [:map
                 [:books [:map-of
                          :string
                          [:map
                           [:recs [:map-of :string [:map]]]
                           [:uids [:set :string]]]]]]))


      )))
