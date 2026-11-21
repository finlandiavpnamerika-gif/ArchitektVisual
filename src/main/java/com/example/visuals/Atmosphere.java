package com.example.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.class_12249;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_638;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;

/**
 * Атмосфера (версия 1): "только что прошёл дождь" - мокрый пол.
 *
 * Как это работает (тот же приём, что у Sky Shader и JumpCircle - геометрия в мире после рендера кадра):
 *  1. Раз в ~2.5 с (кусками по несколько сотен колонок за тик, без лагов) вокруг игрока собираются
 *     верхние грани блоков, на которые падал бы дождь (над ними открытое небо).
 *  2. Каждый кадр поверх каждой такой грани рисуются два полупрозрачных слоя:
 *       A - тёмная "мокрая" плёнка (камень/земля после дождя темнее);
 *       B - отражение неба (цвет зависит от времени суток и погоды), которое усиливается при
 *           взгляде под малым углом к полу (эффект Френеля), плюс блик от солнца/луны.
 *  3. Лужи - плавные пятна шума: цвет и прозрачность считаются в углах блока, поэтому края
 *     луж мягкие, а не кубиками. Во время дождя в лужах расходятся круги от капель.
 * Работает только в Верхнем мире. Грани выше уровня глаз не рисуются (их всё равно не видно).
 */
public final class Atmosphere {
    private Atmosphere() {}

    // ------------------------------------------------------------------ кэш поверхностей

    private static final class Snap {
        final int[] bx, bz;
        final float[] y, x0, x1, z0, z1;
        int n;
        Snap(int cap) {
            bx = new int[cap]; bz = new int[cap];
            y = new float[cap]; x0 = new float[cap]; x1 = new float[cap]; z0 = new float[cap]; z1 = new float[cap];
        }
    }

    private static final int COLS_PER_TICK = 450;     // сколько колонок обрабатывать за тик
    private static final int REBUILD_TICKS = 50;      // как часто обновлять кэш, если игрок стоит
    private static final int SCAN_DOWN = 48;          // насколько ниже глаз искать землю
    private static final int SCAN_UP = 48;            // насколько выше земли искать крышу

    private static Snap live = new Snap(0);
    private static Snap build;
    private static boolean building;
    private static int buildCx, buildCz, buildTop, buildR, buildRow;
    private static int liveCx, liveCz, liveTop, liveR = -1;
    private static int sinceBuild;
    private static final class_2338.class_2339 POS = new class_2338.class_2339();

    public static boolean active() {
        return Config.atmosphere && Config.atmoWet;
    }

    /** Каждый тик клиента. */
    public static void tick(class_310 mc) {
        class_638 w = mc.field_1687;
        if (!active() || w == null || mc.field_1724 == null || w.method_27983() != class_1937.field_25179) {
            if (live.n != 0) live = new Snap(0);
            building = false;
            build = null;
            liveR = -1;
            return;
        }

        sinceBuild++;
        int R = (int) Math.max(8, Math.min(40, Config.atmoRadius));
        int px = (int) Math.floor(mc.field_1724.method_23317());
        int pz = (int) Math.floor(mc.field_1724.method_23321());
        int top = (int) Math.floor(mc.field_1724.method_23318() + mc.field_1724.method_17682() * 0.85);

        if (!building) {
            boolean stale = liveR < 0 || R != liveR
                    || Math.abs(px - liveCx) > 3 || Math.abs(pz - liveCz) > 3 || Math.abs(top - liveTop) > 2
                    || sinceBuild >= REBUILD_TICKS;
            if (stale) {
                building = true;
                buildCx = px; buildCz = pz; buildTop = top; buildR = R; buildRow = 0;
                int cap = (2 * R + 1) * (2 * R + 1);
                build = new Snap(cap);
            }
        }
        if (building) step(w);
    }

