(ns spa.desktop
  (:require
   [shadow.resource :as resource]

   ["react-router-dom" :as router]

   ["@material-ui/core" :as mui]
   ["@material-ui/core/styles" :as styles]
   ["@material-ui/core/colors" :as colors]

   [commons.logging :refer [log]]
   [commons.mui :as cmui :refer [defnc $]]

   [base.context :as b.context]
   [base.service :as service]
   [base.ui :as ui]

   [spa.devtools :as devtools]
   [spa.home-ui :as home]
   [radar.ui :as radar-ui]
   [radar.radar :as radar]))


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

          "& .CardContent--book" {:padding-top (-> theme (.spacing 0.5))
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
                                                 :border-radius (-> theme (.spacing 2))}}})


(defn pages []
  [
   {:path "/ui/menu"
    :content home/MenuPageContent
    :data {:uid :uid
           :user :user}}

   {:path "/ui/radars/:Radar/book/:bookId"
    :content radar-ui/BookPageContent
    :data {:uid :uid
           :user :user
           :radar [:param-doc radar/Radars]}}

   {:path "/ui/radars/:Radar/config"
    :content radar-ui/RadarConfigPageContent
    :data {:uid :uid
           :user :user
           :radar [:param-doc radar/Radars]}}

   {:path "/ui/radars/:Radar"
    :content radar-ui/RadarPageContent
    :data {:uid :uid
           :user :user
           :radar [:param-doc radar/Radars]}}

   {:path "/"
    :content home/HomePageContent
    :data {}}
   ])


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
  (if (b.context/use-uid)
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
     ($ cmui/PageSwitch
        {:pages (pages)
         :devtools-component (when ^boolean js/goog.DEBUG devtools/DevTools)})
     ;; ($ AppNav)
     ($ cmui/VersionInfo)))

(def use-app-styles (ui/make-styles styles))

(defnc Root []
  (let [theme (styles/useTheme)
        styles (use-app-styles theme)]
    ($ router/BrowserRouter
       {}
       ($ :div
        {:class (-> styles .-root)}
        ($ AppBar)
        ($ AppContent)
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
