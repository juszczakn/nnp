(ns nnp.core
  (:use seesaw.core)
  (:use nnp.mainwin)
  (:gen-class))


(defn -main
  "Initialize new main window."
  [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))
  (native!)
  main-window)
