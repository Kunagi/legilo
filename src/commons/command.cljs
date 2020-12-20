(ns commons.command
  (:require
   [malli.core :as m]
   [malli.error :as me]
   [commons.logging :refer [log]]
   [commons.firestore :as firestore]))


(def Command
  [:map
   [:args [:map]]])

(def Effect
  [:vector any?])

(def Effects
  [:vector Effect])

(defn validate-context [command context]
  (doseq [[k schema] (-> command :args)]
    (let [v (get context k)]
      (when-not v
        (throw (ex-info (str  "Missing `" k "` in context.")
                        {:command command
                         :context-key k})))
      (when-let [explain (m/explain schema v)]
        (throw (ex-info (str  "Invalid `" k "` in context: "
                              (me/humanize explain))
                        {:command command
                         :context-key k
                         :context-value v
                         :explain explain}))))))


(defn validate-effects [command effects]
  (when-let [explain (m/explain Effects effects)]
    (throw (ex-info (str  "Invalid command effects: "
                          (me/humanize explain))
                    {:command command
                     :effects effects
                     :explain explain}))))


(defmulti reify-effect> (fn [effect] (first effect)))

(defmethod reify-effect> :db/update
  [[_ doc changes]]
  (firestore/update-fields> doc changes))

(defmethod reify-effect> :log
  [[ _ data]]
  (log ::reify-effect>--log
       :data data))


(defn reify-effects> [effects]
  (log ::reify-effects>
       :effects effects)
  (js/Promise.all
   (mapv reify-effect> effects)))


(defn execute>
  [command context]
  (log ::execute>
       :command command
       :context context)
  (validate-context command context)
  (let [f (get command :f)
        effects (f context)]
    (validate-effects command effects)
    (reify-effects> effects)))