    private static void step(class_638 w) {
        int done = 0;
        int R = buildR;
        while (buildRow <= 2 * R && done < COLS_PER_TICK) {
            int x = buildCx - R + buildRow;
            int dx = x - buildCx;
            for (int z = buildCz - R; z <= buildCz + R; z++) {
                int dz = z - buildCz;
                if (dx * dx + dz * dz > R * R) continue;
                done++;
                scanColumn(w, x, z);
            }
            buildRow++;
        }
        if (buildRow > 2 * R) {
            live = build;
            liveCx = buildCx; liveCz = buildCz; liveTop = buildTop; liveR = buildR;
            build = null;
            building = false;
            sinceBuild = 0;
        }
    }

    /** Ищет сверху вниз первую твёрдую грань под открытым небом и кладёт её в build. */
    private static void scanColumn(class_638 w, int x, int z) {
        int minY = w.method_31607();
        int maxY = minY + w.method_31605() - 1;
        int yHigh = Math.min(maxY, buildTop);
        int yLow = Math.max(minY, buildTop - SCAN_DOWN);

        for (int y = yHigh; y >= yLow; y--) {
            POS.method_10103(x, y, z);
            class_2680 st = w.method_8320(POS);
            if (st.method_26215()) continue;
            if (!st.method_26227().method_15769()) return;          // вода/лава - пропускаем колонку
            class_265 shape = st.method_26220(w, POS);
            if (shape.method_1110()) continue;                      // трава, факелы и т.п. - сквозь них

            // верхняя грань: самый высокий бокс формы (у ступеней - верхняя ступень)
            final float[] best = {0, 0, 0, 0, -1f, 0};
            shape.method_1089((x1, y1, z1, x2, y2, z2) -> {
                float ny = (float) y2;
                float area = (float) ((x2 - x1) * (z2 - z1));
                float cur = (best[3] - best[0]) * (best[5] - best[2]);
                if (ny > best[4] + 1e-5f || (Math.abs(ny - best[4]) <= 1e-5f && area > cur)) {
                    best[0] = (float) x1; best[1] = (float) y1; best[2] = (float) z1;
                    best[3] = (float) x2; best[4] = ny; best[5] = (float) z2;
                }
            });
            if ((best[3] - best[0]) * (best[5] - best[2]) < 0.1f) return;   // заборы, прутья и прочая мелочь

            // над гранью должно быть открытое небо
            int yMax = Math.min(maxY, y + SCAN_UP);
            for (int yy = y + 1; yy <= yMax; yy++) {
                POS.method_10103(x, yy, z);
                class_2680 s2 = w.method_8320(POS);
                if (s2.method_26215()) continue;
                if (!s2.method_26227().method_15769()) return;
                if (!s2.method_26220(w, POS).method_1110()) return;
            }

            Snap s = build;
            int i = s.n++;
            s.bx[i] = x; s.bz[i] = z;
            s.y[i] = y + best[4];
            s.x0[i] = x + best[0]; s.x1[i] = x + best[3];
            s.z0[i] = z + best[2]; s.z1[i] = z + best[5];
            return;
        }
    }

    // ------------------------------------------------------------------ шум

    private static float hash(int x, int z, int s) {
        int h = x * 374761393 + z * 668265263 + s * 1274126177;
        h = (h ^ (h >>> 13)) * 1274126177;
        h ^= h >>> 16;
        return (h & 0xFFFFFF) / 16777216f;
    }

    private static float vnoise(float x, float z, int s) {
        int xi = (int) Math.floor(x), zi = (int) Math.floor(z);
        float fx = x - xi, fz = z - zi;
        float u = fx * fx * (3f - 2f * fx), v = fz * fz * (3f - 2f * fz);
        float a = hash(xi, zi, s), b = hash(xi + 1, zi, s), c = hash(xi, zi + 1, s), d = hash(xi + 1, zi + 1, s);
        return (a + (b - a) * u) + ((c + (d - c) * u) - (a + (b - a) * u)) * v;
    }

    private static float smooth(float e0, float e1, float x) {
        float t = clamp01((x - e0) / (e1 - e0));
        return t * t * (3f - 2f * t);
    }

