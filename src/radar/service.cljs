(ns radar.service
  (:require
   [commons.utils :as u]

   [radar.book :as book]
   [radar.repository :as repository]))


(defn book-recommendation-count [book]
  (-> book :recommendations count))

(defn book-recommended-by-user? [book uid]
  (-> book :recommendations (u/v-contains? uid)))


(defn add-book> [radar-id fields]
  (repository/add-book> radar-id fields))

(defn add-book-command [radar-id]
  (-> book/add
      (assoc-in [:form :submit] #(add-book> radar-id %))))


(defn update-book> [book fields]
  (repository/update-book> book fields))

(defn recommend-book> [uid book]
  (repository/update-book> book {:recommendations [:db/array-union [uid]]}))

(defn un-recommend-book> [uid book]
  (repository/update-book> book {:recommendations [:db/array-remove [uid]]}))

;;;
;;; Example Data
;;;

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
