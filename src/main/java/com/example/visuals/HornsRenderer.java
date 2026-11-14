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
 * Рога: два маленьких изогнутых рога демонического козлёнка.
 *  - загибаются назад и слегка расходятся в стороны, сужаются к острию;
 *  - рёбра-кольца по длине (как у настоящих козлиных рогов);
 *  - «Раскал»: кончики накаляются до оранжево-жёлтого и пульсируют светящимся ореолом.
 * Поворачиваются вместе с головой (поворот и наклон). Рисуются из WorldRendererMixin.
 */
public final class HornsRenderer {
    private HornsRenderer() {}

    // Названия для меню (0-3 = HORN_COLORS, 4 = радуга)
    public static final String[] COLOR_NAMES = {"Багровый", "Чёрный", "Кость", "Фиолетовый", "Радуга"};
    private static final int[] HORN_COLORS = {0xB01818, 0x2A2530, 0xE8DFC8, 0x7A2BC4};

    private static final int EMBER_MID = 0xFF4A12;   // раскалённый, у середины
    private static final int EMBER_TIP = 0xFFC23A;   // раскалённый, у самого острия

    // --- геометрия (в системе головы: центр головы = начало координат, +Z = вперёд, +Y = вверх) ---
    private static final int   K = 16;          // сегментов по длине рога
    private static final int   S = 8;           // граней в сечении
    private static final float BASE_X = 0.14f;  // основание рога: в сторону от центра
    private static final float BASE_Y = 0.21f;  // чуть утоплено в верх черепа
    private static final float BASE_Z = 0.04f;  // чуть вперёд от центра
    private static final float CURVE_R = 0.30f; // радиус изгиба
    private static final float SWEEP = 1.25f;   // на сколько радиан загибается назад (~72°)
    private static final float SPLAY = 0.13f;   // на сколько рога расходятся в стороны к кончику
    private static final float BASE_RADIUS = 0.058f;
    private static final float HEAD_DROP = 0.16f;  // центр головы ниже верха хитбокса
    private static final double MAX_DIST = 64.0;

    // --- заранее посчитанный рог (правый, side = +1; левый получаем зеркалом по X) ---
    private static final float[] PX = new float[K + 1], PY = new float[K + 1], PZ = new float[K + 1];   // ось рога (единичный размер, от основания)
    private static final float[] N1Y = new float[K + 1], N1Z = new float[K + 1];                         // 1-я нормаль сечения (x = 0)
    private static final float[] N2X = new float[K + 1], N2Y = new float[K + 1], N2Z = new float[K + 1]; // 2-я нормаль сечения
    private static final float[] PROF = new float[K + 1];                                                // радиус по длине (0..1)
    private static final float[] RIDGE = new float[K + 1];                                               // «рёбра» -1..1
    private static final float[] CS = new float[S + 1], SN = new float[S + 1];

    static {
        for (int k = 0; k <= K; k++) {
            float t = (float) k / K;
            float a = t * SWEEP;
            PX[k] = SPLAY * (float) Math.pow(t, 1.4);
            PY[k] = CURVE_R * (float) Math.sin(a);
            PZ[k] = -CURVE_R * (1f - (float) Math.cos(a));

            // касательная
            float tx = SPLAY * 1.4f * (float) Math.pow(Math.max(t, 1e-4f), 0.4);
            float ty = CURVE_R * (float) Math.cos(a) * SWEEP;
            float tz = -CURVE_R * (float) Math.sin(a) * SWEEP;
            float tl = (float) Math.sqrt(tx * tx + ty * ty + tz * tz);
            tx /= tl; ty /= tl; tz /= tl;

            // N1 = T x (1,0,0) = (0, tz, -ty), нормируем
            float n1y = tz, n1z = -ty;
            float nl = (float) Math.sqrt(n1y * n1y + n1z * n1z);
            n1y /= nl; n1z /= nl;
            N1Y[k] = n1y; N1Z[k] = n1z;

            // N2 = T x N1  (N1.x = 0)
            float n2x = ty * n1z - tz * n1y;
            float n2y = -tx * n1z;
            float n2z = tx * n1y;
            float l2 = (float) Math.sqrt(n2x * n2x + n2y * n2y + n2z * n2z);
            N2X[k] = n2x / l2; N2Y[k] = n2y / l2; N2Z[k] = n2z / l2;

            RIDGE[k] = (float) Math.sin(t * Math.PI * 2.0 * 4.5);
            PROF[k] = (float) Math.pow(1f - t, 0.85) * (1f + 0.10f * RIDGE[k] * (1f - t));
        }
        for (int j = 0; j <= S; j++) {
            double b = j * (Math.PI * 2.0) / S;
            CS[j] = (float) Math.cos(b);
            SN[j] = (float) Math.sin(b);
        }
    }

