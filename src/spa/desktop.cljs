(ns spa.desktop
  (:require
   ["react-router-dom" :as router]

   ["@material-ui/core" :as mui]
   ["@material-ui/core/colors" :as colors]

   [spark.logging :refer [log]]
   [spark.utils :as u]
   [spark.ui :as ui :refer [def-ui $]]

   [spark.auth :as auth]
   [spa.auth :as legilo-auth]

   [spa.devtools :as devtools]
   [radar.ui :as radar-ui]
   ))


;;; MUI Theme
;; https://material.io/resources/color/#!/?view.left=0&view.right=0&primary.color=3E2723&secondary.color=FF9800
(def theme
  {:palette {:primary {:main (-> colors .-brown (aget 900))}
             :secondary {:main (-> colors .-orange (aget 500))}}})
  


(defn styles [theme]
  (js/console.log "THEME" (-> theme .-palette))
  {"& .CardContent--book" {:padding-top (-> theme (.spacing 0.5))
                           :padding-bottom (-> theme (.spacing 0.5))
                           :min-height (-> theme (.spacing 7))
                           :flex "1"}

   "& .MuiCard-root" {:overflow "unset"}

   "& .b" {:font-weight "bold"}

   "& .flex-grow-1" {:flex-grow 1}

   "& .ml-1" {:margin-left (-> theme (.spacing 1))}

   "& .Color--Primary" {:color (-> theme .-palette .-primary .-main)}

   ;; hack to fix header colors when using autocomplete in dialog
   ;; https://github.com/mui-org/material-ui/issues/16102
   "& header.MuiPaper-root" {:background-color (-> theme .-palette .-primary .-main)
                             :color (-> theme .-palette .-primary .-contrastText)}

   "& .MuiAppBar-root a" {:color "white"
                          :text-decoration "none"}

   "& .MuiAppBar-root .MuiIconButton-root" {:color "white"}

   "& .MuiAppBar-root .MuiToolbar-gutters" {:padding-left 0}

   "& #App" {:position (when-not ^boolean js/goog.DEBUG "absolute")
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
                                          :border-radius (-> theme (.spacing 2))}


   "& .RecommendationButton" {:box-shadow "0px 3px 1px -2px rgb(0 0 0 / 20%), 0px 2px 2px 0px rgb(0 0 0 / 14%), 0px 1px 5px 0px rgb(0 0 0 / 12%)"}})





(def-ui LoginIcon []
  ($ :div {:class "i material-icons"} "login"))


(def-ui SignInButton []
  ($ mui/Button
     {:onClick auth/sign-in
      :size "small"
      :variant "contained"
      :color "secondary"
      :startIcon ($ LoginIcon)}
     "Sign in"))


(def-ui MenuButton [{:keys [to]}]
  ($ mui/IconButton
     {:component ui/Link
      :to to}
     ($ :div {:class "material-icons"} "menu")))


(def-ui SignInButtonOrMenu [{:keys [to]}]
  (if (ui/use-uid)
    ($ MenuButton {:to to})
    ($ SignInButton)))


(def-ui AppBar []
  (let [page (ui/use-page)
        context (ui/use-spark-context)
        back-fn (-> page :back-to)
        back-url (u/fn->value back-fn context)]
    ($ mui/AppBar
       {:position "static"}
       (ui/div
        {:display :flex
         :justify-content "space-between"}
        ($ mui/Toolbar
           (if (or (= "/" js/location.pathname) (nil? back-url))
             (ui/div
              {:padding-left "24px"}
              ($ ui/Link
                 {:to "/"
                  :stlye {:color "white"}}
                 ($ mui/Typography
                    {:id "AppTitle"
                     :variant "h6"}
                    "Legilo")))
             ($ mui/IconButton
                {:to back-url
                 :component ui/Link}
                (ui/icon "arrow_back")))

           (when-let [component (-> page :appbar-title-component)]
             ($ mui/Typography
                {:variant "h6"}
                ($ :span {:style {:margin-left "8px"
                                  :letter-spacing 1
                                  :font-weight 300}}
                   ($ component)))))
        ($ mui/Toolbar
           (when ^boolean goog.DEBUG
             ($ mui/IconButton
                {:component ui/Link
                 :to "/ui/devcards"}
                ($ :div {:class "material-icons"} "developer_mode")))
           ($ radar-ui/MenuIcon)
           ($ SignInButtonOrMenu
              {:to "/ui/menu"}))))))



(def-ui AppContent []
  ($ :div
     {:id "AppContent"
      :style {:overflow-y "scroll"}}
     ($ ui/PageContent)
     (when ^boolean js/goog.DEBUG ($ devtools/DevTools))
     ($ ui/VersionInfo)))


(def-ui Desktop [{:keys [spa]}]
  ($ ui/AppFrame
     {:spa    spa
      :theme  theme
      :styles styles}
     ($ :div
        {:id "App"}
        ($ AppBar)
        ($ AppContent)
        ($ legilo-auth/LoginSelectorDialog)
        )))
