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

    public static final String[] WING_STYLES = {"Ангел", "Летучая мышь"};
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
            double px = MathHelper.lerp((double) tp, p.lastX, p.getX());
            double py = MathHelper.lerp((double) tp, p.lastY, p.getY());
            double pz = MathHelper.lerp((double) tp, p.lastZ, p.getZ());
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
        double h = p.isInSneakingPose() ? 1.05 : 1.35;
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
            } else {
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
        boolean sneak = p.isInSneakingPose();
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
