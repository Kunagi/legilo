(ns spa.impl.firestore-hooks
  (:require
   [helix.hooks :as hooks]

   [spa.impl.logging :refer [log]]
   [spa.impl.firestore :as fs]))


(defn doc-atom [path]
  (log ::doc-atom :path path)
  (let [DATA (atom nil)
        ref (fs/ref path)]
    (-> ref
        (.onSnapshot (fn [doc-snapshot]
                       (log ::doc-snapshot-received
                            :collection path
                            :snapshot doc-snapshot)
                       (reset! DATA (fs/wrap-doc doc-snapshot)))))
    DATA))


(defn col-atom [path]
  (log ::col-atom :path path)
  (let [DATA (atom nil)
        ref (fs/ref path)]
    (-> ref
        (.onSnapshot (fn [query-col-snapshot]
                       (log ::query-snapshot-received
                            :collection path
                            :snapshot query-col-snapshot)
                       (->> ^js query-col-snapshot
                            .-docs
                            (map fs/wrap-doc)
                            (reset! DATA)))))
    DATA))


(defonce SUBS (atom {}))

(defn doc-sub [path]
  (if-let [DATA (get @SUBS path)]
    DATA
    (let [DATA (doc-atom path)]
      (swap! SUBS assoc path DATA)
      DATA)))


(defn col-sub [path]
  (if-let [DATA (get @SUBS path)]
    DATA
    (let [DATA (col-atom path)]
      (swap! SUBS assoc path DATA)
      DATA)))


(defn use-col
  "React hook for a collection."
  [path]
  (let [DATA (col-sub path)
        [docs set-docs] (hooks/use-state @DATA)
        watch-ref (random-uuid)]

    (hooks/use-effect
     :once
     (log ::requesting-collection
          :path path)
     (add-watch DATA
                watch-ref
                (fn [_ _ _ nv]
                  (set-docs nv)))

     #(remove-watch DATA watch-ref))

    docs))

(defn use-doc
  "React hook for a document."
  [path]
  (let [DATA (doc-sub path)
        [doc set-doc] (hooks/use-state @DATA)
        watch-ref (random-uuid)]

    (hooks/use-effect
     :once
     (log ::requesting-document
          :path path)
     (add-watch DATA
                watch-ref
                (fn [_ _ _ nv]
                  (set-doc nv)))

     #(remove-watch DATA watch-ref))

    doc))
