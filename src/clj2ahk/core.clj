(ns clj2ahk.core
  (:require [selmer.parser :as s]
            [selmer.filters :as sf]
            [clojure.edn :as e]
            [clojure.string :as str])
  (:gen-class))

(def emacs-key-map
  {"C" "^"
   "M" "!"
   "S" "+"
   "W" "#"})

(defn emacs-key->ahk-key [s]
  (->> (str/split s #"\-")
       (map #(get emacs-key-map % %))
       str/join))

(sf/add-filter! :ahk-key (fn [s] [:safe (emacs-key->ahk-key s)]))

(defn transpile [m]
  (->> m
       (s/render "{% for opt in opts %}
{{opt}}
{% endfor %}
{% for game in games %}
#IF WinActive(\"{{game.window}}\")
{% for keymap in game.keymaps %}
{{keymap|first|name|ahk-key}}::{{keymap|last|name|ahk-key}}
{% endfor %}

{% for repeat-key in game.repeat-keys %}
{{repeat-key.trigger|name|ahk-key}}::
  Loop {
    Sleep, {{repeat-key.interval|default:\"10\"}}
    GetKeyState, state, {{repeat-key.trigger|name|ahk-key}}, P
    If state = U {
      Break
    } Else If state = D {
      Send, {{ \"{\" }}{{repeat-key.key|name|ahk-key}}{{\"}\"}}
      Return
    }
  }
{% endfor %}
#IF
{% endfor %}")))

(defn -main
  "I don't do a whole lot."
  [& args]
  (let [m (-> (str/join " " args) (e/read-string))]
    (println (transpile m))))
