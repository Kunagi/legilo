(ns legilo.spa.context
  (:require

   [legilo.utils :as u]

   [legilo.spa.api :refer [log]]
   [legilo.spa.ui :as ui]))


(defonce SIGN_IN (atom nil))

(defn set-sign-in-f [f]
  (reset! SIGN_IN f))

;;; current user

(defonce USER (atom nil))

(defn set-user [^js user]
  (when-not (= user @USER)
     (log ::user-changed :user ^js user)
     (reset! USER ^js user)))

(defn user []
  @USER)

(defn uid []
  (when-let [user (user)]
    (-> user .-uid)))

(def use-user (ui/atom-hook USER))

(defn use-uid []
  (when-let [user (use-user)]
    (-> ^js user .-uid)))


(defn wrap-action-in-sign-in [action-f]
  (fn [& args]
    (if (user)
      (apply action-f args)
      (@SIGN_IN))))

