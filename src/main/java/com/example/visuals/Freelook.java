package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;

/**
 * Freelook: пока зажата клавиша, камера крутится вокруг игрока отдельно от его поворота.
 * Персонаж не поворачивается и идёт туда, куда смотрел. Отпустил - камера плавно возвращается.
 * Управляется модулем "Freelook" в меню (Config.thirdPerson) и клавишей VisualsClient.freelookKey.
 */
public final class Freelook {
    private Freelook() {}

    /** клавиша зажата: мышь крутит камеру, а не игрока */
    public static boolean active = false;
    /** клавишу отпустили: камера плавно едет обратно за спину игрока */
    private static boolean returning = false;

    public static float yaw, pitch;
    private static Perspective prev = null;
    private static long lastNanos = 0L;

    /** камера должна использовать yaw/pitch отсюда */
    public static boolean overriding() { return active || returning; }

    /** вызывается каждый тик из Features.tick */
    public static void tick(MinecraftClient mc) {
        if (mc.player == null || mc.options == null) {
            if (overriding()) reset(mc);
            return;
        }
        if (!Config.thirdPerson) {
            if (overriding()) reset(mc);
            return;
        }

        boolean want = mc.currentScreen == null && VisualsClient.freelookKey.isPressed();

        if (want && !active) {
            if (!returning) {                       // новый заход (не продолжение возврата)
                prev = mc.options.getPerspective();
                if (prev == Perspective.FIRST_PERSON) mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                yaw = mc.player.getYaw();
                pitch = mc.player.getPitch();
            }
            active = true;
            returning = false;
        } else if (!want && active) {
            active = false;
            returning = true;
            lastNanos = System.nanoTime();
        }
    }

    /** мышь: вместо поворота игрока крутим камеру (вызывается из FreelookEntityMixin) */
    public static void rotate(double dx, double dy) {
        yaw += (float) dx * 0.15f;
        pitch = MathHelper.clamp(pitch + (float) dy * 0.15f, -90.0f, 90.0f);
    }

    /** вызывается из FreelookCameraMixin раз за кадр: плавный возврат камеры */
    public static void frame() {
        if (!returning) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) { reset(mc); return; }

        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - lastNanos) / 1.0e9f);
        lastNanos = now;
        float f = 1f - (float) Math.exp(-dt * 14f);

        float dyaw = MathHelper.wrapDegrees(mc.player.getYaw() - yaw);
        float dpitch = mc.player.getPitch() - pitch;
        yaw += dyaw * f;
        pitch += dpitch * f;

        if (Math.abs(dyaw) < 0.6f && Math.abs(dpitch) < 0.6f) reset(mc);
    }

    private static void reset(MinecraftClient mc) {
        active = false;
        returning = false;
        if (prev != null && mc != null && mc.options != null) mc.options.setPerspective(prev);
        prev = null;
    }
}
