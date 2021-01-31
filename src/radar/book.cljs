(ns radar.book
  (:require
   [clojure.spec.alpha :as s]
   [commons.utils :as u]
   [commons.models :as m :refer [def-model]]))


(def-model title
  [m/Attr
   {:label "Title"
    :required? true}])

(def-model author
  [m/Attr
   {:label "Author"}])

(def-model asin
  [m/Attr
   {:label "ASIN"
    :helptext "Amazon Standard Identification Number."}])

(def-model isbn
  [m/Attr
   {:label "ISBN"
    :helptext "International Standard Book Number. If you provide this, a
picture of the book will be shown."}])

(def-model tags
  [m/Attr
   {:label "Tags"
    :type "chips"
    :helptext "Type in a tag and submit with RETURN."}])


(defn contains-tag? [book tag]
  (-> book
      :tags
      set
      (contains? tag)))


(s/def ::id string?)
(s/def ::book (s/keys :req-un [::id]))


(def Book
  [:map])


(def-model view-on-amazon
  [m/Command
   {:label "Amazon"
    :icon "shopping_cart"}])


(defn recommendation-count [book]
  (-> book :recommendations count))

(defn recommended-by-user? [book uid]
  (-> book :recommendations (u/v-contains? uid)))
