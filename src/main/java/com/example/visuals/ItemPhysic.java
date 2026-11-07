package com.example.visuals;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import org.joml.Quaternionf;

/**
 * Item Physic: выпавшие предметы лежат на земле (как на референсе) и не покачиваются.
 * Плоские предметы (мечи, кирки, слитки...) ложатся плашмя, блоки просто стоят на земле без покачивания.
 * Миксин ItemEntityRendererMixin открывает "окно" на время отрисовки предмета (begin/end)
 * и вызывает apply() перед рисованием модели.
 */
public final class ItemPhysic {
    private ItemPhysic() {}

    /** высота центра плоского предмета над землёй, блоки */
    public static final double REST = 0.03;

    private static int depth = 0;
    private static float bob = 0f;

    public static void begin(float age, float uniqueOffset) {
        if (depth++ == 0) bob = (float) (Math.sin(age / 10.0 + uniqueOffset) * 0.1 + 0.1);   // то же покачивание, что в ваниле
    }

    public static void end() { if (depth > 0) depth--; }

    /** вызывается после ванильных translate (подъём+покачивание) и поворота вокруг Y */
    public static void apply(MatrixStack m, Box box) {
        if (!Config.itemPhysic || depth == 0) return;
        double lift = Config.physicLift / 100.0;
        m.translate(0.0, -bob + lift, 0.0);                       // убираем покачивание
        if (box != null && box.getLengthZ() < 0.12) {              // плоская модель
            m.translate(0.0, box.minY - 0.0325, 0.0);              // центр модели опускаем к земле
            m.multiply(new Quaternionf().rotationX((float) (-Math.PI / 2.0)));   // лицом вверх
        }
    }
}
