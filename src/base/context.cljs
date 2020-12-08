(ns base.context
  (:require

   [commons.logging :refer [log]]
   [commons.context :as c.context]
   ))


(defonce SIGN_IN (atom nil))

(defn set-sign-in-f [f]
  (reset! SIGN_IN f))

;;; current user

(defonce AUTH_USER (atom nil))

(defn set-user [user]
  (when-not (= user @AUTH_USER)
     (log ::user-changed :user user)
     (reset! AUTH_USER user)))

(defn auth-user []
  @AUTH_USER)

(defn uid []
  (when-let [user (auth-user)]
    (-> user :uid)))

(def use-auth-user (c.context/atom-hook AUTH_USER))

(defn use-uid []
  (when-let [user (use-auth-user)]
    (-> user :uid)))


(defn wrap-action-in-sign-in [action-f]
  (fn [& args]
    (if (auth-user)
      (apply action-f args)
      (@SIGN_IN))))

(defn use-user []
  (let [uid (use-uid)]
    (c.context/use-doc (when uid ["users" uid]))))
