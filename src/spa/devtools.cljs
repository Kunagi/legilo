(ns spa.devtools
  (:require

   ["@material-ui/core" :as mui]

   [commons.firestore :as firestore]
   [commons.firestore-hooks :as firestore-hooks]

   [commons.mui :as ui :refer [defnc $ <> div]]
   [commons.context :as c.context]

   [base.context :as context]))


(defnc DevTools []
  (div
   {:style {:padding-top "2rem"}}
   ($ ui/Stack
      ($ mui/Divider)
      (div {:style {:color "grey"}} "DevTools")


      (ui/data (->> (firestore-hooks/use-cols-union
                     [[{:id "radars"
                        :where ["allow-domain" "==" "koczewski.de"]}]
                      [{:id "radars"
                        :where ["uids" "array-contains" "G4fCIVVCTpUxXMmht0s25jZMnoM2"]}]])
                    (map :title)))

      ;; (ui/data (->> (c.context/use-col [{:id "radars"
      ;;                                    :where ["allow-domain" "==" "koczewski.de"]}])
      ;;               (map :title)))

      ;; (ui/data (->> (c.context/use-col [{:id "radars"
      ;;                                    :where ["uids" "array-contains" "G4fCIVVCTpUxXMmht0s25jZMnoM2"]}])
      ;;               (map :title)))

      (ui/data {:goog.DEBUG js/goog.DEBUG
                :uid (context/use-uid)}))))
