(ns radar.config-page
  (:require
   [clojure.edn :as edn]
   [cljs.pprint :refer [pprint]]
   ["@material-ui/core" :as mui]


   [spark.ui :as ui :refer [def-ui def-page $]]
   [spark.repository :as repository]


   [radar.radar :as radar]
   [radar.ui :as radar-ui]
   [clojure.string :as str]))


(def-ui RadarConfigCard [radar]
  {:from-context [radar]}
  ($ ui/DocFieldsCard
     {:doc    radar
      :fields [radar/title
               radar/allow-domain
               radar/allow-domain-2
               (-> radar/allow-emails
                   ;; (assoc-in [1 :serialize] (fn [emails]
                   ;;                            (->> emails (str/join ", "))))
                   ;; (assoc-in [1 :deserialize] (fn [s]
                   ;;                              (-> (str/split s #","))))
                   )]}))

(defn write-to-clipboard [text]
  (-> (js/navigator.clipboard.writeText text)))

(def-ui RadarBackupCard [radar]
  {:from-context [radar]}
  ($ ui/SimpleCard
     {:title "Radar Data"}
     ($ :div
        {:style {:max-height "30vh"
                 :overflow "auto"}}
        (ui/data radar))
     ($ ui/Button
        {:text "Copy to Clipboard"
         :onClick #(write-to-clipboard (with-out-str (pprint radar)))})))


(def-ui CommandExec [radar]
  {:from-context [radar]}
  (let [[cmd _set-cmd] (ui/use-state "")]
    (ui/stack
      #_($ mui/TextField
           {:onChange  #(-> % .-target .-value set-cmd)
            :multiline true
            :variant   "outlined"
            :rows      10})
      ($ mui/Button
         {:onClick (fn []
                     (js/console.log cmd)
                     (let [data (edn/read-string cmd)]
                       (js/console.log data radar)
                       (repository/update-doc-child>
                         radar
                         [:books] "36b39629-ee30-4461-9c05-ef83b8d937de"
                         {:tags       ["organisationsentwicklung"],
                          :isbn       "978-2960133509",
                          :asin       "2960133501",
                          :ts-updated #inst "2021-02-28T20:40:35.386-00:00",
                          :title      "Reinventing Organizations",
                          :author     "Frederic Laloux",
                          :id         "36b39629-ee30-4461-9c05-ef83b8d937de",
                          :reviews
                          {"8yyN0hkIKuQJHMnLWDcblcNpns23"
                           {:text
                            "Gibt viele Einblicke, die auch über Organisationen hinaus gehen und sich auch auf die eigene Einstellung zu Job und Leben beziehen.",
                            :uid "8yyN0hkIKuQJHMnLWDcblcNpns23"},
                           "n8R0cHVCFiM4v3vgguxAW1RnyNc2"
                           {:text
                            "Pflichtlektüre im Bereich Organisationsentwicklung. Für die Lesefaulen und Bilderfans gibt es auch eine illustrierte Version.",
                            :uid "n8R0cHVCFiM4v3vgguxAW1RnyNc2"},
                           "1fYn2K5BdGOImNNrO7njOJmOpQc2"
                           {:text
                            "Spannende Perspektive auf unterschiedliche Unternehmensformen und ihre Entwicklung. Regt darüberhinaus an, über die eigene Sichtweise zu reflektieren.",
                            :uid "1fYn2K5BdGOImNNrO7njOJmOpQc2"},
                           "xxvTfyemTtTprBMAR7Wdh1H1z383"
                           {:text "Sehr inspirierend und horizont-erweiternd :)",
                            :uid  "xxvTfyemTtTprBMAR7Wdh1H1z383",
                            :id   "xxvTfyemTtTprBMAR7Wdh1H1z383"},
                           "G8ZAlBCmAya26zd5G63eNayBexu1"
                           {:uid "G8ZAlBCmAya26zd5G63eNayBexu1",
                            :text
                            "Ich habe mich mit der illustrierten Version gut aufgehoben gefühlt und in Diskussionen mit Wissenden über das ausführliche Buch (bisher) auch keinen Rückstand verspürt. Nutzt also für den leichten Einstig gerne die illustrierte Variante. :-)",
                            :id  "G8ZAlBCmAya26zd5G63eNayBexu1"}},
                          :recommendations
                          #{"8yyN0hkIKuQJHMnLWDcblcNpns23" "n8R0cHVCFiM4v3vgguxAW1RnyNc2"
                            "xxvTfyemTtTprBMAR7Wdh1H1z383"
                            "1fYn2K5BdGOImNNrO7njOJmOpQc2"}})
                       #_(runtime/reify-effect>
                           [:db/update-child ""])))
          :variant "outlined"}
         "Execute!"))))


(def-ui RadarConfigPageContent []
  ($ ui/Stack
     ($ RadarConfigCard)
     ($ RadarBackupCard)))


(def-page config-page
  {:path                   ["radars" radar/Radar "config"]
   :content                RadarConfigPageContent
   :appbar-title-component radar-ui/RadarAppbarTitle
   :back-to                (fn [{:keys [radar]}]
                             (str "/ui/radars/" (-> radar :id)))})
