package com.example.visuals;

import net.minecraft.client.MinecraftClient;

/**
 * Выполнение макросов. poll() вызывается и из тика, и из кадра HUD:
 * так быстрые нажатия не теряются, а двойного срабатывания нет (состояние клавиши общее).
 * Макрос срабатывает ОДИН раз за нажатие и только когда нет открытого экрана (чат, инвентарь и т.д.).
 */
public final class Macros {
    private Macros() {}

    public static void poll() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.getWindow() == null) return;
        long window = mc.getWindow().getHandle();
        boolean free = mc.currentScreen == null;

        for (Macro m : Config.macros) {
            boolean d = m.key != -1 && Keys.down(window, m.key);
            if (free && m.enabled && d && !m.wasDown) run(mc, m.text);
            m.wasDown = d;
        }
    }

    private static void run(MinecraftClient mc, String text) {
        if (text == null || mc.player == null || mc.player.networkHandler == null) return;
        String t = text.trim();
        if (t.isEmpty()) return;
        if (t.startsWith("/")) mc.player.networkHandler.sendChatCommand(t.substring(1));
        else mc.player.networkHandler.sendChatMessage(t);
    }
}
