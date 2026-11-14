package com.example.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
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

/**
 * Нимб (Angel Halo): светящийся золотой нимб (тор) над головой, плавно парит.
 * Рисуется из WorldRendererMixin так же, как China Hat.
 * Цвет, размер и высота настраиваются в меню (Config.halo*).
 */
public final class HaloRenderer {
    private HaloRenderer() {}

    // Названия для меню (0-3 = HALO_COLORS, 4 = радуга)
    public static final String[] COLOR_NAMES = {"Золотой", "Белый", "Голубой", "Розовый", "Радуга"};
    private static final int[] HALO_COLORS = {0xFFD54A, 0xFFFFFF, 0x7FDFFF, 0xFF9AD5};

    // --- внешний вид (множители размера/высоты из меню) ---
    private static final int    RING_SEGMENTS = 48;     // сегментов по кольцу
    private static final int    TUBE_SIDES    = 8;      // граней в сечении трубки
    private static final float  RING_RADIUS   = 0.30f;  // радиус кольца (при размере 1.0)
    private static final float  CORE_RADIUS   = 0.028f; // толщина яркой трубки
    private static final float  GLOW_RADIUS   = 0.075f; // толщина свечения вокруг
    private static final float  HOVER         = 0.24f;  // высота над макушкой (при высоте 1.0)
    private static final float  BOB_AMPLITUDE = 0.025f; // амплитуда «парения»
    private static final int    ALPHA_CORE    = 245;
    private static final int    ALPHA_GLOW    = 70;
    private static final double MAX_DIST      = 64.0;

    // таблицы sin/cos, чтобы не считать на каждый кадр
    private static final float[] RC = new float[RING_SEGMENTS + 1];
    private static final float[] RS = new float[RING_SEGMENTS + 1];
    private static final float[] TC = new float[TUBE_SIDES + 1];
    private static final float[] TS = new float[TUBE_SIDES + 1];
    static {
        for (int i = 0; i <= RING_SEGMENTS; i++) {
            double a = i * (Math.PI * 2.0) / RING_SEGMENTS;
            RC[i] = (float) Math.cos(a);
            RS[i] = (float) Math.sin(a);
        }
        for (int j = 0; j <= TUBE_SIDES; j++) {
            double b = j * (Math.PI * 2.0) / TUBE_SIDES;
            TC[j] = (float) Math.cos(b);
            TS[j] = (float) Math.sin(b);
        }
    }

    public static void render(class_4184 camera, Matrix4f positionMatrix,
                              class_4597.class_4598 consumers, float tickProgress) {
        class_310 mc = class_310.method_1551();
        if (!Config.halo || mc.field_1687 == null || mc.field_1724 == null) return;

        class_243 cam = camera.method_71156();
        int rgb = pickColor();
        float size = (float) Math.max(0.1, Config.haloSize);
        float height = (float) Math.max(0.1, Config.haloHeight);

        double time = System.currentTimeMillis() / 1000.0;
        float bob = Config.haloBob ? (float) Math.sin(time * 2.0) * BOB_AMPLITUDE : 0f;
        float pulse = 0.85f + 0.15f * (float) Math.sin(time * 3.0);   // лёгкое мерцание свечения

        Matrix4fStack mv = RenderSystem.getModelViewStack();
        mv.pushMatrix();
        mv.mul(positionMatrix);
        try {
            boolean drew = false;
            for (class_1657 p : mc.field_1687.method_18456()) {
                boolean self = p == mc.field_1724;
                if (!self && !Config.friends.contains(p.method_5477().getString())) continue;
                if (self && !camera.method_19333()) continue;      // от первого лица не рисуем
                if (p.method_7325() || p.method_5767()) continue;

                float scale = p.method_55693();
                if (p.method_17682() < 1.0f * scale) continue;          // плавает/ползёт/спит — голова не сверху

                double x = class_3532.method_16436((double) tickProgress, p.field_6014, p.method_23317());
                double y = class_3532.method_16436((double) tickProgress, p.field_6036, p.method_23318());
                double z = class_3532.method_16436((double) tickProgress, p.field_5969, p.method_23321());

                double dx = x - cam.field_1352, dy = y - cam.field_1351, dz = z - cam.field_1350;
                if (dx * dx + dy * dy + dz * dz > MAX_DIST * MAX_DIST) continue;

                class_4587 ms = new class_4587();
                ms.method_22904(dx, dy + p.method_17682() + (HOVER * height + bob) * scale, dz);
                ms.method_22905(scale, scale, scale);
                drawHalo(ms.method_23760().method_23761(), consumers, rgb, size, pulse);
                drew = true;
            }
            if (drew) {
                consumers.method_22994(class_12249.method_76023());
            }
        } finally {
            mv.popMatrix();
        }
    }

