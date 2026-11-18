package com.example.visuals;

/**
 * Aspect Ratio: меняет соотношение сторон 3D-картинки (растяжка, как «растянутое разрешение» в CS).
 * Интерфейс (HUD, меню) не затрагивается: подменяется только aspect в матрице перспективы мира.
 * Подмена делается в {@link com.example.visuals.mixin.AspectRatioMixin}.
 *
 * Значение меньше родного (например, 4:3 на экране 16:9) растягивает картинку по горизонтали,
 * больше родного (21:9) - сжимает.
 */
public final class AspectRatio {
    private AspectRatio() {}

    /** Пресеты: 0 = своё значение со слайдера. */
    public static final String[] NAMES = {"Своё", "4:3", "5:4", "3:2", "16:10", "1:1", "21:9", "32:9"};
    private static final double[] VALUES = {0, 4.0 / 3.0, 5.0 / 4.0, 3.0 / 2.0, 16.0 / 10.0, 1.0, 21.0 / 9.0, 32.0 / 9.0};

    /** Текущее целевое соотношение сторон (ширина / высота). */
    public static double target() {
        int p = Config.aspectPreset;
        if (p > 0 && p < VALUES.length) return VALUES[p];
        return Config.aspectCustom;
    }

    /** @param original родной aspect, который собирался использовать Minecraft. */
    public static float apply(float original) {
        if (!Config.aspectOn) return original;
        double v = target();
        if (!(v >= 0.3 && v <= 6.0)) return original;   // защита от мусора в конфиге
        return (float) v;
    }
}