    // буферы под вершины одного рога (рендер однопоточный)
    private static final float[] VX = new float[(K + 1) * (S + 1)];
    private static final float[] VY = new float[(K + 1) * (S + 1)];
    private static final float[] VZ = new float[(K + 1) * (S + 1)];
    private static final int[]   VC = new int[(K + 1) * (S + 1)];

    public static void render(class_4184 camera, Matrix4f positionMatrix,
                              class_4597.class_4598 consumers, float tickProgress) {
        class_310 mc = class_310.method_1551();
        if (!Config.horns || mc.field_1687 == null || mc.field_1724 == null) return;

        class_243 cam = camera.method_71156();
        int rgb = pickColor();
        float size = (float) Math.max(0.1, Config.hornsSize);

        double time = System.currentTimeMillis() / 1000.0;
        float pulse = 0.78f + 0.22f * (float) Math.sin(time * 2.6)
                + 0.06f * (float) Math.sin(time * 7.3);               // мерцание углей

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
                ms.method_22904(dx, dy + p.method_17682() - HEAD_DROP * scale, dz);   // центр головы
                ms.method_22905(scale, scale, scale);
                Matrix4f m = ms.method_23760().method_23761();
                // поворот вслед за взглядом: сначала наклон (X), потом поворот (Y)
                m.rotateY((float) Math.toRadians(-p.method_36454()));
                m.rotateX((float) Math.toRadians(p.method_36455()));

                drawHorns(m, consumers, rgb, size, pulse);
                drew = true;
            }
            if (drew) {
                consumers.method_22994(class_12249.method_76023());
            }
        } finally {
            mv.popMatrix();
        }
    }

    private static void drawHorns(Matrix4f m, class_4597.class_4598 consumers, int rgb, float size, float pulse) {
        class_4588 q = consumers.method_73477(class_12249.method_76023());
        boolean ember = Config.hornsGlow;
        for (int side = -1; side <= 1; side += 2) {
            // 1) сам рог (непрозрачный)
            build(side, size, rgb, ember, pulse, false);
            emit(q, m, side, 0);
            // 2) раскалённый ореол вокруг кончика (полупрозрачный, поверх)
            if (ember) {
                build(side, size, rgb, true, pulse, true);
                emit(q, m, side, K / 2 - 1);
            }
        }
    }

    /** Считает вершины одного рога в буферы VX/VY/VZ/VC. glowPass = только ореол у кончика. */
    private static void build(int side, float size, int rgb, boolean ember, float pulse, boolean glowPass) {
        int kStart = glowPass ? K / 2 - 1 : 0;
        for (int k = kStart; k <= K; k++) {
            float t = (float) k / K;
            float cx = BASE_X + PX[k] * size;                    // ось (для правого рога)
            float cy = BASE_Y + PY[k] * size;
            float cz = BASE_Z + PZ[k] * size;

            float r = PROF[k] * BASE_RADIUS * size;
            if (glowPass) r = r * 1.7f + 0.028f * size * pulse * (1f - t * 0.6f);

            // цвет по длине: тело с «освещением» -> раскал к кончику
            float heat = ember ? smooth(0.45f, 1.0f, t) : 0f;
            int tipCol = mixRgb(EMBER_MID, EMBER_TIP, smooth(0.75f, 1.0f, t));

            for (int j = 0; j <= S; j++) {
                float c = CS[j], s = SN[j];
                // нормаль сечения (в пространстве правого рога)
                float nx = s * N2X[k];
                float ny = c * N1Y[k] + s * N2Y[k];
                float nz = c * N1Z[k] + s * N2Z[k];

                float vx = cx + nx * r, vy = cy + ny * r, vz = cz + nz * r;
                int idx = k * (S + 1) + j;
                VX[idx] = side * vx;                               // зеркало для левого рога
                VY[idx] = vy;
                VZ[idx] = vz;

                if (glowPass) {
                    int a = (int) (80f * pulse * smooth(0.45f, 1.0f, t));
                    VC[idx] = (clamp255(a) << 24) | tipCol;
                } else {
                    // простое освещение: свет сверху-спереди-сбоку
                    float dot = nx * 0.35f + ny * 0.75f + nz * 0.55f;   // |L| ~ 1
                    float shade = 0.50f + 0.50f * Math.max(0f, dot);
                    shade *= 0.90f + 0.10f * RIDGE[k];                  // впадины между рёбрами темнее
                    int col = scaleRgb(rgb, shade);
                    if (heat > 0f) col = mixRgb(col, tipCol, heat * (0.85f + 0.15f * pulse));
                    VC[idx] = (255 << 24) | col;
                }
            }
        }
    }

    private static void emit(class_4588 q, Matrix4f m, int side, int kStart) {
        for (int k = kStart; k < K; k++) {
            for (int j = 0; j < S; j++) {
                int a = k * (S + 1) + j, b = (k + 1) * (S + 1) + j;
                int c = (k + 1) * (S + 1) + j + 1, d = k * (S + 1) + j + 1;
                if (side > 0) { v(q, m, a); v(q, m, b); v(q, m, c); v(q, m, d); }
                else          { v(q, m, d); v(q, m, c); v(q, m, b); v(q, m, a); }   // зеркало меняет обход
            }
        }
    }

    private static void v(class_4588 q, Matrix4f m, int i) {
        q.method_22918(m, VX[i], VY[i], VZ[i]).method_39415(VC[i]);
    }

    // ---------------------------------------------------------------- цвет

    private static float smooth(float e0, float e1, float x) {
        float t = Math.max(0f, Math.min(1f, (x - e0) / (e1 - e0)));
        return t * t * (3f - 2f * t);
    }

    private static int clamp255(int v) { return Math.max(0, Math.min(255, v)); }

    private static int scaleRgb(int rgb, float k) {
        int r = clamp255((int) (((rgb >> 16) & 0xFF) * k));
        int g = clamp255((int) (((rgb >> 8) & 0xFF) * k));
        int b = clamp255((int) ((rgb & 0xFF) * k));
        return (r << 16) | (g << 8) | b;
    }

    private static int mixRgb(int a, int b, float t) {
        int r = (int) (((a >> 16) & 0xFF) * (1f - t) + ((b >> 16) & 0xFF) * t);
        int g = (int) (((a >> 8) & 0xFF) * (1f - t) + ((b >> 8) & 0xFF) * t);
        int bl = (int) ((a & 0xFF) * (1f - t) + (b & 0xFF) * t);
        return (r << 16) | (g << 8) | bl;
    }

    private static int pickColor() {
        if (Config.hornsCustom) {
            return class_3532.method_15369((float) Config.hornsHue, (float) Config.hornsSat, (float) Config.hornsVal) & 0xFFFFFF;
        }
        int mode = Config.hornsMode;
        if (mode >= 0 && mode < HORN_COLORS.length) return HORN_COLORS[mode];
        float hue = (System.currentTimeMillis() % 4000L) / 4000f;   // радуга
        return class_3532.method_15369(hue, 0.8f, 0.85f) & 0xFFFFFF;
    }
}
