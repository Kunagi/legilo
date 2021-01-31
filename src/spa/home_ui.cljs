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
   [radar.commands :as radar-commands]
   [radar.repository :as radar-repository]
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
        user (context/use-user)
        domain (-> user :auth-domain)
        radars (c.context/use-cols-union
                [(radar-repository/radars-by-uid-col-path (context/use-uid))
                 (radar-repository/radars-by-domain-col-path domain)])]
    ($ cui/Stack
       ($ cui/Flexbox
          ($ cui/CommandButton
             {:command radar-commands/CreateRadar
              :context {:uid uid}}))
       (for [radar (->> radars (sort-by radar/title-in-lowercase))]
         ($ Radar
            {:key (-> radar :firestore/id)
             :radar radar})))))


(defnc HomePageContent []
  (let [uid (context/use-uid)]
    (when uid
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
  ($ CurrentUserCard))
