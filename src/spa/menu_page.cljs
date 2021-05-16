(ns spa.menu-page
  (:require
   ["@material-ui/core" :as mui]

   [spark.logging :refer [log]]
   [spark.auth :as auth]

   [spark.ui :as ui :refer [def-ui def-page $]]

   [base.user :as user]

   [radar.radar :as radar]
   [radar.commands :as radar-commands]))

(def-ui SignOutButton []
  ($ mui/Button
     {:onClick auth/sign-out>
      :variant "contained"
      :color   "secondary"}
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

(def-page menu-page
  {:path          ["menu"]
   :content       MenuPageContent
   :data          {:uid  :uid
                   :user :user}
   :force-sign-in true
   })
