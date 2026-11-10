package com.example.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
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
                || (Config.wp && Config.wpBeam) || Config.ptrail || Config.etrail || Cosmetics.any();
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

    public static void tick(MinecraftClient mc) {
        if (mc.world == null || mc.player == null) {
            PEARLS.clear();
            ELYTRA.clear();
            return;
        }
        tickCount++;

        if (Config.ptrail) {
            Set<Integer> seen = new HashSet<>();
            for (Entity e : mc.world.getEntities()) {
                if (!(e instanceof EnderPearlEntity)) continue;
                seen.add(e.getId());
                push(PEARLS.computeIfAbsent(e.getId(), k -> new ArrayDeque<>()), e.getX(), e.getY(), e.getZ());
            }
            prune(PEARLS, pearlAge(), seen);
        } else if (!PEARLS.isEmpty()) PEARLS.clear();

        if (Config.etrail) {
            Set<UUID> seen = new HashSet<>();
            for (PlayerEntity p : mc.world.getPlayers()) {
                if (!p.isGliding() || p.isSpectator()) continue;
                if (p != mc.player && !Config.etAll) continue;
                seen.add(p.getUuid());
                push(ELYTRA.computeIfAbsent(p.getUuid(), k -> new ArrayDeque<>()),
                        p.getX(), p.getY() + p.getHeight() * 0.4, p.getZ());
            }
            prune(ELYTRA, elytraAge(), seen);
        } else if (!ELYTRA.isEmpty()) ELYTRA.clear();
    }

    // ------------------------------------------------------------------ рендер
    public static void render(Camera camera, Matrix4f positionMatrix,
                              VertexConsumerProvider.Immediate consumers, float tp) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!any() || mc.world == null || mc.player == null) return;

        Vec3d cam = camera.getCameraPos();
        Matrix4f m = new Matrix4f();
        Matrix4fStack mv = RenderSystem.getModelViewStack();
        mv.pushMatrix();
        mv.mul(positionMatrix);
        try {
            VertexConsumer q = consumers.getBuffer(RenderLayers.debugQuads());
            if (Config.chunkB) chunkBorders(q, m, cam, mc);
            if (Config.hbox || Config.itemEsp) entities(q, m, cam, camera, mc, tp);
            if (Config.bout) blockOutline(q, m, cam, mc);
            if (Config.wp && Config.wpBeam) Waypoints.renderBeams(q, m, cam, mc);
            if (Cosmetics.any()) Cosmetics.render(q, m, cam, mc, tp);
            if (Config.ptrail) pearlTrails(q, m, cam, mc, tp);
            if (Config.etrail) elytraTrails(q, m, cam, mc, tp);
            consumers.draw(RenderLayers.debugQuads());
        } finally {
            mv.popMatrix();
        }
    }

    private static int pal(int idx) {
        return Config.COLORS[Math.max(0, Math.min(idx, Config.COLORS.length - 1))] & 0xFFFFFF;
    }

    private static int rainbow(float offset) {
        float h = (((System.currentTimeMillis() % 5000L) / 5000f) + offset) % 1f;
        return MathHelper.hsvToRgb(h, 0.65f, 1f) & 0xFFFFFF;
    }

    // ---------------------------------------------------------------- Hitboxes + Item ESP
    private static boolean valuable(ItemStack st) {
        String p = Registries.ITEM.getId(st.getItem()).getPath();
        for (String v : VALUABLE) if (p.contains(v)) return true;
        return false;
    }

    private static void entities(VertexConsumer q, Matrix4f m, Vec3d cam, Camera camera, MinecraftClient mc, float tp) {
        double hbR2 = Config.hbRange * Config.hbRange, ieR2 = Config.ieRange * Config.ieRange;
        boolean selfBox = Config.hbSelf && !mc.options.getPerspective().isFirstPerson();
        Vec3d fwd = Vec3d.fromPolar(camera.getPitch(), camera.getYaw());
        double sx = cam.x + fwd.x * 0.6, sy = cam.y + fwd.y * 0.6 - 0.12, sz = cam.z + fwd.z * 0.6;
        int items = 0;

        for (Entity e : mc.world.getEntities()) {
            double ddx = e.getX() - cam.x, ddy = e.getY() - cam.y, ddz = e.getZ() - cam.z;
            double d2 = ddx * ddx + ddy * ddy + ddz * ddz;
            double ix = MathHelper.lerp((double) tp, e.lastX, e.getX()) - e.getX();
            double iy = MathHelper.lerp((double) tp, e.lastY, e.getY()) - e.getY();
            double iz = MathHelper.lerp((double) tp, e.lastZ, e.getZ()) - e.getZ();

            if (e instanceof ItemEntity ie && Config.itemEsp) {
                if (d2 > ieR2 || items >= 150) continue;
                ItemStack st = ie.getStack();
                boolean hi = valuable(st);
                if (Config.ieValuable && !hi) continue;
                items++;
                int rgb = hi ? pal(Config.ieColorHi) : pal(Config.ieColor);
                Box b = e.getBoundingBox().offset(ix, iy, iz).expand(0.1);
                WorldDraw.edges(q, m, cam.x, cam.y, cam.z, b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ,
                        (float) Config.ieWidth, WorldDraw.argb(0.95f, rgb));
                if (Config.ieFill)
                    WorldDraw.fill(q, m, cam.x, cam.y, cam.z, b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ,
                            WorldDraw.argb(hi ? 0.28f : 0.16f, rgb));
                if (Config.ieTracers)
                    WorldDraw.ribbon(q, m, cam.x, cam.y, cam.z, sx, sy, sz,
                            (b.minX + b.maxX) / 2, (b.minY + b.maxY) / 2, (b.minZ + b.maxZ) / 2,
                            0.012f, WorldDraw.argb(0.0f, rgb), WorldDraw.argb(0.85f, rgb));
                continue;
            }

            if (!Config.hbox || d2 > hbR2) continue;
            if (e == mc.player && !selfBox) continue;
            if (e instanceof ItemEntity && Config.itemEsp) continue;

            int rgb;
            if (e instanceof PlayerEntity p) {
                if (!Config.hbPlayers) continue;
                rgb = Config.friends.contains(p.getName().getString()) ? FRIEND : pal(Config.hbColorP);
            } else if (e instanceof LivingEntity) {
                if (!Config.hbMobs) continue;
                rgb = pal(Config.hbColorM);
            } else {
                if (!Config.hbOthers) continue;
                rgb = pal(Config.hbColorO);
            }
            Box b = e.getBoundingBox().offset(ix, iy, iz).expand(e.getTargetingMargin());
            WorldDraw.edges(q, m, cam.x, cam.y, cam.z, b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ,
                    (float) Config.hbWidth, WorldDraw.argb((float) Config.hbAlpha, rgb));
            if (Config.hbFill)
                WorldDraw.fill(q, m, cam.x, cam.y, cam.z, b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ,
                        WorldDraw.argb((float) Config.hbAlpha * 0.18f, rgb));
        }
    }

    // ---------------------------------------------------------------- Block Outline
    private static void blockOutline(VertexConsumer q, Matrix4f m, Vec3d cam, MinecraftClient mc) {
        if (!(mc.crosshairTarget instanceof BlockHitResult bhr) || bhr.getType() != HitResult.Type.BLOCK) return;
        BlockPos pos = bhr.getBlockPos();
        BlockState st = mc.world.getBlockState(pos);
        if (st.isAir()) return;
        VoxelShape shape = st.getOutlineShape(mc.world, pos, ShapeContext.of(mc.player));
        if (shape.isEmpty()) return;

        int rgb = Config.boRainbow ? rainbow(0f) : pal(Config.boColor);
        final float hw = (float) Config.boWidth;
        final int line = WorldDraw.argb(1f, rgb);
        final int fill = WorldDraw.argb((float) Config.boFillAlpha, rgb);
        final boolean doFill = Config.boFill;
        final double px = pos.getX(), py = pos.getY(), pz = pos.getZ();
        final double e = 0.002;
        shape.forEachBox((x1, y1, z1, x2, y2, z2) -> {
            WorldDraw.edges(q, m, cam.x, cam.y, cam.z,
                    px + x1 - e, py + y1 - e, pz + z1 - e, px + x2 + e, py + y2 + e, pz + z2 + e, hw, line);
            if (doFill)
                WorldDraw.fill(q, m, cam.x, cam.y, cam.z,
                        px + x1 - e, py + y1 - e, pz + z1 - e, px + x2 + e, py + y2 + e, pz + z2 + e, fill);
        });
    }

    // ---------------------------------------------------------------- Chunk Borders
    private static void chunkBorders(VertexConsumer q, Matrix4f m, Vec3d cam, MinecraftClient mc) {
        int radius = Math.max(1, Math.min(4, Config.cbRadius));
        double px = mc.player.getX(), py = mc.player.getY(), pz = mc.player.getZ();
        int ccx = Math.floorDiv((int) Math.floor(px), 16), ccz = Math.floorDiv((int) Math.floor(pz), 16);
        double bottom = mc.world.getBottomY(), top = mc.world.getBottomY() + mc.world.getHeight();
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
                WorldDraw.ribbon(q, m, cam.x, cam.y, cam.z, x, y0, z, x, y1, z, hw, cur ? bright : dim, cur ? bright : dim);
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
                        WorldDraw.ribbon(q, m, cam.x, cam.y, cam.z, xa, y, za, xb, y, za, hw, c, c);
                        WorldDraw.ribbon(q, m, cam.x, cam.y, cam.z, xb, y, za, xb, y, zb, hw, c, c);
                        WorldDraw.ribbon(q, m, cam.x, cam.y, cam.z, xb, y, zb, xa, y, zb, hw, c, c);
                        WorldDraw.ribbon(q, m, cam.x, cam.y, cam.z, xa, y, zb, xa, y, za, hw, c, c);
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------- Trails
    private static void drawTrail(VertexConsumer q, Matrix4f m, Vec3d cam, ArrayDeque<Pt> d,
                                  Vec3d head, int maxAge, float tp, float width, boolean rainbow, int rgb) {
        double lx, ly, lz;
        float lf;
        int lc;
        boolean have = false;
        lx = ly = lz = 0;
        lf = 1f;
        lc = 0;
        if (head != null) {
            lx = head.x; ly = head.y; lz = head.z;
            lc = rainbow ? rainbow(tickCount * 0.015f) : rgb;
            have = true;
        }
        for (Pt p : d) {
            float age = (tickCount - p.tick) + tp;
            float f = Math.max(0f, 1f - age / maxAge);
            int c = rainbow ? (MathHelper.hsvToRgb((p.tick * 0.015f) % 1f, 0.65f, 1f) & 0xFFFFFF) : rgb;
            if (have) {
                float hw = width * (0.25f + 0.75f * ((lf + f) * 0.5f));
                WorldDraw.ribbon(q, m, cam.x, cam.y, cam.z, lx, ly, lz, p.x, p.y, p.z, hw,
                        WorldDraw.argb(0.9f * lf, lc), WorldDraw.argb(0.9f * f, c));
            }
            lx = p.x; ly = p.y; lz = p.z; lf = f; lc = c; have = true;
            if (f <= 0f) break;
        }
    }

    private static void pearlTrails(VertexConsumer q, Matrix4f m, Vec3d cam, MinecraftClient mc, float tp) {
        int maxAge = pearlAge();
        for (Map.Entry<Integer, ArrayDeque<Pt>> en : PEARLS.entrySet()) {
            Entity e = mc.world.getEntityById(en.getKey());
            Vec3d head = e == null ? null : new Vec3d(
                    MathHelper.lerp((double) tp, e.lastX, e.getX()),
                    MathHelper.lerp((double) tp, e.lastY, e.getY()),
                    MathHelper.lerp((double) tp, e.lastZ, e.getZ()));
            drawTrail(q, m, cam, en.getValue(), head, maxAge, tp, (float) Config.ptWidth, Config.ptRainbow, pal(Config.ptColor));
        }
    }

    private static void elytraTrails(VertexConsumer q, Matrix4f m, Vec3d cam, MinecraftClient mc, float tp) {
        int maxAge = elytraAge();
        for (Map.Entry<UUID, ArrayDeque<Pt>> en : ELYTRA.entrySet()) {
            Entity e = null;
            for (PlayerEntity p : mc.world.getPlayers()) if (p.getUuid().equals(en.getKey())) { e = p; break; }
            Vec3d head = (e == null || !(e instanceof LivingEntity le) || !le.isGliding()) ? null : new Vec3d(
                    MathHelper.lerp((double) tp, e.lastX, e.getX()),
                    MathHelper.lerp((double) tp, e.lastY, e.getY()) + e.getHeight() * 0.4,
                    MathHelper.lerp((double) tp, e.lastZ, e.getZ()));
            drawTrail(q, m, cam, en.getValue(), head, maxAge, tp, (float) Config.etWidth, Config.etRainbow, pal(Config.etColor));
        }
    }
}
