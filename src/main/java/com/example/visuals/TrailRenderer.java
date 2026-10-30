package com.example.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_12249;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public final class TrailRenderer {
    private TrailRenderer() {}

    private static final int    MAX_AGE      = 30;
    private static final int    MAX_POINTS   = 90;
    private static final double MIN_STEP_SQ  = 0.0064;
    private static final double TELEPORT_SQ  = 64.0;
    private static final double MAX_DIST     = 64.0;
    private static final float  HALF_WIDTH   = 0.28f;
    private static final float  CURTAIN_H    = 0.80f;
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

    private static boolean eligible(class_310 mc, class_1657 p) {
        if (p.method_7325() || p.method_5767()) return false;
        return p == mc.field_1724 || Config.friends.contains(p.method_5477().getString());
    }

    public static void tick() {
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || mc.field_1724 == null || !Config.trails) {
            if (!TRAILS.isEmpty()) TRAILS.clear();
            return;
        }
        tickCount++;

        Set<UUID> seen = new HashSet<>();
        for (class_1657 p : mc.field_1687.method_18456()) {
            if (!eligible(mc, p)) continue;
            UUID id = p.method_5667();
            seen.add(id);

            ArrayDeque<Pt> d = TRAILS.get(id);
            if (d == null) { d = new ArrayDeque<>(); TRAILS.put(id, d); }

            double x = p.method_23317(), y = p.method_23318(), z = p.method_23321();
            Pt last = d.peekFirst();
            if (last != null) {
                double dx = x - last.x, dy = y - last.y, dz = z - last.z;
                double sq = dx * dx + dy * dy + dz * dz;
                if (sq > TELEPORT_SQ) d.clear();
                if (sq >= MIN_STEP_SQ || sq > TELEPORT_SQ) d.addFirst(new Pt(x, y, z, tickCount));
            } else {
                d.addFirst(new Pt(x, y, z, tickCount));
            }

            while (!d.isEmpty() && tickCount - d.peekLast().tick > MAX_AGE) d.pollLast();
            while (d.size() > MAX_POINTS) d.pollLast();
        }

        Iterator<UUID> it = TRAILS.keySet().iterator();
        while (it.hasNext()) if (!seen.contains(it.next())) it.remove();
    }

    public static void render(class_4184 camera, Matrix4f positionMatrix,
                              class_4597.class_4598 consumers, float tickProgress) {
        class_310 mc = class_310.method_1551();
        if (!Config.trails || mc.field_1687 == null || mc.field_1724 == null || TRAILS.isEmpty()) return;

        class_243 cam = camera.method_71156();
        int rgb = pickColor();
        Matrix4f m = new class_4587().method_23760().method_23761();

        Matrix4fStack mv = RenderSystem.getModelViewStack();
        mv.pushMatrix();
        mv.mul(positionMatrix);
        try {
            class_4588 q = null;
            for (class_1657 p : mc.field_1687.method_18456()) {
                if (!eligible(mc, p)) continue;
                ArrayDeque<Pt> d = TRAILS.get(p.method_5667());
                if (d == null || d.isEmpty()) continue;

                double hx = class_3532.method_16436((double) tickProgress, p.field_6014, p.method_23317());
                double hy = class_3532.method_16436((double) tickProgress, p.field_6036, p.method_23318());
                double hz = class_3532.method_16436((double) tickProgress, p.field_5969, p.method_23321());
                double ddx = hx - cam.field_1352, ddy = hy - cam.field_1351, ddz = hz - cam.field_1350;
                if (ddx * ddx + ddy * ddy + ddz * ddz > MAX_DIST * MAX_DIST) continue;

                int n = d.size() + 1;
                double[] xs = new double[n], ys = new double[n], zs = new double[n];
                float[] fade = new float[n];
                xs[0] = hx; ys[0] = hy; zs[0] = hz; fade[0] = 1f;
                int i = 1;
                for (Pt pt : d) {
                    float age = (tickCount - pt.tick) + tickProgress;
                    xs[i] = pt.x; ys[i] = pt.y; zs[i] = pt.z;
                    fade[i] = Math.max(0f, 1f - age / MAX_AGE);
                    i++;
                }
                if (n < 2) continue;

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

                if (q == null) q = consumers.method_73477(class_12249.method_76023());

                for (int k = 0; k < n - 1; k++) {
                    int k2 = k + 1;
                    if (fade[k2] <= 0f) break;

                    float x0 = (float) (xs[k]  - cam.field_1352), y0 = (float) (ys[k]  - cam.field_1351) + LIFT, z0 = (float) (zs[k]  - cam.field_1350);
                    float x1 = (float) (xs[k2] - cam.field_1352), y1 = (float) (ys[k2] - cam.field_1351) + LIFT, z1 = (float) (zs[k2] - cam.field_1350);

                    int cf0 = argb((int) (ALPHA_FLAT * fade[k]), rgb),   cf1 = argb((int) (ALPHA_FLAT * fade[k2]), rgb);
                    int cb0 = argb((int) (ALPHA_BOTTOM * fade[k]), rgb), cb1 = argb((int) (ALPHA_BOTTOM * fade[k2]), rgb);
                    int ct0 = argb(0, rgb), ct1 = argb(0, rgb);

                    quad(q, m, x0 - ox[k], y0, z0 - oz[k], cf0,  x0 + ox[k], y0, z0 + oz[k], cf0,
                               x1 + ox[k2], y1, z1 + oz[k2], cf1, x1 - ox[k2], y1, z1 - oz[k2], cf1);
                    quad(q, m, x1 - ox[k2], y1, z1 - oz[k2], cf1, x1 + ox[k2], y1, z1 + oz[k2], cf1,
                               x0 + ox[k], y0, z0 + oz[k], cf0, x0 - ox[k], y0, z0 - oz[k], cf0);

                    float h0 = CURTAIN_H * (0.5f + 0.5f * fade[k]), h1 = CURTAIN_H * (0.5f + 0.5f * fade[k2]);
                    quad(q, m, x0, y0, z0, cb0,  x0, y0 + h0, z0, ct0,
                               x1, y1 + h1, z1, ct1, x1, y1, z1, cb1);
                    quad(q, m, x1, y1, z1, cb1, x1, y1 + h1, z1, ct1,
                               x0, y0 + h0, z0, ct0, x0, y0, z0, cb0);
                }
            }
            if (q != null) consumers.method_22994(class_12249.method_76023());
        } finally {
            mv.popMatrix();
        }
    }

    private static int argb(int a, int rgb) {
        return (Math.max(0, Math.min(255, a)) << 24) | (rgb & 0xFFFFFF);
    }

    private static void quad(class_4588 q, Matrix4f m,
                             float ax, float ay, float az, int ac,
                             float bx, float by, float bz, int bc,
                             float cx, float cy, float cz, int cc,
                             float dx, float dy, float dz, int dc) {
        q.method_22918(m, ax, ay, az).method_39415(ac);
        q.method_22918(m, bx, by, bz).method_39415(bc);
        q.method_22918(m, cx, cy, cz).method_39415(cc);
        q.method_22918(m, dx, dy, dz).method_39415(dc);
    }

    private static int pickColor() {
        int c = Math.max(0, Math.min(Config.trailsColor, Config.COLORS.length - 1));
        return Config.COLORS[c] & 0xFFFFFF;
    }
}
