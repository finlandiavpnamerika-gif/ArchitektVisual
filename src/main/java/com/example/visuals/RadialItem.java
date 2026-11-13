package com.example.visuals;

/** Один сектор кругового меню: подпись + команда (начинается с "/") или сообщение в чат. */
public final class RadialItem {
    public String label;   // что написано на секторе; пусто = показываем саму команду
    public String text;    // "/spawn" - команда, "привет" - сообщение в чат

    public RadialItem(String label, String text) {
        this.label = label == null ? "" : label;
        this.text = text == null ? "" : text;
    }

    public String title() {
        return label.isEmpty() ? text : label;
    }
}
