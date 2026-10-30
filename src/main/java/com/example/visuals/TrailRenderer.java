package com.example.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

/**
 * 3D Trails: лента позади игрока (себя и друзей).
 * tick() копит историю позиций, render() вызывается из WorldRendererMixin.
 */
public final class TrailRenderer {
    private TrailRenderer() {}

    private static final int    MAX_POINTS   = 160;     // длина/высота/цвет берутся из Config (меню)
    private static final double MIN_STEP_SQ  = 0.0064;  // новая точка каждые ~0.08 блока
    private static final double TELEPORT_SQ  = 64.0;    // скачок > 8 блоков = телепорт
    private static final double MAX_DIST     = 64.0;
    private static final float  HALF_WIDTH   = 0.28f;   // половина ширины полосы под ногами
    private static final float  LIFT         = 0.05f;
    private static final int    ALPHA_FLAT   = 205;
    private static final int    ALPHA_BOTTOM = 175;

    private static final class Pt {
        final double x, y, z;
        final int tick;
        Pt(double x, double y, double z, int tick) { this.x = x; this.y = y; this.z = z; this.tick = tick; }
    }

    private static final Map<UUID, ArrayDeque<Pt>> TRAILS = new HashMap<>();
    private static int tickCount = 0;

    private static boolean eligible(MinecraftClient mc, PlayerEntity p) {
        if (p.isSpectator() || p.isInvisible()) return false;
        return p == mc.player || Config.friends.contains(p.getName().getString());
    }

