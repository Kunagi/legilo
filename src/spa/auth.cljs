(ns spa.auth
  (:require
   [cljs-bean.core :as cljs-bean]

   ["@material-ui/core" :as mui]

   [spa.api :refer [log]]
   [spa.ui :as ui :refer [defnc $ <> div]]
   [spa.context :as context]
   ))

;;; firebase auth setup

(def ^js firebase (-> js/window .-firebase))

(-> firebase .auth (.useDeviceLanguage))

(-> firebase
    .auth
    (.onAuthStateChanged (fn [^js user_js]
                           (context/set-user
                            (when user_js
                              {:uid (-> ^js user_js .-uid)
                               :email (-> ^js user_js .-email)
                               :display-name (-> ^js user_js .-displayName)})))))


(defn sign-in-with-microsoft []
  ;; https://firebase.google.com/docs/auth/web/microsoft-oauth?authuser=0
  ;; https://docs.microsoft.com/en-us/azure/active-directory/develop/quickstart-register-app
  ;; https://docs.microsoft.com/en-us/azure/active-directory/azuread-dev/v1-protocols-oauth-code
  ;; pplication (client) ID: fc6ee63d-375e-432c-b3d9-2722eba6a0ee
  ;; Directory (tenant) ID: 088fc83d-8363-42a6-8b7f-02c6f326cb17
  ;; Object ID; 5275d8a6-c7de-4367-a67f-7955ff60c4d6
  (let [AuthProvider (-> firebase .-auth .-OAuthProvider)
        provider (AuthProvider. "microsoft.com")]
    (-> ^js provider
        (.setCustomParameters
         (clj->js {
                   ;; :prompt    "consent"
                   :prompt    "login"
                   })))
    (-> firebase
        .auth
        (.signInWithPopup ^js provider)
        (.then #(log ::signInWithPopup-completed
                     :user-credential %)
               #(log ::signInWithPopup-failed
                     :error %))
        (.catch #(log ::signInWithPopup-failed
                      :error %)))))

(defn sign-in-with-google []
  (log ::sign-in-with-google)
  (let [AuthProvider (-> firebase .-auth .-GoogleAuthProvider)
        provider (AuthProvider.)]
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
                     :error %))
        (.catch #(log ::signInWithPopup-failed
                      :error %)))))

(defn sign-in []
  (sign-in-with-microsoft))

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

(defnc Menu [{:keys [to]}]
  ($ mui/IconButton
     {:component ui/Link
      :to to}
     (div {:class "i material-icons"} "menu")))

(defnc SignInButtonOrMenu [{:keys [to]}]
  (if-let [uid (context/use-uid)]
    ($ Menu {:to to})
    ($ SignInButton)))


(defnc Guard [{:keys [children]}]
  (if (context/use-uid)
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
            (-> user :email)
            " / "
            (-> user :display-name))))
       ($ mui/CardActions
          ($ SignOutButton)))))
