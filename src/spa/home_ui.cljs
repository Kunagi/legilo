(ns spa.home-ui
  (:require
   ["@material-ui/core" :as mui]

   [commons.logging :refer [log]]
   [commons.mui :as cmui :refer [defnc $ <> div]]

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
        {:component cmui/Link
         :to (str "/ui/radars/" (-> radar :firestore/id))}
        ($ mui/CardContent
           (-> radar :name)))))


(defn use-radars []
  (ui/use-col ["radars"]))


(defn show-new-radar-form []
  (ui/show-form-dialog
   {:fields [{:id :name
              :label "Pick a name for your Radar"}]
    :submit #(service/create-radar> (get % :name))}))


(defnc Radars []
  (let [radars (use-radars)]
    ($ cmui/Stack
       ($ cmui/Flexbox
          ($ cmui/Button
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
           (cmui/span
            {:style {:font-weight :bold}}
            (-> user :email)
            " / "
            (-> user :display-name))))
       ($ mui/CardActions
          ($ SignOutButton)))))

(defnc MenuDevCard []
  ($ cmui/SimpleCard
     {:title "Developer Tools"}
     ($ cmui/Button
        {:text "Create Example Radar"
         :onClick create-example-radar>})))


(defnc MenuPageContent []
  ($ mui/Container
     {:maxWidth "sm"}
     ($ ui/Stack
        ($ CurrentUserCard)
        (when js/goog.DEBUG
          ($ MenuDevCard)))))