    public static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null || !Config.trails) {
            if (!TRAILS.isEmpty()) TRAILS.clear();
            return;
        }
        tickCount++;
        final int maxAge = maxAge();

        Set<UUID> seen = new HashSet<>();
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (!eligible(mc, p)) continue;
            UUID id = p.getUuid();
            seen.add(id);

            ArrayDeque<Pt> d = TRAILS.get(id);
            if (d == null) { d = new ArrayDeque<>(); TRAILS.put(id, d); }

            double x = p.getX(), y = p.getY(), z = p.getZ();
            Pt last = d.peekFirst();
            if (last != null) {
                double dx = x - last.x, dy = y - last.y, dz = z - last.z;
                double sq = dx * dx + dy * dy + dz * dz;
                if (sq > TELEPORT_SQ) d.clear();
                if (sq >= MIN_STEP_SQ || sq > TELEPORT_SQ) d.addFirst(new Pt(x, y, z, tickCount));
            } else {
                d.addFirst(new Pt(x, y, z, tickCount));
            }

            while (!d.isEmpty() && tickCount - d.peekLast().tick > maxAge) d.pollLast();
            while (d.size() > MAX_POINTS) d.pollLast();
        }

        Iterator<UUID> it = TRAILS.keySet().iterator();
        while (it.hasNext()) if (!seen.contains(it.next())) it.remove();
    }

    public static void render(Camera camera, Matrix4f positionMatrix,
                              VertexConsumerProvider.Immediate consumers, float tickProgress) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!Config.trails || mc.world == null || mc.player == null || TRAILS.isEmpty()) return;

        Vec3d cam = camera.getCameraPos();
        final int maxAge = maxAge();
        final float curtainH = (float) Math.max(0.0, Config.trailsHeight);
        int rgb = pickColor();
        Matrix4f m = new MatrixStack().peek().getPositionMatrix();   // единичная матрица

        Matrix4fStack mv = RenderSystem.getModelViewStack();
        mv.pushMatrix();
        mv.mul(positionMatrix);
        try {
            VertexConsumer q = null;
            for (PlayerEntity p : mc.world.getPlayers()) {
                if (!eligible(mc, p)) continue;
                ArrayDeque<Pt> d = TRAILS.get(p.getUuid());
                if (d == null || d.isEmpty()) continue;

                // голова ленты = интерполированная позиция игрока
                double hx = MathHelper.lerp((double) tickProgress, p.lastX, p.getX());
                double hy = MathHelper.lerp((double) tickProgress, p.lastY, p.getY());
                double hz = MathHelper.lerp((double) tickProgress, p.lastZ, p.getZ());
                double ddx = hx - cam.x, ddy = hy - cam.y, ddz = hz - cam.z;
                if (ddx * ddx + ddy * ddy + ddz * ddz > MAX_DIST * MAX_DIST) continue;

                int n = d.size() + 1;
                double[] xs = new double[n], ys = new double[n], zs = new double[n];
                float[] fade = new float[n];
                xs[0] = hx; ys[0] = hy; zs[0] = hz; fade[0] = 1f;
                int i = 1;
                for (Pt pt : d) {
                    float age = (tickCount - pt.tick) + tickProgress;
                    xs[i] = pt.x; ys[i] = pt.y; zs[i] = pt.z;
                    fade[i] = Math.max(0f, 1f - age / maxAge);
                    i++;
                }
                if (n < 2) continue;

                // перпендикуляр к направлению движения
                float[] ox = new float[n], oz = new float[n];
                float px = 1f, pz = 0f;
                for (int k = 0; k < n; k++) {
                    int a = Math.max(0, k - 1), b = Math.min(n - 1, k + 1);
                    double dx = xs[a] - xs[b], dz = zs[a] - zs[b];
                    double len = Math.sqrt(dx * dx + dz * dz);
                    if (len > 1.0e-4) { px = (float) (-dz / len); pz = (float) (dx / len); }
                    float w = HALF_WIDTH * (0.35f + 0.65f * fade[k]);
                    ox[k] = px * w; oz[k] = pz * w;
                }

                if (q == null) q = consumers.getBuffer(RenderLayers.debugQuads());

                for (int k = 0; k < n - 1; k++) {
                    int k2 = k + 1;
                    if (fade[k2] <= 0f) break;

                    float x0 = (float) (xs[k]  - cam.x), y0 = (float) (ys[k]  - cam.y) + LIFT, z0 = (float) (zs[k]  - cam.z);
                    float x1 = (float) (xs[k2] - cam.x), y1 = (float) (ys[k2] - cam.y) + LIFT, z1 = (float) (zs[k2] - cam.z);

                    int cf0 = argb((int) (ALPHA_FLAT * fade[k]), rgb),   cf1 = argb((int) (ALPHA_FLAT * fade[k2]), rgb);
                    int cb0 = argb((int) (ALPHA_BOTTOM * fade[k]), rgb), cb1 = argb((int) (ALPHA_BOTTOM * fade[k2]), rgb);
                    int ct0 = argb(0, rgb), ct1 = argb(0, rgb);

                    // плоская полоса (обе стороны)
                    quad(q, m, x0 - ox[k], y0, z0 - oz[k], cf0,  x0 + ox[k], y0, z0 + oz[k], cf0,
                               x1 + ox[k2], y1, z1 + oz[k2], cf1, x1 - ox[k2], y1, z1 - oz[k2], cf1);
                    quad(q, m, x1 - ox[k2], y1, z1 - oz[k2], cf1, x1 + ox[k2], y1, z1 + oz[k2], cf1,
                               x0 + ox[k], y0, z0 + oz[k], cf0, x0 - ox[k], y0, z0 - oz[k], cf0);

                    // вертикальная "занавеска" (обе стороны)
                    float h0 = curtainH * (0.5f + 0.5f * fade[k]), h1 = curtainH * (0.5f + 0.5f * fade[k2]);
                    quad(q, m, x0, y0, z0, cb0,  x0, y0 + h0, z0, ct0,
                               x1, y1 + h1, z1, ct1, x1, y1, z1, cb1);
                    quad(q, m, x1, y1, z1, cb1, x1, y1 + h1, z1, ct1,
                               x0, y0 + h0, z0, ct0, x0, y0, z0, cb0);
                }
            }
            if (q != null) consumers.draw(RenderLayers.debugQuads());
        } finally {
            mv.popMatrix();
        }
    }

    private static int argb(int a, int rgb) {
        return (Math.max(0, Math.min(255, a)) << 24) | (rgb & 0xFFFFFF);
    }

    private static void quad(VertexConsumer q, Matrix4f m,
                             float ax, float ay, float az, int ac,
                             float bx, float by, float bz, int bc,
                             float cx, float cy, float cz, int cc,
                             float dx, float dy, float dz, int dc) {
        q.vertex(m, ax, ay, az).color(ac);
        q.vertex(m, bx, by, bz).color(bc);
        q.vertex(m, cx, cy, cz).color(cc);
        q.vertex(m, dx, dy, dz).color(dc);
    }

    private static int maxAge() {
        return Math.max(5, Math.min(120, Config.trailsLength));
    }

    private static int pickColor() {
        if (Config.trailsCustom) {
            return MathHelper.hsvToRgb((float) Config.trailsHue, (float) Config.trailsSat, (float) Config.trailsVal) & 0xFFFFFF;
        }
        int c = Math.max(0, Math.min(Config.trailsColor, Config.COLORS.length - 1));
        return Config.COLORS[c] & 0xFFFFFF;
    }
}
