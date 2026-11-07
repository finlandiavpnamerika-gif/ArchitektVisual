package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.joml.Quaternionf;

/**
 * Hand View: меняет вид предмета в руке от первого лица (любой предмет, отдельно правая и левая рука).
 * Размер, смещение X/Y/Z, поворот X/Y/Z и отключение замаха. Применяется в HeldItemRendererMixin.
 */
public final class HandView {
    private HandView() {}

    public static final String[] HANDS = {"Правая", "Левая"};
    // строки массива Config.sv
    public static final int SCALE = 0, X = 1, Y = 2, Z = 3, RX = 4, RY = 5, RZ = 6;

    /** 0 = правая рука (справа на экране), 1 = левая. Учитывает ведущую руку игрока. */
    public static int side(Hand hand, AbstractClientPlayerEntity player) {
        boolean right = (hand == Hand.MAIN_HAND) == (player.getMainArm() == Arm.RIGHT);
        return right ? 0 : 1;
    }

    // ---- Левша: главная рука слева (ванильная настройка "Главная рука") ----
    private static Boolean applied = null;
    private static Arm origArm = Arm.RIGHT;

    /** Каждый тик клиента. */
    public static void tick(MinecraftClient mc) {
        if (mc.options == null) return;
        boolean want = Config.leftHanded;
        if (applied != null && applied == want) return;
        var opt = mc.options.getMainArm();
        if (want) {
            origArm = opt.getValue() == Arm.LEFT ? Arm.RIGHT : opt.getValue();
            opt.setValue(Arm.LEFT);
        } else if (applied != null) {
            opt.setValue(origArm);
        }
        applied = want;
    }

    public static void apply(MatrixStack m, int s) {
        double[] v = new double[7];
        for (int i = 0; i < 7; i++) v[i] = Config.sv[i][s];
        if (v[SCALE] == 1.0 && v[X] == 0 && v[Y] == 0 && v[Z] == 0 && v[RX] == 0 && v[RY] == 0 && v[RZ] == 0) return;

        double sign = s == 0 ? 1.0 : -1.0;
        // смещение в пространстве камеры: X вправо, Y вверх, Z вперёд (в сторону от тебя)
        m.translate(v[X] / 100.0, v[Y] / 100.0, -v[Z] / 100.0);
        // поворот и масштаб вокруг примерного положения предмета в руке
        double px = 0.56 * sign, py = -0.52, pz = -0.72;
        m.translate(px, py, pz);
        if (v[RX] != 0 || v[RY] != 0 || v[RZ] != 0) {
            m.multiply(new Quaternionf().rotationXYZ(
                    (float) Math.toRadians(v[RX]), (float) Math.toRadians(v[RY]), (float) Math.toRadians(v[RZ])));
        }
        float sc = (float) v[SCALE];
        m.scale(sc, sc, sc);
        m.translate(-px, -py, -pz);
    }
}
