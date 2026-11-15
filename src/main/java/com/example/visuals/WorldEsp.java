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
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1684;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3726;
import net.minecraft.class_3965;
import net.minecraft.class_4184;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_7923;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

/**
 * Рендер в мире: Hitboxes, Block Outline, Chunk Borders, Item ESP, лучи Waypoints,
 * Pearl Trail и Elytra Trail. Вызывается из WorldRendererMixin.
 */
public final class WorldEsp {
    private WorldEsp() {}

    private static final int FRIEND = 0x55FF55;
    private static final String[] VALUABLE = {
            "netherite", "diamond", "totem", "elytra", "shulker_box", "enchanted_golden_apple", "nether_star",
            "beacon", "trident", "mace", "dragon", "ancient_debris", "emerald", "heart_of_the_sea", "wind_charge",
            "ender_pearl", "golden_apple", "end_crystal", "respawn_anchor"
    };

    public static boolean any() {
        return Config.hbox || Config.bout || Config.chunkB || Config.itemEsp
                || (Config.wp && Config.wpBeam) || (Config.eventPts && Config.epBeam) || Config.ptrail || Config.etrail || Cosmetics.any();
    }

    // ------------------------------------------------------------------ следы
    private static final class Pt {
        final double x, y, z;
        final int tick;
        Pt(double x, double y, double z, int tick) { this.x = x; this.y = y; this.z = z; this.tick = tick; }
    }

    private static final Map<Integer, ArrayDeque<Pt>> PEARLS = new HashMap<>();
    private static final Map<UUID, ArrayDeque<Pt>> ELYTRA = new HashMap<>();
    private static int tickCount = 0;

    private static void push(ArrayDeque<Pt> d, double x, double y, double z) {
        Pt last = d.peekFirst();
        if (last != null) {
            double dx = x - last.x, dy = y - last.y, dz = z - last.z;
            if (dx * dx + dy * dy + dz * dz < 0.0025) return;
            if (dx * dx + dy * dy + dz * dz > 4096) d.clear();       // телепорт
        }
        d.addFirst(new Pt(x, y, z, tickCount));
        while (d.size() > 400) d.pollLast();
    }

