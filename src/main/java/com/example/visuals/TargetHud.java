package com.example.visuals;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix3x2fStack;

/**
 * TargetHUD: окошко с ником, здоровьем и полоской HP цели.
 * Цель = существо, на которое смотришь, или то, которое ты недавно ударил (3 сек).
 * После потери цели окно плавно гаснет. Размер, позиция и цвет настраиваются.
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

    /** Вызвать один раз из VisualsClient.onInitializeClient(). */
    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient()) {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player != null && player == mc.player) { hitId = entity.getId(); hitAt = System.currentTimeMillis(); }
            }
            return ActionResult.PASS;
        });
    }

    public static void tick(MinecraftClient mc) {
        if (!Config.targetHud || mc.world == null || mc.player == null) { has = false; return; }
        long now = System.currentTimeMillis();
        LivingEntity t = null;
        Entity looked = mc.targetedEntity;
        if (looked instanceof LivingEntity le && le.isAlive()) t = le;
        else if (now - hitAt < KEEP_AFTER_HIT_MS) {
            Entity e = mc.world.getEntityById(hitId);
            if (e instanceof LivingEntity le && le.isAlive() && le.distanceTo(mc.player) < 40f) t = le;
        }
        if (t == null) return;
        if (!has || !t.getName().getString().equals(name)) shownHp = -1f;   // новая цель: полоска не "едет"
        has = true;
        lastSeen = now;
        name = t.getName().getString();
        hp = t.getHealth();
        maxHp = Math.max(1f, t.getMaxHealth());
        absorb = t.getAbsorptionAmount();
        dist = t.distanceTo(mc.player);
    }

    private static int ca(int color, float a) {
        int al = Math.max(0, Math.min(255, Math.round(((color >>> 24) & 255) * a)));
        return (al << 24) | (color & 0xFFFFFF);
    }

    private static void rr(DrawContext g, int x, int y, int w, int h, int r, int c) {
        if (r <= 0 || w < 2 * r || h < 2 * r) { g.fill(x, y, x + w, y + h, c); return; }
        g.fill(x, y + r, x + w, y + h - r, c);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = r - (int) Math.round(Math.sqrt(r * r - dy * dy));
            g.fill(x + inset, y + i, x + w - inset, y + i + 1, c);
            g.fill(x + inset, y + h - 1 - i, x + w - inset, y + h - i, c);
        }
    }

    public static void hud(DrawContext g) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!Config.targetHud || !has || mc.world == null || mc.options.hudHidden) return;
        if (mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen)) return;

        long now = System.currentTimeMillis();
        float a = 1f - MathHelper.clamp((now - lastSeen) / (float) FADE_MS, 0f, 1f);   // 1 пока цель есть, потом гаснет
        if (a <= 0f) { has = false; return; }

        // плавное движение полоски HP
        if (shownHp < 0f) shownHp = hp;
        shownHp += (hp - shownHp) * 0.12f;
        if (Math.abs(hp - shownHp) < 0.02f) shownHp = hp;

        TextRenderer tr = mc.textRenderer;
        int sw = g.getScaledWindowWidth(), sh = g.getScaledWindowHeight();
        float s = (float) Config.thScale;
        int accent = 0xFF000000 | (MathHelper.hsvToRgb((float) Config.thHue, 0.75f, 1f) & 0xFFFFFF);
        float slide = (1f - a) * 6f;   // чуть уезжает вниз при затухании

        Matrix3x2fStack m = g.getMatrices();
        m.pushMatrix();
        m.translate(sw * (float) Config.thX / 100f - W * s / 2f, sh * (float) Config.thY / 100f + slide);
        m.scale(s, s);

        rr(g, 2, 3, W, H, 5, ca(0x40000000, a));                 // тень
        rr(g, 0, 0, W, H, 5, ca(0xE0121216, a));                 // фон
        rr(g, 0, 0, 3, H, 1, ca(accent, a));                     // акцентная полоса слева

        g.drawTextWithShadow(tr, tr.trimToWidth(name, Config.thDist ? 88 : 124), 9, 6, ca(0xFFFFFFFF, a));
        if (Config.thDist) {
            String d = String.format(java.util.Locale.ROOT, "%.1fм", dist);
            g.drawTextWithShadow(tr, d, W - 8 - tr.getWidth(d), 6, ca(0xFF9AA0AA, a));
        }

        // полоска здоровья
        int bx = 9, by = 20, bw = W - 18, bh = 11;
        rr(g, bx, by, bw, bh, 3, ca(0x60FFFFFF, a * 0.35f));
        float ratio = MathHelper.clamp(shownHp / maxHp, 0f, 1f);
        int fw = Math.round(bw * ratio);
        if (fw > 1) {
            int col = 0xFF000000 | (MathHelper.hsvToRgb(0.33f * ratio, 0.8f, 1f) & 0xFFFFFF);
            rr(g, bx, by, Math.max(fw, 4), bh, 3, ca(col, a));
            g.fill(bx + 1, by + 1, bx + Math.max(fw, 4) - 1, by + 3, ca(0x30FFFFFF, a));   // блик
        }
        if (absorb > 0f) {   // золотые сердца поверх
            int aw = Math.round(bw * MathHelper.clamp(absorb / maxHp, 0f, 1f));
            if (aw > 1) rr(g, bx, by + bh - 3, aw, 3, 1, ca(0xFFFFD34D, a));
        }
        String t = String.format(java.util.Locale.ROOT, "%.1f / %.0f", hp, maxHp);
        g.drawCenteredTextWithShadow(tr, t, bx + bw / 2, by + 2, ca(0xFFFFFFFF, a));

        m.popMatrix();
    }
}
