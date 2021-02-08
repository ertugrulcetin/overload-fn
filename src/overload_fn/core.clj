(ns overload-fn.core
  (:refer-clojure :exclude [defn])
  (:require
   [clojure.string :as str]
   [clojure.spec.alpha :as s]))


(s/check-asserts true)

(s/def ::distinct (partial apply distinct?))


(defn- use-protocol? [types]
  (not
   (boolean
    (some (fn [args]
            (let [args* (rest args)]
              (when-not (or (empty? args*)
                            (every? nil? args*))
                args))) types))))


(defn- create-protocol-name [name]
  (format "I%sProtocol" (->> name
                             str
                             (re-seq #"[a-zA-Z]+")
                             (map str/capitalize)
                             (str/join))))


(defmacro defn
  [name & body]
  (let [types (map (comp (partial mapv (comp :tag meta))
                         first) body)]
    (s/assert ::distinct types)
    (if (use-protocol? types)
      (let [protocol (symbol (create-protocol-name name))]
        `(do
           (println "Using Protocols")
           (when (and ~protocol (map? ~protocol))
             (-reset-methods ~protocol)
             (.unbindRoot (:var ~protocol)))
           (defprotocol ~protocol
             (~name ~@(distinct
                       (map (fn [args]
                              (vec (cons 'this (rest args))))
                            (distinct (map first body))))))
           (extend-protocol ~protocol
             ~@(apply concat
                      (for [[k v] (group-by (comp :tag meta ffirst) body)]
                        (list k (cons name v)))))
           (def ~name)))
      `(do
         (println "Using Multi-Methods")
         (let [v# (def ~name)]
           (when (bound? v#)
             (.unbindRoot v#)))
         (defmulti ~name (fn [& args#] (mapv (fnil type Object) args#)))
         ~@(for [[args form] body]
             `(defmethod ~name ~(mapv (fn [a#]
                                        (or (-> a# meta :tag)
                                            Object)) args) ~args
                ~form))
         (def ~name)))))

(comment
 (macroexpand-1 '(defn my-multi-fn
                       ([^Double x ^String y]
                        :double-string)
                       ([^Long x ^String y]
                        :long-string)
                       #_([^Object x ^String y]
                          :any-type-string)
                       ([x ^String y]
                        :nil-string))))