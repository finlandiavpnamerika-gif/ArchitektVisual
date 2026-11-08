package com.example.visuals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

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

    private static String time(StatusEffectInstance e) {
        if (e.isInfinite()) return "∞";
        int s = e.getDuration() / 20;
        return String.format("%d:%02d", s / 60, s % 60);
    }

    private static String key(RegistryEntry<StatusEffect> e) {
        return e.getKey().map(k -> k.getValue().toString()).orElse("?");
    }

    public static int[] draw(DrawContext g, MinecraftClient mc, float a) {
        TextRenderer tr = mc.textRenderer;
        int accent = Config.COLORS[Config.potionColor];

        List<StatusEffectInstance> list = new ArrayList<>(mc.player.getStatusEffects());
        boolean demo = false;
        if (list.isEmpty()) {
            MAX.clear();
            if (!(mc.currentScreen instanceof ChatScreen)) return null;
            demo = true;   // пока открыт чат - показываем заглушку, чтобы можно было перетащить
        }
        // самые долгие сверху, бесконечные - первыми
        list.sort((p, q) -> Integer.compare(q.isInfinite() ? Integer.MAX_VALUE : q.getDuration(),
                p.isInfinite() ? Integer.MAX_VALUE : p.getDuration()));

        Set<String> alive = new HashSet<>();
        int n = list.size();
        String[] names = new String[n], times = new String[n];
        for (int i = 0; i < n; i++) {
            StatusEffectInstance e = list.get(i);
            String k = key(e.getEffectType());
            alive.add(k);
            MAX.merge(k, e.getDuration(), Math::max);
            String name = e.getEffectType().value().getName().getString();
            if (Config.potionLevel && e.getAmplifier() > 0) name += " " + roman(e.getAmplifier() + 1);
            names[i] = name;
            times[i] = time(e);
        }
        MAX.keySet().retainAll(alive);

        if (Config.potionHoriz && !demo) {
            int tile = Math.max(30, 0);
            int[] tw = new int[n];
            int w = PAD;
            for (int i = 0; i < n; i++) { tw[i] = Math.max(tile, tr.getWidth(times[i]) + 8); w += tw[i] + (i < n - 1 ? 3 : 0); }
            w += PAD;
            int h = PAD * 2 + 38;
            HudStyle.panel(g, 0, 0, w, h, a, accent);
            long nowH = System.currentTimeMillis();
            int x = PAD;
            for (int i = 0; i < n; i++) {
                StatusEffectInstance e = list.get(i);
                int side = e.getEffectType().value().isBeneficial() ? HudStyle.GOOD : HudStyle.BAD;
                HudStyle.roundFill(g, x, PAD, tw[i], 38, 2, HudStyle.withAlpha(0x38000000, a));
                HudStyle.roundFill(g, x + 2, PAD, tw[i] - 4, 2, 1, HudStyle.withAlpha(side, a));
                if (Config.potionIcons) {
                    Identifier id = e.getEffectType().getKey().map(k -> k.getValue().withPrefixedPath("mob_effect/")).orElse(null);
                    if (id != null) g.drawGuiTexture(RenderPipelines.GUI_TEXTURED, id, x + (tw[i] - ICON) / 2, PAD + 4, ICON, ICON, a);
                }
                if (Config.potionLevel && e.getAmplifier() > 0) {
                    String lv = roman(e.getAmplifier() + 1);
                    HudStyle.text(g, tr, lv, x + tw[i] - tr.getWidth(lv) - 2, PAD + 4, Theme.TEXT, a, true);
                }
                int sec = e.getDuration() / 20;
                int tc = e.isInfinite() ? HudStyle.GOOD : sec <= 10 ? HudStyle.BAD : sec <= 30 ? HudStyle.MID : Theme.TEXT;
                float ta = (!e.isInfinite() && sec <= 10) ? a * (0.55f + 0.45f * (float) Math.abs(Math.sin(nowH / 220.0))) : a;
                HudStyle.text(g, tr, times[i], x + (tw[i] - tr.getWidth(times[i])) / 2, PAD + 24, tc, ta, true);
                if (Config.potionBars) {
                    int max = MAX.getOrDefault(key(e.getEffectType()), Math.max(1, e.getDuration()));
                    float ratio = e.isInfinite() ? 1f : Math.max(0f, Math.min(1f, e.getDuration() / (float) Math.max(1, max)));
                    HudStyle.bar(g, x + 3, PAD + 34, tw[i] - 6, 2, ratio, e.isInfinite() ? HudStyle.GOOD : HudStyle.ratioColor(ratio), a);
                }
                x += tw[i] + 3;
            }
            return new int[] {w, h};
        }

        String title = "Effects";
        String count = demo ? "0" : String.valueOf(n);
        int textW = tr.getWidth(title) + 12 + tr.getWidth(count);
        for (int i = 0; i < n; i++) textW = Math.max(textW, tr.getWidth(names[i]) + 14 + tr.getWidth(times[i]));
        int iconW = Config.potionIcons ? ICON + 5 : 0;
        int w = Math.max(96, PAD * 2 + iconW + textW - (Config.potionIcons ? 0 : 0));
        int h = HEADER + 3 + (demo ? 14 : n * ROW) + 2;

        HudStyle.panel(g, 0, 0, w, h, a, accent);
        HudStyle.header(g, 0, 0, w, HEADER, a, accent);
        HudStyle.text(g, tr, title, PAD, 3, accent, a, true);
        HudStyle.textRight(g, tr, count, w - PAD, 3, Theme.TEXT_DIM, a, true);
        HudStyle.divider(g, PAD, w - PAD, HEADER, HudStyle.withAlpha(accent, 0.35f), a);

        if (demo) {
            HudStyle.text(g, tr, "Нет эффектов", PAD, HEADER + 5, Theme.TEXT_DIM, a, true);
            return new int[] {w, h};
        }

        long now = System.currentTimeMillis();
        int y = HEADER + 3;
        for (int i = 0; i < n; i++) {
            StatusEffectInstance e = list.get(i);
            StatusEffect fx = e.getEffectType().value();
            int side = fx.isBeneficial() ? HudStyle.GOOD : HudStyle.BAD;

            // цветная метка: полезный / вредный
            HudStyle.roundFill(g, 2, y + 2, 2, ROW - 5, 1, HudStyle.withAlpha(side, a));

            int tx = PAD + 1;
            if (Config.potionIcons) {
                Identifier id = e.getEffectType().getKey().map(k -> k.getValue().withPrefixedPath("mob_effect/")).orElse(null);
                if (id != null) g.drawGuiTexture(RenderPipelines.GUI_TEXTURED, id, tx, y + 1, ICON, ICON, a);
                tx += ICON + 4;
            }

            int sec = e.getDuration() / 20;
            int tc = e.isInfinite() ? HudStyle.GOOD : sec <= 10 ? HudStyle.BAD : sec <= 30 ? HudStyle.MID : Theme.TEXT_DIM;
            float ta = a;
            if (!e.isInfinite() && sec <= 10) ta = a * (0.55f + 0.45f * (float) Math.abs(Math.sin(now / 220.0)));   // мигает перед окончанием

            HudStyle.text(g, tr, names[i], tx, y + 2, Theme.TEXT, a, true);
            HudStyle.textRight(g, tr, times[i], w - PAD, y + 2, tc, ta, true);

            if (Config.potionBars) {
                int bw = w - tx - PAD;
                int max = MAX.getOrDefault(key(e.getEffectType()), Math.max(1, e.getDuration()));
                float ratio = e.isInfinite() ? 1f : Math.max(0f, Math.min(1f, e.getDuration() / (float) Math.max(1, max)));
                HudStyle.bar(g, tx, y + 13, bw, 3, ratio, e.isInfinite() ? HudStyle.GOOD : HudStyle.ratioColor(ratio), a);
            }
            y += ROW;
        }
        return new int[] {w, h};
    }
}
