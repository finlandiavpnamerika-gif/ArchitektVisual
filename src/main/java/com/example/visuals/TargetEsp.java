package com.example.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Target ESP: когда смотришь на моба или игрока (или бьёшь его) - вокруг него рисуется анимированная фигура.
 * Фигуры: Chevrons (четыре шеврона, как на референсе), Ring (бегущая дуга), Diamond (два вращающихся ромба),
 * Orbit (три светящиеся точки со шлейфом по кругу).
 * При ударе цвет плавно меняется на "цвет при ударе" и возвращается обратно.
 * Цвет, размер, скорость и прозрачность настраиваются в меню.
 */
public final class TargetEsp {
    private TargetEsp() {}

    public static final String[] SHAPES = {"Chevrons", "Ring", "Diamond", "Orbit"};

    private static final long   GRACE_MS = 700;    // сколько держим цель после того, как отвёл прицел
    private static final long   FADE_MS  = 350;    // и сколько она потом гаснет
    private static final long   FLASH_MS = 250;    // вспышка цвета при ударе
    private static final double MAX_DIST = 64.0;

    private static LivingEntity target = null;
    private static long lastSeen = 0, acquiredAt = 0, flashUntil = 0;

    /** Вызвать один раз из VisualsClient.onInitializeClient(). */
    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient() && Config.targetEsp) {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player != null && player == mc.player && entity instanceof LivingEntity le) {
                    select(le);
                    flashUntil = System.currentTimeMillis() + FLASH_MS;
                }
            }
            return ActionResult.PASS;
        });
    }

    private static void select(LivingEntity e) {
        long now = System.currentTimeMillis();
        if (target != e) { target = e; acquiredAt = now; }
        lastSeen = now;
    }

    public static void render(Camera camera, Matrix4f positionMatrix,
                              VertexConsumerProvider.Immediate consumers, float tickProgress) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!Config.targetEsp || mc.world == null || mc.player == null) return;
        long now = System.currentTimeMillis();

        // цель = то, на что смотрит прицел
        Entity looked = mc.targetedEntity;
        if (looked instanceof LivingEntity le && le != mc.player && le.isAlive() && !le.isSpectator()) select(le);

        LivingEntity t = target;
        if (t == null) return;
        if (!t.isAlive() || t.isRemoved()) { target = null; return; }

        long since = now - lastSeen;
        float visible = since <= GRACE_MS ? 1f : 1f - (since - GRACE_MS) / (float) FADE_MS;
        if (visible <= 0f) { target = null; return; }

        Vec3d cam = camera.getCameraPos();
        double ex = MathHelper.lerp((double) tickProgress, t.lastX, t.getX());
        double ey = MathHelper.lerp((double) tickProgress, t.lastY, t.getY()) + t.getHeight() * 0.5;
        double ez = MathHelper.lerp((double) tickProgress, t.lastZ, t.getZ());
        float cx = (float) (ex - cam.x), cy = (float) (ey - cam.y), cz = (float) (ez - cam.z);
        if (cx * cx + cy * cy + cz * cz > MAX_DIST * MAX_DIST) return;

        // появление: фигура "прилетает" чуть большего размера и сжимается
        float appear = Math.min(1f, (now - acquiredAt) / 260f);
        float ease = 1f - (1f - appear) * (1f - appear);
        float scale = (float) (Math.max(0.6, t.getHeight() * 0.55) * Config.targetEspSize) * (1.7f - 0.7f * ease);

        // цвет: базовый -> цвет удара
        float flash = Math.max(0f, Math.min(1f, (flashUntil - now) / (float) FLASH_MS));
        float hurt = t.maxHurtTime > 0 ? Math.max(0f, Math.min(1f, t.hurtTime / (float) t.maxHurtTime)) : 0f;
        float mix = Math.max(flash, hurt);
        int base = MathHelper.hsvToRgb((float) Config.targetEspHue, 0.85f, 1f) & 0xFFFFFF;
        int hit = MathHelper.hsvToRgb((float) Config.targetEspHitHue, 0.85f, 1f) & 0xFFFFFF;
        int rgb = lerpRgb(base, hit, mix);
        float alpha = (float) Config.targetEspAlpha * visible * (0.4f + 0.6f * ease);

        // оси плоскости, смотрящей в камеру
        Quaternionf rot = camera.getRotation();
        Vector3f r = rot.transform(new Vector3f(1, 0, 0));
        Vector3f u = rot.transform(new Vector3f(0, 1, 0));

        float time = (now % 1000000L) / 1000f * (float) Config.targetEspSpeed;
        Matrix4f m = new MatrixStack().peek().getPositionMatrix();   // единичная матрица

        Matrix4fStack mv = RenderSystem.getModelViewStack();
        mv.pushMatrix();
        mv.mul(positionMatrix);
        try {
            VertexConsumer q = consumers.getBuffer(RenderLayers.debugQuads());
            switch (Math.max(0, Math.min(Config.targetEspShape, SHAPES.length - 1))) {
                case 0 -> chevrons(q, m, cx, cy, cz, r, u, scale, time, rgb, alpha);
                case 1 -> ring(q, m, cx, cy, cz, r, u, scale, time, rgb, alpha);
                case 2 -> diamond(q, m, cx, cy, cz, r, u, scale, time, rgb, alpha);
                default -> orbit(q, m, cx, cy, cz, r, u, scale, time, rgb, alpha);
            }
            consumers.draw(RenderLayers.debugQuads());
        } finally {
            mv.popMatrix();
        }
    }

    // ---------------- фигуры ----------------

    /** Четыре шеврона вокруг цели: вращаются и "дышат". */
    private static void chevrons(VertexConsumer q, Matrix4f m, float cx, float cy, float cz,
                                 Vector3f r, Vector3f u, float s, float t, int rgb, float a) {
        float spin = t * 1.4f;
        float d = s * (0.85f + 0.12f * (float) Math.sin(t * 3.0f));
        float len = s * 0.38f;
        float w = s * 0.10f;
        for (int k = 0; k < 4; k++) {
            float ang = spin + k * (float) (Math.PI / 2);
            float c = (float) Math.cos(ang), sn = (float) Math.sin(ang);
            // шеврон, смотрящий вверх: вершина (0,d), концы (+-len, d-len); потом поворот на ang
            float tx = -sn * d,               ty = c * d;
            float ax = c * len - sn * (d - len), ay = sn * len + c * (d - len);
            float bx = -c * len - sn * (d - len), by = -sn * len + c * (d - len);
            // свечение + основная линия
            line(q, m, cx, cy, cz, r, u, tx, ty, ax, ay, w * 2.4f, rgb, a * 0.22f);
            line(q, m, cx, cy, cz, r, u, tx, ty, bx, by, w * 2.4f, rgb, a * 0.22f);
            line(q, m, cx, cy, cz, r, u, tx, ty, ax, ay, w, rgb, a);
            line(q, m, cx, cy, cz, r, u, tx, ty, bx, by, w, rgb, a);
        }
    }

    /** Кольцо с бегущей дугой. */
    private static void ring(VertexConsumer q, Matrix4f m, float cx, float cy, float cz,
                             Vector3f r, Vector3f u, float s, float t, int rgb, float a) {
        int n = 48;
        float rad = s * 0.9f;
        float w = s * 0.09f;
        for (int i = 0; i < n; i++) {
            float f = i / (float) n;
            float phase = f - t * 0.25f;
            phase -= (float) Math.floor(phase);
            if (phase > 0.7f) continue;                 // дуга занимает 70% окружности
            float fade = 0.35f + 0.65f * (phase / 0.7f); // хвост бледнее головы
            float a0 = (float) (Math.PI * 2.0) * f, a1 = (float) (Math.PI * 2.0) * (i + 1) / n;
            float x0 = (float) Math.cos(a0) * rad, y0 = (float) Math.sin(a0) * rad;
            float x1 = (float) Math.cos(a1) * rad, y1 = (float) Math.sin(a1) * rad;
            line(q, m, cx, cy, cz, r, u, x0, y0, x1, y1, w * 2.4f, rgb, a * 0.2f * fade);
            line(q, m, cx, cy, cz, r, u, x0, y0, x1, y1, w, rgb, a * fade);
        }
    }

    /** Два вложенных ромба, крутятся в разные стороны. */
    private static void diamond(VertexConsumer q, Matrix4f m, float cx, float cy, float cz,
                                Vector3f r, Vector3f u, float s, float t, int rgb, float a) {
        square(q, m, cx, cy, cz, r, u, s * (0.9f + 0.06f * (float) Math.sin(t * 3f)), t * 1.2f + (float) (Math.PI / 4), s * 0.09f, rgb, a);
        square(q, m, cx, cy, cz, r, u, s * 0.55f, -t * 1.8f + (float) (Math.PI / 4), s * 0.07f, rgb, a * 0.65f);
    }

    private static void square(VertexConsumer q, Matrix4f m, float cx, float cy, float cz,
                               Vector3f r, Vector3f u, float rad, float ang, float w, int rgb, float a) {
        for (int k = 0; k < 4; k++) {
            float a0 = ang + k * (float) (Math.PI / 2), a1 = ang + (k + 1) * (float) (Math.PI / 2);
            float x0 = (float) Math.cos(a0) * rad, y0 = (float) Math.sin(a0) * rad;
            float x1 = (float) Math.cos(a1) * rad, y1 = (float) Math.sin(a1) * rad;
            line(q, m, cx, cy, cz, r, u, x0, y0, x1, y1, w * 2.4f, rgb, a * 0.2f);
            line(q, m, cx, cy, cz, r, u, x0, y0, x1, y1, w, rgb, a);
        }
    }

    /** Три светящиеся точки со шлейфом летают вокруг цели по горизонтальному кругу. */
    private static void orbit(VertexConsumer q, Matrix4f m, float cx, float cy, float cz,
                              Vector3f r, Vector3f u, float s, float t, int rgb, float a) {
        float rad = s * 0.85f;
        for (int j = 0; j < 3; j++) {
            for (int k = 0; k < 9; k++) {
                float ang = t * 2.2f + j * (float) (Math.PI * 2.0 / 3.0) - k * 0.13f;
                float yo = (float) Math.sin(t * 1.7f + j * 2.1f - k * 0.05f) * s * 0.55f;
                float px = cx + (float) Math.cos(ang) * rad;
                float py = cy + yo;
                float pz = cz + (float) Math.sin(ang) * rad;
                float f = 1f - k / 9f;
                float hs = s * 0.075f * (0.5f + 0.5f * f);
                billboard(q, m, px, py, pz, r, u, hs * 2.2f, rgb, a * 0.22f * f);
                billboard(q, m, px, py, pz, r, u, hs, rgb, a * f);
            }
        }
    }

    // ---------------- примитивы ----------------

    /** Линия толщиной w в плоскости, смотрящей в камеру. Координаты (x,y) - в этой плоскости от центра c. */
    private static void line(VertexConsumer q, Matrix4f m, float cx, float cy, float cz,
                             Vector3f r, Vector3f u,
                             float x0, float y0, float x1, float y1, float w, int rgb, float a) {
        float dx = x1 - x0, dy = y1 - y0;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1.0e-5f) return;
        float nx = -dy / len * w * 0.5f, ny = dx / len * w * 0.5f;
        int col = argb((int) (255 * a), rgb);
        float[] xs = {x0 + nx, x0 - nx, x1 - nx, x1 + nx};
        float[] ys = {y0 + ny, y0 - ny, y1 - ny, y1 + ny};
        float[] px = new float[4], py = new float[4], pz = new float[4];
        for (int i = 0; i < 4; i++) {
            px[i] = cx + r.x * xs[i] + u.x * ys[i];
            py[i] = cy + r.y * xs[i] + u.y * ys[i];
            pz[i] = cz + r.z * xs[i] + u.z * ys[i];
        }
        dquad(q, m, px[0], py[0], pz[0], col, px[1], py[1], pz[1], col, px[2], py[2], pz[2], col, px[3], py[3], pz[3], col);
    }

    /** Квадрат со стороной 2*hs, смотрящий в камеру, в точке p. */
    private static void billboard(VertexConsumer q, Matrix4f m, float px, float py, float pz,
                                  Vector3f r, Vector3f u, float hs, int rgb, float a) {
        int col = argb((int) (255 * a), rgb);
        float rx = r.x * hs, ry = r.y * hs, rz = r.z * hs;
        float ux = u.x * hs, uy = u.y * hs, uz = u.z * hs;
        dquad(q, m, px - rx - ux, py - ry - uy, pz - rz - uz, col,
                    px + rx - ux, py + ry - uy, pz + rz - uz, col,
                    px + rx + ux, py + ry + uy, pz + rz + uz, col,
                    px - rx + ux, py - ry + uy, pz - rz + uz, col);
    }

    private static int lerpRgb(int a, int b, float t) {
        int rr = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
        int gg = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
        int bb = (int) ((a & 255) + ((b & 255) - (a & 255)) * t);
        return (rr << 16) | (gg << 8) | bb;
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
