package com.example.visuals;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** Time & Date: реальное время, дата и (по желанию) время суток в мире. */
public final class ClockHud {
    private ClockHud() {}

    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter HMS = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static int[] draw(DrawContext g, MinecraftClient mc, float a) {
        List<String> labels = new ArrayList<>();
        List<String> values = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        labels.add("Время");
        values.add(now.format(Config.clockSeconds ? HMS : HM));
        colors.add(Theme.TEXT);
        if (Config.clockDate) {
            labels.add("Дата");
            values.add(now.format(DATE));
            colors.add(Theme.TEXT);
        }
        if (Config.clockGame && mc.world != null) {
            long t = (mc.world.getTimeOfDay() + 6000) % 24000;   // 0 тиков = 06:00
            int hh = (int) (t / 1000), mm = (int) ((t % 1000) * 60 / 1000);
            labels.add("В мире");
            values.add(String.format("%02d:%02d", hh, mm));
            colors.add(Theme.TEXT);
        }
        return HudRows.draw(g, mc.textRenderer, "Clock", labels, values, colors, Config.COLORS[Config.clockColor], a);
    }
}
