package com.example.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
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
 * 3D China Hat: полупрозрачный конус над головой + яркий обод.
 * Рисуется из WorldRendererMixin в конце WorldRenderer.render().
 */
public final class ChinaHatRenderer {
    private ChinaHatRenderer() {}

    // Названия для меню (0-4 = Config.COLORS, 5 = радуга)
    public static final String[] COLOR_NAMES = {"Фиолетовый", "Голубой", "Зелёный", "Жёлтый", "Красный", "Радуга"};

    // --- настройки внешнего вида ---
    private static final int   SEGMENTS   = 48;
    private static final float RADIUS     = 0.62f;  // радиус основания
    private static final float HEIGHT     = 0.32f;  // высота конуса
    private static final float LIFT       = 0.04f;  // насколько выше головы
    private static final float RIM_WIDTH  = 0.05f;  // ширина обода
    private static final int   ALPHA_TIP  = 190;    // прозрачность у вершины (0-255)
    private static final int   ALPHA_EDGE = 110;    // прозрачность у основания
    private static final int   ALPHA_RIM  = 235;    // прозрачность обода
    private static final double MAX_DIST  = 64.0;

    public static void render(Camera camera, Matrix4f positionMatrix,
                              VertexConsumerProvider.Immediate consumers, float tickProgress) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!Config.chinaHat || mc.world == null || mc.player == null) return;

        Vec3d cam = camera.getCameraPos();
        int rgb = pickColor();

        Matrix4fStack mv = RenderSystem.getModelViewStack();
        mv.pushMatrix();
        mv.mul(positionMatrix);
        try {
            boolean drew = false;
            for (PlayerEntity p : mc.world.getPlayers()) {
                boolean self = p == mc.player;
                if (!self && !Config.friends.contains(p.getName().getString())) continue;
                if (self && !camera.isThirdPerson()) continue;      // от первого лица не рисуем
                if (p.isSpectator() || p.isInvisible()) continue;

                float scale = p.getScale();
                if (p.getHeight() < 1.0f * scale) continue;          // плавает/ползёт/спит — голова не сверху

                double x = MathHelper.lerp((double) tickProgress, p.lastX, p.getX());
                double y = MathHelper.lerp((double) tickProgress, p.lastY, p.getY());
                double z = MathHelper.lerp((double) tickProgress, p.lastZ, p.getZ());

                double dx = x - cam.x, dy = y - cam.y, dz = z - cam.z;
                if (dx * dx + dy * dy + dz * dz > MAX_DIST * MAX_DIST) continue;

                MatrixStack ms = new MatrixStack();
                ms.translate(dx, dy + p.getHeight() + LIFT * scale, dz);
                ms.scale(scale, scale, scale);
                drawHat(ms.peek().getPositionMatrix(), consumers, rgb);
                drew = true;
            }
            if (drew) {
                consumers.draw(RenderLayers.debugTriangleFan());
                consumers.draw(RenderLayers.debugQuads());
            }
        } finally {
            mv.popMatrix();
        }
    }

    private static void drawHat(Matrix4f m, VertexConsumerProvider.Immediate consumers, int rgb) {
        int tip  = (ALPHA_TIP  << 24) | rgb;
        int edge = (ALPHA_EDGE << 24) | rgb;
        int rim  = (ALPHA_RIM  << 24) | rgb;

        // 1) заливка конуса (triangle fan)
        VertexConsumer fan = consumers.getBuffer(RenderLayers.debugTriangleFan());
        fan.vertex(m, 0f, HEIGHT, 0f).color(tip);
        for (int i = 0; i <= SEGMENTS; i++) {
            float a = (float) (i * (Math.PI * 2.0) / SEGMENTS);
            fan.vertex(m, MathHelper.cos(a) * RADIUS, 0f, MathHelper.sin(a) * RADIUS).color(edge);
        }
        // слой сменится ниже -> фан отрисуется сам

        // 2) обод: плоское кольцо + вертикальная полоска снаружи
        VertexConsumer q = consumers.getBuffer(RenderLayers.debugQuads());
        float r0 = RADIUS, r1 = RADIUS + RIM_WIDTH, h0 = -0.03f, h1 = 0.0f;
        for (int i = 0; i < SEGMENTS; i++) {
            float a0 = (float) (i * (Math.PI * 2.0) / SEGMENTS);
            float a1 = (float) ((i + 1) * (Math.PI * 2.0) / SEGMENTS);
            float c0 = MathHelper.cos(a0), s0 = MathHelper.sin(a0);
            float c1 = MathHelper.cos(a1), s1 = MathHelper.sin(a1);

            // кольцо
            q.vertex(m, c0 * r0, h1, s0 * r0).color(rim);
            q.vertex(m, c0 * r1, h1, s0 * r1).color(rim);
            q.vertex(m, c1 * r1, h1, s1 * r1).color(rim);
            q.vertex(m, c1 * r0, h1, s1 * r0).color(rim);

            // боковая полоска
            q.vertex(m, c0 * r1, h0, s0 * r1).color(rim);
            q.vertex(m, c0 * r1, h1, s0 * r1).color(rim);
            q.vertex(m, c1 * r1, h1, s1 * r1).color(rim);
            q.vertex(m, c1 * r1, h0, s1 * r1).color(rim);
        }
    }

    private static int pickColor() {
        int mode = Config.chinaHatMode;
        if (mode >= 0 && mode < Config.COLORS.length) return Config.COLORS[mode] & 0xFFFFFF;
        float hue = (System.currentTimeMillis() % 4000L) / 4000f;   // радуга
        return MathHelper.hsvToRgb(hue, 0.65f, 1.0f) & 0xFFFFFF;
    }
}
