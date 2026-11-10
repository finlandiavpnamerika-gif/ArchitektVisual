package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Проекция точки мира на экран (в масштабированных GUI-координатах). */
public final class Projection {
    private final Vec3d cp;
    private final Quaternionf inv;
    private final float f;
    public final int sw, sh;

    private Projection(Vec3d cp, Quaternionf inv, float f, int sw, int sh) {
        this.cp = cp; this.inv = inv; this.f = f; this.sw = sw; this.sh = sh;
    }

    public static Projection of(MinecraftClient mc) {
        Camera cam = mc.gameRenderer.getCamera();
        if (cam == null || !cam.isReady()) return null;
        int sw = mc.getWindow().getScaledWidth(), sh = mc.getWindow().getScaledHeight();
        float fov = (float) Math.toRadians(mc.options.getFov().getValue());
        float f = (sh / 2f) / (float) Math.tan(fov / 2f);
        return new Projection(cam.getCameraPos(), cam.getRotation().conjugate(new Quaternionf()), f, sw, sh);
    }

    /** Возвращает {x, y, расстояние} или null, если точка позади камеры. */
    public float[] project(double x, double y, double z) {
        double dx = x - cp.x, dy = y - cp.y, dz = z - cp.z;
        Vector3f v = new Vector3f((float) dx, (float) dy, (float) dz);
        inv.transform(v);
        if (v.z >= -0.05f) return null;
        float sx = sw / 2f + v.x / -v.z * f;
        float sy = sh / 2f - v.y / -v.z * f;
        return new float[] {sx, sy, (float) Math.sqrt(dx * dx + dy * dy + dz * dz)};
    }
}
