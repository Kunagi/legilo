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
  (let [user (ui/use-doc user/User uid)]
    (when user
      ($ mui/Tooltip
         {:title (user/best-display-name user)}
         ($ mui/Avatar
            {:src (user/best-photo-url user)
             :alt (user/best-display-name user)})))))
