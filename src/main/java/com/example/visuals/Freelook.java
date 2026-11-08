package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

/**
 * Freelook: камера крутится вокруг игрока отдельно от его поворота.
 * Игрок не поворачивается и идёт туда, куда смотрел до включения.
 *
 * Включение (модуль "Freelook" в меню должен быть включён):
 *  - клавиша (по умолчанию Left Alt): режим "зажатие" - работает, пока держишь;
 *    режим "переключатель" - нажал = включил, нажал ещё раз = выключил
 *  - команда /freelook: включить/выключить в любом режиме
 * Выключение: камера плавно возвращается за спину игрока (или сразу, если выключен плавный возврат).
 */
public final class Freelook {
    private Freelook() {}

    /** мышь крутит камеру, а не игрока */
    public static boolean active = false;
    /** камера плавно возвращается к игроку */
    private static boolean returning = false;
    /** включён командой или переключателем */
    private static boolean latched = false;

    public static float yaw, pitch;
    /** поворот игрока на момент включения: пока Freelook работает, он не меняется */
    private static float lockYaw, lockPitch;

    private static Perspective prev = null;
    private static boolean forced = false;     // мы сами переключили вид на третье лицо
    private static long lastNanos = 0L;

    /** камера должна брать yaw/pitch отсюда */
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

    /** каждый тик (из Features.tick) */
    public static void tick(MinecraftClient mc) {
        if (mc.options == null || mc.player == null || mc.world == null || !mc.player.isAlive() || !Config.thirdPerson) {
            latched = false;
            if (overriding()) reset(mc);
            if (VisualsClient.freelookKey != null) while (VisualsClient.freelookKey.wasPressed()) { /* сброс очереди нажатий */ }
            return;
        }

        boolean pressed = false;
        while (VisualsClient.freelookKey.wasPressed()) pressed = true;     // нажатия за тик (для переключателя)
        if (mc.currentScreen != null) return;                              // в меню/инвентаре состояние не меняем

        boolean want;
        if (Config.freelookToggle) {
            if (pressed) latched = !latched;
            want = latched;
        } else {
            want = VisualsClient.freelookKey.isPressed() || latched;
        }

        if (want && !active) start(mc);
        else if (!want && active) stop(mc);
    }

    private static void start(MinecraftClient mc) {
        if (!returning) {                                   // новый заход, а не продолжение возврата
            prev = mc.options.getPerspective();
            forced = false;
            if (Config.freelookThird && prev == Perspective.FIRST_PERSON) {
                mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                forced = true;
            }
            yaw = mc.player.getYaw();
            pitch = mc.player.getPitch();
        }
        lockYaw = mc.player.getYaw();
        lockPitch = mc.player.getPitch();
        active = true;
        returning = false;
        mc.player.sendMessage(Text.literal("Freelook: ВКЛ"), true);
    }

    private static void stop(MinecraftClient mc) {
        active = false;
        mc.player.sendMessage(Text.literal("Freelook: ВЫКЛ"), true);
        if (Config.freelookSmooth) {
            returning = true;
            lastNanos = System.nanoTime();
        } else {
            reset(mc);
        }
    }

    /** мышь: вместо поворота игрока крутим камеру (из FreelookEntityMixin) */
    public static void rotate(double dx, double dy) {
        float k = 0.15f * (float) Config.freelookSens;
        yaw += (float) dx * k;
        pitch = MathHelper.clamp(pitch + (float) dy * k, -90.0f, 90.0f);
    }

    /** раз за кадр из FreelookCameraMixin */
    public static void frame() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) { reset(mc); return; }

        if (active) {
            // Страховка: если игрок всё же повернулся (не от мыши) - переносим поворот на камеру
            // и возвращаем игроку прежний, чтобы персонаж не крутился вместе с камерой.
            float dy = MathHelper.wrapDegrees(mc.player.getYaw() - lockYaw);
            float dp = mc.player.getPitch() - lockPitch;
            if (dy != 0f || dp != 0f) {
                yaw += dy;
                pitch = MathHelper.clamp(pitch + dp, -90.0f, 90.0f);
                mc.player.setYaw(lockYaw);
                mc.player.setPitch(lockPitch);
            }
            return;
        }
        if (!returning) return;

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
        // вид возвращаем, только если игрок сам не сменил его (F5) за это время
        if (forced && prev != null && mc != null && mc.options != null
                && mc.options.getPerspective() == Perspective.THIRD_PERSON_BACK) {
            mc.options.setPerspective(prev);
        }
        forced = false;
        prev = null;
    }
}
