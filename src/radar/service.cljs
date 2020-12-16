(ns radar.service
  (:require
   [commons.logging :refer [log]]
   [commons.utils :as u]

   [radar.radar :as radar]
   [radar.book :as book]
   [radar.repository :as repository]))


(defn book-recommendation-count [book]
  (-> book :recommendations count))

(defn book-recommended-by-user? [book uid]
  (-> book :recommendations (u/v-contains? uid)))


(defn add-book> [radar-id data]
  (let [book-id (str (random-uuid))
        book (assoc data :id book-id)]
    (repository/update-radar>
     radar-id
     {(str  "books." book-id) book})))

(defn update-book> [radar-id book-id changes]
  (log ::update-book>
       :radar-id radar-id
       :book-id book-id
       :changes changes)
  (repository/update-radar>
   radar-id
   (reduce (fn [changes [k v]]
             (assoc changes
                    (str "books." book-id "." (name k))
                    v))
           {} changes)))

(defn add-book-command [radar-id]
  (-> radar/add-book
      (assoc-in [:form :submit] #(add-book> radar-id %))))

;; ;; TODO
;; (defn update-book> [radar-id book-id fields]
;;   (repository/update-radar>
;;    radar-id
;;    {(str "books." book-id) fields}))


(defn recommend-book> [radar-id book-id uid]
  (repository/update-radar>
   radar-id
   {(str "books." book-id ".recommendations") [:db/array-union [uid]]}))

(defn un-recommend-book> [radar-id book-id uid]
  (repository/update-radar>
   radar-id
   {(str "books." book-id ".recommendations") [:db/array-remove [uid]]}))

(defn create-radar> [uid data]
  (repository/create-radar> (assoc data :uids [uid])))

(defn update-radar> [radar-id changes]
  (repository/update-radar> radar-id changes))

(defn create-radar-command [uid]
  {:label "Create new Radar"
   :form {:fields [radar/title radar/allow-domain]
    :submit #(create-radar> uid %)}})


(defn update-review-text> [radar-id book-id uid changes]
  (repository/update-radar>
   radar-id
   {(str "books." book-id ".reviews." uid) changes}))

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

(defn add-example-books-command [radar-id]
  (-> radar/add-example-books
      (assoc :onClick #(add-example-books> radar-id))))
