(ns clj2ahk.core
  (:require [selmer.parser :as s]
            [clojure.edn :as e]
            [clojure.string :as str])
  (:gen-class))

(defn transpile [m]
  (->> m
       (s/render "{% for opt in opts %}
{{opt}}
{% endfor %}
{% for game in games %}
#IF WinActive(\"{{game.window}}\")
{% for keymap in game.keymaps %}
{{keymap|first|name}}::{{keymap|last|name}}
{% endfor %}

{% for repeat-key in game.repeat-keys %}
{{repeat-key.trigger|name}}::
  Loop {
    Sleep, {{repeat-key.interval|default:\"10\"}}
    GetKeyState, state, {{repeat-key.trigger | name}}, P
    If state = U {
      Break
    } Else If state = D {
      Send, {{ \"{\" }}{{repeat-key.key | name }}{{\"}\"}}
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
