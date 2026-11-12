package com.example.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Random;
import net.minecraft.class_12249;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

/**
 * Sky Shader: своё небо вместо ванильного.
 * Рисуется купол из цветных квадов вокруг камеры (после мира, с проверкой глубины, поэтому земля его закрывает).
 * Прозрачность купола у горизонта плавно уходит в ноль, чтобы ванильный горизонт и туман не давали шва.
 * Пресеты: Aurora (северное сияние), Galaxy (туманность и Млечный Путь), Purple Night (фиолетовое небо).
 * Цвета купола считаются один раз при смене пресета; каждый кадр анимируются поворот, мерцание звёзд и сияние.
 */
public final class SkyRenderer {
    private SkyRenderer() {}

    public static final String[] PRESETS = {
        "Aurora", "Galaxy", "Purple Night",
        "Sunset", "Blood Moon", "Nebula", "Synthwave", "Emerald Aurora"
    };

    private static final int   AZ = 64, EL = 18;               // сетка купола
    private static final float EL_MIN = -0.10f;                // чуть ниже горизонта (рад)
    private static final float EL_MAX = (float) (Math.PI / 2);
    private static final int   STARS = 1100;

    // направления вершин купола (единичные) и их цвета
    private static final float[] DX = new float[(AZ + 1) * (EL + 1)];
    private static final float[] DY = new float[(AZ + 1) * (EL + 1)];
    private static final float[] DZ = new float[(AZ + 1) * (EL + 1)];
    private static final int[]   DOME = new int[(AZ + 1) * (EL + 1)];
    private static int cachedPreset = -1;

    // звёзды: направление, оси квада, размер, фаза мерцания, цвет
    private static final float[] SX = new float[STARS], SY = new float[STARS], SZ = new float[STARS];
    private static final float[] SRX = new float[STARS], SRY = new float[STARS], SRZ = new float[STARS];
    private static final float[] SUX = new float[STARS], SUY = new float[STARS], SUZ = new float[STARS];
    private static final float[] SSIZE = new float[STARS], SPHASE = new float[STARS];
    private static final int[]   SCOL = new int[STARS];

    static {
        for (int j = 0; j <= EL; j++) {
            float el = EL_MIN + (EL_MAX - EL_MIN) * j / EL;
            for (int i = 0; i <= AZ; i++) {
                float az = (float) (Math.PI * 2.0) * i / AZ;
                int k = j * (AZ + 1) + i;
                DX[k] = (float) (Math.cos(el) * Math.cos(az));
                DY[k] = (float) Math.sin(el);
                DZ[k] = (float) (Math.cos(el) * Math.sin(az));
            }
        }

        Random rnd = new Random(1337);
        for (int s = 0; s < STARS; s++) {
            float y = 0.03f + 0.97f * rnd.nextFloat();
            float phi = (float) (rnd.nextFloat() * Math.PI * 2.0);
            float rr = (float) Math.sqrt(1f - y * y);
            float x = rr * (float) Math.cos(phi), z = rr * (float) Math.sin(phi);
            SX[s] = x; SY[s] = y; SZ[s] = z;

            // right = normalize(up x dir), up2 = dir x right
            float rx = z, ry = 0f, rz = -x;
            float rl = (float) Math.sqrt(rx * rx + rz * rz);
            if (rl < 1e-4f) { rx = 1f; rz = 0f; rl = 1f; }
            rx /= rl; rz /= rl;
            float ux = y * rz - z * ry, uy = z * rx - x * rz, uz = x * ry - y * rx;
            SRX[s] = rx; SRY[s] = ry; SRZ[s] = rz;
            SUX[s] = ux; SUY[s] = uy; SUZ[s] = uz;

            float q = rnd.nextFloat();
            SSIZE[s] = 0.0022f + 0.0055f * q * q * q * q;
            SPHASE[s] = rnd.nextFloat() * 6.2831f;
            float kind = rnd.nextFloat();
            SCOL[s] = kind < 0.60f ? 0xF2F2FF : (kind < 0.85f ? 0xFFB8F2 : 0xB3D1FF);
        }
    }

