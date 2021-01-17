(ns commons.domain-api
  (:require-macros [commons.domain-api]
                   [clojure.string :as str])
  (:require
   [clojure.string :as str]
   [malli.core :as m]
   [malli.error :as me]))


(def PropertyKey
  keyword?)

(def PropertyPath
  [:vector PropertyKey])


(defn doc-id [doc]
  (let [id (get doc :firestore/id)]
    (when-not id (throw (ex-info "Missing :firestore/id in document."
                                 {:doc doc})))
    id))


(defmulti init-command (fn [command] (-> command :type)))


(defn- safe-get [m k schema]
  (if-let [v (get m k)]
    (do
      (when-let [explain (m/explain schema v)]
        (throw (ex-info (str  "Invalid value on `" k "`: "
                              (me/humanize explain))
                        {:subject m
                         :key k
                         :value v
                         :explain explain})))
      v)
    (throw (ex-info (str "Missing " k " in " (get m :id)) k))))

;;; command implementations


(defmethod init-command nil [command]
  command)


(defmethod init-command :create-doc-child-entity [command]
  (let [doc-param (safe-get command :doc-param keyword?)
        inner-path (safe-get command :inner-path PropertyPath)
        template (get command :template)]
    (assoc command
           :args {doc-param any?}
           :f (fn [context]
                (let [doc (get context doc-param)
                      child-id (str (random-uuid))
                      deal (merge template
                                  {:id child-id})
                      inner-path-as-string (reduce (fn [s path-element]
                                                     (if s
                                                       (str s "." (name path-element))
                                                       (name path-element)))
                                                   nil inner-path)
                      changes {(str inner-path-as-string "." child-id) deal}]
                  [[:db/update doc changes]])))))
