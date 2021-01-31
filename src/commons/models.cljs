(ns commons.models
  (:require-macros [commons.models])
  (:require
   [clojure.string :as str]
   [malli.core :as m]
   [malli.error :as me]
   [commons.utils :as u]
   ))


;;; validation helpers


(def $PropertyKey
  keyword?)

(def $PropertyPath
  [:vector $PropertyKey])

(def $Model
  [:map
   [:model/id string?]])

(def $Attr
  (into $Model
        [[:id keyword?]]))

(def $Entity
  (into $Model
        [[:id keyword?]]))

(def $Doc
  $Entity
  #_(into $Entity
          [[:firestore/collection string?]]))

(def $Col
  (into $Model
        [[:path string?]]))


(defn- validate-model-schema [model schema]
  (when-let [explain (m/explain schema model)]
    (throw (ex-info (str  "Invalid model input in "
                          (-> model :model/id)
                          ": "
                          (me/humanize explain))
                    {:explain explain}))))

;;; attributes


(defn Attr [model]
  (validate-model-schema
   model [:map
          [:label string?]])
  (-> model
      (assoc :attr/key (-> model :model/symbol str/lower-case keyword))
      ;; (assoc :id (-> model :model/symbol str/lower-case keyword))
      ))

(defn attr? [attr]
  (-> attr :attr/key boolean))

(defn attr->form-field [attr]
  (assoc attr
         :id (-> attr :attr/key)))


;;; documents


(defn Doc [model]
  (validate-model-schema
   model [:map
          ])
  (-> model
      (assoc :name (-> model :model/symbol))
      (assoc :id (-> model :model/symbol str/lower-case keyword))
      (assoc :$ [:map
                 [:id string?]])))


(defn Col [model]
  (validate-model-schema
   model [:map
          [:doc $Doc]
          ])
  (-> model
      (assoc :path (-> model :model/symbol str/lower-case))
      )
  )

(defn col-doc-name [col]
  (-> col :doc :name))

(defn col-path [col]
  (-> col :path))


(defn col-id-generator [col]
  (or (-> col :id-generator)
      (fn [_context] (-> (random-uuid) str))))


(defn ColSubset [model]
  (validate-model-schema
   model [:map
          [:col $Col]
          [:wheres any?]])
  (-> model))


(defn col-subset-path [col-subset args]
  (let [col (-> col-subset :col)
        wheres (-> col-subset :wheres)
        wheres (if (fn? wheres)
                 (wheres args)
                 wheres)]
    [{:id (col-path col)
      :wheres wheres}]))


(defn DocEntity [model]
  (validate-model-schema
   model [:map
          ])
  (-> model
      ;; (assoc :id (-> model :model/symbol str/lower-case keyword))
      ))


;;; commands


(defn Command [model]
  (validate-model-schema
   model [:map
          [:label {:optional true} string?]])
  (-> model
      ))

(defn command-label [command]
  (or (-> command :label)
      (-> command :model/symbol)))


(defn Command--create-doc [command]
  (validate-model-schema
   command [:map
            [:col $Col]
            [:update-values {:optional true} any?]
            #_[:values-from-context {:optional true} [:vector keyword?]]])
  (Command
   (assoc command
          :f (fn [context]
               (let [Col (get command :col)
                     values (get context :values)
                     values (u/safe-apply
                             (get command :update-values)
                             [values context])]
                 [[:db/create Col values]])))))


(defn Command--update-doc [command]
  (validate-model-schema
   command [:map
            [:doc $Doc]])
  (let [doc-def (get command :doc)
        doc-param (-> doc-def :id)
        static-changes (get command :changes)
        changes-param (get command :changes-param :values)]
    (Command
     (assoc command
            :context-args [[doc-param [:map
                                       [:id string?]]]]
            :f (fn [context]
                 (let [doc (get context doc-param)
                       changes (merge (get context changes-param)
                                      static-changes)]
                   [[:db/update doc changes]]))))))


(defn Command--update-doc--add-child [command]
  (validate-model-schema
   command [:map
            [:doc-param keyword?]
            [:inner-path $PropertyPath]])
  (let [doc-param (get command :doc-param)
        inner-path (get command :inner-path)
        changes-param (get command :changes-param :values)
        template (get command :template)
        id-generator (get command :id-generator #(-> (random-uuid) str))]
    (Command
     (assoc command
            :context-args [[doc-param [:map
                                       [:id string?]]]]
            :f (fn [context]
                 (let [doc (get context doc-param)
                       child-id (id-generator context)
                       entity (merge template
                                     {:id child-id}
                                     (get context changes-param))
                       inner-path-as-string (reduce (fn [s path-element]
                                                      (if s
                                                        (str s "." (name path-element))
                                                        (name path-element)))
                                                    nil inner-path)
                       changes {(str inner-path-as-string "." child-id) entity}]
                   [[:db/update doc changes]]))))))


(defn Command--update-doc--update-child [command]
  (validate-model-schema
   command [:map
            [:doc-param keyword?]
            [:child-param keyword?]
            [:inner-path $PropertyPath]])
  (let [doc-param (get command :doc-param)
        inner-path (get command :inner-path)
        child-param (get command :child-param :values)
        changes-param (get command :changes-param :values)
        static-changes (get command :static-changes)]
    (Command
     (assoc command
            :context-args [[doc-param [:map
                                       [:id string?]]
                            child-param [:map
                                         [:id string?]]]]
            :f (fn [context]
                 (let [doc (get context doc-param)
                       child (get context child-param)
                       child-id (-> child :id)
                       changes (merge (get context changes-param)
                                      static-changes)
                       inner-path-as-string (reduce (fn [s path-element]
                                                      (if s
                                                        (str s "." (name path-element))
                                                        (name path-element)))
                                                    nil inner-path)

                       changes (reduce (fn [changes [k v]]
                                         (assoc changes
                                                (str inner-path-as-string
                                                     "." child-id
                                                     "." (name k))
                                                v))
                                       {} changes)
                       ]
                   [[:db/update doc changes]]))))))