    // ================= рендер =================

    public static void render(class_4184 camera, Matrix4f positionMatrix,
                              class_4597.class_4598 consumers, float tickProgress) {
        class_310 mc = class_310.method_1551();
        if (!Config.skyShader || mc.field_1687 == null || mc.field_1724 == null) return;
        if (!mc.field_1687.method_8597().comp_642()) return;   // только Верхний мир

        // ночь / дождь
        float night = 1f;
        if (Config.skyNightOnly) {
            float ang = (float) ((mc.field_1687.method_8532() % 24000L) / 24000.0 * Math.PI * 2.0);
            float sun = (float) Math.sin(ang);                 // 1 = полдень, -1 = полночь
            night = clamp01((0.12f - sun) / 0.40f);
        }
        float rain = mc.field_1687.method_8430(tickProgress);
        float k = (float) Config.skyAlpha * night * (1f - 0.75f * rain);
        if (k <= 0.01f) return;

        int preset = Math.max(0, Math.min(Config.skyPreset, PRESETS.length - 1));
        if (cachedPreset != preset) { buildColors(preset); cachedPreset = preset; }

        float R = Math.max(64f, mc.field_1690.method_42503().method_41753() * 16f * 2.2f);
        float t = (System.currentTimeMillis() % 1000000L) / 1000f * (float) Config.skySpeed;
        Matrix4f rot = new Matrix4f().rotationY(t * 0.02f);    // медленный поворот неба вокруг вертикали
        Matrix4f id = new class_4587().method_23760().method_23761();

        Matrix4fStack mv = RenderSystem.getModelViewStack();
        mv.pushMatrix();
        mv.mul(positionMatrix);
        try {
            class_4588 q = consumers.method_73477(class_12249.method_76023());

            // купол
            for (int j = 0; j < EL; j++) {
                for (int i = 0; i < AZ; i++) {
                    int a = j * (AZ + 1) + i, b = a + 1, c = a + (AZ + 1) + 1, d = a + (AZ + 1);
                    int ca = scaleAlpha(DOME[a], k), cb = scaleAlpha(DOME[b], k);
                    int cc = scaleAlpha(DOME[c], k), cd = scaleAlpha(DOME[d], k);
                    if (((ca | cb | cc | cd) >>> 24) == 0) continue;
                    dquad(q, rot,
                            DX[a] * R, DY[a] * R, DZ[a] * R, ca,
                            DX[b] * R, DY[b] * R, DZ[b] * R, cb,
                            DX[c] * R, DY[c] * R, DZ[c] * R, cc,
                            DX[d] * R, DY[d] * R, DZ[d] * R, cd);
                }
            }

            // звёзды (чуть ближе купола, чтобы не конфликтовать по глубине)
            if (Config.skyStars && preset != 3) {   // на закате звёзд нет
                float Rs = R * 0.99f;
                for (int s = 0; s < STARS; s++) {
                    float fade = smooth(0.0f, 0.20f, SY[s]);
                    if (fade <= 0f) continue;
                    float tw = 0.55f + 0.45f * (float) Math.sin(t * 2.2f + SPHASE[s]);
                    int col = argb((int) (255 * k * fade * tw), SCOL[s]);
                    float hs = SSIZE[s] * Rs;
                    float px = SX[s] * Rs, py = SY[s] * Rs, pz = SZ[s] * Rs;
                    float rx = SRX[s] * hs, ry = SRY[s] * hs, rz = SRZ[s] * hs;
                    float ux = SUX[s] * hs, uy = SUY[s] * hs, uz = SUZ[s] * hs;
                    dquad(q, rot,
                            px - rx - ux, py - ry - uy, pz - rz - uz, col,
                            px + rx - ux, py + ry - uy, pz + rz - uz, col,
                            px + rx + ux, py + ry + uy, pz + rz + uz, col,
                            px - rx + ux, py - ry + uy, pz - rz + uz, col);
                }
            }

            // анимированное северное сияние
            switch (preset) {
                case 0 -> aurora(q, id, R * 0.985f, t, k, 0x33FFCC, 0xCC59FF);
                case 7 -> aurora(q, id, R * 0.985f, t, k, 0x55FF66, 0x1E90FF);
                case 4 -> disc(q, id, R * 0.97f, 0.55f, 0.50f, 0.67f, 0.075f, 0xFF4A35, 0xC01808, k, 0.14f);
                case 6 -> disc(q, id, R * 0.97f, -0.80f, 0.17f, 0.57f, 0.17f, 0xFFE14D, 0xFF2E93, k, 0.10f);
                default -> {}
            }

            consumers.method_22994(class_12249.method_76023());
        } finally {
            mv.popMatrix();
        }
    }

