(ns radar.book
  (:require
   [clojure.spec.alpha :as s]
   [commons.domain-api :refer [def-attr def-command]]))


(def-attr title
  {:label "Title"
   :required? true})

(def-attr author
  {:label "Author"})

(def-attr asin
  {:label "ASIN"
   :helptext "Amazon Standard Identification Number. If you provide this, a
picture of the book will be shown."})

(def-attr isbn
  {:label "ISBN"})

(def-attr tags
  {:label "Tags"
   :type "chips"
   :helptext "Type in a tag and submit with RETURN."})


(s/def ::id string?)
(s/def ::book (s/keys :req-un [::id]))


(def-command recommend
  {:label "Recommend"
   :icon "thumb_up"})

(def-command un-recommend
  {:label "Retract Recommendation"})

(def-command view-on-amazon
  {:label "View on Amazon"
   :icon "shopping_cart"})

