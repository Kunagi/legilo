(ns gcf.amazon
  (:require
   ["amazon-paapi" :as paapi]))


(defn handle-debug> []
  (let [common-parameters {
                           :AccessKey "AKIAJYUZKAPB7ZVQKSEQ"
                           :SecretKey "---censored---"
                           :PartnerTag "frankenburg-21"
                           }
        request-parameters {
                            :ASIN "B07H65KP63"
                            :Resources ["ItemInfo.Title" "Offers.Listings.Price"]
                            }]
    (-> paapi
        (.GetVariations (clj->js common-parameters) (clj->js request-parameters))))
  )
