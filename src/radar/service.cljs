(ns radar.service
  (:require

   [commons.firestore :as firestore]))


(defn book-recommendation-count [book]
  (-> book :recommendations count))


(defn recommend-book [uid book]
  (firestore/update-fields> book {:recommendations (firestore/array-union [uid])}))


(defn add-book> [radar-id fields]
  (firestore/create-doc> ["radars" radar-id "books"] fields))


(defn update-book> [radar-id book fields]
  (if book
    (firestore/update-fields> book fields)
    (add-book> radar-id fields)))


(defn add-example-books> [radar-id]
  (js/Promise.all
   (map #(add-book> radar-id %)
        [
         {:title "Domain Driven Design" :asin "0321125215"}
         {:title "I Am a Strange Loop" :asin "0465030785"}
         {:title "Reinventing Organizations" :asin "3800652854"}
         {:title "Waking Up" :asin "1451636016"}
         {:title "Developer Hegemony" :asin "B0722H41SG"}
         {:title "Business Model Generation" :asin "359339474X"}
         {:title "An Open Heart" :asin "0316989797"}
         {:title "Structure and Interpretation of Computer Programs" :asin "0262510871"}
         {:title "Out of Your Mind" :asin "1591791650"}
         {:title "Consider the Lobster" :asin "0316156116"}
         {:title "Leitfaden für faule Eltern" :asin "3499626721"}
         ])))
