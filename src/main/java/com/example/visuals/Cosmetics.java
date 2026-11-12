package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * Косметика, нарисованная в мире: Wings, Aura и Animated Cape.
 * Рисуется для себя (в виде от 3-го лица) и по желанию для друзей.
 */
public final class Cosmetics {
    private Cosmetics() {}

    public static final String[] WING_STYLES = {
        "Ангел", "Летучая мышь",
        "Бабочка", "Дракон", "Кристаллы", "Феникс"
    };
    public static final String[] AURA_STYLES = {"Кольца", "Орбиты", "Спираль"};

    private static final double[] FEATHER_LEN = {0.6, 0.8, 1.0, 1.1, 1.05, 0.9, 0.7};

    public static boolean any() { return Config.wingsOn || Config.auraOn || Config.capeOn; }

    private static boolean eligible(MinecraftClient mc, PlayerEntity p, boolean friends) {
        if (p.isSpectator() || p.isInvisible()) return false;
        if (p == mc.player) return !mc.options.getPerspective().isFirstPerson();
        return friends && Config.friends.contains(p.getName().getString());
    }

    private static int pal(int idx) {
        return Config.COLORS[Math.max(0, Math.min(idx, Config.COLORS.length - 1))] & 0xFFFFFF;
    }

    private static int hue(float h) {
        return MathHelper.hsvToRgb(((h % 1f) + 1f) % 1f, 0.65f, 1f) & 0xFFFFFF;
    }

    private static int mixRgb(int a, int b, float t) {
        return HudStyle.mix(0xFF000000 | a, 0xFF000000 | b, MathHelper.clamp(t, 0f, 1f)) & 0xFFFFFF;
    }

    /** Базис игрока: right, back (горизонтальные единичные векторы). */
    private static final class Frame {
        double rx, rz, bx, bz;
        Frame(float yawDeg) {
            double y = Math.toRadians(yawDeg);
            double fx = -Math.sin(y), fz = Math.cos(y);
            bx = -fx; bz = -fz;
            rx = -Math.cos(y); rz = -Math.sin(y);
        }
        /** Вектор крыла: s - сторона (+1/-1), th - подъём, be - отклонение назад. */
        double[] wing(int s, double th, double be) {
            double o = Math.cos(th), u = Math.sin(th);
            double cb = Math.cos(be), sb = Math.sin(be);
            return new double[] {
                    s * o * cb * rx + o * sb * bx,
                    u,
                    s * o * cb * rz + o * sb * bz};
        }
    }

    public static void render(VertexConsumer q, Matrix4f m, Vec3d cam, MinecraftClient mc, float tp) {
        double t = System.currentTimeMillis() / 1000.0;
        for (PlayerEntity p : mc.world.getPlayers()) {
            double px = MathHelper.lerp((double) tp, p.lastRenderX, p.getX());
            double py = MathHelper.lerp((double) tp, p.lastRenderY, p.getY());
            double pz = MathHelper.lerp((double) tp, p.lastRenderZ, p.getZ());
            double dx = px - cam.x, dy = py - cam.y, dz = pz - cam.z;
            if (dx * dx + dy * dy + dz * dz > 48 * 48) continue;
            float yaw = MathHelper.lerpAngleDegrees(tp, p.lastBodyYaw, p.bodyYaw);
            Frame fr = new Frame(yaw);
            boolean glide = p.isGliding();

            if (Config.wingsOn && !glide && eligible(mc, p, Config.wingFriends)) wings(q, m, cam, p, px, py, pz, fr, t);
            if (Config.auraOn && eligible(mc, p, Config.auraFriends)) aura(q, m, cam, px, py, pz, t);
            if (Config.capeOn && !glide && eligible(mc, p, Config.capeFriends)) cape(q, m, cam, p, px, py, pz, fr, t);
        }
    }

