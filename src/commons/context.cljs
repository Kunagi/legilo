(ns commons.context
  (:require
   [cljs-bean.core :as cljs-bean]
   [helix.hooks :as hooks]

   ["react-router-dom" :as router]

   [commons.firestore-hooks :as firestore]

   ))


(def use-col firestore/use-col)
(def use-cols-union firestore/use-cols-union)
(def use-doc firestore/use-doc)


(defn use-params []
  (cljs-bean/->clj (router/useParams)))


(defn use-param [param-key]
  (-> (use-params) (get param-key)))


(defn atom-hook
  ([ATOM]
   (atom-hook ATOM identity))
  ([ATOM transformator]
   (fn use-atom []
     (let [[value set-value] (hooks/use-state @ATOM)
           watch-key (random-uuid)]

       (hooks/use-effect
        :once
        (add-watch ATOM watch-key
                   (fn [_k _r _ov nv]
                     (set-value nv)))
        #(remove-watch ATOM watch-key))

       (transformator value)))))
