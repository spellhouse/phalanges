(ns phalanges.core
  "js/KeyboardEvent utilities."
  (:require
   [clojure.string :as string]
   [clojure.set :as set]
   [goog.object])
  (:require-macros
   [phalanges.core :refer [define-key-code-predicates]])
  (:import goog.events.KeyCodes))

(def
  ^{:doc "ClojureScript friendly version of goog.events.KeyCodes."}
  keyword->keycode
  (reduce
    (fn [m k]
      (let [v (aget goog.events.KeyCodes k)
            k (-> (string/lower-case k)
                  (string/replace #"_" "-")
                  (keyword))]
        (if-not (fn? v)
          (assoc m k v)
          m)))
    {}
    (array-seq (goog.object/getKeys goog.events.KeyCodes))))

(def
  ^{:doc "Inversion of keyword->keycode."}
  keycode->keyword
  (set/map-invert keyword->keycode))

(defn key-code
  "Return the keyCode property of a js/KeyboardEvent. "
  [e]
  (.-keyCode e))

(defn repeating?
  "Return the repeat property of js/KeyboardEvent. True if the key is 
  being held down."
  [e]
  (.-repeat e))

(defn control-key?
  "Return the ctrlKey property of a js/KeyboardEvent. True if the 
  control key is being held."
  [e]
  (boolean (.-ctrlKey e)))

(defn shift-key?
  "Return the shiftKey property of a js/KeyboardEvent. True if the
  shift key is being held."
  [e]
  (boolean (.-shiftKey e)))

(defn alt-key?
  "Return the altKey property of a js/KeyboardEvent. True if the
  alt key is being held."
  [e]
  (boolean (.-altKey e)))

(defn meta-key?
  "Return the metaKey property of a js/KeyboardEvent. True if the
  meta key is being held."
  [e]
  (boolean (.-metaKey e)))

(define-key-code-predicates)

(defn modifier-set
  "Given a js/KeyboardEvent return a set of keywords corresponding to the
  modifier keys involved.
  

  Ex.
    (modifiers e)
    ;; => #{:control :shift}
  "
  [e]
  (let [mods {:ctrl (control-key? e)
              :shift (shift-key? e)
              :alt (alt-key? e)
              :meta (meta-key? e)}]
    (set (keys (filter val mods)))))

(defn key-set
  "Given a js/KeyboardEvent return a set of keywords corresponding to the
  keys pressed. Includes modifiers.

  Ex.
    (key-set e) 
    ;; => #{:shift :a}
  "
  [e]
  (conj (modifier-set e) (keycode->keyword (key-code e))))

