(ns openlibrary.service)

(defn cover-url-by-isbn [isbn]
  (str "https://covers.openlibrary.org/b/isbn/" isbn "-M.jpg"))