    // ================= северное сияние =================

    private static final int AS = 90;
    private static final float[] BX = new float[AS + 1], BY = new float[AS + 1], BZ = new float[AS + 1];
    private static final float[] TX = new float[AS + 1], TY = new float[AS + 1], TZ = new float[AS + 1];
    private static final int[] AC = new int[AS + 1];

    private static void aurora(class_4588 q, Matrix4f m, float R, float t, float k, int colA, int colB) {
        for (int r = 0; r < 3; r++) {
            float az0 = r * 2.1f + 0.4f;
            float span = 2.6f;
            for (int i = 0; i <= AS; i++) {
                float a = az0 + span * i / AS;
                float e0 = 0.30f + 0.11f * (float) Math.sin(a * 2.3f + t * 0.35f + r * 1.7f);
                float eh = 0.34f + 0.17f * (float) Math.sin(a * 4.1f - t * 0.6f + r);
                float wave = 0.55f + 0.45f * (float) Math.sin(a * 7f + t * 0.9f + r * 2f);
                float mixc = 0.5f + 0.5f * (float) Math.sin(a * 2f + r + t * 0.2f);
                // бирюза <-> фиолет
                int rgb = lerpRgb(colA, colB, mixc);
                AC[i] = argb((int) (255 * 0.62f * wave * k), rgb);

                float ce = (float) Math.cos(e0), se = (float) Math.sin(e0);
                BX[i] = ce * (float) Math.cos(a) * R; BY[i] = se * R; BZ[i] = ce * (float) Math.sin(a) * R;
                float e1 = e0 + eh;
                float ct = (float) Math.cos(e1), st = (float) Math.sin(e1);
                TX[i] = ct * (float) Math.cos(a) * R; TY[i] = st * R; TZ[i] = ct * (float) Math.sin(a) * R;
            }
            for (int i = 0; i < AS; i++) {
                int bot0 = AC[i], bot1 = AC[i + 1];
                int top0 = bot0 & 0xFFFFFF, top1 = bot1 & 0xFFFFFF;   // верх прозрачный
                dquad(q, m,
                        BX[i], BY[i], BZ[i], bot0,
                        TX[i], TY[i], TZ[i], top0,
                        TX[i + 1], TY[i + 1], TZ[i + 1], top1,
                        BX[i + 1], BY[i + 1], BZ[i + 1], bot1);
            }
        }
    }

    // ================= цвета купола =================

    private static void buildColors(int preset) {
        float[] out = new float[3];
        for (int k = 0; k < DOME.length; k++) {
            float dx = DX[k], dy = DY[k], dz = DZ[k];
            switch (preset) {
                case 0 -> auroraSky(dx, dy, dz, out);
                case 1 -> galaxySky(dx, dy, dz, out);
                case 2 -> purpleSky(dx, dy, dz, out);
                case 3 -> sunsetSky(dx, dy, dz, out);
                case 4 -> bloodSky(dx, dy, dz, out);
                case 5 -> nebulaSky(dx, dy, dz, out);
                case 6 -> synthSky(dx, dy, dz, out);
                default -> emeraldSky(dx, dy, dz, out);
            }
            float alpha = smooth(0.0f, 0.25f, dy);      // у горизонта купол прозрачный -> виден ванильный туман
            int r = (int) (clamp01(out[0]) * 255), g = (int) (clamp01(out[1]) * 255), b = (int) (clamp01(out[2]) * 255);
            DOME[k] = ((int) (alpha * 255) << 24) | (r << 16) | (g << 8) | b;
        }
    }

