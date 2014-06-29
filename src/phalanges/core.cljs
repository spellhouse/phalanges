(ns phalanges.core
  (:require
   [clojure.string :as string]
   [goog.object])
  (:require-macros
   [phalanges.core :refer [define-key-code-predicates]])
  (:import goog.events.KeyCodes))

(def keycode->keyword
  (reduce
    (fn [m k]
      (let [v (aget goog.events.KeyCodes k)
            k (-> (string/lower-case k)
                  (string/replace #"_" "-")
                  (keyword))]
        (if-not (fn? v)
          (assoc m v k)
          m)))
    {}
    (array-seq (goog.object/getKeys goog.events.KeyCodes))))

(def keyword->keycode
  (into {} (map (juxt val key) keycode->keyword)))

(defn key-code [e]
  (.-keyCode e))

(defn control-key? [e]
  (.-ctrlKey e))

(defn shift-key? [e]
  (.-shiftKey e))

(defn alt-key? [e]
  (.-altKey e))

(defn meta-key? [e]
  (.-metaKey e))

(define-key-code-predicates)

(defn modifiers
  "Given a js/KeyboardEvent return a set of keywords."
  [e]
  (let [mods {:ctrl (control-key? e)
              :shift (shift-key? e)
              :alt (alt-key? e)
              :meta (meta-key? e)}]
    (set (keys (filter val mods)))))

(defn key-set
  "Given a js/KeyboardEvent return a set of keywords.

  Ex.
    (key-set e) => #{:shift :a}
  "
  [e]
  (conj (modifiers e) (keycode->keyword (key-code e))))

