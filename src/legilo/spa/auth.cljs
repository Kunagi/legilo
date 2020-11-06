(ns legilo.spa.auth
  (:require

   ["@material-ui/core" :as mui]

   [legilo.spa.api :refer [log]]
   [legilo.spa.ui :as ui :refer [defnc $ <> div]]
   [legilo.spa.context :as context]
   ))

;;; firebase auth setup

(def ^js firebase (-> js/window .-firebase))

(-> firebase .auth (.useDeviceLanguage))

(-> firebase
    .auth
    (.onAuthStateChanged (fn [user]
                           (log ::auth-state-changed
                                :user user)
                           (context/set-user user))))

(defn sign-in-with-microsoft []
  ;; https://firebase.google.com/docs/auth/web/microsoft-oauth?authuser=0
  ;; https://docs.microsoft.com/en-us/azure/active-directory/develop/quickstart-register-app
  )

(defn sign-in-with-google []
  (log ::sign-in-with-google)
  (let [GoogleAuthProvider (-> firebase .-auth .-GoogleAuthProvider)
        provider (GoogleAuthProvider.)]
    (.addScope ^js provider "openid")
    (.addScope ^js provider "profile")
    (.addScope ^js provider "email")
    (.addScope ^js provider "https://www.googleapis.com/auth/userinfo.email")
    (.addScope ^js provider "https://www.googleapis.com/auth/userinfo.profile")
    (-> firebase
        .auth
        (.signInWithPopup ^js provider)
        (.then #(log ::signInWithPopup-completed
                     :user-credential %)
               #(log ::signInWithPopup-failed
                     :error %)))))


(defn sign-in []
  (sign-in-with-google))

(context/set-sign-in-f sign-in)


(defn sign-out []
  (-> firebase .auth .signOut)
  (js/window.location.replace "/"))


;;; ui


(defnc LoginIcon []
  (div {:class "i material-icons"} "login"))

(defnc SignInButton []
  ($ mui/Button
     {:onClick sign-in
      :size "small"
      :variant "contained"
      :color "secondary"
      :startIcon ($ LoginIcon)}
     "Sign in"))

(defnc SignOutButton []
  ($ mui/Button
     {:onClick sign-out
      :variant "contained"
      :color "secondary"
      :startIcon ($ LoginIcon)}
     "Sign Out"))

(defnc Menu [{:keys [^ js user to]}]
  ($ mui/IconButton
     {:component ui/Link
      :to to}
     (div {:class "i material-icons"} "menu")))

(defnc SignInButtonOrMenu [{:keys [to]}]
  (if-let [user (context/use-user)]
    ($ Menu {:user user :to to})
    ($ SignInButton)))


(defnc Guard [{:keys [children]}]
  (if (context/use-user)
    children
    (div "Sign in required")))


(defnc CurrentUserCard []
  (when-let [user (context/use-user)]
    ($ mui/Card
       ($ mui/CardContent
          (div
           "Signed in as "
           (ui/span
            {:style {:font-weight :bold}}
            (-> user .-email))))
       ($ mui/CardActions
          ($ SignOutButton)))))
