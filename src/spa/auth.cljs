(ns spa.auth
  (:require

   ["@material-ui/core" :as mui]

   [spark.logging :refer [log]]
   [spark.auth :as c.auth]
   [spark.ui :as ui :refer [def-ui $]]

   ))


(defonce LOGIN_DIALOG (atom nil))

(def use-login-dialog (ui/atom-hook LOGIN_DIALOG))

(defonce EMAIL_SIGN_IN (atom nil))

(def use-email-sign-in (ui/atom-hook EMAIL_SIGN_IN))

(defn send-sign-in-ling-to-email [email url]
  (log ::sign-in-with-email
       :email email
       :url url)
  (let [settings {:url             url
                  :handleCodeInApp true}]
    (-> c.auth/firebase
        .auth
        (.sendSignInLinkToEmail email (clj->js settings))
        (.then #(do
                  (js/window.localStorage.setItem "signInEmail" email)
                  (swap! EMAIL_SIGN_IN assoc
                         :email email
                         :status :waiting-for-email))
               #(reset! EMAIL_SIGN_IN {:error %}))))
  )

(defn sign-in-with-email []
  (log ::sign-in-with-email
       :url (-> js/window.location.href))
  (reset! EMAIL_SIGN_IN {:status :input-email
                         :url    (-> js/window.location.href)}))

(defn show-login-dialog []
  (reset! LOGIN_DIALOG {:open? true}))

(defn hide-login-dialog []
  (reset! LOGIN_DIALOG nil))

(def-ui LoginSelector []
  (let [email-sign-in     (use-email-sign-in)
        [email set-email] (ui/use-state (-> email-sign-in :email))]
    (if email-sign-in
      ($ ui/Stack
         #_(ui/data email-sign-in)
         (when (= :waiting-for-email (-> email-sign-in :status))
           "Öffne deine E-Mail und Klicke den Link!")
         (when (= :sending-email (-> email-sign-in :status))
           "E-Mail wird versendet...")
         (when (= :input-email (-> email-sign-in :status))
           ($ :form
              {:onSubmit (fn [^js event]
                           (swap! EMAIL_SIGN_IN assoc
                                  :email email
                                  :status :sending-email)
                           (-> event .preventDefault)
                           (send-sign-in-ling-to-email email
                                                       (-> email-sign-in :url))
                           false)}
              ($ mui/TextField
                 {:defaultValue email
                  :onChange     #(set-email (-> % .-target .-value))
                  :id           "email"
                  :name         "email"
                  :type         "email"
                  :label        "E-Mail"
                  :autoFocus    true
                  :fullWidth    true}))))
      ($ ui/Stack
         ($ ui/Button
            {:text      "Microsoft"
             :onClick   c.auth/sign-in-with-microsoft
             :startIcon :login })
         ($ ui/Button
            {:text      "Google"
             :onClick   c.auth/sign-in-with-google
             :startIcon :login })

         ($ ui/Button
            {:text      "E-Mail"
             :onClick   sign-in-with-email
             :startIcon :login })
         ($ :div)))))


(def-ui LoginSelectorDialog []
  (let [login-dialog (use-login-dialog)]
    ($ mui/Dialog
       {:open    (-> login-dialog :open? boolean)
        :onClose hide-login-dialog}
       ($ mui/DialogTitle
          "Wie möchtest Du dich anmelden?")
       ($ mui/DialogContent
          ($ LoginSelector))
       )))

(defn sign-in []
  (show-login-dialog))