    /** Aurora: сине-бирюзовое небо с фиолетовыми облаками. */
    private static void auroraSky(float dx, float dy, float dz, float[] o) {
        float h = clamp01(dy), low = 1f - h;
        float n1 = fbm(dx * 2.2f + 3f, dy * 2.2f, dz * 2.2f, 4);
        float n2 = fbm(dx * 3.1f - 5f, dy * 3.1f + 2f, dz * 3.1f, 4);
        float r = 0.04f + 0.22f * low * low;
        float g = 0.10f + 0.10f * low;
        float b = 0.26f + 0.12f * low;
        float teal = smooth(0.35f, 0.80f, n1) * (0.55f + 0.45f * h);
        float purp = smooth(0.40f, 0.85f, n2) * (0.40f + 0.60f * low);
        o[0] = r + 0.10f * teal + 0.55f * purp;
        o[1] = g + 0.60f * teal + 0.18f * purp;
        o[2] = b + 0.55f * teal + 0.75f * purp;
    }

    /** Galaxy: тёмное небо, наклонная светящаяся полоса, оранжево-розовая туманность. */
    private static void galaxySky(float dx, float dy, float dz, float[] o) {
        float h = clamp01(dy), low = 1f - h;
        float d = dx * 0.35f + dy * 0.80f + dz * 0.49f;       // расстояние до плоскости полосы
        float band = (float) Math.exp(-d * d / 0.045f);
        float c1 = fbm(dx * 3.2f + 1f, dy * 3.2f, dz * 3.2f + 4f, 5);
        float c2 = fbm(dx * 6.0f - 3f, dy * 6.0f + 8f, dz * 6.0f, 4);
        float glow = band * (0.35f + 1.2f * c1);
        float hot = band * smooth(0.45f, 0.85f, c2);
        float haze = smooth(0.40f, 0.90f, fbm(dx * 2f - 7f, dy * 2f, dz * 2f + 1f, 4)) * 0.28f;
        float r = 0.015f + 0.03f * low, g = 0.02f + 0.03f * low, b = 0.06f + 0.08f * low;
        o[0] = r + 1.00f * hot * 1.1f + 0.80f * glow * 0.55f + 0.18f * haze;
        o[1] = g + 0.50f * hot * 1.1f + 0.22f * glow * 0.55f + 0.34f * haze;
        o[2] = b + 0.16f * hot * 1.1f + 0.55f * glow * 0.55f + 0.85f * haze;
    }

    /** Purple Night: фиолетовый верх, розово-пурпурный горизонт с полосами облаков. */
    private static void purpleSky(float dx, float dy, float dz, float[] o) {
        float low = 1f - clamp01(dy);
        float t = low * low;
        float streak = fbm(dx * 1.5f, dy * 7f, dz * 1.5f, 4);
        o[0] = lerp(0.07f, 0.88f, t) + 0.25f * t * streak;
        o[1] = lerp(0.03f, 0.24f, t) + 0.06f * t * streak;
        o[2] = lerp(0.20f, 0.52f, t) + 0.12f * t * streak;
    }

    // ================= диск (луна / солнце) =================

    private static final int DISC_SEG = 40;

