(ns amazon.service)

(def partner-id "legilo-21")


(defn href [asin]
  (str "https://www.amazon.de/gp/product/" asin
       "?tag=" partner-id))

(defn search-href [text]
  (str "https://www.amazon.de/s"
       "?tag=" partner-id
       "&k=" text))

(defn cover-url-by-asin [asin]
  (str "//ws-eu.amazon-adsystem.com/widgets/q?MarketPlace=DE&ASIN="
       asin
       "&ServiceVersion=20070822&ID=AsinImage&WS=1&Format=_SL160_&tag="
       partner-id))
