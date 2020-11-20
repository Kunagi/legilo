(ns base.service
  (:require
   ["@material-ui/core" :as mui]

   [commons.firestore :as firestore]
   [commons.logging :refer [log]]

   [base.context :as context]
   ))


(defn update-last-usage [uid email display-name photo-url phone-number]
  (log ::update-last-usage
       :uid uid
       :email email)
  (firestore/load-and-save>
   ["users" uid]
   #(merge % {:last-usage (firestore/timestamp)
              :auth-email email
              :auth-display-name display-name
              :auth-photo photo-url
              :auth-phone phone-number})))


(add-watch context/USER
           ::update
           (fn [_k _r _ov ^js user]
             (when user
               (log ::user-changed
                    :user user)
               (update-last-usage (-> user :uid)
                                  (-> user :email)
                                  (-> user :displayName)
                                  (-> user :photoURL)
                                  (-> user :phoneNumber)))))

;;;
;;; firebase auth setup
;;;

(def ^js firebase (-> js/window .-firebase))

(-> firebase .auth (.useDeviceLanguage))

(-> firebase
    .auth
    (.onAuthStateChanged (fn [^js user_js]
                           (context/set-user
                            (when user_js
                              {:uid (-> ^js user_js .-uid)
                               :email (-> ^js user_js .-email)
                               :display-name (-> ^js user_js .-displayName)
                               :photoURL (-> ^js user_js .-photoURL)
                               :phoneNumber (-> ^js user_js .-phoneNumber)})))))


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
        (.signInWithRedirect ^js provider))))


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


;;;
;;; Radars
;;;


(defn create-radar> [radar-name]
  (firestore/create-doc> ["radars"] {:name radar-name}))