    private static void prune(Map<?, ArrayDeque<Pt>> map, int maxAge, Set<?> seen) {
        Iterator<? extends Map.Entry<?, ArrayDeque<Pt>>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<?, ArrayDeque<Pt>> en = it.next();
            ArrayDeque<Pt> d = en.getValue();
            while (!d.isEmpty() && tickCount - d.peekLast().tick > maxAge) d.pollLast();
            if (d.isEmpty() && !seen.contains(en.getKey())) it.remove();
        }
    }

    private static int pearlAge() { return Math.max(5, Math.min(200, Config.ptLength)); }
    private static int elytraAge() { return Math.max(5, Math.min(200, Config.etLength)); }

    public static void tick(class_310 mc) {
        if (mc.field_1687 == null || mc.field_1724 == null) {
            PEARLS.clear();
            ELYTRA.clear();
            return;
        }
        tickCount++;

        if (Config.ptrail) {
            Set<Integer> seen = new HashSet<>();
            for (class_1297 e : mc.field_1687.method_18112()) {
                if (!(e instanceof class_1684)) continue;
                seen.add(e.method_5628());
                push(PEARLS.computeIfAbsent(e.method_5628(), k -> new ArrayDeque<>()), e.method_23317(), e.method_23318(), e.method_23321());
            }
            prune(PEARLS, pearlAge(), seen);
        } else if (!PEARLS.isEmpty()) PEARLS.clear();

        if (Config.etrail) {
            Set<UUID> seen = new HashSet<>();
            for (class_1657 p : mc.field_1687.method_18456()) {
                if (!p.method_6128() || p.method_7325()) continue;
                if (p != mc.field_1724 && !Config.etAll) continue;
                seen.add(p.method_5667());
                push(ELYTRA.computeIfAbsent(p.method_5667(), k -> new ArrayDeque<>()),
                        p.method_23317(), p.method_23318() + p.method_17682() * 0.4, p.method_23321());
            }
            prune(ELYTRA, elytraAge(), seen);
        } else if (!ELYTRA.isEmpty()) ELYTRA.clear();
    }

    // ------------------------------------------------------------------ рендер
    public static void render(class_4184 camera, Matrix4f positionMatrix,
                              class_4597.class_4598 consumers, float tp) {
        class_310 mc = class_310.method_1551();
        if (!any() || mc.field_1687 == null || mc.field_1724 == null) return;

        class_243 cam = camera.method_71156();
        Matrix4f m = new Matrix4f();
        Matrix4fStack mv = RenderSystem.getModelViewStack();
        mv.pushMatrix();
        mv.mul(positionMatrix);
        try {
            class_4588 q = consumers.method_73477(class_12249.method_76023());
            if (Config.chunkB) chunkBorders(q, m, cam, mc);
            if (Config.hbox || Config.itemEsp) entities(q, m, cam, camera, mc, tp);
            if (Config.bout) blockOutline(q, m, cam, mc);
            if (Config.wp && Config.wpBeam) Waypoints.renderBeams(q, m, cam, mc);
            if (Config.eventPts && Config.epBeam) EventPoints.renderBeams(q, m, cam, mc);
            if (Cosmetics.any()) Cosmetics.render(q, m, cam, mc, tp);
            if (Config.ptrail) pearlTrails(q, m, cam, mc, tp);
            if (Config.etrail) elytraTrails(q, m, cam, mc, tp);
            consumers.method_22994(class_12249.method_76023());
        } finally {
            mv.popMatrix();
        }
    }

    private static int pal(int idx) {
        return Config.COLORS[Math.max(0, Math.min(idx, Config.COLORS.length - 1))] & 0xFFFFFF;
    }

    private static int rainbow(float offset) {
        float h = (((System.currentTimeMillis() % 5000L) / 5000f) + offset) % 1f;
        return class_3532.method_15369(h, 0.65f, 1f) & 0xFFFFFF;
    }

    // ---------------------------------------------------------------- Hitboxes + Item ESP
    private static boolean valuable(class_1799 st) {
        String p = class_7923.field_41178.method_10221(st.method_7909()).method_12832();
        for (String v : VALUABLE) if (p.contains(v)) return true;
        return false;
    }

    private static void entities(class_4588 q, Matrix4f m, class_243 cam, class_4184 camera, class_310 mc, float tp) {
        double hbR2 = Config.hbRange * Config.hbRange, ieR2 = Config.ieRange * Config.ieRange;
        boolean selfBox = Config.hbSelf && !mc.field_1690.method_31044().method_31034();
        class_243 fwd = class_243.method_1030(camera.method_19329(), camera.method_19330());
        double sx = cam.field_1352 + fwd.field_1352 * 0.6, sy = cam.field_1351 + fwd.field_1351 * 0.6 - 0.12, sz = cam.field_1350 + fwd.field_1350 * 0.6;
        int items = 0;

        for (class_1297 e : mc.field_1687.method_18112()) {
            double ddx = e.method_23317() - cam.field_1352, ddy = e.method_23318() - cam.field_1351, ddz = e.method_23321() - cam.field_1350;
            double d2 = ddx * ddx + ddy * ddy + ddz * ddz;
            double ix = class_3532.method_16436((double) tp, e.field_6014, e.method_23317()) - e.method_23317();
            double iy = class_3532.method_16436((double) tp, e.field_6036, e.method_23318()) - e.method_23318();
            double iz = class_3532.method_16436((double) tp, e.field_5969, e.method_23321()) - e.method_23321();

            if (e instanceof class_1542 ie && Config.itemEsp) {
                if (d2 > ieR2 || items >= 150) continue;
                class_1799 st = ie.method_6983();
                boolean hi = valuable(st);
                if (Config.ieValuable && !hi) continue;
                items++;
                int rgb = hi ? pal(Config.ieColorHi) : pal(Config.ieColor);
                class_238 b = e.method_5829().method_989(ix, iy, iz).method_1014(0.1);
                WorldDraw.edges(q, m, cam.field_1352, cam.field_1351, cam.field_1350, b.field_1323, b.field_1322, b.field_1321, b.field_1320, b.field_1325, b.field_1324,
                        (float) Config.ieWidth, WorldDraw.argb(0.95f, rgb));
                if (Config.ieFill)
                    WorldDraw.fill(q, m, cam.field_1352, cam.field_1351, cam.field_1350, b.field_1323, b.field_1322, b.field_1321, b.field_1320, b.field_1325, b.field_1324,
                            WorldDraw.argb(hi ? 0.28f : 0.16f, rgb));
                if (Config.ieTracers)
                    WorldDraw.ribbon(q, m, cam.field_1352, cam.field_1351, cam.field_1350, sx, sy, sz,
                            (b.field_1323 + b.field_1320) / 2, (b.field_1322 + b.field_1325) / 2, (b.field_1321 + b.field_1324) / 2,
                            0.012f, WorldDraw.argb(0.0f, rgb), WorldDraw.argb(0.85f, rgb));
                continue;
            }

            if (!Config.hbox || d2 > hbR2) continue;
            if (e == mc.field_1724 && !selfBox) continue;
            if (e instanceof class_1542 && Config.itemEsp) continue;

            int rgb;
            if (e instanceof class_1657 p) {
                if (!Config.hbPlayers) continue;
                rgb = Config.friends.contains(p.method_5477().getString()) ? FRIEND : pal(Config.hbColorP);
            } else if (e instanceof class_1309) {
                if (!Config.hbMobs) continue;
                rgb = pal(Config.hbColorM);
            } else {
                if (!Config.hbOthers) continue;
                rgb = pal(Config.hbColorO);
            }
            class_238 b = e.method_5829().method_989(ix, iy, iz).method_1014(e.method_5871());
            WorldDraw.edges(q, m, cam.field_1352, cam.field_1351, cam.field_1350, b.field_1323, b.field_1322, b.field_1321, b.field_1320, b.field_1325, b.field_1324,
                    (float) Config.hbWidth, WorldDraw.argb((float) Config.hbAlpha, rgb));
            if (Config.hbFill)
                WorldDraw.fill(q, m, cam.field_1352, cam.field_1351, cam.field_1350, b.field_1323, b.field_1322, b.field_1321, b.field_1320, b.field_1325, b.field_1324,
                        WorldDraw.argb((float) Config.hbAlpha * 0.18f, rgb));
        }
    }

    // ---------------------------------------------------------------- Block Outline
    private static void blockOutline(class_4588 q, Matrix4f m, class_243 cam, class_310 mc) {
        if (!(mc.field_1765 instanceof class_3965 bhr) || bhr.method_17783() != class_239.class_240.field_1332) return;
        class_2338 pos = bhr.method_17777();
        class_2680 st = mc.field_1687.method_8320(pos);
        if (st.method_26215()) return;
        class_265 shape = st.method_26172(mc.field_1687, pos, class_3726.method_16195(mc.field_1724));
        if (shape.method_1110()) return;

        int rgb = Config.boRainbow ? rainbow(0f) : pal(Config.boColor);
        final float hw = (float) Config.boWidth;
        final int line = WorldDraw.argb(1f, rgb);
        final int fill = WorldDraw.argb((float) Config.boFillAlpha, rgb);
        final boolean doFill = Config.boFill;
        final double px = pos.method_10263(), py = pos.method_10264(), pz = pos.method_10260();
        final double e = 0.002;
        shape.method_1089((x1, y1, z1, x2, y2, z2) -> {
            WorldDraw.edges(q, m, cam.field_1352, cam.field_1351, cam.field_1350,
                    px + x1 - e, py + y1 - e, pz + z1 - e, px + x2 + e, py + y2 + e, pz + z2 + e, hw, line);
            if (doFill)
                WorldDraw.fill(q, m, cam.field_1352, cam.field_1351, cam.field_1350,
                        px + x1 - e, py + y1 - e, pz + z1 - e, px + x2 + e, py + y2 + e, pz + z2 + e, fill);
        });
    }

    // ---------------------------------------------------------------- Chunk Borders
    private static void chunkBorders(class_4588 q, Matrix4f m, class_243 cam, class_310 mc) {
        int radius = Math.max(1, Math.min(4, Config.cbRadius));
        double px = mc.field_1724.method_23317(), py = mc.field_1724.method_23318(), pz = mc.field_1724.method_23321();
        int ccx = Math.floorDiv((int) Math.floor(px), 16), ccz = Math.floorDiv((int) Math.floor(pz), 16);
        double bottom = mc.field_1687.method_31607(), top = mc.field_1687.method_31607() + mc.field_1687.method_31605();
        double y0 = Math.max(bottom, py - Config.cbHeight), y1 = Math.min(top, py + Config.cbHeight);
        float hw = (float) Config.cbWidth;
        int rgb = pal(Config.cbColor);
        int bright = WorldDraw.argb((float) Config.cbAlpha, rgb);
        int dim = WorldDraw.argb((float) Config.cbAlpha * 0.4f, rgb);

        // вертикальные линии по углам чанков
        for (int i = -radius; i <= radius + 1; i++) {
            for (int k = -radius; k <= radius + 1; k++) {
                double x = (ccx + i) * 16.0, z = (ccz + k) * 16.0;
                boolean cur = (i == 0 || i == 1) && (k == 0 || k == 1);
                WorldDraw.ribbon(q, m, cam.field_1352, cam.field_1351, cam.field_1350, x, y0, z, x, y1, z, hw, cur ? bright : dim, cur ? bright : dim);
            }
        }

        // горизонтальные уровни через каждые 16 блоков
        if (Config.cbLevels) {
            int first = (int) Math.floor(y0 / 16.0) * 16;
            for (int y = first; y <= y1; y += 16) {
                if (y < y0 || y > top) continue;
                for (int i = -radius; i <= radius; i++) {
                    for (int k = -radius; k <= radius; k++) {
                        double xa = (ccx + i) * 16.0, za = (ccz + k) * 16.0, xb = xa + 16, zb = za + 16;
                        int c = (i == 0 && k == 0) ? bright : dim;
                        WorldDraw.ribbon(q, m, cam.field_1352, cam.field_1351, cam.field_1350, xa, y, za, xb, y, za, hw, c, c);
                        WorldDraw.ribbon(q, m, cam.field_1352, cam.field_1351, cam.field_1350, xb, y, za, xb, y, zb, hw, c, c);
                        WorldDraw.ribbon(q, m, cam.field_1352, cam.field_1351, cam.field_1350, xb, y, zb, xa, y, zb, hw, c, c);
                        WorldDraw.ribbon(q, m, cam.field_1352, cam.field_1351, cam.field_1350, xa, y, zb, xa, y, za, hw, c, c);
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------- Trails
    private static void drawTrail(class_4588 q, Matrix4f m, class_243 cam, ArrayDeque<Pt> d,
                                  class_243 head, int maxAge, float tp, float width, boolean rainbow, int rgb) {
        double lx, ly, lz;
        float lf;
        int lc;
        boolean have = false;
        lx = ly = lz = 0;
        lf = 1f;
        lc = 0;
        if (head != null) {
            lx = head.field_1352; ly = head.field_1351; lz = head.field_1350;
            lc = rainbow ? rainbow(tickCount * 0.015f) : rgb;
            have = true;
        }
        for (Pt p : d) {
            float age = (tickCount - p.tick) + tp;
            float f = Math.max(0f, 1f - age / maxAge);
            int c = rainbow ? (class_3532.method_15369((p.tick * 0.015f) % 1f, 0.65f, 1f) & 0xFFFFFF) : rgb;
            if (have) {
                float hw = width * (0.25f + 0.75f * ((lf + f) * 0.5f));
                WorldDraw.ribbon(q, m, cam.field_1352, cam.field_1351, cam.field_1350, lx, ly, lz, p.x, p.y, p.z, hw,
                        WorldDraw.argb(0.9f * lf, lc), WorldDraw.argb(0.9f * f, c));
            }
            lx = p.x; ly = p.y; lz = p.z; lf = f; lc = c; have = true;
            if (f <= 0f) break;
        }
    }

    private static void pearlTrails(class_4588 q, Matrix4f m, class_243 cam, class_310 mc, float tp) {
        int maxAge = pearlAge();
        for (Map.Entry<Integer, ArrayDeque<Pt>> en : PEARLS.entrySet()) {
            class_1297 e = mc.field_1687.method_8469(en.getKey());
            class_243 head = e == null ? null : new class_243(
                    class_3532.method_16436((double) tp, e.field_6014, e.method_23317()),
                    class_3532.method_16436((double) tp, e.field_6036, e.method_23318()),
                    class_3532.method_16436((double) tp, e.field_5969, e.method_23321()));
            drawTrail(q, m, cam, en.getValue(), head, maxAge, tp, (float) Config.ptWidth, Config.ptRainbow, pal(Config.ptColor));
        }
    }

    private static void elytraTrails(class_4588 q, Matrix4f m, class_243 cam, class_310 mc, float tp) {
        int maxAge = elytraAge();
        for (Map.Entry<UUID, ArrayDeque<Pt>> en : ELYTRA.entrySet()) {
            class_1297 e = null;
            for (class_1657 p : mc.field_1687.method_18456()) if (p.method_5667().equals(en.getKey())) { e = p; break; }
            class_243 head = (e == null || !(e instanceof class_1309 le) || !le.method_6128()) ? null : new class_243(
                    class_3532.method_16436((double) tp, e.field_6014, e.method_23317()),
                    class_3532.method_16436((double) tp, e.field_6036, e.method_23318()) + e.method_17682() * 0.4,
                    class_3532.method_16436((double) tp, e.field_5969, e.method_23321()));
            drawTrail(q, m, cam, en.getValue(), head, maxAge, tp, (float) Config.etWidth, Config.etRainbow, pal(Config.etColor));
        }
    }
}
