(ns radar.commands
  (:require
   [commons.models :as m :refer [def-model]]

   [radar.radar :as radar]
   [radar.book :as book]))


(def-model CreateRadar
  [m/Command--create-doc
   {:label "Create new Radar"

    :col radar/Radars

    :context-args [[:uid string?]]

    :form (fn [{:keys [uid]}]
            {:fields [radar/title radar/allow-domain]
             :values {:uids [uid]}})
    }])


(def-model AddBook
  [m/Command--update-doc--add-child
   {:label "Add Book"
    :icon "add"

    :doc-param :radar
    :inner-path [:books]

    :form {:fields [book/title book/author book/isbn book/asin book/tags]}}])



(def-model UpdateBook
  [m/Command--update-doc--update-child
   {:label "Edit Book"

    :doc-param :radar
    :child-param :book
    :inner-path [:books]

    :form (fn [{:keys [book]}]
            {:fields [book/title book/author book/isbn book/asin]
             :fields-values book})}])


(def-model UpdateBookTags
  [m/Command--update-doc--update-child
   {:label "Edit Book Tags"

    :doc-param :radar
    :child-param :book
    :inner-path [:books]

    :form (fn [{:keys [book]}]
            {:fields [book/tags]
             :fields-values book}) }])
