package com.example.visuals;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_1309;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_408;
import org.joml.Matrix3x2fStack;

/**
 * TargetHUD: окошко с ником, здоровьем и полоской HP цели.
 * Цель = существо, на которое смотришь, или то, которое ты недавно ударил (3 сек).
 * После потери цели окно плавно гаснет. Размер, позиция и цвет настраиваются.
 * Вид: аватар, ник, чип дистанции, пилюльная полоса HP и капсула с бронёй цели.
 */
public final class TargetHud {
    private TargetHud() {}

    private static final long KEEP_AFTER_HIT_MS = 3000, FADE_MS = 300;
    private static final int W = 140, H = 40;

    private static int hitId = -1;
    private static long hitAt = 0, lastSeen = 0;
    private static boolean has = false;
    // снимок цели (чтобы окно могло гаснуть, даже когда цель уже недоступна)
    private static String name = "";
    private static float hp, maxHp, absorb, shownHp = -1f, dist;
    private static final class_1799[] gear = new class_1799[5];   // шлем, нагрудник, поножи, ботинки, предмет в руке

    /** Вызвать один раз из VisualsClient.onInitializeClient(). */
    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.method_8608()) {
                class_310 mc = class_310.method_1551();
                if (mc.field_1724 != null && player == mc.field_1724) { hitId = entity.method_5628(); hitAt = System.currentTimeMillis(); }
            }
            return class_1269.field_5811;
        });
    }

    public static void tick(class_310 mc) {
        if (!Config.targetHud || mc.field_1687 == null || mc.field_1724 == null) { has = false; return; }
        long now = System.currentTimeMillis();
        class_1309 t = null;
        class_1297 looked = mc.field_1692;
        if (looked instanceof class_1309 le && le.method_5805()) t = le;
        else if (now - hitAt < KEEP_AFTER_HIT_MS) {
            class_1297 e = mc.field_1687.method_8469(hitId);
            if (e instanceof class_1309 le && le.method_5805() && le.method_5739(mc.field_1724) < 40f) t = le;
        }
        if (t == null) return;
        if (!has || !t.method_5477().getString().equals(name)) shownHp = -1f;   // новая цель: полоска не "едет"
        has = true;
        lastSeen = now;
        name = t.method_5477().getString();
        hp = t.method_6032();
        maxHp = Math.max(1f, t.method_6063());
        absorb = t.method_6067();
        dist = t.method_5739(mc.field_1724);
        gear[0] = t.method_6118(class_1304.field_6169);
        gear[1] = t.method_6118(class_1304.field_6174);
        gear[2] = t.method_6118(class_1304.field_6172);
        gear[3] = t.method_6118(class_1304.field_6166);
        gear[4] = t.method_6047();
    }

    private static int ca(int color, float a) {
        int al = Math.max(0, Math.min(255, Math.round(((color >>> 24) & 255) * a)));
        return (al << 24) | (color & 0xFFFFFF);
    }

    public static void hud(class_332 g) {
        class_310 mc = class_310.method_1551();
        if (!Config.targetHud || !has || mc.field_1687 == null || mc.field_1690.field_1842) return;
        if (mc.field_1755 != null && !(mc.field_1755 instanceof class_408)) return;

        long now = System.currentTimeMillis();
        float a = 1f - class_3532.method_15363((now - lastSeen) / (float) FADE_MS, 0f, 1f);   // 1 пока цель есть, потом гаснет
        if (a <= 0f) { has = false; return; }

        // плавное движение полоски HP
        if (shownHp < 0f) shownHp = hp;
        shownHp += (hp - shownHp) * 0.12f;
        if (Math.abs(hp - shownHp) < 0.02f) shownHp = hp;

        class_327 tr = mc.field_1772;
        int sw = g.method_51421(), sh = g.method_51443();
        float s = (float) Config.thScale;
        int accent = 0xFF000000 | (class_3532.method_15369((float) Config.thHue, 0.75f, 1f) & 0xFFFFFF);
        float slide = (1f - a) * 6f;   // чуть уезжает вниз при затухании

        Matrix3x2fStack m = g.method_51448();
        m.pushMatrix();
        m.translate(sw * (float) Config.thX / 100f - W * s / 2f, sh * (float) Config.thY / 100f + slide);
        m.scale(s, s);

        HudStyle.panel(g, 0, 0, W, H, a, accent);
        HudStyle.avatar(g, tr, name, 7, 7, 26, a, accent);

        // ник и чип дистанции
        int nameMax = W - 39 - 8;
        if (Config.thDist) {
            String d = String.format(java.util.Locale.ROOT, "%.1fм", dist);
            int cw = HudStyle.chipWidth(tr, d);
            HudStyle.chip(g, tr, d, W - 7 - cw, 7, 0xFFB4B9CC, a);
            nameMax -= cw + 4;
        }
        HudStyle.text(g, tr, HudStyle.trim(tr, name, nameMax), 39, 9, 0xFFFFFFFF, a, true);

        // полоска здоровья (пилюля в цвет акцента) и значение справа
        int bx = 39, by = 24, bw = W - 39 - 8, bh = 9;
        float ratio = class_3532.method_15363(shownHp / maxHp, 0f, 1f);
        HudStyle.bar(g, bx, by, bw, bh, ratio, accent, a);
        if (ratio > 0.02f) g.method_25294(bx + 3, by + 1, bx + Math.max(4, Math.round(bw * ratio)) - 3, by + 2, ca(0x40FFFFFF, a));   // блик
        if (absorb > 0f) {   // золотые сердца поверх
            int aw = Math.round(bw * class_3532.method_15363(absorb / maxHp, 0f, 1f));
            if (aw > 2) HudStyle.roundFill(g, bx, by + bh - 2, aw, 2, 1, ca(0xFFFFD34D, a));
        }
        String t = Math.round(hp) + "hp";
        HudStyle.text(g, tr, t, bx + bw - 4 - HudStyle.width(tr, t), by + 1, 0xFFFFFFFF, a, true);

        // капсула с бронёй и предметом цели
        int cnt = 0;
        for (class_1799 st : gear) if (st != null && !st.method_7960()) cnt++;
        if (cnt > 0) {
            int sw2 = cnt * 15 + 9, sx = (W - sw2) / 2, sy = H + 4;
            HudStyle.capsule(g, sx, sy, sw2, 16, a);
            if (a > 0.4f) {
                int ix = sx + 5;
                for (class_1799 st : gear) {
                    if (st == null || st.method_7960()) continue;
                    m.pushMatrix();
                    m.translate(ix, sy + 2);
                    m.scale(0.75f, 0.75f);
                    g.method_51427(st, 0, 0);
                    m.popMatrix();
                    ix += 15;
                }
            }
        }

        m.popMatrix();
    }
}
