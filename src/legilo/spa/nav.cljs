(ns legilo.spa.nav
  (:require

   ["@material-ui/core" :as mui]

   [legilo.spa.api :refer [log]]
   [legilo.spa.auth :as auth]
   [legilo.spa.ui :as ui :refer [defnc $ <>]]
   ))

(defnc PageContent []
  ($ mui/Container
     {:maxWidth "sm"}
     ($ ui/Stack
        ($ auth/CurrentUserCard))))
