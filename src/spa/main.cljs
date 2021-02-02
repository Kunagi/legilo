(ns spa.main
  (:require
   ["react-dom" :as rdom]
   [helix.core :refer [$]]

   [spark.firestore-init-spa]
   [spark.effects]

   [spark.logging :refer [log]]
    
   [spark.models :as models]
   [spark.auth :as auth]
   [spark.repository :as repository]
   [spark.ui :as ui]

   [base.user :as user]

   [spa.desktop :as desktop]
))


(defn resolve-page-data [k]
  (cond

    (= :uid k)
    (ui/use-uid)

    (= :user k)
    (ui/use-doc user/Users (ui/use-uid))

    (fn? k)
    (k)

    (= :param-doc (first k))
    (if (= 2 (count k))
      (let [col (second k)
            param-key (keyword (models/col-doc-name col))
            param-value (ui/use-param-2 param-key)]
        (ui/use-doc [(models/col-path col) param-value]))
      (let [param-key (second k)
            param-value (ui/use-param param-key)
            path-fn (nth k 2)
            path (path-fn param-value)]
        (ui/use-doc path)))

    (vector? k)
    (ui/use-doc k)

    :else
    k))

(reset! ui/DATA_RESOLVER resolve-page-data)


(defn main! []
  (log ::main!)
  (auth/initialize
   {:user-Col user/Users
    :sign-in auth/sign-in-with-microsoft})
  (rdom/render ($ desktop/Desktop) (js/document.getElementById "app")))
