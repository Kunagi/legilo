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
  (when-not (= user @USER)
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
    (-> user :uid)))


(defn wrap-action-in-sign-in [action-f]
  (fn [& args]
    (if (user)
      (apply action-f args)
      (@SIGN_IN))))

