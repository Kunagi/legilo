(ns spa.main
  (:require
   [clojure.spec.alpha :as s]
   ["react-dom" :as rdom]
   [helix.core :refer [$]]

   [spark.firestore-init-spa]
   [spark.effects]

   [spark.logging :refer [log]]

   [spark.core :as spark :refer [def-spa]]
   [spark.auth :as auth]
   [spark.repository :as repository]
   [spark.ui :as ui]

   [base.user :as user]

   [spa.desktop :as desktop]
   [spa.home-ui :as home]
   [radar.ui :as radar]
   [spa.menu-page :refer [menu-page]]
   [radar.config-page :refer [config-page]]
   [radar.book-page :refer [book-page]]
   ))

(when js/goog.DEBUG
  (s/check-asserts true))


(defn update-app-context [context]
  (let [uid (ui/use-uid)
        user (ui/use-doc user/User uid)]
    (assoc context
           :user user)))

(def-spa Legilo
  {:pages [menu-page
           book-page
           config-page
           radar/RadarPage
           home/HomePage]
   :update-app-context update-app-context})

(defn show-auth-error [^js error]
  (let [error (js->clj error)
        msg (or (-> error :message)
                (str error))]
    (ui/show-error msg)))

(defn main! []
  (log ::main!)
  (auth/initialize
   {:user-doc-schema user/User
    :sign-in auth/sign-in-with-microsoft
    :error-handler show-auth-error})
  (rdom/render ($ desktop/Desktop
                  {:spa Legilo})
               (js/document.getElementById "app")))
