package com.example.visuals;

import net.minecraft.class_310;

/**
 * Выполнение макросов. poll() вызывается и из тика, и из кадра HUD:
 * так быстрые нажатия не теряются, а двойного срабатывания нет (состояние клавиши общее).
 * Макрос срабатывает ОДИН раз за нажатие и только когда нет открытого экрана (чат, инвентарь и т.д.).
 */
public final class Macros {
    private Macros() {}

    public static void poll() {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null || mc.method_22683() == null) return;
        long window = mc.method_22683().method_4490();
        boolean free = mc.field_1755 == null;

        for (Macro m : Config.macros) {
            boolean d = m.key != -1 && Keys.down(window, m.key);
            if (free && m.enabled && d && !m.wasDown) run(mc, m.text);
            m.wasDown = d;
        }
    }

    /** Выполняет команду ("/...") или отправляет сообщение в чат. Используется и круговым меню. */
    public static void run(class_310 mc, String text) {
        if (text == null || mc.field_1724 == null || mc.field_1724.field_3944 == null) return;
        String t = text.trim();
        if (t.isEmpty()) return;
        if (t.startsWith("/")) mc.field_1724.field_3944.method_45730(t.substring(1));
        else mc.field_1724.field_3944.method_45729(t);
    }
}
