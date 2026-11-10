package com.example.visuals;

import java.util.Random;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/** Эффекты боя: частицы критов, эффект убийства (вспышка, частицы, звук) и индикатор атаки. */
public final class CombatFx {
    private CombatFx() {}

    public static final String[] CRIT_STYLES = {"Крит", "Магия", "Искры", "Огонь", "Сердца"};
    private static final String[] CRIT_IDS = {"crit", "enchanted_hit", "end_rod", "flame", "heart"};

    public static final String[] KILL_PARTICLES = {"Нет", "Тотем", "Огонь", "Сердца", "Крит", "Дым"};
    private static final String[] KILL_IDS = {"", "totem_of_undying", "flame", "heart", "crit", "large_smoke"};

    public static final String[] ATK_STYLES = {"Полоска", "Кольцо"};

    private static final Random R = new Random();
    private static long flashAt = 0;

    private static void spawn(MinecraftClient mc, String id, double x, double y, double z,
                              double vx, double vy, double vz) {
        if (mc.world == null || id.isEmpty()) return;
        ParticleType<?> t = Registries.PARTICLE_TYPE.get(Identifier.of(id));
        if (t instanceof ParticleEffect pe) {
            mc.world.addParticleClient(pe, false, false, x, y, z, vx, vy, vz);
        }
    }

    /** Частицы критического удара вокруг цели. */
    public static void crit(MinecraftClient mc, Entity e) {
        String id = CRIT_IDS[Math.max(0, Math.min(Config.critStyle, CRIT_IDS.length - 1))];
        int n = (int) Config.critCount;
        double cy = e.getY() + e.getHeight() * 0.6;
        for (int i = 0; i < n; i++) {
            spawn(mc, id, e.getX() + (R.nextDouble() - 0.5) * e.getWidth(), cy + (R.nextDouble() - 0.5) * 0.5,
                    e.getZ() + (R.nextDouble() - 0.5) * e.getWidth(),
                    (R.nextDouble() - 0.5) * 0.6, R.nextDouble() * 0.4, (R.nextDouble() - 0.5) * 0.6);
        }
    }

    /** Эффект убийства: вспышка на экране, частицы в точке смерти и звук. */
    public static void kill(MinecraftClient mc, double x, double y, double z) {
        if (Config.killFlash) flashAt = System.currentTimeMillis();
        String id = KILL_IDS[Math.max(0, Math.min(Config.killParticles, KILL_IDS.length - 1))];
        for (int i = 0; i < 24 && !id.isEmpty(); i++) {
            spawn(mc, id, x + (R.nextDouble() - 0.5) * 0.6, y + 0.3 + R.nextDouble() * 1.4, z + (R.nextDouble() - 0.5) * 0.6,
                    (R.nextDouble() - 0.5) * 0.5, R.nextDouble() * 0.5, (R.nextDouble() - 0.5) * 0.5);
        }
        if (Config.killSound) Combat.playSound(mc, "entity.player.levelup", 1.4f, 0.8f);
    }

    /** Вызывается каждый кадр из HUD-коллбэка: вспышка и индикатор атаки. */
    public static void hud(DrawContext g) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;
        int sw = mc.getWindow().getScaledWidth(), sh = mc.getWindow().getScaledHeight();

        // вспышка при убийстве
        long dt = System.currentTimeMillis() - flashAt;
        if (flashAt != 0 && dt < 450) {
            float k = 1f - dt / 450f;
            int c = Config.COLORS[Config.killColor];
            g.fill(0, 0, sw, sh, HudStyle.withAlpha(0xFF000000 | (c & 0xFFFFFF), 0.30f * k * k));
        }

        // индикатор атаки
        if (Config.atkInd && mc.currentScreen == null) drawIndicator(g, mc, sw / 2, sh / 2);
    }

    private static void drawIndicator(DrawContext g, MinecraftClient mc, int cx, int cy) {
        float p = Math.max(0f, Math.min(1f, mc.player.getAttackCooldownProgress(0f)));
        if (p >= 0.999f && !Config.atkAlways) return;
        int base = Config.COLORS[Config.atkColor];
        int col = p >= 0.999f ? base : HudStyle.mix(0xFFFF6B6B, base, p);
        int size = (int) Math.round(Config.atkSize);

        if (Config.atkStyle == 0) {
            int w = 8 + size * 4, y = cy + Config.atkOffset;
            HudStyle.roundFill(g, cx - w / 2 - 1, y - 1, w + 2, 5, 2, 0x90000000);
            int f = Math.round(w * p);
            if (f > 0) HudStyle.roundFill(g, cx - w / 2, y, f, 3, 1, col);
        } else {
            int r = 6 + size * 2;
            int steps = 48;
            int lit = Math.round(steps * p);
            for (int i = 0; i < steps; i++) {
                double ang = -Math.PI / 2 + i * (2 * Math.PI / steps);
                int x = cx + (int) Math.round(Math.cos(ang) * r);
                int y = cy + (int) Math.round(Math.sin(ang) * r);
                g.fill(x, y, x + 2, y + 2, i < lit ? col : 0x55000000);
            }
        }
    }
}
