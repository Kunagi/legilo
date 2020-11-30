(ns spa.home-ui
  (:require
   ["@material-ui/core" :as mui]

   [commons.logging :refer [log]]
   [commons.mui :as cui :refer [defnc $ <> div]]

   [base.context :as context]
   [base.service :as service]
   [base.ui :as ui]

   [radar.radar :as radar]
   [radar.repository :as radar-repository]
   [radar.service :as radar-service]
   ))


;;;
;;; Radars
;;;

(defnc Radar [{:keys [radar]}]
  ($ mui/Card
     ($ mui/CardActionArea
        {:component cui/Link
         :to (str "/ui/radars/" (-> radar :firestore/id))}
        ($ mui/CardContent
           (-> radar :title)))))


(defnc Radars []
  (let [uid (context/use-uid)
        radars (ui/use-col (radar-repository/visible-radars-col-path
                            (context/use-uid)))]
    ($ cui/Stack
       ($ cui/Flexbox
          ($ cui/Button
             {:command (radar-service/create-radar-command uid)}))
       (for [radar (->> radars (sort-by radar/title-in-lowercase))]
       ($ Radar
          {:key (-> radar :firestore/id)
           :radar radar})))))


(defnc HomePageContent []
  ($ mui/Container
     {:maxWidth "sm"}
     ($ Radars)))



;;;
;;; Sidebar Main Menu
;;;

(defn create-example-radar> [])

(defnc SignOutButton []
  ($ mui/Button
     {:onClick service/sign-out
      :variant "contained"
      :color "secondary"}
     "Sign Out"))


(defnc CurrentUserCard []
  (when-let [user (context/use-user)]
    ($ mui/Card
       ($ mui/CardContent
          (div
           "Signed in as "
           (cui/span
            {:style {:font-weight :bold}}
            (-> user :email)
            " / "
            (-> user :display-name))))
       ($ mui/CardActions
          ($ SignOutButton)))))


(defnc MenuPageContent []
  ($ mui/Container
     {:maxWidth "sm"}
     ($ ui/Stack
        ($ CurrentUserCard))))
