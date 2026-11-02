package com.example.visuals;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_1293;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_640;

/** Info HUD: FPS, пинг, координаты, направление и эффекты (перенесён из Features.hud). */
public final class InfoHud {
    private InfoHud() {}

    private static final int PAD = 6;
    private static final int LINE = 10;
    private static final int HEADER = 14;
    private static final int COL_GAP = 12;

    private static String time(class_1293 e) {
        if (e.method_48559()) return "inf";
        int s = e.method_5584() / 20;
        return String.format(Locale.ROOT, "%02d:%02d", s / 60, s % 60);
    }

    /** Рисует в (0,0), возвращает {w, h}. */
    public static int[] draw(class_332 g, class_310 mc, float a) {
        class_327 tr = mc.field_1772;
        int accent = Config.COLORS[Config.hudColor];

        // ---- данные ----
        List<String> labels = new ArrayList<>();
        List<String> values = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        if (Config.hudFps) {
            int fps = mc.method_47599();
            labels.add("FPS");
            values.add(String.valueOf(fps));
            colors.add(fps >= 60 ? HudStyle.GOOD : fps >= 30 ? HudStyle.MID : HudStyle.BAD);
        }
        if (Config.hudPing) {
            class_640 entry = mc.method_1562() == null
                    ? null : mc.method_1562().method_2871(mc.field_1724.method_5667());
            int ping = entry == null ? 0 : entry.method_2959();
            labels.add("Ping");
            values.add(ping + " ms");
            colors.add(ping <= 80 ? HudStyle.GOOD : ping <= 150 ? HudStyle.MID : HudStyle.BAD);
        }
        if (Config.hudCoords) {
            labels.add("XYZ");
            values.add(String.format(Locale.ROOT, "%.1f / %.1f / %.1f",
                    mc.field_1724.method_23317(), mc.field_1724.method_23318(), mc.field_1724.method_23321()));
            colors.add(Theme.TEXT);
        }
        if (Config.hudFacing) {
            String f = mc.field_1724.method_5735().name().toLowerCase(Locale.ROOT);
            labels.add("Facing");
            values.add(Character.toUpperCase(f.charAt(0)) + f.substring(1));
            colors.add(Theme.TEXT);
        }

        List<String> effNames = new ArrayList<>();
        List<String> effTimes = new ArrayList<>();
        if (Config.hudEffects) {
            for (class_1293 e : mc.field_1724.method_6026()) {
                String name = e.method_5579().comp_349().method_5560().getString();
                if (e.method_5578() > 0) name = name + " " + (e.method_5578() + 1);
                effNames.add(name);
                effTimes.add(time(e));
            }
        }

        // ---- размеры ----
        String title = "ArchitektVisuals";
        int inner = tr.method_1727(title);
        for (int i = 0; i < labels.size(); i++)
            inner = Math.max(inner, tr.method_1727(labels.get(i)) + COL_GAP + tr.method_1727(values.get(i)));
        for (int i = 0; i < effNames.size(); i++)
            inner = Math.max(inner, tr.method_1727(effNames.get(i)) + COL_GAP + tr.method_1727(effTimes.get(i)));

        int w = inner + PAD * 2;
        int h = HEADER + 4 + labels.size() * LINE
                + (effNames.isEmpty() ? 0 : 6 + 11 + effNames.size() * LINE) + 3;

        // ---- рисуем ----
        HudStyle.panel(g, 0, 0, w, h, a, accent);
        HudStyle.header(g, 0, 0, w, HEADER, a, accent);
        HudStyle.text(g, tr, title, PAD, 3, accent, a, true);
        HudStyle.divider(g, PAD, w - PAD, HEADER, HudStyle.withAlpha(accent, 0.35f), a);

        int y = HEADER + 4;
        for (int i = 0; i < labels.size(); i++) {
            HudStyle.text(g, tr, labels.get(i), PAD, y, Theme.TEXT_DIM, a, true);
            HudStyle.textRight(g, tr, values.get(i), w - PAD, y, colors.get(i), a, true);
            y += LINE;
        }

        if (!effNames.isEmpty()) {
            y += 2;
            HudStyle.divider(g, PAD, w - PAD, y, 0x30FFFFFF, a);
            y += 4;
            HudStyle.text(g, tr, "EFFECTS", PAD, y, accent, a, true);
            y += 11;
            for (int i = 0; i < effNames.size(); i++) {
                HudStyle.text(g, tr, effNames.get(i), PAD, y, Theme.TEXT, a, true);
                HudStyle.textRight(g, tr, effTimes.get(i), w - PAD, y, Theme.TEXT_DIM, a, true);
                y += LINE;
            }
        }
        return new int[] {w, h};
    }
}
