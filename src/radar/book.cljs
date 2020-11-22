(ns radar.book
  (:require
   [commons.domain-api :refer [def-attr def-command]]))


(def-attr title
  {:label "Title"
   :required? true})

(def-attr author
  {:label "Author"})

(def-attr asin
  {:label "ASIN"})

(def-attr isbn
  {:label "ISBN"})

(def-attr tags
  {:label "Tags"
   :type "chips"})




(def-command recommend
  {:label "Recommend"
   :icon "thumb_up"})

(def-command un-recommend
  {:label "Retract Recommendation"})

(def-command view-on-amazon
  {:label "View on Amazon"
   :icon "shopping_cart"})
