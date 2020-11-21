(ns spa.desktop
  (:require
   [shadow.resource :as resource]

   ["react-router-dom" :as router]

   ["@material-ui/core" :as mui]
   ["@material-ui/core/styles" :as styles]
   ["@material-ui/core/colors" :as colors]

   [commons.logging :refer [log]]
   [commons.mui :as cmui :refer [defnc $]]

   [base.context :as context]
   [base.service :as service]
   [base.ui :as ui]

   [spa.devtools :as devtools]
   [spa.home-ui :as home]
   [radar.ui :as radar]))


;;; MUI Theme
;; https://material.io/resources/color/#!/?view.left=0&view.right=0&primary.color=3E2723&secondary.color=FF9800
(def theme
  {:palette {:primary {
                       :main (-> colors .-brown (aget 900))}
             :secondary {:main (-> colors .-orange (aget 500))}}})
  


(defn styles [theme]
  {:root {:position       "absolute"
          :height         "100%"
          :width          "100%"
          :display        "flex"
          :flex-direction "column"

          "& .MuiCard-root" {:overflow "unset"}

          "& .b" {:font-weight "bold"}

          "& .Color--Primary" {:color (-> theme .-palette .-primary .-main)}

          "& .MuiAppBar-root a" {:color "white"
                                 :text-decoration "none"}

          "& #AppTitle" {:font-weight    900
                         :letter-spacing 1}

          "& #AppContent" {:height   "100%"
                           :overflow "auto"
                           :padding ((-> theme .-spacing) 1)}

          "& .BookCardMedia" {:width "140px"
                              :padding-bottom "150%"
                              ;:background-size "cover"
                              ;:background-position "center"
                              }}})


(defnc PageSwitch []
  ($ router/Switch
     ($ router/Route {:path "/ui/menu"} ($ home/MenuPageContent))
     ($ router/Route {:path "/ui/radars/:radarId/book/:bookId"} ($ radar/BookPageContent))
     ($ router/Route {:path "/ui/radars/:radarId"} ($ radar/RadarPageContent))
     ($ router/Route {:path "/"} ($ home/HomePageContent))
     ))

(defnc LoginIcon []
  ($ :div {:class "i material-icons"} "login"))


(defnc SignInButton []
  ($ mui/Button
     {:onClick service/sign-in
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
  (if (context/use-uid)
    ($ MenuButton {:to to})
    ($ SignInButton)))


(defn AppBar []
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
         ($ SignInButtonOrMenu
            {:to "/ui/menu"})))))


(defn VersionInfo []
  ($ :div
   {:style {:margin-top "4rem"
            :text-align :right
            :color "lightgrey"
            :font-size "75%"}}
   "v1."
   (str (resource/inline "./version.txt"))
   " · "
   (str (resource/inline "./version-time.txt"))))


(defn AppContent []
  ($ :div
   {:id "AppContent"}
   ($ PageSwitch)
   (VersionInfo)
   (when ^boolean js/goog.DEBUG
     ($ devtools/DevTools))
   ))


(def use-app-styles (ui/make-styles styles))

(defnc Root []
  (let [theme (styles/useTheme)
        styles (use-app-styles theme)]
    ($ router/BrowserRouter
       {}
       ($ :div
        {:class (-> styles .-root)}
        (AppBar)
        (AppContent)
        ($ cmui/FormDialogsContainer)
        ))))


(defnc Desktop []
  (let [theme (-> theme
                  clj->js
                  styles/createMuiTheme
                  styles/responsiveFontSizes)]
    (log ::theme :theme theme)
    ($ mui/ThemeProvider
       {:theme theme}
       ($ mui/CssBaseline)
       ($ Root))))
