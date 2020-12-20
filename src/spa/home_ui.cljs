(ns spa.home-ui
  (:require
   ["@material-ui/core" :as mui]

   [commons.logging :refer [log]]
   [commons.context :as c.context]
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
  (let [{:keys [user uid]} (c.context/use-context-data)
        domain (-> user :auth-domain)
        radars (c.context/use-cols-union
                [(radar-repository/radars-by-uid-col-path (context/use-uid))
                 (radar-repository/radars-by-domain-col-path domain)])]
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
     ($ ui/UserGuard
        ($ Radars))))



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
  (when-let [{:keys [user]} (c.context/use-context-data)]
    ($ mui/Card
       ($ mui/CardContent
          (div
           "Signed in as "
           (cui/span
            {:style {:font-weight :bold}}
            (-> user :auth-email)
            " / "
            (-> user :auth-display-name))))
       ($ mui/CardActions
          ($ SignOutButton)))))


(defnc MenuPageContent []
  ($ mui/Container
     {:maxWidth "sm"}
     ($ ui/Stack
        ($ CurrentUserCard))))
