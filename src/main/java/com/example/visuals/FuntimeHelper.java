package com.example.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

/**
 * FunTime Helper: показывает зоны предметов.
 *
 *  - Дезориентация, Явная пыль -> кольцо радиусом 10 блоков.
 *  - Трапка -> куб 3x3x3 вокруг игрока.
 *  - Пласт  -> стенка 3x3 перед игроком.
 *
 * Когда ТЫ используешь предмет - фигура рисуется на время действия.
 * Если включено "Чужие в руке" - фигура рисуется у игроков с таким предметом в руке.
 * Предмет определяется по имени (без цветов, без регистра).
 * Время действия (ticks) ПРИМЕРНОЕ - подправь в таблице KINDS (20 тиков = 1 секунда).
 */
public final class FuntimeHelper {
    private FuntimeHelper() {}

    private enum Shape { RING, CUBE, WALL }

    private static final class Kind {
        final String key;    // кусок имени предмета (нижний регистр)
        final Shape shape;
        final double size;   // RING: радиус; CUBE/WALL: сторона в блоках
        final int ticks;     // сколько тиков держится фигура
        final int rgb;
        Kind(String key, Shape shape, double size, int ticks, int rgb) {
            this.key = key; this.shape = shape; this.size = size; this.ticks = ticks; this.rgb = rgb;
        }
    }

    // ====== ТАБЛИЦА ПРЕДМЕТОВ: правь здесь ======
    private static final Kind[] KINDS = {
            new Kind("дезориентация", Shape.RING, 10.0, 20 * 15, 0xB388FF),
            new Kind("явная пыль",    Shape.RING, 10.0, 20 * 15, 0x69F0AE),
            new Kind("трапка",        Shape.CUBE,  3.0, 20 * 15, 0xFF5252),
            new Kind("пласт",         Shape.WALL,  3.0, 20 * 15, 0xFFD740),
    };
    // =============================================

    private static final int    MAX_ZONES  = 16;
    private static final double MAX_DIST   = 96.0;
    private static final float  BAND_WIDTH = 0.15f;   // ширина кольца
    private static final float  WALL_H     = 1.2f;    // высота стенки у кольца
    private static final float  LIFT       = 0.03f;
    private static final double CUBE_DOWN  = 0.5;     // куб начинается на столько ниже ног игрока
    private static final double WALL_DIST  = 2.0;     // стенка стоит на таком расстоянии перед игроком

    private static final class Zone {
        final double x, y, z;
        final float yaw;
        final Kind kind;
        final int endTick;
        Zone(double x, double y, double z, float yaw, Kind kind, int endTick) {
            this.x = x; this.y = y; this.z = z; this.yaw = yaw; this.kind = kind; this.endTick = endTick;
        }
    }

    private static final List<Zone> ZONES = new ArrayList<>();
    private static int tickCount = 0;

