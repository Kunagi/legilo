(ns spa
  (:require
   [helix.experimental.refresh :as helix-refresh]
   [spa.api :refer [log]]))


(helix-refresh/inject-hook!)


(defn ^:dev/after-load after-load []
  (log ::dev-after-load)
  (helix-refresh/refresh!))