    private static float clamp01(float v) { return v < 0f ? 0f : (v > 1f ? 1f : v); }

    /** Маска луж 0..1 в точке мира: плавные пятна (два слоя шума). */
    private static float puddle(float x, float z) {
        float n = 0.62f * vnoise(x * 0.16f, z * 0.16f, 1) + 0.38f * vnoise(x * 0.41f, z * 0.41f, 2);
        float thr = 0.74f - 0.34f * (float) Config.atmoPuddleAmount;
        return smooth(thr - 0.07f, thr + 0.09f, n);
    }

    // ------------------------------------------------------------------ рендер

    // константы кадра
    private static float fCx, fCy, fCz, fR, fT, fWet, fGloss, fLight, fLx, fLy, fLz;
    private static boolean fPuddles, fGlossOn;
    private static float fSkyR, fSkyG, fSkyB, fSpR, fSpG, fSpB;
    // результат corner()
    private static int outA, outB;

    private static int pack(float a, float r, float g, float b) {
        int ia = (int) (clamp01(a) * 255f + 0.5f), ir = (int) (clamp01(r) * 255f + 0.5f);
        int ig = (int) (clamp01(g) * 255f + 0.5f), ib = (int) (clamp01(b) * 255f + 0.5f);
        return (ia << 24) | (ir << 16) | (ig << 8) | ib;
    }

