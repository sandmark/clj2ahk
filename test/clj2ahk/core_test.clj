(ns clj2ahk.core-test
  (:require [clojure.test :refer [deftest testing is are]]
            [matcher-combinators.clj-test]
            [matcher-combinators.matchers :as m]
            [mockfn.macros :refer [providing]]
            [clojure.string :as str]
            [clj2ahk.core :as sut]))

(deftest opts-test
  (testing "Single Option"
    (is (match? #(str/includes? % "#UseHook\n") (sut/transpile {:opts ["#UseHook"]}))))
  (testing "Multiple Options"
    (let [r (sut/transpile {:opts ["#UseHook" "#SingleInstance force"]})]
      (is (match? #(str/includes? % "#UseHook\n") r))
      (is (match? #(str/includes? % "#SingleInstance force\n") r)))))

(deftest game-test
  (testing "#IF Block"
    (let [r (sut/transpile {:games [{:window "some window"}]})]
      (is (match? #(str/includes? % "#IF WinActive(\"some window\")\n") r))
      (is (match? #(str/includes? % "#IF\n") r))))
  (testing "Keymaps"

    (let [r (sut/transpile {:games [{:window  "window"
                                     :keymaps {:w :Up :a :Left :s :Down :d :Right}}]})]
      (are [keymap] (match? #(str/includes? % keymap) r)
        "w::Up" "a::Left" "s::Down" "d::Right")))
  (testing "RepeatKey"
    (let [r (sut/transpile {:games [{:window      "window"
                                     :repeat-keys [{:trigger :t :key :e}]}]})]
      (are [s] (match? #(str/includes? % s) r)
        "Sleep, 10\n"
        "GetKeyState, state, t, P\n"
        "Send, {e}\n"))
    (is (match? #(str/includes? % "Sleep, 1")
                (sut/transpile {:games [{:window      "window"
                                         :repeat-keys [{:trigger :t :key :e :interval 1}]}]})))))

(deftest parse-key-test
  (testing "Ctrl"
    (is (match? "^k" (sut/emacs-key->ahk-key "C-k"))))
  (testing "Alt"
    (is (match? "!k" (sut/emacs-key->ahk-key "M-k"))))
  (testing "Shift"
    (is (match? "+k" (sut/emacs-key->ahk-key "S-k"))))
  (testing "Win"
    (is (match? "#k" (sut/emacs-key->ahk-key "W-k"))))
  (testing "Multiple"
    (let [r (sut/transpile {:games [{:window      "window"
                                     :repeat-keys [{:trigger "M-S-t" :key "C-s"}]
                                     :keymaps     {"S-w" "M-Numpad1"}}]})]
      (are [s] (match? #(str/includes? % s) r)
        "Send, {^s}"
        "GetKeyState, state, !+t"
        "+w::!Numpad1"))))
