(ns radar.radar
  (:require
   [clojure.spec.alpha :as s]
   [spark.core :as spark :refer [def-field def-doc]]
    

   [base.user :as user]
   [radar.book :as book]
   ))


(def-field title
  [:string 
   {:label "Name"
    :required? true}])


(def-field allow-domain
  [:string
   {:label "Allow Domain"
    :helptext "Here you can specify a domain, like example.com or your-org.com.
All users from this domain will have access to this Radar."}])


(def-doc Radar
  [{:firestore/collection "radars"}])


(defn col-path--by-uid [user]
  [{:id "radars"
    :wheres [["uids" "!=" nil]
             ["uids" "array-contains" (or (-> user user/id) "_")]]}])

(defn col-path--by-domain [user]
  [{:id "radars"
    :wheres [["allow-domain" "!=" nil]
             ["allow-domain" "==" (or (-> user user/auth-domain) "_")]]}])

(defn union-col-paths--for-user [user]
  [(col-path--by-uid user)
   (col-path--by-domain user)])

;; (def-model RadarsForUser
;;   [m/ColSubset--union
;;    {:col Radars
;;     :wheres (fn [{:keys [user]}]
;;               (let [by-uid [["uids" "!=" nil]
;;                             ["uids" "array-contains" (or (-> user :id) "_")]]
;;                     by-domain [["allow-domain" "!=" nil]
;;                                ["allow-domain" "==" (or (-> user :auth-domain) "_")]]]
;;                  [by-uid by-domain]))}])

;; (def-model RadarsUserByUid
;;   [m/ColSubset
;;    {:col Radars
;;     :wheres (fn [{:keys [user]}]
;;               [["uids" "array-contains" (-> user :id)]])}])

;; (def-model RadarsByDomain
;;   [m/ColSubset
;;    {:col Radars
;;     :wheres (fn [{:keys [user]}]
;;               [["allow-domain" "==" (-> user :auth-domain)]])}])

(defn all-tags [radar]
  (->> radar :books vals (mapcat :tags) set))


(def sections
  [{:key :must-read
    :name "Must Read"
    :idx 0}
   {:key :should-read
    :name "Should Read"
    :idx 1}
   {:key :trial
    :name "Trial"
    :idx 2}
   {:key :assess
    :name "Assess"
    :idx 3}
   ])


(defn books-by-section-key [books]
  (->> books
       (group-by #(let [c (-> % :recommendations count)]
                    (cond
                      (>= c 5) :must-read
                      (>= c 3) :should-read
                      (>= c 1) :trial
                      :else :assess)))))


(defn title-in-lowercase [radar]
  (when-let [title (-> radar :title)]
    (-> title .toLowerCase)))

(defn books [radar]
  (-> radar :books vals))

(defn book-by-id [radar book-id]
  (get-in radar [:books (keyword book-id)]))
