(ns spa
  (:require
   [clojure.spec.alpha :as s]
   [helix.experimental.refresh :as helix-refresh]
   [spark.dev.devcards-page]
   [spark.logging :refer [log]]
   [spark.core :as spark]

   [radar.radar :as radar]
   [radar.book-page :refer [book-page]]
   ))


(helix-refresh/inject-hook!)
(s/check-asserts true)

(defn ^:dev/after-load after-load []
  (log ::dev-after-load)
  (helix-refresh/refresh!))

(comment
  (-> radar/Radar spark/schema-opts)
  (spark/doc-schema-router-param radar/Radar)
  (spark/page-docs book-page))

(comment
  (let [do-more> (fn [s] (js/Promise.resolve (str s " more")))
        do-it>   (fn [] (js/Promise.resolve (do-more> "do-it")))
        result   (do-it>)]
    (-> result)))
