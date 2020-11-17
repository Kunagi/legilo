(ns commons.firestore
  (:require
   [cljs-bean.core :as cljs-bean]))



;;; Firebase


(def ^js firebase
  (-> js/window .-firebase))

(defn ^js firestore []
  (-> firebase (.firestore)))


;;; wrap docs to have access to id and ref

(defn wrap-doc [^js query-doc-snapshot]
  (-> (cljs-bean/->clj (.data query-doc-snapshot))
      (assoc :firestore/meta {:id (.-id query-doc-snapshot)
                             :ref (.-ref query-doc-snapshot)})))

(defn wrap-docs [^js query-snapshot]
  (mapv wrap-doc (-> query-snapshot .-docs)))

(defn doc? [doc]
  (-> doc :firestore/meta boolean))

(defn doc-id [doc]
  (-> doc :firestore/meta :id))

(defn doc-ref [doc]
  (-> doc :firestore/meta :ref))

(defn doc-path [doc]
  (-> doc doc-ref .-path (.split "/") (->> (into []))))

(defn unwrap-doc [doc]
  (cljs-bean/->js (dissoc doc :firestore/meta)))


;;; helpers


(defn ^js update--array-remove [elements]
  (-> js/firebase.firestore.FieldValue .-arrayRemove (apply (clj->js elements))))

(defn ^js update--array-union [elements]
  (-> js/firebase.firestore.FieldValue .-arrayUnion (apply (clj->js elements))))

(defn ^js update--timestamp []
  (-> js/firebase.firestore.FieldValue .serverTimestamp))



;;; collection and doc references


(defn- fs-collection [source path-elem]
  (if (map? path-elem)
    (let [{:keys [id wheres where]} path-elem
          wheres (if where
                   (conj wheres where))
          collection (-> ^js source (.collection id))]
      (reduce (fn [collection [attr op val]]
                (-> ^js collection (.where attr op val)))
              collection wheres))
    (-> ^js source (.collection path-elem))))

(defn ^js ref [path]
  (loop [col nil
         doc nil
         path path]
    (if (empty? path)
      (if doc doc col)
      (cond

        doc
        (recur (-> ^js doc (fs-collection (first path)))
               nil
               (rest path))

        col
        (recur nil
               (-> ^js col (.doc (first path)))
               (rest path))

        :else
        (recur (-> (firestore) (fs-collection (first path)))
               nil
               (rest path))))))


;;;

(defn doc> [path]
  (js/Promise.
   (fn [resolve reject]
     (-> (ref path)
         .get
         (.then (fn [^js doc]
                  (resolve (wrap-doc doc))))))))


(defn update-doc> [path props]
  (let [path (if (doc? path)
               (doc-path path)
               path)]
    (if (fn? props)
      (-> (doc> path)
          (.then #(update-doc> path (props %))))
      (-> (ref path)
          (.set (unwrap-doc props)
                (clj->js {"merge" true}))))))
