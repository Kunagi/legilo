(ns spa.home-ui
  (:require
   ["@material-ui/core" :as mui]

   [spark.logging :refer [log]]
   [spark.auth :as auth]

   [spark.core :as spark :refer [def-page]]
   [spark.ui :as ui :refer [def-ui $]]

   [base.user :as user]

   [radar.radar :as radar]
   [radar.commands :as radar-commands]))


;;;
;;; Radars
;;;


(def-ui Radar [radar]
  ($ mui/Card
     ($ mui/CardActionArea
        {:component ui/Link
         :to (str "/ui/radars/" (-> radar :firestore/id))}
        ($ mui/CardContent
           (-> radar :title)))))

(def-ui Radars [user]
  {:from-context [user]}
  (let [radars (ui/use-cols-union (radar/union-col-paths--for-user user))]
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

(def-ui HomePageContent []
  (let [uid (ui/use-uid)]
    (when uid
      ($ Radars))))

(def-page HomePage
  {:path "/"
   :content HomePageContent})

;;;
;;; Sidebar Main Menu
;;;

(defn create-example-radar> [])

(def-ui SignOutButton []
  ($ mui/Button
     {:onClick auth/sign-out
      :variant "contained"
      :color "secondary"}
     "Sign Out"))

(def-ui CurrentUserCard [user]
  {:from-context [user]}
  (when user
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

(def-ui MenuPageContent []
  ($ CurrentUserCard))

(def-page MenuPage
  {:path "/ui/menu"
   :content MenuPageContent
   :data {:uid :uid
          :user :user}})
