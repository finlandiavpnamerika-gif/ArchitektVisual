package com.example.visuals;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/** Watermark: название клиента + (по желанию) ник, FPS и время. */
public final class WatermarkHud {
    private WatermarkHud() {}

    public static final String[] NAMES = {"ArchitektVisuals", "Architekt", "AV"};

    private static int rainbow(float offset) {
        float h = ((System.currentTimeMillis() % 4000L) / 4000f + offset) % 1f;
        return java.awt.Color.HSBtoRGB(h, 0.65f, 1f) | 0xFF000000;
    }

    /** Рисует в (0,0), возвращает {w, h}. */
    public static int[] draw(DrawContext g, MinecraftClient mc, float a) {
        TextRenderer tr = mc.textRenderer;
        int accent = Config.watermarkRainbow ? rainbow(0f) : Config.COLORS[Config.watermarkColor];
        String name = NAMES[Math.max(0, Math.min(Config.watermarkName, NAMES.length - 1))];

        List<String> extra = new ArrayList<>();
        if (Config.watermarkNick && mc.player != null) extra.add(mc.getSession().getUsername());
        if (Config.watermarkFps) extra.add(mc.getCurrentFps() + " FPS");
        if (Config.watermarkTime) {
            LocalTime t = LocalTime.now();
            extra.add(String.format("%02d:%02d", t.getHour(), t.getMinute()));
        }

        int pad = 6, h = 16;
        int nameW = tr.getWidth(name);
        int w = pad * 2 + nameW;
        for (String s : extra) w += tr.getWidth(s) + 11;

        HudStyle.panel(g, 0, 0, w, h, a, accent);
        HudStyle.header(g, 0, 0, w, h / 2, a, accent);

        // название: при радуге каждая буква своего цвета
        int x = pad;
        if (Config.watermarkRainbow) {
            for (int i = 0; i < name.length(); i++) {
                String ch = String.valueOf(name.charAt(i));
                HudStyle.text(g, tr, ch, x, 4, rainbow(i * 0.06f), a, true);
                x += tr.getWidth(ch);
            }
        } else {
            HudStyle.text(g, tr, name, x, 4, accent, a, true);
            x += nameW;
        }

        for (String s : extra) {
            x += 4;
            g.fill(x, 3, x + 1, h - 3, HudStyle.withAlpha(Theme.TEXT_DIM, a * 0.6f));
            x += 4;
            HudStyle.text(g, tr, s, x, 4, Theme.TEXT, a, true);
            x += tr.getWidth(s) + 3;
        }
        return new int[] {w, h};
    }
}
