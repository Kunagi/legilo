(ns spa.desktop
  (:require
   ["react-router-dom" :as router]

   ["@material-ui/core" :as mui]
   ["@material-ui/core/colors" :as colors]

   [spark.logging :refer [log]]
   [spark.ui :as ui :refer [defnc $]]

   [spark.auth :as auth]

   [spa.devtools :as devtools]
   [radar.ui :as radar-ui]
   ))


;;; MUI Theme
;; https://material.io/resources/color/#!/?view.left=0&view.right=0&primary.color=3E2723&secondary.color=FF9800
(def theme
  {:palette {:primary {:main (-> colors .-brown (aget 900))}
             :secondary {:main (-> colors .-orange (aget 500))}}})
  


(defn styles [theme]
  {"& .CardContent--book" {:padding-top (-> theme (.spacing 0.5))
                           :padding-bottom (-> theme (.spacing 0.5))
                           :min-height (-> theme (.spacing 7))
                           :flex "1"}

   "& .MuiCard-root" {:overflow "unset"}

   "& .b" {:font-weight "bold"}

   "& .flex-grow-1" {:flex-grow 1}

   "& .ml-1" {:margin-left (-> theme (.spacing 1))}

   "& .Color--Primary" {:color (-> theme .-palette .-primary .-main)}

   "& .MuiAppBar-root a" {:color "white"
                          :text-decoration "none"}

   "& #App" {:position "absolute"
             :height "100%"
             :width "100%"
             :display "flex"
             :flex-direction "column"}

   "& #AppTitle" {:font-weight    900
                  :letter-spacing 1}

   "& #AppContent" {:height   "100%"
                    :overflow "auto"
                    :padding ((-> theme .-spacing) 1)}

   "& .BookCardMedia" {:width "140px"
                       :padding-bottom "150%"
                              ;:background-size "cover"
                              ;:background-position "center"
                       }
   "& .Recommendation .MuiPaper-rounded" {:border-top-left-radius 0
                                          :border-radius (-> theme (.spacing 2))}})





(defnc LoginIcon []
  ($ :div {:class "i material-icons"} "login"))


(defnc SignInButton []
  ($ mui/Button
     {:onClick auth/sign-in
      :size "small"
      :variant "contained"
      :color "secondary"
      :startIcon ($ LoginIcon)}
     "Sign in"))


(defnc MenuButton [{:keys [to]}]
  ($ mui/IconButton
     {:component ui/Link
      :to to}
     ($ :div {:class "i material-icons"} "menu")))


(defnc SignInButtonOrMenu [{:keys [to]}]
  (if (ui/use-uid)
    ($ MenuButton {:to to})
    ($ SignInButton)))


(defnc AppBar []
  ($ mui/AppBar
     {:position "static"}
     ($ :div
        {:style {:display :flex
                 :justify-content "space-between"}}
        ($ mui/Toolbar
           ($ ui/Link
              {:to "/"
               :stlye {:color "white"}}
              ($ mui/Typography
                 {:id "AppTitle"
                  :variant "h6"}
                 "Legilo"
                 ($ :span {:style {:font-weight 300}}
                    " | Book Radar"))))
        ($ mui/Toolbar
           ($ router/Switch
              ($ router/Route {:path "/ui/radars/:radarId"}
                 ($ radar-ui/MenuIcon)))
           ($ SignInButtonOrMenu
              {:to "/ui/menu"}))
        )))


(defnc AppContent []
  ($ :div
     {:id "AppContent"
      :style {:overflow-y "scroll"}}
     ($ ui/PageContent)
     (when ^boolean js/goog.DEBUG ($ devtools/DevTools))
     ($ ui/VersionInfo)))


(defnc Desktop [{:keys [spa]}]
  ($ ui/AppFrame
     {:spa spa
      :theme theme
      :styles styles}
     ($ :div
        {:id "App"}
        ($ AppBar)
        ($ AppContent))))
