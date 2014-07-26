(ns phalanges.core
  "js/KeyboardEvent utilities."
  (:require
   [clojure.string :as string]
   [clojure.set :as set]
   [goog.object]
   [goog.evens :as events])
  (:require-macros
   [phalanges.core :refer [define-key-code-predicates]])
  (:import goog.events.KeyCodes))

;; ---------------------------------------------------------------------
;; Utilities

(defn prune
  "Dissociates entries in a nested associative structure when,
   for each value of a key in the ks path, (pred val) is true.
   Applies the test/dissociation from the bottom-up."
  [m [k & ks] pred]
  (if (contains? m k)
    (if ks
      (let [v (prune (get m k) ks pred)]
        (if (pred v)
          (dissoc m k)
          (assoc m k v)))
      (if (pred (get m k))
        (dissoc m k)
        m))
    m))

;; ---------------------------------------------------------------------
;; Keyword/Keycode conversion

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

;; ---------------------------------------------------------------------
;; KeyboardEvent wrappers

(defn key-code
  "Return the keyCode property of a js/KeyboardEvent."
  [e]
  (.-keyCode e))

(defn key-up?
  "True if e is keyup event, false otherwise."
  [e]
  (= (.-type e) "keyup"))

(defn key-down?
  "True if e is keydown event, false otherwise."
  [e]
  (= (.-type e) "keydown"))

(defn key-press?
  "True if e is keypress event, false otherwise."
  [e]
  (= (.-type e) "keypress"))

(defn repeating?
  "Return the repeat property of js/KeyboardEvent. True if the key is 
  being held down, false otherwise."
  [e]
  (boolean (.-repeat e)))

(defn control-key?
  "Return the ctrlKey property of a js/KeyboardEvent. True if the 
  control key is being held, false otherwise."
  [e]
  (boolean (.-ctrlKey e)))

(defn shift-key?
  "Return the shiftKey property of a js/KeyboardEvent. True if the
  shift key is being held, false otherwise."
  [e]
  (boolean (.-shiftKey e)))

(defn alt-key?
  "Return the altKey property of a js/KeyboardEvent. True if the
  alt key is being held, false otherwise."
  [e]
  (boolean (.-altKey e)))

(defn meta-key?
  "Return the metaKey property of a js/KeyboardEvent. True if the
  meta key is being held, false otherwise."
  [e]
  (boolean (.-metaKey e)))

(define-key-code-predicates)

;; ---------------------------------------------------------------------
;; Keyset

(defn modifier-set
  "Given a js/KeyboardEvent return a set of keywords corresponding to the
  modifier keys involved.
  

  Ex.
    (modifier-set e)
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

;; ---------------------------------------------------------------------
;; Key sequence listener

(defn- get-register! [el]
  (or (aget el "__phalanges_register")
      (let [new-register (atom [])]
        (aset el "__phalanges_register" new-register)
        new-register)))

(defn- get-fn-store! [el]
  (or (aget el "__phalanges_fn_store")
      (let [new-fn-store (atom {})]
        (aset el "__phalanges_fn_store" new-fn-store)
        new-fn-store)))

(defn- get-sequence-listener! [el]
  (or (aget el "__phalanges_sequence_listener")
      (let [register (get-register! el)
            fn-store (get-fn-store! el)
            listener (fn [e]
                       (let [ks (key-set e)]
                         (swap! register conj ks)
                         (let [x (get-in @fn-store @register)]
                           (if (nil? x)
                             (reset! register [key-set])
                             (when (fn? x)
                               (x @register)
                               (swap! register empty))))))]
        (aset el "__phalanges_sequence_listener" listener)
        listener)))

(defn listen-sequence!
  "Listen for keydown events such that whenever a sub-sequence of 
  mapping key-set over those events matches key-sets f is called
  with the matched sub-sequence.
  
  Ex.
    ;; Typing \"a b c\" into #test-input will show \"a b c\" in the
    ;; console.
    (let [el (js/document.getElementById \"test-input\")]
      (listen-sequence! el [#{:a} #{:b} #{:c}] #(js/console.log \"a b c\")))
  "
  [el key-sets f]
  (swap! (get-fn-store! el) assoc-in key-sets f)
  (events/listen el "keydown" (get-sequence-listener! el)))

(defn unlisten-sequence!
  "Stop listening for a sequence of keydown events."
  [el key-sets]
  (swap! (get-fn-store! el)
         (fn [store]
           (let [edit-path (butlast key-sets)
                 key (last key-sets)]
             (-> (update-in store edit-path dissoc key)
                 (prune edit-path empty?))))))
