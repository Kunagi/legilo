(ns spa.main
  (:require
   ["react-dom" :as rdom]
   [helix.core :refer [$]]

   [commons.firestore-init-spa]
   [commons.effects]

   [commons.logging :refer [log]]
   [commons.context :as c.context]
   [commons.models :as models]

   [base.service]
   [base.context :as b.context]

   [spa.desktop :as desktop]
))


(defn resolve-page-data [k]
  (cond

    (= :uid k)
    (b.context/use-uid)

    (= :user k)
    (b.context/use-user)

    (fn? k)
    (k)

    (= :param-doc (first k))
    (if (= 2 (count k))
      (let [col (second k)
            param-key (keyword (models/col-doc-name col))
            param-value (c.context/use-param-2 param-key)]
        (c.context/use-doc [(models/col-path col) param-value]))
      (let [param-key (second k)
            param-value (c.context/use-param param-key)
            path-fn (nth k 2)
            path (path-fn param-value)]
        (c.context/use-doc path)))

    (vector? k)
    (c.context/use-doc k)

    :else
    k))

(reset! c.context/DATA_RESOLVER resolve-page-data)


(defn main! []
  (log ::main!)
  (rdom/render ($ desktop/Desktop) (js/document.getElementById "app")))
