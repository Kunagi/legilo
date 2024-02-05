(ns radar.ui
  (:require
   ["@material-ui/core" :as mui]

   [spark.ui :as ui :refer [def-ui $]]

   [base.user :as user]
   ))

(def-ui ConfigMenuIcon [radar]
  {:from-context [radar]}
  (when radar
    ($ mui/IconButton
       {:component ui/RouterLink
        :to        (str "/ui/radars/" (-> radar :id) "/config")}
       ($ :div {:class "i material-icons"} "settings"))))

(def-ui ActivitylogMenuIcon [radar]
  {:from-context [radar]}
  (when radar
    ($ mui/IconButton
       {:component ui/RouterLink
        :to        (str "/ui/radars/" (-> radar :id) "/activitylog")}
       ($ :div {:class "i material-icons"} "history"))))


(def-ui RadarAppbarTitle [radar]
  {:from-context [radar]}
  (-> radar :title))


(def-ui Avatar [uid]
  (let [user (ui/use-doc user/User uid)
        text (str (user/best-display-name user)
                      " " (user/auth-email user))]
    (when user
      ($ mui/Tooltip
         {:title text}
         ($ mui/Avatar
            {:src (user/best-photo-url user)
             :alt text})))))
