package com.example.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Random;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Particles: свои частицы (светящиеся билборды).
 * Режимы: Удар (взрыв частиц при ударе по цели), Аура (частицы вокруг тебя), Оба.
 * Формы: Квадрат, Ромб, Звезда, Точка. Цвет, радуга, количество, размер, время жизни, гравитация, прозрачность.
 */
public final class Particles {
    private Particles() {}

    public static final String[] MODES  = {"Удар", "Аура", "Оба", "Мир"};
    public static final String[] SHAPES = {"Квадрат", "Ромб", "Звезда", "Точка"};

    private static final int MAX = 700;
    private static final Random RND = new Random();

    private static final class P {
        double x, y, z;
        float vx, vy, vz;
        float age, life, size, hue, spin, spinV;
        boolean ambient;
    }

    private static final ArrayList<P> LIST = new ArrayList<>();
    private static long last = 0;
    private static float auraAcc = 0f, worldAcc = 0f;

    /** Вызвать один раз из VisualsClient.onInitializeClient(). */
    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient() && Config.particles && (Config.partMode == 0 || Config.partMode == 2)) {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player != null && player == mc.player && entity instanceof LivingEntity le) burst(le);
            }
            return ActionResult.PASS;
        });
    }

    private static float rand(float a, float b) { return a + (b - a) * RND.nextFloat(); }

    private static P make(double x, double y, double z, float speed, float up) {
        P p = new P();
        p.x = x; p.y = y; p.z = z;
        float th = rand(0f, (float) (Math.PI * 2.0));
        float ph = rand(-0.6f, 1.0f);
        float cp = (float) Math.cos(ph);
        p.vx = (float) Math.cos(th) * cp * speed;
        p.vz = (float) Math.sin(th) * cp * speed;
        p.vy = (float) Math.sin(ph) * speed + up;
        p.life = (float) Config.partLife * rand(0.7f, 1.2f);
        p.size = (float) Config.partSize * rand(0.7f, 1.25f);
        p.hue = Config.partRainbow ? RND.nextFloat() : (float) ((Config.partHue + rand(-0.04f, 0.04f) + 1.0) % 1.0);
        p.spin = rand(0f, 6.28f);
        p.spinV = rand(-4f, 4f);
        return p;
    }

    private static void burst(LivingEntity e) {
        int n = (int) Config.partCount;
        for (int i = 0; i < n && LIST.size() < MAX; i++) {
            LIST.add(make(e.getX() + rand(-0.25f, 0.25f), e.getY() + e.getHeight() * rand(0.2f, 0.95f), e.getZ() + rand(-0.25f, 0.25f),
                    rand(1.2f, 3.6f), 0.8f));
        }
    }

    public static void render(Camera camera, Matrix4f positionMatrix,
                              VertexConsumerProvider.Immediate consumers, float tickProgress) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) { LIST.clear(); return; }
        long now = System.currentTimeMillis();
        float dt = last == 0 ? 0f : Math.min(0.05f, (now - last) / 1000f);
        last = now;
        if (!Config.particles) { LIST.clear(); return; }

        // аура вокруг игрока
        if ((Config.partMode == 1 || Config.partMode == 2) && dt > 0f) {
            auraAcc += dt * (float) Config.partCount * 1.5f;
            double px = MathHelper.lerp((double) tickProgress, mc.player.lastX, mc.player.getX());
            double py = MathHelper.lerp((double) tickProgress, mc.player.lastY, mc.player.getY());
            double pz = MathHelper.lerp((double) tickProgress, mc.player.lastZ, mc.player.getZ());
            while (auraAcc >= 1f && LIST.size() < MAX) {
                auraAcc -= 1f;
                float ang = rand(0f, 6.28f), rad = rand(0.5f, 1.0f);
                P p = make(px + Math.cos(ang) * rad, py + rand(0.05f, mc.player.getHeight()), pz + Math.sin(ang) * rad, 0.15f, 0.35f);
                LIST.add(p);
            }
            if (auraAcc > 5f) auraAcc = 0f;
        }

        // частицы, летающие по всему миру вокруг игрока
        if (Config.partMode == 3 && dt > 0f) {
            worldAcc += dt * (float) Config.partCount * 3f;
            while (worldAcc >= 1f && LIST.size() < MAX) {
                worldAcc -= 1f;
                float ang = rand(0f, 6.28f), rad = rand(2f, 14f);
                P p = make(mc.player.getX() + Math.cos(ang) * rad, mc.player.getY() + rand(-2f, 6f),
                        mc.player.getZ() + Math.sin(ang) * rad, 0.25f, 0f);
                p.ambient = true;
                p.life *= 3f;
                LIST.add(p);
            }
            if (worldAcc > 5f) worldAcc = 0f;
        }

        // обновление
        float grav = (float) Config.partGravity;
        for (int i = LIST.size() - 1; i >= 0; i--) {
            P p = LIST.get(i);
            p.age += dt;
            if (p.age >= p.life) { LIST.remove(i); continue; }
            if (!p.ambient) {
                p.vy -= grav * dt;
                float drag = 1f - 1.5f * dt;
                p.vx *= drag; p.vy *= drag; p.vz *= drag;
            }
            p.x += p.vx * dt; p.y += p.vy * dt; p.z += p.vz * dt;
            p.spin += p.spinV * dt;
        }
        if (LIST.isEmpty()) return;

        Vec3d cam = camera.getCameraPos();
        Quaternionf rot = camera.getRotation();
        Vector3f r = rot.transform(new Vector3f(1, 0, 0));
        Vector3f u = rot.transform(new Vector3f(0, 1, 0));
        Matrix4f m = new MatrixStack().peek().getPositionMatrix();
        int shape = Math.max(0, Math.min(Config.partShape, SHAPES.length - 1));

        Matrix4fStack mv = RenderSystem.getModelViewStack();
        mv.pushMatrix();
        mv.mul(positionMatrix);
        try {
            VertexConsumer q = consumers.getBuffer(RenderLayers.debugQuads());
            for (P p : LIST) {
                float f = p.age / p.life;
                float a = (float) Config.partAlpha * (float) Math.pow(1f - f, 0.7) * Math.min(1f, p.age / 0.06f);
                float s = p.size * (1f - 0.55f * f);
                int rgb = MathHelper.hsvToRgb(p.hue, 0.75f, 1f) & 0xFFFFFF;
                float cx = (float) (p.x - cam.x), cy = (float) (p.y - cam.y), cz = (float) (p.z - cam.z);
                // оси частицы с поворотом вокруг взгляда
                float c = (float) Math.cos(p.spin), sn = (float) Math.sin(p.spin);
                Vector3f ar = new Vector3f(r).mul(c).add(new Vector3f(u).mul(sn));
                Vector3f au = new Vector3f(u).mul(c).sub(new Vector3f(r).mul(sn));
                // свечение
                rect(q, m, cx, cy, cz, ar, au, s * 1.9f, s * 1.9f, rgb, a * 0.18f);
                switch (shape) {
                    case 0 -> rect(q, m, cx, cy, cz, ar, au, s, s, rgb, a);
                    case 1 -> {
                        Vector3f dr = new Vector3f(ar).add(au).mul(0.7071f);
                        Vector3f du = new Vector3f(au).sub(ar).mul(0.7071f);
                        rect(q, m, cx, cy, cz, dr, du, s * 1.15f, s * 1.15f, rgb, a);
                    }
                    case 2 -> {
                        rect(q, m, cx, cy, cz, ar, au, s * 1.6f, s * 0.32f, rgb, a);
                        rect(q, m, cx, cy, cz, ar, au, s * 0.32f, s * 1.6f, rgb, a);
                        rect(q, m, cx, cy, cz, ar, au, s * 0.5f, s * 0.5f, 0xFFFFFF, a * 0.8f);
                    }
                    default -> octagon(q, m, cx, cy, cz, ar, au, s, rgb, a);
                }
            }
            consumers.draw(RenderLayers.debugQuads());
        } finally {
            mv.popMatrix();
        }
    }

    private static int argb(int a, int rgb) {
        return (Math.max(0, Math.min(255, a)) << 24) | (rgb & 0xFFFFFF);
    }

    /** Прямоугольник с полуразмерами hx (по ar) и hy (по au). */
    private static void rect(VertexConsumer q, Matrix4f m, float cx, float cy, float cz,
                             Vector3f ar, Vector3f au, float hx, float hy, int rgb, float a) {
        int col = argb((int) (255 * a), rgb);
        float rx = ar.x * hx, ry = ar.y * hx, rz = ar.z * hx;
        float ux = au.x * hy, uy = au.y * hy, uz = au.z * hy;
        dquad(q, m, cx - rx - ux, cy - ry - uy, cz - rz - uz, col,
                    cx + rx - ux, cy + ry - uy, cz + rz - uz, col,
                    cx + rx + ux, cy + ry + uy, cz + rz + uz, col,
                    cx - rx + ux, cy - ry + uy, cz - rz + uz, col);
    }

    private static void octagon(VertexConsumer q, Matrix4f m, float cx, float cy, float cz,
                                Vector3f ar, Vector3f au, float s, int rgb, float a) {
        int col = argb((int) (255 * a), rgb);
        float[] px = new float[8], py = new float[8], pz = new float[8];
        for (int i = 0; i < 8; i++) {
            float ang = i * (float) (Math.PI / 4.0);
            float x = (float) Math.cos(ang) * s, y = (float) Math.sin(ang) * s;
            px[i] = cx + ar.x * x + au.x * y;
            py[i] = cy + ar.y * x + au.y * y;
            pz[i] = cz + ar.z * x + au.z * y;
        }
        for (int k = 0; k < 4; k++) {
            int a0 = 2 * k, a1 = 2 * k + 1, a2 = (2 * k + 2) % 8;
            dquad(q, m, cx, cy, cz, col, px[a0], py[a0], pz[a0], col, px[a1], py[a1], pz[a1], col, px[a2], py[a2], pz[a2], col);
        }
    }

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
