package com.example.visuals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

/** Килл-эффект: цель становится призраком, взлетает в небо и угасает. */
public final class GhostFx {
    private GhostFx() {}

    private static final int LIFE = 50; // тиков (~2.5 сек)
    private static final Random R = new Random();

    private static final class Ghost {
        double x, y, z, h;
        int age;
    }

    private static final List<Ghost> GHOSTS = new ArrayList<>();

    public static void spawn(MinecraftClient mc, double x, double y, double z, double h) {
        Ghost g = new Ghost();
        g.x = x; g.y = y; g.z = z; g.h = Math.max(0.6, h);
        GHOSTS.add(g);
        Combat.playSound(mc, "particle.soul_escape", 0.9f, 1.0f);
        Combat.playSound(mc, "entity.vex.ambient", 1.3f, 0.5f);
    }

    public static void tick(MinecraftClient mc) {
        if (mc.world == null) {
            GHOSTS.clear();
            return;
        }
        Iterator<Ghost> it = GHOSTS.iterator();
        while (it.hasNext()) {
            Ghost g = it.next();
            if (++g.age > LIFE) {
                it.remove();
                continue;
            }
            double t = g.age / (double) LIFE;      // 0..1
            double fade = 1.0 - t;                 // плотность угасает
            double rise = 0.10 * g.age + 0.004 * g.age * g.age; // подъём с ускорением
            double cx = g.x + 0.25 * Math.sin(g.age * 0.35);
            double cz = g.z + 0.25 * Math.cos(g.age * 0.35);
            double cy = g.y + g.h * 0.5 + rise;
            double w = g.h * 0.35;

            // тело призрака: облачко
            int body = (int) Math.ceil(6 * fade);
            for (int i = 0; i < body; i++) {
                add(mc, ParticleTypes.CLOUD,
                        cx + (R.nextDouble() - 0.5) * w,
                        cy + (R.nextDouble() - 0.3) * g.h * 0.6,
                        cz + (R.nextDouble() - 0.5) * w,
                        0, 0.02, 0);
            }

            // хвост: души тянутся снизу
            int tail = (int) Math.ceil(3 * fade);
            for (int i = 0; i < tail; i++) {
                add(mc, ParticleTypes.SOUL,
                        cx + (R.nextDouble() - 0.5) * w * 0.6,
                        cy - R.nextDouble() * 2.5,
                        cz + (R.nextDouble() - 0.5) * w * 0.6,
                        0, 0.03, 0);
            }

            // искры в конце, когда призрак растворяется
            if (t > 0.6 && g.age % 2 == 0) {
                add(mc, ParticleTypes.END_ROD,
                        cx + (R.nextDouble() - 0.5) * w,
                        cy + (R.nextDouble() - 0.5) * g.h * 0.5,
                        cz + (R.nextDouble() - 0.5) * w,
                        0, 0.04, 0);
            }
        }
    }

    private static void add(MinecraftClient mc, ParticleEffect effect,
                            double x, double y, double z, double vx, double vy, double vz) {
        mc.particleManager.addParticle(effect, x, y, z, vx, vy, vz);
    }
}
