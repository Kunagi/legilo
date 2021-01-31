(ns spa.home-ui
  (:require
   ["@material-ui/core" :as mui]

   [commons.logging :refer [log]]
   [commons.models :as models :refer [def-model]]
   [commons.context :as c.context]
   [commons.mui :as cui :refer [defnc $]]

   [base.context :as context]
   [base.service :as service]

   [radar.radar :as radar]
   [radar.commands :as radar-commands]
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
  (let [user (context/use-user)
        radars (c.context/use-col-subset radar/RadarsForUser {:user user})]
    ($ cui/Stack
       (when user
         ($ cui/Flexbox
            ($ cui/CommandButton
               {:command radar-commands/CreateRadar
                :context {:user user}})))
       (for [radar (->> radars (sort-by radar/title-in-lowercase))]
         ($ Radar
            {:key (-> radar :firestore/id)
             :radar radar})))))


(defnc HomePageContent []
  (let [uid (context/use-uid)]
    (when uid
      ($ Radars))))


(def-model HomePage
  [models/Page
   {:path "/"
    :content HomePageContent
    :data {}}])

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
          ($ :div
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


(def-model MenuPage
  [models/Page
   {:path "/ui/menu"
    :content MenuPageContent
    :data {:uid :uid
           :user :user}}])
