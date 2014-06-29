(ns phalanges.core
  (:require
   [clojure.string :as string]))

(def key-data
  '[{:default a :shift A}
    {:default alt}
    {:default b :shift B}
    {:default backslash :shift pipe}
    {:default backspace}
    {:default c :shift C}
    {:default caps-lock}
    {:default close-square-bracket :shift close-curley-bracket}
    {:default comma :shift open-angle-bracket}
    {:default d :shift D}
    {:default dash :shift underscore}
    {:default delete}
    {:default down}
    {:default e :shift E}
    {:default eight :shift asterix}
    {:default end}
    {:default enter}
    {:default equals :shift plus}
    {:default esc}
    {:default f :shift F}
    {:default f1}
    {:default f10}
    {:default f11}
    {:default f12}
    {:default f2}
    {:default f3}
    {:default f4}
    {:default f5}
    {:default f6}
    {:default f7}
    {:default f8}
    {:default f9}
    {:default ff-dash}
    {:default ff-equals}
    {:default ff-semicolon}
    {:default first-media-key}
    {:default five :shift percent}
    {:default four :shift dollar}
    {:default g :shift G}
    {:default h :shift H}
    {:default home}
    {:default i :shift I}
    {:default insert}
    {:default j :shift J}
    {:default k :shift K}
    {:default l :shift L}
    {:default last-media-key}
    {:default left}
    {:default m :shift M}
    {:default mac-enter}
    {:default mac-ff-meta}
    {:default mac-wk-cmd-left}
    {:default mac-wk-cmd-right}
    {:default n :shift N}
    {:default nine :shift open-round-bracket}
    {:default num-center}
    {:default num-division}
    {:default num-eight}
    {:default num-five}
    {:default num-four}
    {:default num-minus}
    {:default num-multiply}
    {:default num-nine}
    {:default num-one}
    {:default num-period}
    {:default num-plus}
    {:default num-seven}
    {:default num-six}
    {:default num-three}
    {:default num-two}
    {:default num-zero}
    {:default numlock}
    {:default o :shift O}
    {:default one :shift exclamation-mark}
    {:default open-square-bracket :shift open-curley-bracket}
    {:default p :shift P}
    {:default page-down}
    {:default page-up}
    {:default pause}
    {:default period :shift close-angle-bracket}
    {:default phantom}
    {:default print-screen}
    {:default q :shift Q}
    {:default question-mark :shift forward-slash}
    {:default r :shift R}
    {:default right}
    {:default s :shift S}
    {:default scroll-lock}
    {:default semicolon :shift colon}
    {:default seven :shift ampersand}
    {:default single-quote :shift double-quote}
    {:default six :shift caret}
    {:default slash :shift pipe}
    {:default space}
    {:default t :shift T}
    {:default tab}
    {:default three :shift sharp}
    {:default tilde :shift backtick}
    {:default two :shift at-sign}
    {:default u :shift U}
    {:default up}
    {:default v :shift V}
    {:default w :shift W}
    {:default win-ime}
    {:default win-key-ff-linux}
    {:default win-key-right}
    {:default x :shift X}
    {:default y :shift Y}
    {:default z :shift Z}
    {:default zero :shift close-round-bracket}])

(defn goog-key-code-lookup [s]
  (let [okey (-> (name s)
                 (string/upper-case)
                 (string/replace #"-" "_"))]
    `(aget goog.events.KeyCodes ~okey)))

(defn do-key-code-check-defn
  [{:keys [default]}]
  (let [sym (symbol (str default "-key?"))]
    `(defn ~sym [e#]
       (= (phalanges.core/key-code e#)
          ~(goog-key-code-lookup default)))))

(defn do-key-code-with-shift-check-defn
  [{:keys [default shift]}]
  (when shift
    (let [sym (symbol (str shift "-key?"))]
      `(defn ~sym [e#]
         (and (= (phalanges.core/key-code e#)
                 ~(goog-key-code-lookup default))
              (phalanges.core/shift-key? e#))))))

(defn do-defns [key-data]
  (let [f (juxt do-key-code-check-defn
                do-key-code-with-shift-check-defn)]
    (remove nil? (f key-data))))

(defmacro define-key-code-predicates []
  `(do ~@(mapcat do-defns key-data)))
