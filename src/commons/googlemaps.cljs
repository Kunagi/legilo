(ns commons.googlemaps
  (:require
   [commons.logging :refer [log]]
   [commons.context :as c.context]
   [commons.mui :as cui :refer [defnc $ <>]]
   ))


(def zoomlevel-continent 5)
(def zoomlevel-city 10)
(def zoomlevel-streets 15)

(def location-rinteln {:lat 52.1599958
                       :lng 8.9770099})

(defn load [api-key]
  (log ::load
       :api-key api-key)
  (let [script (js/document.createElement "script")]
    (set! (.-src script) (str "https://maps.googleapis.com/maps/api/js?key="
                              api-key))
    ;; (set! (.-defer script) true)
    (-> js/document .-head (.appendChild script))))


(defn init-map [map-element-id map-config]
  (let [e (js/document.getElementById map-element-id)
        map (js/google.maps.Map. e (clj->js map-config))]
    map))


(defn create-marker
  [map props]
  (log ::create-marker
       :map map
       :props props)
  (let [marker (-> props
                   (assoc :map map)
                   (assoc :animation js/google.maps.Animation.DROP))]
    (js/google.maps.Marker. (clj->js marker))))


(defnc Map
  [{:keys [id
           height
           zoom
           markers]}]
  (let [map-element-id (or id "map")]

    (c.context/use-effect
     :always
     (log ::Map.init-map)
     (init-map map-element-id
               {:center location-rinteln
                :zoom 10})
     (js/setTimeout
      #(js/navigator.geolocation.getCurrentPosition
        (fn [^js position]
          (log ::Map.position
               :position position)
          (let [map (init-map
                     map-element-id
                     {:center {:lat (-> position .-coords .-latitude)
                               :lng (-> position .-coords .-longitude)}
                      :zoom (or zoom 10)})]
            (doseq [marker markers]
              (create-marker map marker))))
        (fn [error]
          (log ::Map.Error
               :error error))) 1000)
     nil)

    ($ :div
       {:id map-element-id
        :style {:height (or height "40vh")}})))



(defn compute-distance-text> [origin destination]
  (let [service (js/google.maps.DistanceMatrixService.)
        config {:origins [origin]
                :destinations [destination]
                :travelMode "DRIVING"}]
    (js/Promise.
     (fn [resolve reject]
       (.getDistanceMatrix service (clj->js config)
                           (fn [^js response _status]
                             (let [distance (-> response
                                                js->clj
                                                (get "rows")
                                                first
                                                (get "elements")
                                                first
                                                (get "distance")
                                                (get "text"))]
                               (log ::compute-distance.result
                                    :distance distance)
                               (resolve distance))))))))
