package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

/**
 * Freelook: камера крутится вокруг игрока отдельно от его поворота.
 * Персонаж не поворачивается и идёт туда, куда смотрел.
 *
 * Включение (модуль "Freelook" в меню должен быть включён):
 *  - клавиша: зажал = Freelook пока держишь; быстрый тап = закрепить (ещё тап = выключить)
 *  - команда /freelook = закрепить/выключить (работает и без клавиатуры)
 * Отпустил / выключил - камера плавно возвращается за спину игрока.
 */
public final class Freelook {
    private Freelook() {}

    private static final long TAP_MS = 250L;

    /** Freelook работает: мышь крутит камеру, а не игрока */
    public static boolean active = false;
    /** плавный возврат камеры к игроку */
    private static boolean returning = false;
    /** закреплён тапом по клавише или командой */
    private static boolean latched = false;

    private static boolean keyPrev = false;
    private static long keyPressedAt = 0L;

    public static float yaw, pitch;
    private static Perspective prev = null;
    private static long lastNanos = 0L;

    /** камера должна использовать yaw/pitch отсюда */
    public static boolean overriding() { return active || returning; }

    /** команда /freelook */
    public static void toggleLatch() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (!Config.thirdPerson) {
            mc.player.sendMessage(Text.literal("Freelook: сначала включи модуль в меню"), true);
            return;
        }
        latched = !latched;
    }

    /** вызывается каждый тик из Features.tick */
    public static void tick(MinecraftClient mc) {
        if (mc.player == null || mc.options == null) {
            latched = false;
            keyPrev = false;
            if (overriding()) reset(mc);
            return;
        }
        if (!Config.thirdPerson) {
            latched = false;
            keyPrev = false;
            if (overriding()) reset(mc);
            return;
        }

        boolean down = mc.currentScreen == null && VisualsClient.freelookKey.isPressed();
        long now = System.currentTimeMillis();
        if (down && !keyPrev) keyPressedAt = now;
        if (!down && keyPrev && now - keyPressedAt < TAP_MS) latched = !latched;   // быстрый тап = закрепить / снять
        keyPrev = down;

        boolean want = mc.currentScreen == null && (down || latched);

        if (want && !active) {
            if (!returning) {                       // новый заход (не продолжение возврата)
                prev = mc.options.getPerspective();
                if (prev == Perspective.FIRST_PERSON) mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                yaw = mc.player.getYaw();
                pitch = mc.player.getPitch();
            }
            active = true;
            returning = false;
            mc.player.sendMessage(Text.literal("Freelook: ВКЛ"), true);
        } else if (!want && active) {
            active = false;
            returning = true;
            lastNanos = System.nanoTime();
            mc.player.sendMessage(Text.literal("Freelook: ВЫКЛ"), true);
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
