package com.example.visuals;

import net.minecraft.client.render.VertexConsumer;
import org.joml.Matrix4f;

/**
 * Примитивы для рисования линий и коробок в мире через debugQuads.
 * Линии - это ленты, повёрнутые лицом к камере; все координаты считаются относительно камеры.
 */
public final class WorldDraw {
    private WorldDraw() {}

    public static int argb(float a, int rgb) {
        return (Math.max(0, Math.min(255, (int) (255 * a))) << 24) | (rgb & 0xFFFFFF);
    }

    /** Четырёхугольник с обеими сторонами (не зависит от отсечения граней). */
    public static void quad2(VertexConsumer q, Matrix4f m,
                             float ax, float ay, float az, int ac, float bx, float by, float bz, int bc,
                             float cx, float cy, float cz, int cc, float dx, float dy, float dz, int dc) {
        q.vertex(m, ax, ay, az).color(ac); q.vertex(m, bx, by, bz).color(bc);
        q.vertex(m, cx, cy, cz).color(cc); q.vertex(m, dx, dy, dz).color(dc);
        q.vertex(m, dx, dy, dz).color(dc); q.vertex(m, cx, cy, cz).color(cc);
        q.vertex(m, bx, by, bz).color(bc); q.vertex(m, ax, ay, az).color(ac);
    }

    /** Лента между двумя точками мира (шириной 2*hw), цвет плавно меняется от ca к cb. */
    public static void ribbon(VertexConsumer q, Matrix4f m, double camX, double camY, double camZ,
                              double x1, double y1, double z1, double x2, double y2, double z2,
                              float hw, int ca, int cb) {
        float ax = (float) (x1 - camX), ay = (float) (y1 - camY), az = (float) (z1 - camZ);
        float bx = (float) (x2 - camX), by = (float) (y2 - camY), bz = (float) (z2 - camZ);
        float dx = bx - ax, dy = by - ay, dz = bz - az;
        float mx = (ax + bx) * 0.5f, my = (ay + by) * 0.5f, mz = (az + bz) * 0.5f;
        // сторона = направление x вектор от камеры к середине
        float sx = dy * mz - dz * my, sy = dz * mx - dx * mz, sz = dx * my - dy * mx;
        float len = (float) Math.sqrt(sx * sx + sy * sy + sz * sz);
        if (len < 1.0e-6f) return;
        float dist = (float) Math.sqrt(mx * mx + my * my + mz * mz);
        float k = hw * (1f + dist / 24f) / len;     // чем дальше, тем шире, чтобы линии не пропадали
        sx *= k; sy *= k; sz *= k;
        quad2(q, m, ax - sx, ay - sy, az - sz, ca, ax + sx, ay + sy, az + sz, ca,
                    bx + sx, by + sy, bz + sz, cb, bx - sx, by - sy, bz - sz, cb);
    }

    /** Рёбра коробки. */
    public static void edges(VertexConsumer q, Matrix4f m, double cx, double cy, double cz,
                             double x1, double y1, double z1, double x2, double y2, double z2,
                             float hw, int c) {
        // нижняя и верхняя рамки
        ribbon(q, m, cx, cy, cz, x1, y1, z1, x2, y1, z1, hw, c, c);
        ribbon(q, m, cx, cy, cz, x2, y1, z1, x2, y1, z2, hw, c, c);
        ribbon(q, m, cx, cy, cz, x2, y1, z2, x1, y1, z2, hw, c, c);
        ribbon(q, m, cx, cy, cz, x1, y1, z2, x1, y1, z1, hw, c, c);
        ribbon(q, m, cx, cy, cz, x1, y2, z1, x2, y2, z1, hw, c, c);
        ribbon(q, m, cx, cy, cz, x2, y2, z1, x2, y2, z2, hw, c, c);
        ribbon(q, m, cx, cy, cz, x2, y2, z2, x1, y2, z2, hw, c, c);
        ribbon(q, m, cx, cy, cz, x1, y2, z2, x1, y2, z1, hw, c, c);
        // вертикальные рёбра
        ribbon(q, m, cx, cy, cz, x1, y1, z1, x1, y2, z1, hw, c, c);
        ribbon(q, m, cx, cy, cz, x2, y1, z1, x2, y2, z1, hw, c, c);
        ribbon(q, m, cx, cy, cz, x2, y1, z2, x2, y2, z2, hw, c, c);
        ribbon(q, m, cx, cy, cz, x1, y1, z2, x1, y2, z2, hw, c, c);
    }

    /** Полупрозрачная заливка граней коробки. */
    public static void fill(VertexConsumer q, Matrix4f m, double cx, double cy, double cz,
                            double x1, double y1, double z1, double x2, double y2, double z2, int c) {
        float ax = (float) (x1 - cx), ay = (float) (y1 - cy), az = (float) (z1 - cz);
        float bx = (float) (x2 - cx), by = (float) (y2 - cy), bz = (float) (z2 - cz);
        quad2(q, m, ax, ay, az, c, bx, ay, az, c, bx, ay, bz, c, ax, ay, bz, c);   // низ
        quad2(q, m, ax, by, az, c, bx, by, az, c, bx, by, bz, c, ax, by, bz, c);   // верх
        quad2(q, m, ax, ay, az, c, bx, ay, az, c, bx, by, az, c, ax, by, az, c);   // север
        quad2(q, m, ax, ay, bz, c, bx, ay, bz, c, bx, by, bz, c, ax, by, bz, c);   // юг
        quad2(q, m, ax, ay, az, c, ax, ay, bz, c, ax, by, bz, c, ax, by, az, c);   // запад
        quad2(q, m, bx, ay, az, c, bx, ay, bz, c, bx, by, bz, c, bx, by, az, c);   // восток
    }
}
