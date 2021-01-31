(ns radar.commands
  (:require
   [commons.models :as m :refer [def-model]]

   [radar.radar :as radar]))


(def-model CreateRadar
  [m/Command--create-doc
   {:label "Create new Radar"

    :col radar/Radars

    :context-args [[:uid string?]]

    :form (fn [{:keys [uid]}]
            {:fields [radar/title radar/allow-domain]
             :values {:uids [uid]}})
    }])
