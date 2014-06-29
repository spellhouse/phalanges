(ns phalanges.dev
  (:require
   [figwheel.client :as fw :include-macros true]
   [weasel.repl :as ws-repl]
   [phalanges.core :as keyboard]))

(defonce ws-repl-connection
  (ws-repl/connect "ws://localhost:9001" :verbose true))

(fw/watch-and-reload
  ;; :websocket-url "ws:localhost:3449/figwheel-ws" default
  :jsload-callback
  (fn [] (js/console.log (str (gensym "RELOAD"))))) 
