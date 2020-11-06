(ns legilo.spa.impl.logging)


(defn log [event-keyword & {:as event-data}]
  (let [[event-namespace event-name] (if (qualified-keyword? event-keyword)
                                       [(namespace event-keyword)
                                        (name event-keyword)]
                                       (if (keyword? event-keyword)
                                         [nil (name event-keyword)]
                                         [nil (str event-keyword)]))]
    (if event-namespace
      (js/console.log
       (str "%c" event-namespace " %c" event-name)
       "background-color: #5472d3; color: white; padding: 3px;"
       "background-color: #002171; color: white; padding: 3px;"
       event-data)
      (js/console.log
       (str " %c" event-name)
       "background-color: #002171; color: white; padding: 3px;"
       event-data))))

