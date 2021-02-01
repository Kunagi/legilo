(ns spa.main
  (:require
   ["react-dom" :as rdom]
   [helix.core :refer [$]]

   [commons.firestore-init-spa]
   [commons.effects]

   [commons.logging :refer [log]]
    
   [commons.models :as models]
   [commons.auth :as auth]
   [commons.repository :as repository]
   [commons.mui :as cui]

   [base.user :as user]

   [spa.desktop :as desktop]
))


(defn resolve-page-data [k]
  (cond

    (= :uid k)
    (cui/use-uid)

    (= :user k)
    (cui/use-doc user/Users (cui/use-uid))

    (fn? k)
    (k)

    (= :param-doc (first k))
    (if (= 2 (count k))
      (let [col (second k)
            param-key (keyword (models/col-doc-name col))
            param-value (cui/use-param-2 param-key)]
        (cui/use-doc [(models/col-path col) param-value]))
      (let [param-key (second k)
            param-value (cui/use-param param-key)
            path-fn (nth k 2)
            path (path-fn param-value)]
        (cui/use-doc path)))

    (vector? k)
    (cui/use-doc k)

    :else
    k))

(reset! cui/DATA_RESOLVER resolve-page-data)


(defn main! []
  (log ::main!)
  (auth/initialize
   {:user-Col user/Users
    :sign-in auth/sign-in-with-microsoft})
  (rdom/render ($ desktop/Desktop) (js/document.getElementById "app")))