    private static void drawHalo(Matrix4f m, class_4597.class_4598 consumers, int rgb,
                                 float size, float pulse) {
        class_4588 q = consumers.method_73477(class_12249.method_76023());
        float ringR = RING_RADIUS * size;

        // 1) яркое ядро (чуть светлее базового цвета) — рисуем первым
        tube(q, m, ringR, CORE_RADIUS * size, lighten(rgb, 0.35f), ALPHA_CORE, true);
        // 2) мягкое свечение вокруг — поверх, полупрозрачное
        tube(q, m, ringR, GLOW_RADIUS * size, rgb, (int) (ALPHA_GLOW * pulse), false);
    }

    /** Трубка (тор) в плоскости XZ вокруг оси Y. shaded = псевдо-освещение: верх ярче, низ темнее. */
    private static void tube(class_4588 q, Matrix4f m, float ringR, float tubeR,
                             int rgb, int alpha, boolean shaded) {
        int[] sideColor = new int[TUBE_SIDES];
        for (int j = 0; j < TUBE_SIDES; j++) {
            if (shaded) {
                float mid = (TS[j] + TS[j + 1]) * 0.5f;           // -1 (низ) .. +1 (верх)
                float k = 0.70f + 0.30f * (0.5f + 0.5f * mid);    // 0.70 .. 1.00
                sideColor[j] = (alpha << 24) | scale(rgb, k);
            } else {
                sideColor[j] = (alpha << 24) | rgb;
            }
        }

        for (int i = 0; i < RING_SEGMENTS; i++) {
            float c0 = RC[i], s0 = RS[i], c1 = RC[i + 1], s1 = RS[i + 1];
            for (int j = 0; j < TUBE_SIDES; j++) {
                float r0 = ringR + tubeR * TC[j],     y0 = tubeR * TS[j];
                float r1 = ringR + tubeR * TC[j + 1], y1 = tubeR * TS[j + 1];
                int col = sideColor[j];

                q.method_22918(m, c0 * r0, y0, s0 * r0).method_39415(col);
                q.method_22918(m, c1 * r0, y0, s1 * r0).method_39415(col);
                q.method_22918(m, c1 * r1, y1, s1 * r1).method_39415(col);
                q.method_22918(m, c0 * r1, y1, s0 * r1).method_39415(col);
            }
        }
    }

    private static int lighten(int rgb, float amount) {
        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        r += (int) ((255 - r) * amount);
        g += (int) ((255 - g) * amount);
        b += (int) ((255 - b) * amount);
        return (r << 16) | (g << 8) | b;
    }

    private static int scale(int rgb, float k) {
        int r = Math.min(255, (int) (((rgb >> 16) & 0xFF) * k));
        int g = Math.min(255, (int) (((rgb >> 8) & 0xFF) * k));
        int b = Math.min(255, (int) ((rgb & 0xFF) * k));
        return (r << 16) | (g << 8) | b;
    }

    private static int pickColor() {
        if (Config.haloCustom) {
            return class_3532.method_15369((float) Config.haloHue, (float) Config.haloSat, (float) Config.haloVal) & 0xFFFFFF;
        }
        int mode = Config.haloMode;
        if (mode >= 0 && mode < HALO_COLORS.length) return HALO_COLORS[mode];
        float hue = (System.currentTimeMillis() % 4000L) / 4000f;   // радуга
        return class_3532.method_15369(hue, 0.55f, 1.0f) & 0xFFFFFF;
    }
}