    public static void render(class_4184 camera, Matrix4f positionMatrix,
                              class_4597.class_4598 consumers, float tickProgress) {
        class_310 mc = class_310.method_1551();
        class_638 w = mc.field_1687;
        if (!active() || w == null || mc.field_1724 == null) return;
        if (w.method_27983() != class_1937.field_25179) return;
        Snap s = live;
        if (s.n == 0) return;

        class_243 cam = camera.method_71156();
        fCx = (float) cam.field_1352; fCy = (float) cam.field_1351; fCz = (float) cam.field_1350;
        fR = (float) Math.max(8, Math.min(40, Config.atmoRadius));
        fT = (System.currentTimeMillis() % 1000000L) / 1000f;
        fWet = (float) Config.atmoWetness;
        fPuddles = Config.atmoPuddles;
        fGlossOn = Config.atmoGloss;
        fGloss = (float) Config.atmoGlossPower;

        // --- небо и свет: время суток и дождь
        float rain = w.method_8430(tickProgress);
        double tod = (w.method_8532() % 24000L) + tickProgress;
        float ang = (float) (tod / 24000.0 * Math.PI * 2.0);
        float sunX = (float) Math.cos(ang), sunY = (float) Math.sin(ang);        // 0 = восход (+X), 6000 = зенит
        boolean sunUp = sunY > -0.08f;
        fLx = sunUp ? sunX : -sunX;
        fLy = sunUp ? sunY : -sunY;
        fLz = 0.18f;
        float ln = (float) Math.sqrt(fLx * fLx + fLy * fLy + fLz * fLz);
        fLx /= ln; fLy /= ln; fLz /= ln;
        fLight = sunUp ? smooth(-0.08f, 0.14f, sunY) : 0.55f * smooth(0.10f, 0.40f, -sunY);
        fLight *= 1f - 0.85f * rain;                                             // под тучами солнца нет

        float day = smooth(-0.10f, 0.28f, sunY);
        float dusk = (float) Math.exp(-Math.pow(sunY / 0.20f, 2.0)) * 0.75f * (1f - rain);
        float skyR = lerp(0.07f, 0.66f, day), skyG = lerp(0.10f, 0.76f, day), skyB = lerp(0.20f, 0.90f, day);
        skyR = lerp(skyR, 1.00f, dusk); skyG = lerp(skyG, 0.58f, dusk); skyB = lerp(skyB, 0.38f, dusk);
        float gray = (0.10f + 0.50f * day);
        fSkyR = lerp(skyR, gray, rain * 0.8f); fSkyG = lerp(skyG, gray * 1.03f, rain * 0.8f); fSkyB = lerp(skyB, gray * 1.1f, rain * 0.8f);
        if (sunUp) { fSpR = 1.00f; fSpG = 0.95f - 0.20f * dusk; fSpB = 0.82f - 0.35f * dusk; }
        else { fSpR = 0.72f; fSpG = 0.84f; fSpB = 1.00f; }

        // взгляд камеры: отсекаем то, что сильно позади
        Vector3f fwd = new Vector3f(0f, 0f, -1f).rotate(camera.method_23767());

        Matrix4f m = new Matrix4f();
        Matrix4fStack mv = RenderSystem.getModelViewStack();
        mv.pushMatrix();
        mv.mul(positionMatrix);
        try {
            class_4588 q = consumers.method_73477(class_12249.method_76023());
            final float R2 = fR * fR;
            int rippleBudget = (Config.atmoRipples && rain > 0.15f) ? 160 : 0;

            for (int i = 0; i < s.n; i++) {
                float topY = s.y[i];
                if (topY > fCy - 0.02f) continue;                                  // выше глаз - снизу не видно

                float mx = (s.x0[i] + s.x1[i]) * 0.5f - fCx, mz = (s.z0[i] + s.z1[i]) * 0.5f - fCz;
                float h2 = mx * mx + mz * mz;
                if (h2 > R2) continue;
                float my = topY - fCy;
                if (fwd.x * mx + fwd.y * my + fwd.z * mz < -2.5f) continue;       // за спиной

                float dist = (float) Math.sqrt(h2 + my * my);
                float y = topY + 0.004f + dist * 0.0005f;                          // чуть над блоком, дальше - выше (против z-fighting)
                float ax = s.x0[i], bx = s.x1[i], az = s.z0[i], bz = s.z1[i];

                float rx0 = ax - fCx, rx1 = bx - fCx, rz0 = az - fCz, rz1 = bz - fCz, ry = y - fCy;

                corner(ax, y, az); int a00 = outA, b00 = outB;
                corner(bx, y, az); int a10 = outA, b10 = outB;
                corner(bx, y, bz); int a11 = outA, b11 = outB;
                corner(ax, y, bz); int a01 = outA, b01 = outB;

                if (((a00 | a10 | a11 | a01) >>> 24) != 0)
                    WorldDraw.quad2(q, m, rx0, ry, rz0, a00, rx1, ry, rz0, a10, rx1, ry, rz1, a11, rx0, ry, rz1, a01);
                if (((b00 | b10 | b11 | b01) >>> 24) != 0) {
                    float y2 = ry + 0.0035f;
                    WorldDraw.quad2(q, m, rx0, y2, rz0, b00, rx1, y2, rz0, b10, rx1, y2, rz1, b11, rx0, y2, rz1, b01);
                }

                // круги от капель во время дождя (только в лужах и рядом)
                if (rippleBudget > 0 && h2 < 196f) {
                    rippleBudget -= ripple(q, m, s.bx[i], s.bz[i], topY, rain);
                }
            }
            consumers.method_22994(class_12249.method_76023());
        } finally {
            mv.popMatrix();
        }
    }

    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }

    /** Считает цвета двух слоёв (A - тёмная плёнка, B - отражение/блик) в одной вершине. */
    private static void corner(float wx, float wy, float wz) {
        float dx = wx - fCx, dy = wy - fCy, dz = wz - fCz;
        float hd2 = dx * dx + dz * dz;
        float dist = (float) Math.sqrt(hd2 + dy * dy);
        if (dist < 1e-3f) dist = 1e-3f;
        float edge = clamp01((fR - (float) Math.sqrt(hd2)) / (fR * 0.45f));      // мягкий край у границы дальности
        if (edge <= 0f) { outA = 0; outB = 0; return; }

        float cosInc = clamp01(-dy / dist);                                       // 1 - смотрим вниз, 0 - вдоль пола
        float g = 1f - cosInc;
        float fres = g * g * (float) Math.sqrt(Math.sqrt(g));                    // ~ (1-cos)^2.25

        float pud = fPuddles ? puddle(wx, wz) : 0f;
        float stain = 0.80f + 0.20f * vnoise(wx * 0.55f, wz * 0.55f, 7);

        // слой A: мокрая тёмная плёнка
        float aA = (0.20f * stain + 0.22f * pud) * fWet * edge;
        outA = pack(aA, 0.03f, 0.05f, 0.08f);

        // слой B: отражение неба
        float sheen = ((0.12f + 0.55f * pud) * fres + 0.16f * pud) * fWet;

        float spec = 0f;
        if (fGlossOn && fLight > 0.001f) {
            float rx = dx / dist, ry = -dy / dist, rz = dz / dist;                // отражённый луч взгляда
            float d = rx * fLx + ry * fLy + rz * fLz;
            if (d > 0f) {
                float d2 = d * d, d4 = d2 * d2, d8 = d4 * d4, d16 = d8 * d8, d32 = d16 * d16;
                float sharp = d32 * d4;                                           // d^36 - узкий блик (лужи)
                float broad = d8 * 0.20f;                                         // d^8  - широкий глянец мокрого камня
                float shimmer = 0.72f + 0.28f * (float) Math.sin(fT * 2.3f + wx * 4.1f + wz * 3.3f);
                spec = (sharp * (0.35f + 0.65f * pud) + broad * (0.35f + 0.65f * pud)) * shimmer * fLight * fGloss;
            }
        }

        float aB = clamp01(sheen + spec * 0.95f) * edge;
        float k = clamp01(spec * 2.2f);
        outB = pack(aB, lerp(fSkyR, fSpR, k), lerp(fSkyG, fSpG, k), lerp(fSkyB, fSpB, k));
    }

    /** Одна расходящаяся окружность от капли на блоке (bx, bz). Возвращает, сколько "бюджета" потрачено. */
    private static int ripple(class_4588 q, Matrix4f m, int bx, int bz, float topY, float rain) {
        final float period = 1.1f;
        float ph = hash(bx, bz, 3);
        float tt = fT / period + ph;
        int cyc = (int) Math.floor(tt);
        float f = tt - cyc;
        if (hash(bx, bz, cyc * 7 + 11) > 0.10f + 0.25f * rain) return 0;          // не в каждый цикл капля падает
        float px = bx + 0.15f + 0.7f * hash(bx, bz, cyc * 13 + 5);
        float pz = bz + 0.15f + 0.7f * hash(bx, bz, cyc * 13 + 9);
        if (puddle(px, pz) < 0.30f && fPuddles) return 0;                         // круги только в лужах

        float r1 = 0.06f + 0.42f * f, r0 = Math.max(0f, r1 - 0.07f);
        float a = (1f - f) * (1f - f) * 0.55f * rain;
        if (a < 0.02f) return 0;
        int c0 = pack(a * 0.25f, 0.85f, 0.92f, 1f), c1 = pack(a, 0.85f, 0.92f, 1f);
        float cx = px - fCx, cz = pz - fCz, cy = topY + 0.009f - fCy;
        final int N = 10;
        for (int i = 0; i < N; i++) {
            float s0 = i * (float) (Math.PI * 2 / N), s1 = (i + 1) * (float) (Math.PI * 2 / N);
            float x0 = (float) Math.cos(s0), z0 = (float) Math.sin(s0), x1 = (float) Math.cos(s1), z1 = (float) Math.sin(s1);
            WorldDraw.quad2(q, m,
                    cx + x0 * r0, cy, cz + z0 * r0, c0, cx + x0 * r1, cy, cz + z0 * r1, c1,
                    cx + x1 * r1, cy, cz + z1 * r1, c1, cx + x1 * r0, cy, cz + z1 * r0, c0);
        }
        return 1;
    }
}