    private static void disc(class_4588 q, Matrix4f m, float R,
                             float dx, float dy, float dz,
                             float ang, int cTop, int cBot, float k, float glow) {
        float l = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        dx /= l; dy /= l; dz /= l;
        float rx = dz, rz = -dx;
        float rl = (float) Math.sqrt(rx * rx + rz * rz);
        rx /= rl; rz /= rl;
        float ux = dy * rz, uy = dz * rx - dx * rz, uz = -dy * rx;

        int aFull = (int) (255 * k);
        int centerCol = argb(aFull, lerpRgb(cBot, cTop, 0.5f));
        float cx = dx * R, cy = dy * R, cz = dz * R;
        float sa = (float) Math.sin(ang), ca = (float) Math.cos(ang);
        float sb = (float) Math.sin(ang + glow), cb = (float) Math.cos(ang + glow);

        for (int i = 0; i < DISC_SEG; i++) {
            float p0 = (float) (Math.PI * 2.0) * i / DISC_SEG;
            float p1 = (float) (Math.PI * 2.0) * (i + 1) / DISC_SEG;
            float c0 = (float) Math.cos(p0), s0 = (float) Math.sin(p0);
            float c1 = (float) Math.cos(p1), s1 = (float) Math.sin(p1);

            float ix0 = (dx * ca + (rx * c0 + ux * s0) * sa) * R;
            float iy0 = (dy * ca + (uy * s0) * sa) * R;
            float iz0 = (dz * ca + (rz * c0 + uz * s0) * sa) * R;
            float ix1 = (dx * ca + (rx * c1 + ux * s1) * sa) * R;
            float iy1 = (dy * ca + (uy * s1) * sa) * R;
            float iz1 = (dz * ca + (rz * c1 + uz * s1) * sa) * R;
            int col0 = argb(aFull, lerpRgb(cBot, cTop, 0.5f + 0.5f * s0));
            int col1 = argb(aFull, lerpRgb(cBot, cTop, 0.5f + 0.5f * s1));

            dquad(q, m, cx, cy, cz, centerCol, ix0, iy0, iz0, col0, ix1, iy1, iz1, col1, cx, cy, cz, centerCol);

            float ox0 = (dx * cb + (rx * c0 + ux * s0) * sb) * R;
            float oy0 = (dy * cb + (uy * s0) * sb) * R;
            float oz0 = (dz * cb + (rz * c0 + uz * s0) * sb) * R;
            float ox1 = (dx * cb + (rx * c1 + ux * s1) * sb) * R;
            float oy1 = (dy * cb + (uy * s1) * sb) * R;
            float oz1 = (dz * cb + (rz * c1 + uz * s1) * sb) * R;
            int g0 = col0 & 0xFFFFFF, g1 = col1 & 0xFFFFFF;
            int in0 = argb((int) (aFull * 0.45f), col0), in1 = argb((int) (aFull * 0.45f), col1);
            dquad(q, m, ix0, iy0, iz0, in0, ox0, oy0, oz0, g0, ox1, oy1, oz1, g1, ix1, iy1, iz1, in1);
        }
    }

    // ================= новые небеса =================

    /** Sunset: оранжевый горизонт -> розовый -> индиго. */
    private static void sunsetSky(float dx, float dy, float dz, float[] o) {
        float h = clamp01(dy);
        float streak = fbm(dx * 1.6f, dy * 9f, dz * 1.6f, 4);
        float m1 = smooth(0.10f, 0.55f, h);
        float m2 = smooth(0.40f, 0.95f, h);
        float r = lerp(lerp(1.00f, 0.85f, m1), 0.12f, m2);
        float g = lerp(lerp(0.55f, 0.28f, m1), 0.08f, m2);
        float b = lerp(lerp(0.20f, 0.40f, m1), 0.32f, m2);
        float cl = smooth(0.45f, 0.85f, streak) * (1f - m2) * 0.35f;
        o[0] = r + cl; o[1] = g + cl * 0.45f; o[2] = b + cl * 0.2f;
    }

    /** Blood Moon: тёмно-красное небо с дымкой. */
    private static void bloodSky(float dx, float dy, float dz, float[] o) {
        float h = clamp01(dy), low = 1f - h;
        float haze = smooth(0.40f, 0.90f, fbm(dx * 2.2f + 9f, dy * 2.2f, dz * 2.2f, 4));
        o[0] = 0.07f + 0.50f * low * low + 0.22f * haze * low;
        o[1] = 0.00f + 0.04f * low + 0.03f * haze;
        o[2] = 0.02f + 0.07f * low + 0.05f * haze;
    }

