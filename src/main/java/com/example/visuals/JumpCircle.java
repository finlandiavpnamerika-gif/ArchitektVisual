package com.example.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

/** JumpCircle: расходящийся круг/кольцо на земле в месте прыжка (и приземления). */
public final class JumpCircle {
    private JumpCircle() {}

    public static final String[] STYLES = {"Кольцо", "Круг", "Двойное", "Звезда"};

    private static final class C {
        double x, y, z;
        long born;
        float hue;
        float spin;
    }

    private static final ArrayList<C> LIST = new ArrayList<>();
    private static boolean wasGround = true;
    private static double lastVy = 0;
    private static int rainbow = 0;

    private static void spawn(MinecraftClient mc) {
        C c = new C();
        c.x = mc.player.getX();
        c.y = mc.player.getY() + 0.02;
        c.z = mc.player.getZ();
        c.born = System.currentTimeMillis();
        c.hue = Config.jcRainbow ? ((rainbow++ * 0.13f) % 1f) : (float) Config.jcHue;
        c.spin = (float) (Math.random() * Math.PI * 2);
        if (LIST.size() > 40) LIST.remove(0);
        LIST.add(c);
    }

    /** Каждый тик клиента. */
    public static void tick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null || !Config.jumpCircle) { LIST.clear(); wasGround = true; return; }
        boolean ground = mc.player.isOnGround();
        double vy = mc.player.getVelocity().y;
        if (wasGround && !ground && vy > 0.1) spawn(mc);                          // прыжок
        if (Config.jcLand && !wasGround && ground && lastVy < -0.3) spawn(mc);    // приземление
        wasGround = ground;
        lastVy = vy;
    }

    public static void render(Camera camera, Matrix4f positionMatrix,
                              VertexConsumerProvider.Immediate consumers, float tickProgress) {
        if (LIST.isEmpty()) return;
        long now = System.currentTimeMillis();
        float lifeMs = (float) Config.jcLife * 1000f;
        LIST.removeIf(c -> now - c.born > lifeMs);
        if (LIST.isEmpty()) return;

        Vec3d cam = camera.getCameraPos();
        Matrix4f m = new org.joml.Matrix4f();
        Matrix4fStack mv = RenderSystem.getModelViewStack();
        mv.pushMatrix();
        mv.mul(positionMatrix);
        try {
            VertexConsumer q = consumers.getBuffer(RenderLayers.debugQuads());
            for (C c : LIST) {
                float t = (now - c.born) / lifeMs;                 // 0..1
                float e = 1f - (1f - t) * (1f - t) * (1f - t);     // easeOutCubic: быстро расходится, потом замедляется
                float fade = (float) Config.jcAlpha * (1f - t) * Math.min(1f, t * 12f);
                float R = (float) Config.jcSize * (0.25f + 0.75f * e);
                int rgb = MathHelper.hsvToRgb(Config.jcRainbow ? (c.hue + t * 0.25f) % 1f : c.hue, 0.7f, 1f) & 0xFFFFFF;
                float cx = (float) (c.x - cam.x), cy = (float) (c.y - cam.y), cz = (float) (c.z - cam.z);
                float w = (float) Config.jcWidth;

                switch (Config.jcStyle) {
                    case 1 -> ring(q, m, cx, cy, cz, 0f, R, rgb, fade * 0.05f, fade * 0.85f, 0f);   // сплошной круг, яркий к краю
                    case 2 -> {
                        ring(q, m, cx, cy, cz, R * (1f - w), R, rgb, fade * 0.1f, fade, 0f);
                        float R2 = R * 0.62f;
                        ring(q, m, cx, cy + 0.003f, cz, R2 * (1f - w), R2, rgb, fade * 0.05f, fade * 0.7f, 0f);
                    }
                    case 3 -> {
                        ring(q, m, cx, cy, cz, R * (1f - w), R, rgb, fade * 0.1f, fade, 0f);
                        star(q, m, cx, cy + 0.004f, cz, R * (1f - w), c.spin + t * 2.2f, rgb, fade * 0.8f);
                    }
                    default -> ring(q, m, cx, cy, cz, R * (1f - w), R, rgb, fade * 0.08f, fade, 0f);
                }
            }
            consumers.draw(RenderLayers.debugQuads());
        } finally {
            mv.popMatrix();
        }
    }

    private static int argb(float a, int rgb) {
        return (Math.max(0, Math.min(255, (int) (255 * a))) << 24) | (rgb & 0xFFFFFF);
    }

    /** Кольцо в плоскости XZ: внутренний радиус r0 (альфа a0), внешний r1 (альфа a1). */
    private static void ring(VertexConsumer q, Matrix4f m, float cx, float cy, float cz,
                             float r0, float r1, int rgb, float a0, float a1, float rot) {
        final int N = 56;
        int c0 = argb(a0, rgb), c1 = argb(a1, rgb);
        for (int i = 0; i < N; i++) {
            float s0 = rot + i * (float) (Math.PI * 2 / N), s1 = rot + (i + 1) * (float) (Math.PI * 2 / N);
            float x0 = (float) Math.cos(s0), z0 = (float) Math.sin(s0), x1 = (float) Math.cos(s1), z1 = (float) Math.sin(s1);
            quad2(q, m, cx + x0 * r0, cy, cz + z0 * r0, c0, cx + x0 * r1, cy, cz + z0 * r1, c1,
                        cx + x1 * r1, cy, cz + z1 * r1, c1, cx + x1 * r0, cy, cz + z1 * r0, c0);
        }
    }

    /** Четырёхлучевая звезда внутри круга. */
    private static void star(VertexConsumer q, Matrix4f m, float cx, float cy, float cz, float R, float rot, int rgb, float a) {
        int cc = argb(a * 0.0f, rgb), ce = argb(a, rgb);
        for (int k = 0; k < 4; k++) {
            float ang = rot + k * (float) (Math.PI / 2);
            float dx = (float) Math.cos(ang), dz = (float) Math.sin(ang);
            float px = -dz * R * 0.12f, pz = dx * R * 0.12f;
            quad2(q, m, cx + px, cy, cz + pz, cc, cx - px, cy, cz - pz, cc,
                        cx + dx * R * 0.95f, cy, cz + dz * R * 0.95f, ce, cx + dx * R * 0.95f, cy, cz + dz * R * 0.95f, ce);
        }
    }

    /** Четырёхугольник с обеими сторонами (чтобы не зависеть от отсечения). */
    private static void quad2(VertexConsumer q, Matrix4f m,
                              float ax, float ay, float az, int ac, float bx, float by, float bz, int bc,
                              float cx, float cy, float cz, int cc, float dx, float dy, float dz, int dc) {
        q.vertex(m, ax, ay, az).color(ac); q.vertex(m, bx, by, bz).color(bc);
        q.vertex(m, cx, cy, cz).color(cc); q.vertex(m, dx, dy, dz).color(dc);
        q.vertex(m, dx, dy, dz).color(dc); q.vertex(m, cx, cy, cz).color(cc);
        q.vertex(m, bx, by, bz).color(bc); q.vertex(m, ax, ay, az).color(ac);
    }
}
