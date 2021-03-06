(ns hello
  (:import (java.util.concurrent TimeUnit)
           (com.google.common.base Stopwatch)
           [kmeans.datageneration DataGenerator]
           [kmeans Point]
           [kmeans.partitioning Partitioner]
           [kmeans.clustering ClusteringService]
           [kmeans.clustering Clustering]
           [kmeans.parsing DataParser])
  (:gen-class))

(defn string-to-number [s]
  ;(Integer/parseInt s))
  (read-string s))

(defn work [amount current]
  (if (< amount current)
    (work amount (inc current))))


(defn time-function [x]
  (let [stopwatch (. Stopwatch (createStarted))]
    (x)
    (. stopwatch
      (stop)
      (elapsed (. TimeUnit MILLISECONDS)))))

(defn partition-workers [vectors, workers]
  (. (Partitioner.) (partition vectors (string-to-number workers))))

(defn generate-data []
  (. DataGenerator (generateData)))

(defn generate-random-vectors [count]
  (. DataGenerator (generateRandomVectors count)))

(defn clustering [data means]
  (. ClusteringService (ClusterKMeansMSIncremental data means)))

(defn mapper [data means clusters]
  ;(println (str "Running mapper: " (.. Thread (currentThread) (getId))))
  (let [cluster (clustering data means)]
    ; (println (str "Mapper: " (.. Thread (currentThread) (getId)) " done clustering"))
    (dosync
      (commute clusters conj cluster)))
  ;(println (str "Mapper: " (.. Thread (currentThread) (getId)) " done delivering"))
  clusters)

(defn map-partitioners [partitions means clusters]
  (let [listSize (. partitions (size))]
    (defn helper [mappers current]
      (let [result
            (if (< current listSize)
              ;(let [partition (. partitions (get current))]
              (helper
                (conj mappers
                  (future
                    (mapper (. (. partitions (get current)) (getData)) means clusters)))
                (inc current))
              mappers)]
        result)))
  (helper '() 0))

(def clusters (ref '()))

(defn merge-clusters [clusters nrClusters]
  (let [number-of-clusters (count clusters)
        merged-clusters (Clustering. nrClusters)]
    (loop [current 0]
      (when (< current number-of-clusters)
        (. merged-clusters (mergeWith (. clusters (get current))))
        ;(println (str "Reducer recevied nr: " current))
        (recur (+ current 1))))
    merged-clusters))

(defn calculate-means [clustering means]
  (. means clear)
  (let [clustering-size (. clustering size)]
    (loop [current 0]
      (when (< current clustering-size)
        (. means add (. (. clustering (get current)) getMean))
        (recur (+ current 1))))))

(defn print-result [sw, amount-of-vectors, maxIterationCount, nrClusters, nrThreads, outputFolderName]
  (. ResultWriter (PrintResult sw amount-of-vectors maxIterationCount nrClusters nrThreads "Clojure" outputFolderName)))

(defn print-means [means]
  (print "Means: ")
  (doseq [i means]
    (let [size (. i (size))]
      (loop [current 0]
        (when (< current size)
          (print (str " " (. i (itemAt current))))
          (recur (+ current 1))))))
  (println ""))

(defn run-clustering [vectors means nrWorkers maxIterations vector-size]
  (let
    [partitioning (partition-workers vectors nrWorkers)
     nrClusters (. means (size))
     partitions (. partitioning (getPartitions))
     stopwatch (. Stopwatch (createStarted))]
    (loop [current 0]
      (when (< current (string-to-number maxIterations))
        ;(println "Running mappers")
        (doall (map deref (map-partitioners partitions means clusters))) ; Synchronization before reduce
        ;(println "Running reducers")
        (let [reducer (merge-clusters @clusters nrClusters)]
          (. reducer calcMeansUsingMeanSum)
          (calculate-means (. reducer getClusters) means))
        (dosync
          (ref-set clusters '()))
        ;(println (str "Reducer finished iteration " (+ current 1)))
        (recur (+ current 1))))
    (print (. stopwatch
             (stop)
             (elapsed (. TimeUnit MILLISECONDS))))
    (print-result stopwatch (string-to-number vector-size) (string-to-number maxIterations) nrClusters (string-to-number nrWorkers) "")))

(defn run-static-test []
  (let
    [static-vectors (. DataParser (parseStaticData))
     static-means (. DataParser (parseStaticDataMeans))]
    (run-clustering static-vectors static-means 5 2)
    (. ResultWriter (printVectors static-means))
    (. ResultWriter (writeVectorsToDisk static-means, "Clojure"))))

(defn run-clustering-standard [vector-size nrWorkers maxIterations]
  (let
    [vectors (generate-random-vectors (string-to-number vector-size))
     means (generate-random-vectors 5)]
    (run-clustering vectors means, nrWorkers, maxIterations, vector-size))
  )

; #vectors #mappers
(defn -main [& args]
  (let [nr-vectors (nth args 0)
        nr-mappers (nth args 1)
        nr-iterations (nth args 2)]
    (println nr-vectors nr-mappers nr-iterations)
    (run-clustering-standard nr-vectors nr-mappers nr-iterations)
    ;(run-static-test)
    (shutdown-agents)))