    /** Nebula: бирюзовые и пурпурные облака газа. */
    private static void nebulaSky(float dx, float dy, float dz, float[] o) {
        float h = clamp01(dy), low = 1f - h;
        float n1 = fbm(dx * 2.6f + 2f, dy * 2.6f, dz * 2.6f + 6f, 5);
        float n2 = fbm(dx * 3.4f - 4f, dy * 3.4f + 5f, dz * 3.4f, 5);
        float n3 = fbm(dx * 7.0f + 1f, dy * 7.0f - 3f, dz * 7.0f, 3);
        float cyan = smooth(0.42f, 0.85f, n1);
        float mag  = smooth(0.45f, 0.88f, n2);
        float dust = smooth(0.55f, 0.90f, n3) * 0.25f;
        o[0] = 0.02f + 0.03f * low + 0.75f * mag + 0.10f * cyan + dust * 0.6f;
        o[1] = 0.02f + 0.03f * low + 0.15f * mag + 0.65f * cyan + dust * 0.3f;
        o[2] = 0.07f + 0.08f * low + 0.70f * mag + 0.80f * cyan + dust;
    }

    /** Synthwave: тёмно-синий верх, розово-пурпурный низ. */
    private static void synthSky(float dx, float dy, float dz, float[] o) {
        float h = clamp01(dy);
        float m = smooth(0.0f, 0.75f, h);
        float band = (float) Math.exp(-Math.pow((h - 0.18f) / 0.10f, 2)) * 0.35f;
        o[0] = lerp(0.95f, 0.06f, m) * (0.55f + 0.45f * (1f - m)) + band * 0.4f;
        o[1] = lerp(0.18f, 0.02f, m) + band * 0.05f;
        o[2] = lerp(0.62f, 0.20f, m) + band * 0.25f;
    }

    /** Emerald Aurora: тёмно-зелёное небо с изумрудными облаками. */
    private static void emeraldSky(float dx, float dy, float dz, float[] o) {
        float h = clamp01(dy), low = 1f - h;
        float n1 = fbm(dx * 2.2f + 3f, dy * 2.2f, dz * 2.2f, 4);
        float n2 = fbm(dx * 3.1f - 5f, dy * 3.1f + 2f, dz * 3.1f, 4);
        float grn = smooth(0.35f, 0.80f, n1) * (0.55f + 0.45f * h);
        float blu = smooth(0.45f, 0.88f, n2) * (0.40f + 0.60f * low);
        o[0] = 0.02f + 0.06f * low + 0.05f * grn;
        o[1] = 0.09f + 0.10f * low + 0.60f * grn + 0.15f * blu;
        o[2] = 0.12f + 0.12f * low + 0.20f * grn + 0.55f * blu;
    }

    // ================= шум =================

    private static float hash(int x, int y, int z) {
        int h = x * 374761393 + y * 668265263 + z * 1274126177;
        h = (h ^ (h >>> 13)) * 1274126177;
        h ^= (h >>> 16);
        return (h & 0xFFFF) / 65535f;
    }

    private static float noise(float x, float y, float z) {
        int xi = (int) Math.floor(x), yi = (int) Math.floor(y), zi = (int) Math.floor(z);
        float xf = x - xi, yf = y - yi, zf = z - zi;
        float u = xf * xf * (3f - 2f * xf), v = yf * yf * (3f - 2f * yf), w = zf * zf * (3f - 2f * zf);
        float c000 = hash(xi, yi, zi),         c100 = hash(xi + 1, yi, zi);
        float c010 = hash(xi, yi + 1, zi),     c110 = hash(xi + 1, yi + 1, zi);
        float c001 = hash(xi, yi, zi + 1),     c101 = hash(xi + 1, yi, zi + 1);
        float c011 = hash(xi, yi + 1, zi + 1), c111 = hash(xi + 1, yi + 1, zi + 1);
        float x00 = lerp(c000, c100, u), x10 = lerp(c010, c110, u);
        float x01 = 
