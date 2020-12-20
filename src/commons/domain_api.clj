(ns commons.domain-api)


(defn- def-with-id [sym config-map]
  (let [id (keyword (name sym))
        config-map (assoc config-map :id id)]
    `(def ~sym ~config-map)))


(defmacro def-type [sym config-map]
  (def-with-id sym config-map))

(defmacro def-attr [sym config-map]
  (def-with-id sym config-map))

(defmacro def-command [sym config-map]
  (def-with-id sym config-map))
