package com.example.visuals;

import net.minecraft.client.MinecraftClient;

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
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.getWindow() == null) return;
        long window = mc.getWindow().getHandle();
        boolean d = Config.radialKey != -1 && Keys.down(window, Config.radialKey);
        if (d && !wasDown && mc.currentScreen == null) {
            mc.execute(() -> {
                if (mc.currentScreen == null) mc.setScreen(new RadialMenuScreen());
            });
        }
        wasDown = d;
    }
}
