package com.example.visuals;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.effect.StatusEffectInstance;

/** Info HUD: FPS, пинг, координаты, направление и эффекты (перенесён из Features.hud). */
public final class InfoHud {
    private InfoHud() {}

    private static final int PAD = 6;
    private static final int LINE = 10;
    private static final int HEADER = 14;
    private static final int COL_GAP = 12;

    private static String time(StatusEffectInstance e) {
        if (e.isInfinite()) return "inf";
        int s = e.getDuration() / 20;
        return String.format(Locale.ROOT, "%02d:%02d", s / 60, s % 60);
    }

    /** Рисует в (0,0), возвращает {w, h}. */
    public static int[] draw(DrawContext g, MinecraftClient mc, float a) {
        TextRenderer tr = mc.textRenderer;
        int accent = Config.COLORS[Config.hudColor];

        // ---- данные ----
        List<String> labels = new ArrayList<>();
        List<String> values = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        if (Config.hudFps) {
            int fps = mc.getCurrentFps();
            labels.add("FPS");
            values.add(String.valueOf(fps));
            colors.add(fps >= 60 ? HudStyle.GOOD : fps >= 30 ? HudStyle.MID : HudStyle.BAD);
        }
        if (Config.hudPing) {
            PlayerListEntry entry = mc.getNetworkHandler() == null
                    ? null : mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            int ping = entry == null ? 0 : entry.getLatency();
            labels.add("Ping");
            values.add(ping + " ms");
            colors.add(ping <= 80 ? HudStyle.GOOD : ping <= 150 ? HudStyle.MID : HudStyle.BAD);
        }
        if (Config.hudCoords) {
            labels.add("XYZ");
            values.add(String.format(Locale.ROOT, "%.1f / %.1f / %.1f",
                    mc.player.getX(), mc.player.getY(), mc.player.getZ()));
            colors.add(Theme.TEXT);
        }
        if (Config.hudFacing) {
            String f = mc.player.getHorizontalFacing().name().toLowerCase(Locale.ROOT);
            labels.add("Facing");
            values.add(Character.toUpperCase(f.charAt(0)) + f.substring(1));
            colors.add(Theme.TEXT);
        }

        List<String> effNames = new ArrayList<>();
        List<String> effTimes = new ArrayList<>();

        if (Config.hudHoriz) {
            int n = labels.size();
            if (n == 0) return new int[] {1, 1};
            int w = PAD;
            for (int i = 0; i < n; i++) w += tr.getWidth(labels.get(i)) + 4 + tr.getWidth(values.get(i)) + (i < n - 1 ? 14 : 0);
            w += PAD;
            int h = 16;
            HudStyle.panel(g, 0, 0, w, h, a, accent);
            int x = PAD;
            for (int i = 0; i < n; i++) {
                HudStyle.text(g, tr, labels.get(i), x, 4, Theme.TEXT_DIM, a, true);
                x += tr.getWidth(labels.get(i)) + 4;
                HudStyle.text(g, tr, values.get(i), x, 4, colors.get(i), a, true);
                x += tr.getWidth(values.get(i));
                if (i < n - 1) { g.fill(x + 6, 3, x + 7, h - 3, HudStyle.withAlpha(0x40FFFFFF, a)); x += 14; }
            }
            return new int[] {w, h};
        }

        // ---- размеры ----
        String title = "ArchitektVisuals";
        int inner = tr.getWidth(title);
        for (int i = 0; i < labels.size(); i++)
            inner = Math.max(inner, tr.getWidth(labels.get(i)) + COL_GAP + tr.getWidth(values.get(i)));
        for (int i = 0; i < effNames.size(); i++)
            inner = Math.max(inner, tr.getWidth(effNames.get(i)) + COL_GAP + tr.getWidth(effTimes.get(i)));

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
