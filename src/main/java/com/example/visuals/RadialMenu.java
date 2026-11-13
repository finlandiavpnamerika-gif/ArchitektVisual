package com.example.visuals;

import net.minecraft.class_310;

/**
 * Открытие кругового меню по клавише. poll() вызывается и из тика, и из кадра HUD
 * (как Macros.poll): быстрый тап не теряется, состояние клавиши общее.
 * Открывается только в игре, когда нет другого экрана (чат, инвентарь и т.д.).
 */
public final class RadialMenu {
    public static final int MAX_ITEMS = 12;

    // true на старте и после смены клавиши, чтобы не открыться от уже зажатой клавиши
    private static boolean wasDown = true;

    private RadialMenu() {}

    public static void resync() {
        wasDown = true;
    }

    public static void poll() {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null || mc.method_22683() == null) return;
        long window = mc.method_22683().method_4490();
        boolean d = Config.radialKey != -1 && Keys.down(window, Config.radialKey);
        if (d && !wasDown && mc.field_1755 == null) {
            mc.execute(() -> {
                if (mc.field_1755 == null) mc.method_1507(new RadialMenuScreen());
            });
        }
        wasDown = d;
    }
}
