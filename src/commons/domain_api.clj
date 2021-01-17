(ns commons.domain-api)


(defn- def-with-id [sym config-map init-f]
  (let [id (keyword (name sym))
        config-map (assoc config-map :id id)]
    (if init-f
      `(def ~sym (~init-f ~config-map))
      `(def ~sym ~config-map))))


(defmacro def-type [sym config-map]
  (def-with-id sym config-map nil))

(defmacro def-attr [sym config-map]
  (def-with-id sym config-map nil))

(defmacro def-command [sym config-map]
  (def-with-id sym config-map `init-command))
