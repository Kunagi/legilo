(ns spa.devtools
  (:require

   ["@material-ui/core" :as mui]

   [commons.mui :as cui :refer [defnc $ ]]
    
))


(defnc DevTools []
  ($ :div
   {:style {:padding-top "2rem"}}
   ($ cui/Stack
      ($ mui/Divider)
      ($ :div {:style {:color "grey"}} "DevTools")

      (cui/data (cui/use-context-data))

      #_(cui/data (->> (firestore-hooks/use-cols-union
                     [[{:id "radars"
                        :where ["allow-domain" "==" "koczewski.de"]}]
                      [{:id "radars"
                        :where ["uids" "array-contains" "G4fCIVVCTpUxXMmht0s25jZMnoM2"]}]])
                    (map :title)))

      ;; (ui/data (->> (cui/use-col [{:id "radars"
      ;;                                    :where ["allow-domain" "==" "koczewski.de"]}])
      ;;               (map :title)))

      ;; (ui/data (->> (cui/use-col [{:id "radars"
      ;;                                    :where ["uids" "array-contains" "G4fCIVVCTpUxXMmht0s25jZMnoM2"]}])
      ;;               (map :title)))

      ;; (cui/data {:goog.DEBUG js/goog.DEBUG
      ;;            :uid (context/use-uid)})
      )))
