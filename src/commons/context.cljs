(ns commons.context
  (:require-macros [commons.context])
  (:require
   [cljs-bean.core :as cljs-bean]
   ["react" :as react]
   [helix.hooks :as hooks]

   ["react-router-dom" :as router]

   [commons.firestore-hooks :as firestore]

   ))


(def create-context react/createContext)
(def use-context react/useContext)

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
        (set-value @ATOM)
        (add-watch ATOM watch-key
                   (fn [_k _r ov nv]
                     (when-not (= ov nv)
                       (set-value nv))))
        #(remove-watch ATOM watch-key))

       (transformator value)))))
