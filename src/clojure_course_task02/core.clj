(ns clojure-course-task02.core
  (:require [clojure.java.io :as io])
  (:gen-class))

(defn separate
  "Returns a vector:
   [ (filter f s), (filter (complement f) s) ]"
  [f s]
  [(filter f s) (filter (complement f) s)])

(defn dirs-and-files [path]
  (->> 
    (seq (.listFiles (io/file path)))
    (separate #(.isDirectory %))))

(defn find-files [file-name path]
  (let [[dirs files] (dirs-and-files path)]
    (->> files
         (map #(.getName %))
         (filter #(re-find (re-pattern file-name) %))
         (concat (->> dirs
                      (map #(.getPath %))
                      (map #(future (find-files file-name %)))
                      (mapcat deref)
                      ;(mapcat #(find-files file-name %))
                      )))))
(comment 
  (time (dotimes [_ 1e3] (find-files "^core.+" "./")))
  ;= "Elapsed time: 2097.048 msecs"
  ;without optimization:
  ;= "Elapsed time: 1336.011 msecs"
  )

(defn usage []
  (println "Usage: $ run.sh file_name path"))

(defn -main [file-name path]
  (if (or (nil? file-name)
          (nil? path))
    (usage)
    (do
      (println "Searching for " file-name " in " path "...")
      (dorun (map println (find-files file-name path))))))