    // ------------------------------------------------------------------ Wings
    private static void wings(VertexConsumer q, Matrix4f m, Vec3d cam, PlayerEntity p,
                              double px, double py, double pz, Frame fr, double t) {
        boolean air = !p.isOnGround();
        double h = p.isSneaking() ? 1.05 : 1.35;
        double bx = px + fr.bx * 0.14, by = py + h, bz = pz + fr.bz * 0.14;
        double size = Config.wingSize;
        double flap = Math.sin(t * Config.wingSpeed * (air ? 5.0 : 1.6)) * (air ? 0.45 : 0.12);
        int rgb = Config.wingRainbow ? hue((float) (t * 0.2)) : pal(Config.wingColor);
        int white = 0xFFFFFF;

        for (int s = -1; s <= 1; s += 2) {
            if (Config.wingStyle == 0) {
                int n = FEATHER_LEN.length;
                for (int i = 0; i < n; i++) {
                    double th = Math.toRadians(-8 + i * 14) + flap * (0.4 + 0.6 * i / n);
                    double be = Math.toRadians(38 - i * 3);
                    double[] d = fr.wing(s, th, be);
                    double len = FEATHER_LEN[i] * size;
                    double tx = bx + d[0] * len, ty = by + d[1] * len, tz = bz + d[2] * len;
                    double mx = bx + d[0] * len * 0.45, my = by + d[1] * len * 0.45, mz = bz + d[2] * len * 0.45;
                    // ширина пера: вектор, перпендикулярный направлению пера и "вперёд" игрока
                    double fx = -fr.bx, fz = -fr.bz;
                    double wx = d[1] * fz, wy = d[2] * fx - d[0] * fz, wz = -d[1] * fx;
                    double wl = Math.sqrt(wx * wx + wy * wy + wz * wz);
                    if (wl < 1.0e-4) continue;
                    double k = 0.055 * size / wl;
                    wx *= k; wy *= k; wz *= k;
                    int c0 = WorldDraw.argb(0.95f, rgb), c1 = WorldDraw.argb(0.8f, mixRgb(rgb, white, 0.5f));
                    int c2 = WorldDraw.argb(0.45f, mixRgb(rgb, white, 0.85f));
                    WorldDraw.quad2(q, m,
                            (float) (bx - cam.x), (float) (by - cam.y), (float) (bz - cam.z), c0,
                            (float) (mx + wx - cam.x), (float) (my + wy - cam.y), (float) (mz + wz - cam.z), c1,
                            (float) (tx - cam.x), (float) (ty - cam.y), (float) (tz - cam.z), c2,
                            (float) (mx - wx - cam.x), (float) (my - wy - cam.y), (float) (mz - wz - cam.z), c1);
                }
            } else if (Config.wingStyle == 1) {
                // летучая мышь: кость плеча, запястье, четыре пальца и перепонка между ними
                double[] e = fr.wing(s, 0.55 + flap * 0.5, 0.5);
                double ex = bx + e[0] * 0.45 * size, ey = by + e[1] * 0.45 * size, ez = bz + e[2] * 0.45 * size;
                double[] w = fr.wing(s, 1.0 + flap * 1.3, 0.9);
                double wxp = ex + w[0] * 0.5 * size, wyp = ey + w[1] * 0.5 * size, wzp = ez + w[2] * 0.5 * size;
                double[][] tip = new double[4][];
                for (int k = 0; k < 4; k++) {
                    double[] f = fr.wing(s, 0.95 - k * 0.55 + flap * 1.6, 0.9 + 0.15 * k);
                    double len = (0.85 - 0.1 * k) * size;
                    tip[k] = new double[] {wxp + f[0] * len, wyp + f[1] * len, wzp + f[2] * len};
                }
                int bone = WorldDraw.argb(0.95f, rgb);
                int mem = WorldDraw.argb(0.55f, mixRgb(rgb, 0x200000, 0.55f));
                // перепонка
                tri(q, m, cam, bx, by, bz, wxp, wyp, wzp, tip[0][0], tip[0][1], tip[0][2], mem);
                for (int k = 0; k < 3; k++)
                    tri(q, m, cam, bx, by, bz, tip[k][0], tip[k][1], tip[k][2], tip[k + 1][0], tip[k + 1][1], tip[k + 1][2], mem);
                // кости
                WorldDraw.ribbon(q, m, cam.x, cam.y, cam.z, bx, by, bz, ex, ey, ez, 0.02f, bone, bone);
                WorldDraw.ribbon(q, m, cam.x, cam.y, cam.z, ex, ey, ez, wxp, wyp, wzp, 0.018f, bone, bone);
                for (int k = 0; k < 4; k++)
                    WorldDraw.ribbon(q, m, cam.x, cam.y, cam.z, wxp, wyp, wzp, tip[k][0], tip[k][1], tip[k][2], 0.012f, bone, bone);
            } else {
                newWing(q, m, cam, fr, s, bx, by, bz, size, flap, rgb, t, Config.wingStyle);
            }
        }
    }

