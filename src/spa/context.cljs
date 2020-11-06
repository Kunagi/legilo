(ns spa.context
  (:require

   [spa.api :refer [log]]
   [spa.ui :as ui]))


(defonce SIGN_IN (atom nil))

(defn set-sign-in-f [f]
  (reset! SIGN_IN f))

;;; current user

(defonce USER (atom nil))

(defn set-user [user]
  (js/console.log "SET USER" user)
  (js/console.log "CURRENT USER" @USER)
  (js/console.log "USER=?" (= user @USER))
  (when-not (= user @USER)
    (js/console.log "USER CHANGED" user)
     (log ::user-changed :user user)
     (reset! USER user)))

(defn user []
  @USER)

(defn uid []
  (when-let [user (user)]
    (-> user :uid)))

(def use-user (ui/atom-hook USER))

(defn use-uid []
  (when-let [user (use-user)]
    (js/console.log "USE-UID" user (-> user :uid))
    (-> user :uid)))


(defn wrap-action-in-sign-in [action-f]
  (fn [& args]
    (if (user)
      (apply action-f args)
      (@SIGN_IN))))

