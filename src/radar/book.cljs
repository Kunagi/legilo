(ns radar.book
  (:require
   [commons.domain-api :refer [def-attr def-command]]))


(def-attr title
  {:label "Book Title"
   :required? true})

(def-attr asin
  {:label "ASIN"})

(def-attr isbn
  {:label "ISBN"})

(def-attr tags
  {:label "Tags"
   :type "chips"})


(def-command view-on-amazon
  {:label "View on Amazon"
   :icon "shopping_cart"})
