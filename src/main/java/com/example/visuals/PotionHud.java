package com.example.visuals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_10799;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_408;
import net.minecraft.class_6880;

/** Potion HUD: активные эффекты с иконкой, уровнем, таймером и полоской оставшегося времени. */
public final class PotionHud {
    private PotionHud() {}

    private static final int PAD = 5;
    private static final int HEADER = 14;
    private static final int ROW = 21;
    private static final int ICON = 18;

    /** Максимальная длительность каждого эффекта (для полоски). */
    private static final Map<String, Integer> MAX = new HashMap<>();

    private static String roman(int n) {
        String[] r = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return n >= 1 && n <= 10 ? r[n] : String.valueOf(n);
    }

    private static String time(class_1293 e) {
        if (e.method_48559()) return "∞";
        int s = e.method_5584() / 20;
        return s >= 60 ? (s / 60) + "m" + (s % 60) + "s" : s + "s";
    }

    private static String key(class_6880<class_1291> e) {
        return e.method_40230().map(k -> k.method_29177().toString()).orElse("?");
    }

    public static int[] draw(class_332 g, class_310 mc, float a) {
        class_327 tr = mc.field_1772;
        int accent = Config.COLORS[Config.potionColor];

        List<class_1293> list = new ArrayList<>(mc.field_1724.method_6026());
        boolean demo = false;
        if (list.isEmpty()) {
            MAX.clear();
            if (!(mc.field_1755 instanceof class_408)) return null;
            demo = true;   // пока открыт чат - показываем заглушку, чтобы можно было перетащить
        }
        // самые долгие сверху, бесконечные - первыми
        list.sort((p, q) -> Integer.compare(q.method_48559() ? Integer.MAX_VALUE : q.method_5584(),
                p.method_48559() ? Integer.MAX_VALUE : p.method_5584()));

        Set<String> alive = new HashSet<>();
        int n = list.size();
        String[] names = new String[n], times = new String[n];
        for (int i = 0; i < n; i++) {
            class_1293 e = list.get(i);
            String k = key(e.method_5579());
            alive.add(k);
            MAX.merge(k, e.method_5584(), Math::max);
            String name = e.method_5579().comp_349().method_5560().getString();
            if (Config.potionLevel && e.method_5578() > 0) name += " " + roman(e.method_5578() + 1);
            names[i] = name;
            times[i] = time(e);
        }
        MAX.keySet().retainAll(alive);

        if (Config.potionHoriz && !demo) {
            int tile = Math.max(30, 0);
            int[] tw = new int[n];
            int w = PAD;
            for (int i = 0; i < n; i++) { tw[i] = Math.max(tile, tr.method_1727(times[i]) + 8); w += tw[i] + (i < n - 1 ? 3 : 0); }
            w += PAD;
            int h = PAD * 2 + 38;
            HudStyle.panel(g, 0, 0, w, h, a, accent);
            long nowH = System.currentTimeMillis();
            int x = PAD;
            for (int i = 0; i < n; i++) {
                class_1293 e = list.get(i);
                int side = e.method_5579().comp_349().method_5573() ? HudStyle.GOOD : HudStyle.BAD;
                HudStyle.roundFill(g, x, PAD, tw[i], 38, 2, HudStyle.withAlpha(0x38000000, a));
                HudStyle.roundFill(g, x + 2, PAD, tw[i] - 4, 2, 1, HudStyle.withAlpha(side, a));
                if (Config.potionIcons) {
                    class_2960 id = e.method_5579().method_40230().map(k -> k.method_29177().method_45138("mob_effect/")).orElse(null);
                    if (id != null) g.method_71501(class_10799.field_56883, id, x + (tw[i] - ICON) / 2, PAD + 4, ICON, ICON, a);
                }
                if (Config.potionLevel && e.method_5578() > 0) {
                    String lv = roman(e.method_5578() + 1);
                    HudStyle.text(g, tr, lv, x + tw[i] - tr.method_1727(lv) - 2, PAD + 4, Theme.TEXT, a, true);
                }
                int sec = e.method_5584() / 20;
                int tc = e.method_48559() ? HudStyle.GOOD : sec <= 10 ? HudStyle.BAD : sec <= 30 ? HudStyle.MID : Theme.TEXT;
                float ta = (!e.method_48559() && sec <= 10) ? a * (0.55f + 0.45f * (float) Math.abs(Math.sin(nowH / 220.0))) : a;
                HudStyle.text(g, tr, times[i], x + (tw[i] - tr.method_1727(times[i])) / 2, PAD + 24, tc, ta, true);
                if (Config.potionBars) {
                    int max = MAX.getOrDefault(key(e.method_5579()), Math.max(1, e.method_5584()));
                    float ratio = e.method_48559() ? 1f : Math.max(0f, Math.min(1f, e.method_5584() / (float) Math.max(1, max)));
                    HudStyle.bar(g, x + 3, PAD + 34, tw[i] - 6, 2, ratio, e.method_48559() ? HudStyle.GOOD : HudStyle.ratioColor(ratio), a);
                }
                x += tw[i] + 3;
            }
            return new int[] {w, h};
        }

        String title = "Potions";
        int textW = HudStyle.titleWidth(tr, Theme.ICON_VISUAL, title);
        for (int i = 0; i < n; i++) textW = Math.max(textW, tr.method_1727(names[i]) + 14 + tr.method_1727(times[i]));
        int iconW = Config.potionIcons ? ICON + 5 : 0;
        int w = Math.max(96, PAD * 2 + iconW + textW - (Config.potionIcons ? 0 : 0));
        int h = HEADER + 3 + (demo ? 14 : n * ROW) + 2;

        HudStyle.panel(g, 0, 0, w, h, a, accent);
        HudStyle.header(g, 0, 0, w, HEADER, a, accent);
        HudStyle.title(g, tr, Theme.ICON_VISUAL, title, PAD, 3, a, accent);
        HudStyle.divider(g, PAD, w - PAD, HEADER, 0xFFFFFFFF, a * 0.35f);

        if (demo) {
            HudStyle.text(g, tr, "Нет эффектов", PAD, HEADER + 5, Theme.TEXT_DIM, a, true);
            return new int[] {w, h};
        }

        long now = System.currentTimeMillis();
        int y = HEADER + 3;
        for (int i = 0; i < n; i++) {
            class_1293 e = list.get(i);
            class_1291 fx = e.method_5579().comp_349();
            int side = fx.method_5573() ? HudStyle.GOOD : HudStyle.BAD;

            // цветная метка: полезный / вредный
            HudStyle.roundFill(g, 2, y + 2, 2, ROW - 5, 1, HudStyle.withAlpha(side, a));

            int tx = PAD + 1;
            if (Config.potionIcons) {
                class_2960 id = e.method_5579().method_40230().map(k -> k.method_29177().method_45138("mob_effect/")).orElse(null);
                if (id != null) g.method_71501(class_10799.field_56883, id, tx, y + 1, ICON, ICON, a);
                tx += ICON + 4;
            }

            int sec = e.method_5584() / 20;
            int tc = e.method_48559() ? HudStyle.GOOD : sec <= 10 ? HudStyle.BAD : sec <= 30 ? HudStyle.MID : Theme.TEXT;
            float ta = a;
            if (!e.method_48559() && sec <= 10) ta = a * (0.55f + 0.45f * (float) Math.abs(Math.sin(now / 220.0)));   // мигает перед окончанием

            HudStyle.text(g, tr, names[i], tx, y + 2, Theme.TEXT, a, true);
            HudStyle.textRight(g, tr, times[i], w - PAD, y + 2, tc, ta, true);

            if (Config.potionBars) {
                int bw = w - tx - PAD;
                int max = MAX.getOrDefault(key(e.method_5579()), Math.max(1, e.method_5584()));
                float ratio = e.method_48559() ? 1f : Math.max(0f, Math.min(1f, e.method_5584() / (float) Math.max(1, max)));
                HudStyle.bar(g, tx, y + 13, bw, 3, ratio, e.method_48559() ? HudStyle.GOOD : HudStyle.ratioColor(ratio), a);
            }
            y += ROW;
        }
        return new int[] {w, h};
    }
}