    /** Вызвать один раз из VisualsClient.onInitializeClient(). */
    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient() && Config.zones) {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player != null && player == mc.player) onUse(player, player.getStackInHand(hand));
            }
            return ActionResult.PASS;
        });
    }

    private static Kind kindOf(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        String name = stack.getName().getString().toLowerCase(Locale.ROOT);
        for (Kind k : KINDS) if (name.contains(k.key)) return k;
        return null;
    }

    private static void onUse(PlayerEntity player, ItemStack stack) {
        Kind k = kindOf(stack);
        if (k == null) return;
        // предмет на кулдауне - значит реально не сработал
        if (player.getItemCooldownManager().isCoolingDown(stack)) return;

        // не дублируем, если только что поставили такую же
        for (Zone z : ZONES) {
            if (z.kind == k && z.endTick - tickCount > k.ticks - 5) return;
        }
        if (ZONES.size() >= MAX_ZONES) ZONES.remove(0);
        ZONES.add(new Zone(player.getX(), player.getY(), player.getZ(), player.getYaw(), k, tickCount + k.ticks));
    }

    public static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null || !Config.zones) {
            if (!ZONES.isEmpty()) ZONES.clear();
            return;
        }
        tickCount++;
        ZONES.removeIf(z -> tickCount >= z.endTick);
    }

    public static void render(Camera camera, Matrix4f positionMatrix,
                              VertexConsumerProvider.Immediate consumers, float tickProgress) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!Config.zones || mc.world == null || mc.player == null) return;

        boolean anyHeld = false;
        if (Config.zonesHeld) {
            for (PlayerEntity p : mc.world.getPlayers()) {
                if (p != mc.player && !p.isSpectator() && kindOf(p.getMainHandStack()) != null) { anyHeld = true; break; }
            }
        }
        if (ZONES.isEmpty() && !anyHeld) return;

        Vec3d cam = camera.getCameraPos();
        Matrix4f m = new MatrixStack().peek().getPositionMatrix();   // единичная матрица

        Matrix4fStack mv = RenderSystem.getModelViewStack();
        mv.pushMatrix();
        mv.mul(positionMatrix);
        try {
            VertexConsumer q = consumers.getBuffer(RenderLayers.debugQuads());

            // 1) фигуры от моих использований
            for (Zone z : ZONES) {
                float left = (z.endTick - tickCount) - tickProgress;
                float fade = Math.max(0f, Math.min(1f, left / 20f));   // гаснет за последнюю секунду
                draw(q, m, cam, z.kind, z.x, z.y, z.z, z.yaw, fade);
            }

            // 2) предметы в руках у других игроков (превью)
            if (Config.zonesHeld) {
                for (PlayerEntity p : mc.world.getPlayers()) {
                    if (p == mc.player || p.isSpectator()) continue;
                    Kind k = kindOf(p.getMainHandStack());
                    if (k == null) continue;
                    double hx = MathHelper.lerp((double) tickProgress, p.lastX, p.getX());
                    double hy = MathHelper.lerp((double) tickProgress, p.lastY, p.getY());
                    double hz = MathHelper.lerp((double) tickProgress, p.lastZ, p.getZ());
                    double dx = hx - cam.x, dy = hy - cam.y, dz = hz - cam.z;
                    if (dx * dx + dy * dy + dz * dz > MAX_DIST * MAX_DIST) continue;
                    draw(q, m, cam, k, hx, hy, hz, p.getYaw(), 0.55f);
                }
            }

            consumers.draw(RenderLayers.debugQuads());
        } finally {
            mv.popMatrix();
        }
    }

    private static void draw(VertexConsumer q, Matrix4f m, Vec3d cam, Kind k,
                             double x, double y, double z, float yawDeg, float strength) {
        if (strength <= 0f) return;
        switch (k.shape) {
            case RING -> ring(q, m, cam, x, y, z, k.size, k.rgb, strength);
            case CUBE -> cube(q, m, cam, x, y, z, k.size, k.rgb, strength);
            case WALL -> wall(q, m, cam, x, y, z, yawDeg, k.size, k.rgb, strength);
        }
    }

    // ---------------- кольцо ----------------

    /** Кольцо на земле + полупрозрачная стенка вверх + лёгкая заливка. */
    private static void ring(VertexConsumer q, Matrix4f m, Vec3d cam,
                             double cx, double cy, double cz,
                             double radius, int rgb, float strength) {
        int n = Math.max(32, Math.min(128, (int) (radius * 10)));
        float r = (float) radius;
        float ri = Math.max(0.05f, r - BAND_WIDTH);

        float ox = (float) (cx - cam.x);
        float oy = (float) (cy - cam.y) + LIFT;
        float oz = (float) (cz - cam.z);

        int cBand  = argb((int) (210 * strength), rgb);
        int cWallB = argb((int) (120 * strength), rgb);
        int cWallT = argb(0, rgb);
        int cFill  = argb((int) (26 * strength), rgb);

        for (int i = 0; i < n; i++) {
            double a0 = (Math.PI * 2.0) * i / n;
            double a1 = (Math.PI * 2.0) * (i + 1) / n;
            float c0 = (float) Math.cos(a0), s0 = (float) Math.sin(a0);
            float c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);

            float ox0 = ox + c0 * r,  oz0 = oz + s0 * r;     // внешняя точка 0
            float ox1 = ox + c1 * r,  oz1 = oz + s1 * r;     // внешняя точка 1
            float ix0 = ox + c0 * ri, iz0 = oz + s0 * ri;    // внутренняя точка 0
            float ix1 = ox + c1 * ri, iz1 = oz + s1 * ri;    // внутренняя точка 1

            // кольцо (обе стороны)
            dquad(q, m, ox0, oy, oz0, cBand, ix0, oy, iz0, cBand, ix1, oy, iz1, cBand, ox1, oy, oz1, cBand);
            // стенка (обе стороны)
            dquad(q, m, ox0, oy, oz0, cWallB, ox0, oy + WALL_H, oz0, cWallT, ox1, oy + WALL_H, oz1, cWallT, ox1, oy, oz1, cWallB);
            // заливка внутри (обе стороны)
            dquad(q, m, ox, oy, oz, cFill, ix0, oy, iz0, cFill, ix1, oy, iz1, cFill, ox, oy, oz, cFill);
        }
    }

    // ---------------- куб (трапка) ----------------

    private static void cube(VertexConsumer q, Matrix4f m, Vec3d cam,
                             double cx, double cy, double cz, double size, int rgb, float strength) {
        float s = (float) size;
        float x0 = (float) (cx - size / 2.0 - cam.x);
        float y0 = (float) (cy - CUBE_DOWN - cam.y);
        float z0 = (float) (cz - size / 2.0 - cam.z);

        // низ и верх
        face(q, m, x0, y0,     z0,  s, 0, 0,  0, 0, s, rgb, strength);
        face(q, m, x0, y0 + s, z0,  s, 0, 0,  0, 0, s, rgb, strength);
        // север и юг
        face(q, m, x0, y0, z0,      s, 0, 0,  0, s, 0, rgb, strength);
        face(q, m, x0, y0, z0 + s,  s, 0, 0,  0, s, 0, rgb, strength);
        // запад и восток
        face(q, m, x0,     y0, z0,  0, 0, s,  0, s, 0, rgb, strength);
        face(q, m, x0 + s, y0, z0,  0, 0, s,  0, s, 0, rgb, strength);
    }

    // ---------------- стенка (пласт) ----------------

    private static void wall(VertexConsumer q, Matrix4f m, Vec3d cam,
                             double px, double py, double pz, float yawDeg, double size, int rgb, float strength) {
        double yaw = Math.toRadians(yawDeg);
        double fx = -Math.sin(yaw), fz = Math.cos(yaw);     // куда смотрит игрок
        double sx = -fz, sz = fx;                           // вдоль стенки (перпендикуляр)

        double wx = px + fx * WALL_DIST, wz = pz + fz * WALL_DIST;
        float s = (float) size;
        float ox = (float) (wx - sx * size / 2.0 - cam.x);
        float oy = (float) (py - cam.y);
        float oz = (float) (wz - sz * size / 2.0 - cam.z);

        face(q, m, ox, oy, oz, (float) (sx * size), 0, (float) (sz * size), 0, s, 0, rgb, strength);
    }

    // ---------------- примитивы ----------------

    /** Прямоугольник из угла O и двух рёбер u, v: заливка + яркая рамка по краю (обе стороны). */
    private static void face(VertexConsumer q, Matrix4f m,
                             float ox, float oy, float oz,
                             float ux, float uy, float uz,
                             float vx, float vy, float vz,
                             int rgb, float strength) {
        int cFill = argb((int) (55 * strength), rgb);
        int cEdge = argb((int) (230 * strength), rgb);
        final float t = 0.07f;   // толщина рамки (доля ребра)

        // заливка
        dquad(q, m, ox, oy, oz, cFill,
                    ox + ux, oy + uy, oz + uz, cFill,
                    ox + ux + vx, oy + uy + vy, oz + uz + vz, cFill,
                    ox + vx, oy + vy, oz + vz, cFill);

        // нижняя и верхняя полоски (вдоль u)
        strip(q, m, ox, oy, oz, ux, uy, uz, vx * t, vy * t, vz * t, cEdge);
        strip(q, m, ox + vx * (1 - t), oy + vy * (1 - t), oz + vz * (1 - t), ux, uy, uz, vx * t, vy * t, vz * t, cEdge);
        // левая и правая полоски (вдоль v)
        strip(q, m, ox, oy, oz, vx, vy, vz, ux * t, uy * t, uz * t, cEdge);
        strip(q, m, ox + ux * (1 - t), oy + uy * (1 - t), oz + uz * (1 - t), vx, vy, vz, ux * t, uy * t, uz * t, cEdge);
    }

    /** Узкая полоска: от угла O на длину a, шириной b. */
    private static void strip(VertexConsumer q, Matrix4f m,
                              float ox, float oy, float oz,
                              float ax, float ay, float az,
                              float bx, float by, float bz, int c) {
        dquad(q, m, ox, oy, oz, c,
                    ox + ax, oy + ay, oz + az, c,
                    ox + ax + bx, oy + ay + by, oz + az + bz, c,
                    ox + bx, oy + by, oz + bz, c);
    }

    private static int argb(int a, int rgb) {
        return (Math.max(0, Math.min(255, a)) << 24) | (rgb & 0xFFFFFF);
    }

    /** Четырёхугольник, видимый с обеих сторон. */
    private static void dquad(VertexConsumer q, Matrix4f m,
                              float ax, float ay, float az, int ac,
                              float bx, float by, float bz, int bc,
                              float cx, float cy, float cz, int cc,
                              float dx, float dy, float dz, int dc) {
        quad(q, m, ax, ay, az, ac, bx, by, bz, bc, cx, cy, cz, cc, dx, dy, dz, dc);
        quad(q, m, dx, dy, dz, dc, cx, cy, cz, cc, bx, by, bz, bc, ax, ay, az, ac);
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
}
