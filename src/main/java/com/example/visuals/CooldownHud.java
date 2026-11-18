package com.example.visuals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1796;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;

/** HUD с кулдаунами предметов (жемчуг, хорус, щит, серверные кулдауны и т.д.). */
public final class CooldownHud {
    private CooldownHud() {}

    public static final String[] COLOR_NAMES = {"Авто", "Фиолетовый", "Голубой", "Зелёный", "Жёлтый", "Красный"};

    private static final int ROW_H = 22;
    private static final int BAR_W = 56;

    private static final class St {
        long startMs;
        float p0;
    }

    private static final class Row {
        class_1799 stack;
        float progress;
        float seconds; // < 0 = пока неизвестно
    }

    private static final Map<class_1792, St> STATE = new HashMap<>();

    private static int col(int rgb, int a) {
        return (Math.max(0, Math.min(255, a)) << 24) | (rgb & 0xFFFFFF);
    }

    public static void hud(class_332 ctx) {
        if (!Config.cdHud) return;
        class_310 mc = class_310.method_1551();
        class_1657 player = mc.field_1724;
        if (player == null || mc.field_1690.field_1842) {
            STATE.clear();
            return;
        }

        class_1796 cm = player.method_7357();
        long now = System.currentTimeMillis();
        Set<class_1792> seen = new HashSet<>();
        List<Row> rows = new ArrayList<>();

        List<class_1799> stacks = new ArrayList<>();
        for (int i = 0; i < 36; i++) stacks.add(player.method_31548().method_5438(i));
        stacks.add(player.method_6079());

        for (class_1799 s : stacks) {
            if (s.method_7960()) continue;
            class_1792 item = s.method_7909();
            if (seen.contains(item)) continue;
            float p = cm.method_7905(s, 0f);
            if (p <= 0f) continue;
            seen.add(item);

            St st = STATE.get(item);
            if (st == null) {
                st = new St();
                st.startMs = now;
                st.p0 = p;
                STATE.put(item, st);
            }

            Row r = new Row();
            r.stack = s;
            r.progress = Math.min(1f, p);
            float drop = st.p0 - p;
            long elapsed = now - st.startMs;
            if (drop > 0.02f && elapsed > 80) {
                float totalMs = elapsed / drop;
                r.seconds = Math.max(0f, p * totalMs / 1000f);
            } else {
                r.seconds = -1f;
            }
            rows.add(r);
        }
        STATE.keySet().retainAll(seen);

        if (rows.isEmpty()) return;

        float a = HudStyle.clampAlpha(Config.cdAlpha);
        class_327 tr = mc.field_1772;
        int x = Config.cdX;
        int y = Config.cdY;
        final int PAD = 6, HEAD = 15, ROW = 21;

        String title = "Cooldowns";
        int w = HudStyle.titleWidth(tr, Theme.ICON_UTILS, title) + PAD * 2;
        int n = rows.size();
        String[] names = new String[n], times = new String[n];
        for (int i = 0; i < n; i++) {
            Row r = rows.get(i);
            names[i] = tr.method_27523(r.stack.method_7964().getString(), 80);
            if (!Config.cdTime) times[i] = "";
            else if (r.seconds < 0) times[i] = Math.round(r.progress * 100) + "%";
            else if (r.seconds >= 10f) times[i] = (int) Math.ceil(r.seconds) + "с";
            else times[i] = String.format(Locale.ROOT, "%.1fс", r.seconds);
            w = Math.max(w, PAD + 2 + 16 + 5 + tr.method_1727(names[i]) + 12 + tr.method_1727(times[i]) + PAD);
        }
        int h = HEAD + 4 + n * ROW + 1;

        HudStyle.panel(ctx, x, y, w, h, a, Theme.ACCENT);
        HudStyle.header(ctx, x, y, w, HEAD - 2, a, Theme.ACCENT);
        HudStyle.title(ctx, tr, Theme.ICON_UTILS, title, x + PAD, y + 4, a, Theme.ACCENT);
        HudStyle.divider(ctx, x + PAD, x + w - PAD, y + HEAD, 0xFFFFFFFF, a * 0.35f);

        int ry = y + HEAD + 3;
        for (int i = 0; i < n; i++) {
            Row r = rows.get(i);
            int rgb;
            if (Config.cdColor == 0) {
                rgb = r.progress > 0.5f ? 0xE74C3C : (r.progress > 0.2f ? 0xF1C40F : 0x2ECC71);
            } else {
                rgb = Config.COLORS[Math.min(Config.cdColor - 1, Config.COLORS.length - 1)];
            }
            int color = 0xFF000000 | (rgb & 0xFFFFFF);

            HudStyle.roundFill(ctx, x + 3, ry + 2, 2, ROW - 6, 1, HudStyle.withAlpha(color, a));   // цветная метка слева
            ctx.method_51427(r.stack, x + PAD + 1, ry + 1);

            int tx = x + PAD + 1 + 16 + 5;
            HudStyle.text(ctx, tr, names[i], tx, ry + 2, Theme.TEXT, a, true);
            if (!times[i].isEmpty()) HudStyle.textRight(ctx, tr, times[i], x + w - PAD, ry + 2, Theme.TEXT, a, true);
            HudStyle.bar(ctx, tx, ry + 13, x + w - PAD - tx, 3, r.progress, color, a);

            ry += ROW;
        }
    }
}
