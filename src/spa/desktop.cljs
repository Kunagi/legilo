(ns spa.desktop
  (:require
   [shadow.resource :as resource]

   ["react-router-dom" :as router]

   ["@material-ui/core" :as mui]
   ["@material-ui/core/styles" :as styles]
   ["@material-ui/core/colors" :as colors]

   [spa.api :refer [log]]
   [spa.ui :as ui :refer [defnc $ <> div]]
   [spa.devtools :as devtools]
   [spa.auth :as auth]
   [spa.nav :as nav]
   [spa.home :as home]
   [radar.ui :as radar]))


;;; MUI Theme
;; https://material.io/resources/color/#!/?view.left=0&view.right=0&primary.color=3E2723&secondary.color=FF9800
(def theme
  {:palette {:primary {
                       :main (-> colors .-brown (aget 900))}
             :secondary {:main (-> colors .-orange (aget 500))}}})
  


(defn styles [theme]
  {:root {
          :position       "absolute"
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
                           :padding ((-> theme .-spacing) 1)}}})



(defnc PageSwitch []
  ($ router/Switch
     ($ ui/Route {:path "/ui/nav"} ($ nav/PageContent))
     ($ ui/Route {:path "/ui/radars/:radarId/book/:bookId"} ($ radar/BookPageContent))
     ($ ui/Route {:path "/ui/radars/:radarId"} ($ radar/RadarPageContent))
     ($ ui/Route {:path "/"} ($ home/PageContent))
     ))


(defn AppBar []
  ($ mui/AppBar
     {:position "static"}
     (div
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
         ($ auth/SignInButtonOrMenu
            {:to "/ui/nav"})))))


(defn VersionInfo []
  (div
   {:style {:margin-top "4rem"
            :text-align :right
            :color "lightgrey"
            :font-size "75%"}}
   "v1."
   (str (resource/inline "./version.txt"))
   " · "
   (str (resource/inline "./version-time.txt"))))


(defn AppContent []
  (div
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
       (div
        {:class (-> styles .-root)}
        (AppBar)
        (AppContent)
        ($ ui/FormDialog)
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
