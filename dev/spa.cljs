(ns spa
  (:require
   [clojure.spec.alpha :as s]
   [helix.experimental.refresh :as helix-refresh]
   [spark.dev.devcards-page]
   [spark.logging :refer [log]]))


(helix-refresh/inject-hook!)
(s/check-asserts true)

(defn ^:dev/after-load after-load []
  (log ::dev-after-load)
  (helix-refresh/refresh!))


(comment
   (let [do-more> (fn [s] (js/Promise.resolve (str s " more")))
         do-it> (fn [] (js/Promise.resolve (do-more> "do-it")))
         result (do-it>)]
     (-> result)))
