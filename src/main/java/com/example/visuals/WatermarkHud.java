package com.example.visuals;

import java.time.LocalTime;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_640;

/**
 * Watermark: капсула - логотип «A» + название, ник, пинг и FPS (и время по желанию).
 * Пинг показывается вместе с FPS (опция "FPS").
 */
public final class WatermarkHud {
    private WatermarkHud() {}

    public static final String[] NAMES = {"ArchitektVisuals", "Architekt", "AV"};

    private static final int H = 20, PAD = 8, LOGO_H = 12, GAP = 6;
    private static final int LOGO_W = HudStyle.logoWidth(LOGO_H);

    private static int rainbow(float offset) {
        float h = ((System.currentTimeMillis() % 4000L) / 4000f + offset) % 1f;
        return java.awt.Color.HSBtoRGB(h, 0.65f, 1f) | 0xFF000000;
    }

    /** Вертикальный разделитель между блоками. */
    private static void sep(class_332 g, int x, float a) {
        g.method_25294(x, 5, x + 1, H - 5, HudStyle.withAlpha(0x26FFFFFF, a));
    }

    /** Рисует в (0,0), возвращает {w, h}. */
    public static int[] draw(class_332 g, class_310 mc, float a) {
        class_327 tr = mc.field_1772;
        int accent = Config.watermarkRainbow ? rainbow(0f) : Config.COLORS[Config.watermarkColor];
        String name = NAMES[Math.max(0, Math.min(Config.watermarkName, NAMES.length - 1))];

        String nick = null;
        if (Config.watermarkNick && mc.field_1724 != null) nick = mc.method_1548().method_1676();

        String ping = null, fps = null, time = null;
        if (Config.watermarkFps) {
            fps = mc.method_47599() + " FPS";
            if (mc.field_1724 != null && mc.method_1562() != null) {
                class_640 e = mc.method_1562().method_2871(mc.field_1724.method_5667());
                if (e != null) ping = e.method_2959() + " ms";
            }
        }
        if (Config.watermarkTime) {
            LocalTime t = LocalTime.now();
            time = String.format("%02d:%02d", t.getHour(), t.getMinute());
        }

        // ширина
        int nameW = HudStyle.width(tr, name);
        int w = PAD + LOGO_W + 5 + nameW;
        if (nick != null) w += GAP + 1 + GAP + HudStyle.width(tr, nick);
        if (ping != null) w += GAP + 1 + GAP + HudStyle.width(tr, ping);
        if (fps != null) w += (ping != null ? 8 : GAP + 1 + GAP) + HudStyle.width(tr, fps);
        if (time != null) w += GAP + 1 + GAP + HudStyle.width(tr, time);
        w += PAD;

        HudStyle.capsule(g, 0, 0, w, H, a);

        int x = PAD;
        HudStyle.logo(g, x, (H - LOGO_H) / 2, LOGO_H, a);
        x += LOGO_W + 5;

        // название: при радуге каждая буква своего цвета
        if (Config.watermarkRainbow) {
            for (int i = 0; i < name.length(); i++) {
                String ch = String.valueOf(name.charAt(i));
                HudStyle.text(g, tr, ch, x, 6, rainbow(i * 0.06f), a, true);
                x += HudStyle.width(tr, ch);
            }
        } else {
            HudStyle.text(g, tr, name, x, 6, Theme.TEXT, a, true);
            x += nameW;
        }

        if (nick != null) {
            x += GAP; sep(g, x, a); x += 1 + GAP;
            HudStyle.text(g, tr, nick, x, 6, Theme.TEXT, a, true);
            x += HudStyle.width(tr, nick);
        }
        if (ping != null) {
            x += GAP; sep(g, x, a); x += 1 + GAP;
            HudStyle.text(g, tr, ping, x, 6, HudStyle.MUTED, a, true);
            x += HudStyle.width(tr, ping);
        }
        if (fps != null) {
            if (ping != null) x += 8;
            else { x += GAP; sep(g, x, a); x += 1 + GAP; }
            HudStyle.text(g, tr, fps, x, 6, HudStyle.MUTED, a, true);
            x += HudStyle.width(tr, fps);
        }
        if (time != null) {
            x += GAP; sep(g, x, a); x += 1 + GAP;
            HudStyle.text(g, tr, time, x, 6, HudStyle.MUTED, a, true);
        }
        return new int[] {w, H};
    }
}
