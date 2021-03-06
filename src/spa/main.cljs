(ns spa.main
  (:require
   [clojure.spec.alpha :as s]
   ["react-dom" :as rdom]
   [helix.core :refer [$]]

   [spark.firestore-init-spa]
   [spark.effects]

   [spark.logging :as logging :refer [log]]

   [spark.core :as spark :refer [def-spa]]
   [spark.auth :as auth]
   [spark.repository :as repository]
   [spark.ui :as ui]

   [base.user :as user]

   [spa.desktop :as desktop]
   [spa.auth :as legilo-auth]
   [radar.ui :as radar]
   [spa.menu-page :refer [menu-page]]
   [radar.radars-page :refer [radars-page]]
   [radar.radar-page :refer [radar-page]]
   [radar.radar-activitylog-page :refer [radar-activitylog-page]]
   [radar.config-page :refer [config-page]]
   [radar.book-page :refer [book-page]]
   ))

(when js/goog.DEBUG
  (s/check-asserts true))

(logging/install-tap)


(defn update-app-context [context]
  (let [uid (ui/use-uid)
        user (ui/use-doc user/User uid)]
    (assoc context
           :user user)))

(def-spa Legilo
  {:pages                     [menu-page
                               book-page
                               config-page
                               radar-activitylog-page
                               radar-page
                               radars-page]
   :update-app-context        update-app-context
   :sign-in-request-component desktop/SignInRequest
   :theme                     desktop/theme
   :styles                    #'desktop/styles
   :root-component            desktop/Desktop
   })

(defn show-auth-error [^js error]
  (let [error (js->clj error)
        msg   (or (-> error :message)
                  (str error))]
    (ui/show-error msg)))


(defn main! []
  (log ::main!)
  (auth/initialize
   {:user-doc-schema user/User
    :sign-in         legilo-auth/sign-in
    :error-handler   show-auth-error})
  (ui/load-spa Legilo))
