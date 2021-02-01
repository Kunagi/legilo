(ns spa.home-ui
  (:require
   ["@material-ui/core" :as mui]

   [commons.logging :refer [log]]
   [commons.models :as models :refer [def-model]]
   [commons.auth :as auth]

   [commons.mui :as ui :refer [defnc $]]

   [base.user :as user]

   [radar.radar :as radar]
   [radar.commands :as radar-commands]))


;;;
;;; Radars
;;;


(defnc Radar [{:keys [radar]}]
  ($ mui/Card
     ($ mui/CardActionArea
        {:component ui/Link
         :to (str "/ui/radars/" (-> radar :firestore/id))}
        ($ mui/CardContent
           (-> radar :title)))))

(defnc Radars []
  (let [uid (ui/use-uid)
        user (ui/use-doc user/Users uid)
        radars (ui/use-col-subset radar/RadarsForUser {:user user})
        ;; radars (ui/use-col [{:id "radars"
        ;;                             :where ["title" "==" "xxx"]}])
        ]
    ($ ui/Stack
       (when user
         ($ ui/Flexbox
            ($ ui/CommandButton
               {:command radar-commands/CreateRadar
                :context {:user user}})))
       (for [radar (->> radars (sort-by radar/title-in-lowercase))]
         ($ Radar
            {:key (-> radar :firestore/id)
             :radar radar})))))

(defnc HomePageContent []
  (let [uid (ui/use-uid)]
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
     {:onClick auth/sign-out
      :variant "contained"
      :color "secondary"}
     "Sign Out"))

(defnc CurrentUserCard []
  (when-let [{:keys [user]} (ui/use-context-data)]
    ($ mui/Card
       ($ mui/CardContent
          ($ :div
             "Signed in as "
             ($ :span
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
