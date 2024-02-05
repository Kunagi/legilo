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
          ($ ui/Stack
             ($ :center
                {:style {:font-weight :bold
                         :font-size "120%"}}
                (-> user :auth-display-name))
             ($ :center
                {:style {:font-weight :bold}}
                (-> user :auth-email))
             ($ :center
                ($ mui/Avatar
                   {:src (user/best-photo-url user)
                    }))
             (when (and (user/best-photo-url user)
                        (-> (user/best-photo-url user) (.includes "gravatar.com")))
               ($ :center
                 ($ :a
                    {:href "https://gravatar.com"
                     :target "_blank"}
                    "Photo loaded from gravatar.com"))))
          (ui/DEBUG (user/best-photo-url user))
          (ui/DEBUG user))
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
