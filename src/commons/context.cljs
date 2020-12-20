(ns commons.context
  (:require-macros [commons.context])
  (:require
   [cljs-bean.core :as cljs-bean]
   ["react" :as react]
   [helix.hooks :as hooks]
   [camel-snake-kebab.core :as csk]

   ["react-router-dom" :as router]

   [commons.firestore-hooks :as firestore]

   ))

(def use-state react/useState)
(def create-context react/createContext)
(def use-context react/useContext)

(def use-col firestore/use-col)
(def use-cols-union firestore/use-cols-union)
(def use-doc firestore/use-doc)


(defn use-params []
  (->> (router/useParams)
       cljs-bean/->clj
       (reduce (fn [m [k v]]
                 (assoc m (csk/->kebab-case k) v))
               {})))


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


;;;
;;; page and context data
;;;

(def DATA_RESOLVER (atom nil))


(def page (create-context {:page nil
                           :data nil}))

(defn use-page []
  (let [data-resolver @DATA_RESOLVER
        _ (when-not data-resolver
            (throw (ex-info "DATA_RESOLVER not initialized"
                            {})))
        page (use-context page)
        data (reduce (fn [m [k identifier]]
                       (assoc m k (data-resolver identifier)))
                     {} (-> page :data))]
    (assoc page :data data)))


(defn use-context-data []
   (let [page (use-page)
         params (use-params)
         data (merge params
                     (-> page :data))]
     data))
