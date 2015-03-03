(ns phalanges.core
  "js/KeyboardEvent utilities."
  (:require
   [clojure.string :as string]
   [clojure.set :as set]
   [goog.object]
   [goog.events :as events])
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

;; Produced with following code:
;; (reduce
;;     (fn [m k]
;;       (let [v (aget goog.events.KeyCodes k)
;;             k (-> (string/lower-case k)
;;                   (string/replace #"_" "-")
;;                   (keyword))]
;;         (if-not (fn? v)
;;           (assoc m k v)
;;           m)))
;;     {}
;;     (array-seq (goog.object/getKeys goog.events.KeyCodes)))

(def
  ^{:doc "ClojureScript friendly version of goog.events.KeyCodes."}
  keyword->keycode
  {:a 65
   :alt 18
   :apostrophe 192
   :b 66
   :backslash 220
   :backspace 8
   :c 67
   :caps-lock 20
   :close-square-bracket 221
   :comma 188
   :context-menu 93
   :ctrl 17
   :d 68
   :dash 189
   :delete 46
   :down 40
   :e 69
   :eight 56
   :end 35
   :enter 13
   :equals 187
   :esc 27
   :f 70
   :f1 112
   :f10 121
   :f11 122
   :f12 123
   :f2 113
   :f3 114
   :f4 115
   :f5 116
   :f6 117
   :f7 118
   :f8 119
   :f9 120
   :ff-dash 173
   :ff-equals 61
   :ff-semicolon 59
   :first-media-key 166
   :five 53
   :four 52
   :g 71
   :h 72
   :home 36
   :i 73
   :insert 45
   :j 74
   :k 75
   :l 76
   :last-media-key 183
   :left 37
   :m 77
   :mac-enter 3
   :mac-ff-meta 224
   :mac-wk-cmd-left 91
   :mac-wk-cmd-right 93
   :meta 91
   :n 78
   :nine 57
   :num-center 12
   :num-division 111
   :num-eight 104
   :num-five 101
   :num-four 100
   :num-minus 109
   :num-multiply 106
   :num-nine 105
   :num-one 97
   :num-period 110
   :num-plus 107
   :num-seven 103
   :num-six 102
   :num-three 99
   :num-two 98
   :num-zero 96
   :numlock 144
   :o 79
   :one 49
   :open-square-bracket 219
   :p 80
   :page-down 34
   :page-up 33
   :pause 19
   :period 190
   :phantom 255
   :print-screen 44
   :q 81
   :question-mark 63
   :r 82
   :right 39
   :s 83
   :scroll-lock 145
   :semicolon 186
   :seven 55
   :shift 16
   :single-quote 222
   :six 54
   :slash 191
   :space 32
   :t 84
   :tab 9
   :three 51
   :tilde 192
   :two 50
   :u 85
   :up 38
   :v 86
   :w 87
   :win-ime 229
   :win-key 224
   :win-key-ff-linux 0
   :win-key-right 92
   :x 88
   :y 89
   :z 90
   :zero 48})

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
;; KeyCodes wrappers

(def
  ^{:arglists '([e])
    :doc "Returns true if the event contains a text modifying key."}
  text-modifying-key?
  KeyCodes.isTextModifyingKeyEvent)

(def
  ^{:arglists '([key-code & [held-key-code shift-key? control-key? alt-key?]])
    :doc "Returns true if the key fires a keypress event in the current
 browser."}
  fires-key-press?
  KeyCodes.firesKeyPressEvent)

(def
  ^{:arglists '([key-code])
    :doc "Returns true if the key produces a character.
This does not cover characters on non-US keyboards (Russian, Hebrew, etc.)."}
  character-key?
  KeyCodes.isCharacterKey)

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

(defn key-char
  "Given a js/KeyboardEvent return the keyCode property converted to a
  string."
  [e]
  (char (key-code e)))


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
