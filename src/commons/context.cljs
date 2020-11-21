(ns commons.context
  (:require
   [helix.hooks :as hooks]
   ))


(defn atom-hook
  ([ATOM]
   (atom-hook ATOM identity))
  ([ATOM transformator]
   (fn use-atom []
     (let [[value set-value] (hooks/use-state @ATOM)
           watch-key (random-uuid)]

       (hooks/use-effect
        :once
        (add-watch ATOM watch-key
                   (fn [_k _r _ov nv]
                     (set-value nv)))
        #(remove-watch ATOM watch-key))

       (transformator value)))))
