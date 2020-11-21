(ns spa.home-ui
  (:require
   ["@material-ui/core" :as mui]

   [commons.logging :refer [log]]
   [commons.mui :as cui :refer [defnc $ <> div]]

   [base.context :as context]
   [base.service :as service]
   [base.ui :as ui]
   ))


;;;
;;; Radars
;;;

(defnc Radar [{:keys [radar]}]
  ($ mui/Card
     ($ mui/CardActionArea
        {:component cui/Link
         :to (str "/ui/radars/" (-> radar :firestore/id))}
        ($ mui/CardContent
           (-> radar :name)))))


(defn use-radars []
  (ui/use-col ["radars"]))


(defn show-new-radar-form []
  (cui/show-form-dialog
   {:fields [{:id :name
              :label "Pick a name for your Radar"
              :value "My Radar"}]
    :submit #(service/create-radar> (get % :name))}))


(defnc Radars []
  (let [radars (use-radars)]
    ($ cui/Stack
       ($ cui/Flexbox
          ($ cui/Button
             {:text "Create new Radar"
              :onClick show-new-radar-form}))
     (for [radar radars]
       ($ Radar
          {:key (-> radar :firestore/id)
           :radar radar})))))


(defnc HomePageContent []
  ($ mui/Container
     {:maxWidth "sm"}
     ($ Radars)))



;;;
;;; Sidebar Main Menu
;;;

(defn create-example-radar> [])

(defnc SignOutButton []
  ($ mui/Button
     {:onClick service/sign-out
      :variant "contained"
      :color "secondary"}
     "Sign Out"))

(defnc CurrentUserCard []
  (when-let [user (context/use-user)]
    ($ mui/Card
       ($ mui/CardContent
          (div
           "Signed in as "
           (cui/span
            {:style {:font-weight :bold}}
            (-> user :email)
            " / "
            (-> user :display-name))))
       ($ mui/CardActions
          ($ SignOutButton)))))

(defnc MenuDevCard []
  ($ cui/SimpleCard
     {:title "Developer Tools"}
     ($ cui/Button
        {:text "Create Example Radar"
         :onClick create-example-radar>})))


(defnc MenuPageContent []
  ($ mui/Container
     {:maxWidth "sm"}
     ($ ui/Stack
        ($ CurrentUserCard)
        (when js/goog.DEBUG
          ($ MenuDevCard)))))
