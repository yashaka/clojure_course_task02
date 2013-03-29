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
    (.listFiles (io/file path))
    (separate #(.isDirectory %))))

;; "simple" find-files, i.e. without parallel optimization
(defn sfind-files [file-name path]
  (let [[dirs files] (dirs-and-files path)
        matched-files  (->> files
                            (map #(.getName %))
                            (filter #(re-find (re-pattern file-name) %)))
        children-files (->> dirs
                            (map #(.getPath %))
                            (mapcat #(sfind-files file-name %)))]
         (concat matched-files children-files)))

;; find-files with parallel optimization
(defn find-files [file-name path]
  (let [[dirs files] (dirs-and-files path)
        matched-files  (->> files
                            (map #(.getName %))
                            (filter #(re-find (re-pattern file-name) %)))
        children-files (->> dirs
                            (map #(.getPath %))
                            (map #(future (find-files file-name %)))
                            (mapcat deref))]
         (concat matched-files children-files)))

(comment 
  (time (dotimes [_ 1e2] (find-files "java" "/Users/ayia/Projects")))
  ;= "Elapsed time: 600.96 msecs"
  (time (dotimes [_ 1e2] (sfind-files "java" "/Users/ayia/Projects")))
  ;= "Elapsed time: 380.918 msecs"
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
