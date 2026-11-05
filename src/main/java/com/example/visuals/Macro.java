package com.example.visuals;

/** Один макрос: клавиша -> команда (начинается с "/") или сообщение в чат. */
public final class Macro {
    public int key;            // GLFW-код клавиши, -1 = не задана
    public String text;        // "/spawn" - команда, "привет" - сообщение в чат
    public boolean enabled = true;
    public boolean wasDown = true;   // true на старте, чтобы не сработать от уже зажатой клавиши

    public Macro(int key, String text) {
        this.key = key;
        this.text = text;
    }
}
