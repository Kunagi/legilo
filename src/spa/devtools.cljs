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


      #_(ui/data (->> (firestore-hooks/use-cols-union
                     [[{:id "radars"
                        :where ["allow-domain" "==" "koczewski.de"]}]
                      [{:id "radars"
                        :where ["uids" "array-contains" "G4fCIVVCTpUxXMmht0s25jZMnoM2"]}]])
                    (map :title)))

      ;; (ui/data (->> (ui/use-col [{:id "radars"
      ;;                                    :where ["allow-domain" "==" "koczewski.de"]}])
      ;;               (map :title)))

      ;; (ui/data (->> (ui/use-col [{:id "radars"
      ;;                                    :where ["uids" "array-contains" "G4fCIVVCTpUxXMmht0s25jZMnoM2"]}])
      ;;               (map :title)))

      ;; (ui/data {:goog.DEBUG js/goog.DEBUG
      ;;            :uid (context/use-uid)})
      )))