    // ------------------------------------------------------------------ Новые крылья

    private static double[] pt(double bx, double by, double bz, double[] d, double len) {
        return new double[] {bx + d[0] * len, by + d[1] * len, bz + d[2] * len};
    }

    private static double[] cross(double[] a, double[] b) {
        return new double[] {a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
    }

    private static double[] norm(double[] a) {
        double l = Math.sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2]);
        if (l < 1.0e-6) return new double[] {1, 0, 0};
        return new double[] {a[0] / l, a[1] / l, a[2] / l};
    }

    /** Треугольник с цветом в каждой вершине. */
    private static void triC(VertexConsumer q, Matrix4f m, Vec3d cam,
                             double[] a, int ca, double[] b, int cb, double[] c, int cc) {
        float ax = (float) (a[0] - cam.x), ay = (float) (a[1] - cam.y), az = (float) (a[2] - cam.z);
        WorldDraw.quad2(q, m, ax, ay, az, ca,
                (float) (b[0] - cam.x), (float) (b[1] - cam.y), (float) (b[2] - cam.z), cb,
                (float) (c[0] - cam.x), (float) (c[1] - cam.y), (float) (c[2] - cam.z), cc,
                ax, ay, az, ca);
    }

    private static void bone(VertexConsumer q, Matrix4f m, Vec3d cam, double[] a, double[] b, float w, int c) {
        WorldDraw.ribbon(q, m, cam.x, cam.y, cam.z,
                a[0], a[1], a[2], b[0], b[1], b[2], w, c, c);
    }

    /** Одно перо (как у ангела): корень -> середина (шире) -> кончик. */
    private static void feather(VertexConsumer q, Matrix4f m, Vec3d cam, Frame fr,
                                double bx, double by, double bz, int s,
                                double th, double be, double len, double halfW, int c0, int c1, int c2) {
        double[] d = fr.wing(s, th, be);
        double tx = bx + d[0] * len, ty = by + d[1] * len, tz = bz + d[2] * len;
        double mx = bx + d[0] * len * 0.45, my = by + d[1] * len * 0.45, mz = bz + d[2] * len * 0.45;
        double fx = -fr.bx, fz = -fr.bz;
        double wx = d[1] * fz, wy = d[2] * fx - d[0] * fz, wz = -d[1] * fx;
        double wl = Math.sqrt(wx * wx + wy * wy + wz * wz);
        if (wl < 1.0e-4) return;
        double k = halfW / wl;
        wx *= k; wy *= k; wz *= k;
        WorldDraw.quad2(q, m,
                (float) (bx - cam.x), (float) (by - cam.y), (float) (bz - cam.z), c0,
                (float) (mx + wx - cam.x), (float) (my + wy - cam.y), (float) (mz + wz - cam.z), c1,
                (float) (tx - cam.x), (float) (ty - cam.y), (float) (tz - cam.z), c2,
                (float) (mx - wx - cam.x), (float) (my - wy - cam.y), (float) (mz - wz - cam.z), c1);
    }

    /** Веер-лопасть крыла бабочки: от корня по дуге углов th0..th1. */
    private static void wingFan(VertexConsumer q, Matrix4f m, Vec3d cam, Frame fr,
                                double bx, double by, double bz, int s,
                                double th0, double th1, double be, double l0, double l1, double size,
                                int cIn, int cOut, int cEdge) {
        final int N = 12;
        double[] root = {bx, by, bz};
        double[] prev = null;
        for (int k = 0; k <= N; k++) {
            double u = (double) k / N;
            double th = th0 + (th1 - th0) * u;
            double len = size * (l0 + l1 * Math.sin(Math.PI * u));
            double[] cur = pt(bx, by, bz, fr.wing(s, th, be), len);
            if (prev != null) {
                triC(q, m, cam, root, cIn, prev, cOut, cur, cOut);
                bone(q, m, cam, prev, cur, 0.014f, cEdge);
            }
            prev = cur;
        }
    }

    private static void newWing(VertexConsumer q, Matrix4f m, Vec3d cam, Frame fr, int s,
                                double bx, double by, double bz, double size, double flap,
                                int rgb, double t, int style) {
        double[] root = {bx, by, bz};
        int white = 0xFFFFFF;

        if (style == 2) {
            // ---------- Бабочка: большое верхнее и малое нижнее крыло ----------
            int cIn = WorldDraw.argb(0.85f, rgb);
            int cOut = WorldDraw.argb(0.55f, mixRgb(rgb, white, 0.6f));
            int cEdge = WorldDraw.argb(0.95f, mixRgb(rgb, 0x000000, 0.45f));
            double fl = flap * 1.5;
            wingFan(q, m, cam, fr, bx, by, bz, s, -0.05 + fl, 1.30 + fl, 0.55, 0.30, 0.80, size, cIn, cOut, cEdge);
            wingFan(q, m, cam, fr, bx, by, bz, s, -1.05 + fl * 0.6, -0.05 + fl * 0.6, 0.45, 0.12, 0.60, size,
                    cIn, WorldDraw.argb(0.5f, mixRgb(rgb, 0xFF80D0, 0.5f)), cEdge);
            // «глазок» на верхнем крыле
            double[] eye = pt(bx, by, bz, fr.wing(s, 0.65 + fl, 0.55), 0.62 * size);
            int ec = WorldDraw.argb(0.9f, mixRgb(rgb, white, 0.85f));
            bone(q, m, cam, new double[] {eye[0], eye[1] - 0.05 * size, eye[2]},
                    new double[] {eye[0], eye[1] + 0.05 * size, eye[2]}, 0.05f, ec);

        } else if (style == 3) {
            // ---------- Дракон: 5 пальцев, зубчатая перепонка, шипы ----------
            int dark = WorldDraw.argb(0.80f, mixRgb(rgb, 0x000000, 0.65f));
            int mid = WorldDraw.argb(0.65f, mixRgb(rgb, 0x000000, 0.40f));
            int outer = WorldDraw.argb(0.55f, rgb);
            int boneC = WorldDraw.argb(0.97f, mixRgb(rgb, white, 0.15f));
            double[] elbow = pt(bx, by, bz, fr.wing(s, 0.60 + flap * 0.5, 0.45), 0.55 * size);
            double[] wrist = pt(elbow[0], elbow[1], elbow[2], fr.wing(s, 1.05 + flap * 1.2, 0.85), 0.60 * size);
            double[][] tip = new double[5][];
            for (int k = 0; k < 5; k++) {
                double[] f = fr.wing(s, 1.15 - k * 0.50 + flap * 1.6, 0.85 + 0.12 * k);
                tip[k] = pt(wrist[0], wrist[1], wrist[2], f, (1.05 - 0.10 * k) * size);
            }
            triC(q, m, cam, root, dark, wrist, mid, tip[0], outer);
            for (int k = 0; k < 4; k++) {
                double[] mp = {(tip[k][0] + tip[k + 1][0]) * 0.5, (tip[k][1] + tip[k + 1][1]) * 0.5, (tip[k][2] + tip[k + 1][2]) * 0.5};
                double[] sc = {mp[0] + (wrist[0] - mp[0]) * 0.30, mp[1] + (wrist[1] - mp[1]) * 0.30, mp[2] + (wrist[2] - mp[2]) * 0.30};
                triC(q, m, cam, root, dark, tip[k], outer, sc, mid);
                triC(q, m, cam, root, dark, sc, mid, tip[k + 1], outer);
            }
            bone(q, m, cam, root, elbow, 0.028f, boneC);
            bone(q, m, cam, elbow, wrist, 0.024f, boneC);
            for (int k = 0; k < 5; k++) bone(q, m, cam, wrist, tip[k], 0.014f, boneC);
            // шип на локте и коготь на запястье
            bone(q, m, cam, elbow, new double[] {elbow[0], elbow[1] + 0.20 * size, elbow[2]}, 0.02f, boneC);
            bone(q, m, cam, wrist, new double[] {wrist[0], wrist[1] + 0.14 * size, wrist[2]}, 0.016f, boneC);

        } else if (style == 4) {
            // ---------- Кристаллы: ледяные/магические осколки ----------
            double[] LEN = {0.55, 0.80, 1.00, 1.10, 0.95, 0.70};
            int cBase = WorldDraw.argb(0.75f, mixRgb(rgb, 0x000000, 0.25f));
            int cMid = WorldDraw.argb(0.80f, mixRgb(rgb, white, 0.30f));
            int cTip = WorldDraw.argb(0.85f, mixRgb(rgb, white, 0.90f));
            for (int i = 0; i < LEN.length; i++) {
                double th = Math.toRadians(-6 + i * 17) + flap * (0.4 + 0.6 * i / LEN.length)
                        + 0.04 * Math.sin(t * 2.0 + i * 1.3);
                double be = Math.toRadians(42 - i * 3);
                double[] d = fr.wing(s, th, be);
                double len = LEN[i] * size, w = 0.07 * size * (0.8 + 0.4 * (i % 2));
                double[] b0 = pt(bx, by, bz, d, 0.10 * size);
                double[] c = pt(bx, by, bz, d, len * 0.42);
                double[] tp = pt(bx, by, bz, d, len);
                double[] p1 = norm(cross(d, new double[] {0, 1, 0}));
                double[] p2 = norm(cross(d, p1));
                double[][] ring = new double[4][];
                for (int k = 0; k < 4; k++) {
                    double sx = (k == 0 ? 1 : k == 2 ? -1 : 0), sy = (k == 1 ? 1 : k == 3 ? -1 : 0);
                    ring[k] = new double[] {
                            c[0] + (p1[0] * sx + p2[0] * sy) * w,
                            c[1] + (p1[1] * sx + p2[1] * sy) * w,
                            c[2] + (p1[2] * sx + p2[2] * sy) * w};
                }
                for (int k = 0; k < 4; k++) {
                    double[] a = ring[k], b = ring[(k + 1) % 4];
                    triC(q, m, cam, b0, cBase, a, cMid, b, cMid);
                    triC(q, m, cam, tp, cTip, a, cMid, b, cMid);
                }
                int glint = WorldDraw.argb(0.9f, 0xFFFFFF);
                bone(q, m, cam, ring[0], tp, 0.008f, glint);
            }

        } else {
            // ---------- Феникс: огненные перья в 3 ряда ----------
            int n = 9;
            for (int r = 0; r < 3; r++) {
                for (int i = 0; i < n; i++) {
                    double u = (i + 0.5) / n;
                    double th = Math.toRadians(-15 + i * 13) + flap * (0.4 + 0.6 * i / n) * (1.0 + 0.2 * r)
                            + 0.03 * Math.sin(t * 4.0 + i + r);
                    double be = Math.toRadians(30 + r * 8 - i * 2);
                    double len = (0.70 + 0.50 * Math.sin(Math.PI * u)) * (1.25 - 0.20 * r) * size;
                    int cA = Config.wingRainbow ? hue((float) (t * 0.2 + r * 0.1)) : 0xFF2200;
                    int cB = Config.wingRainbow ? hue((float) (t * 0.2 + 0.15 + r * 0.1)) : 0xFF9A1A;
                    int cC = Config.wingRainbow ? mixRgb(hue((float) (t * 0.2 + 0.3)), white, 0.7f) : 0xFFF0A0;
                    feather(q, m, cam, fr, bx, by, bz, s, th, be, len, 0.05 * size,
                            WorldDraw.argb(0.95f, cA), WorldDraw.argb(0.85f - 0.1f * r, cB), WorldDraw.argb(0.40f, cC));
                }
            }
        }
    }

    private static void tri(VertexConsumer q, Matrix4f m, Vec3d cam,
                            double ax, double ay, double az, double bx, double by, double bz,
                            double cx, double cy, double cz, int c) {
        float a1 = (float) (ax - cam.x), a2 = (float) (ay - cam.y), a3 = (float) (az - cam.z);
        WorldDraw.quad2(q, m, a1, a2, a3, c,
                (float) (bx - cam.x), (float) (by - cam.y), (float) (bz - cam.z), c,
                (float) (cx - cam.x), (float) (cy - cam.y), (float) (cz - cam.z), c,
                a1, a2, a3, c);
    }

    // ------------------------------------------------------------------ Aura
    private static void aura(VertexConsumer q, Matrix4f m, Vec3d cam, double px, double py, double pz, double t) {
        double size = Config.auraSize, sp = Config.auraSpeed;
        int base = Config.auraRainbow ? hue((float) (t * 0.2)) : pal(Config.auraColor);
        double r0 = 0.6 * size;

        switch (Config.auraStyle) {
            case 1 -> {   // орбиты
                for (int i = 0; i < 8; i++) {
                    double a = t * sp * 2.0 + i * Math.PI / 4.0;
                    double h = 0.2 + 1.6 * (0.5 + 0.5 * Math.sin(t * sp + i * 0.9));
                    double x = px + Math.cos(a) * r0, y = py + h, z = pz + Math.sin(a) * r0;
                    int c = WorldDraw.argb(0.9f, Config.auraRainbow ? hue((float) (t * 0.2 + i / 8.0)) : base);
                    WorldDraw.ribbon(q, m, cam.x, cam.y, cam.z, x, y - 0.07, z, x, y + 0.07, z, 0.035f, c, c);
                    WorldDraw.ribbon(q, m, cam.x, cam.y, cam.z, x - Math.sin(a) * 0.07, y, z + Math.cos(a) * 0.07,
                            x + Math.sin(a) * 0.07, y, z - Math.cos(a) * 0.07, 0.035f, c, c);
                }
            }
            case 2 -> {   // спираль
                for (int strand = 0; strand < 2; strand++) {
                    double lx = 0, ly = 0, lz = 0;
                    boolean have = false;
                    for (double y = 0; y <= 1.95; y += 0.08) {
                        double a = y * 6.0 + t * sp * 2.0 + strand * Math.PI;
                        double rr = r0 * (0.85 + 0.15 * Math.sin(y * 3 + t));
                        double x = px + Math.cos(a) * rr, yy = py + y, z = pz + Math.sin(a) * rr;
                        float fade = (float) Math.sin(Math.PI * Math.min(1.0, y / 1.95));
                        int c = WorldDraw.argb(0.85f * fade,
                                Config.auraRainbow ? hue((float) (t * 0.2 + y * 0.3)) : base);
                        if (have) WorldDraw.ribbon(q, m, cam.x, cam.y, cam.z, lx, ly, lz, x, yy, z, 0.022f, c, c);
                        lx = x; ly = yy; lz = z; have = true;
                    }
                }
            }
            default -> {  // кольца
                for (int i = 0; i < 3; i++) {
                    double ph = (t * sp * 0.5 + i / 3.0) % 1.0;
                    double y = py + ph * 1.95;
                    float fade = (float) Math.sin(Math.PI * ph);
                    int c = WorldDraw.argb(0.8f * fade, Config.auraRainbow ? hue((float) (t * 0.2 + i * 0.2)) : base);
                    double rr = r0 * (0.9 + 0.2 * Math.sin(ph * Math.PI));
                    int seg = 28;
                    for (int k = 0; k < seg; k++) {
                        double a0 = k * 2 * Math.PI / seg, a1 = (k + 1) * 2 * Math.PI / seg;
                        WorldDraw.ribbon(q, m, cam.x, cam.y, cam.z,
                                px + Math.cos(a0) * rr, y, pz + Math.sin(a0) * rr,
                                px + Math.cos(a1) * rr, y, pz + Math.sin(a1) * rr, 0.02f, c, c);
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------ Cape
    private static void cape(VertexConsumer q, Matrix4f m, Vec3d cam, PlayerEntity p,
                             double px, double py, double pz, Frame fr, double t) {
        boolean sneak = p.isSneaking();
        double top = sneak ? 1.38 : 1.46;
        double ox = px + fr.bx * (sneak ? 0.27 : 0.14), oy = py + top, oz = pz + fr.bz * (sneak ? 0.27 : 0.14);

        Vec3d v = p.getVelocity();
        double speed = Math.sqrt(v.x * v.x + v.z * v.z);
        double ang = MathHelper.clamp(0.14 + speed * 2.4 + (sneak ? 0.18 : 0.0), 0.1, 1.15);
        double dxv = fr.bx * Math.sin(ang), dyv = -Math.cos(ang), dzv = fr.bz * Math.sin(ang);

        final int rows = 10, cols = 5;
        double len = 1.2, width = 0.5;
        double wave = Config.capeWave;
        int c1 = Config.capeRainbow ? 0 : pal(Config.capeColor), c2 = pal(Config.capeColor2);

        double[][][] pts = new double[rows + 1][cols + 1][];
        int[][] col = new int[rows + 1][cols + 1];
        for (int r = 0; r <= rows; r++) {
            double fr0 = (double) r / rows;
            for (int c = 0; c <= cols; c++) {
                double fc = (double) c / cols - 0.5;
                double lat = fc * width * (1.0 + 0.18 * fr0);
                double w = wave * Math.sin(t * 3.0 + r * 0.7 + c * 0.35) * 0.06 * fr0 * (0.35 + speed * 4.0);
                pts[r][c] = new double[] {
                        ox + dxv * len * fr0 + fr.rx * lat + fr.bx * w,
                        oy + dyv * len * fr0,
                        oz + dzv * len * fr0 + fr.rz * lat + fr.bz * w};
                int base = Config.capeRainbow ? hue((float) (t * 0.15 + fr0 * 0.35)) : mixRgb(c1, c2, (float) fr0);
                float shade = (float) (0.85 + 0.15 * Math.sin(t * 3.0 + r * 0.7 + c * 0.35));
                col[r][c] = WorldDraw.argb(0.97f, mixRgb(0x000000, base, shade));
            }
        }
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double[] a = pts[r][c], b = pts[r][c + 1], d = pts[r + 1][c], e = pts[r + 1][c + 1];
                WorldDraw.quad2(q, m,
                        (float) (a[0] - cam.x), (float) (a[1] - cam.y), (float) (a[2] - cam.z), col[r][c],
                        (float) (b[0] - cam.x), (float) (b[1] - cam.y), (float) (b[2] - cam.z), col[r][c + 1],
                        (float) (e[0] - cam.x), (float) (e[1] - cam.y), (float) (e[2] - cam.z), col[r + 1][c + 1],
                        (float) (d[0] - cam.x), (float) (d[1] - cam.y), (float) (d[2] - cam.z), col[r + 1][c]);
            }
        }
    }
}
