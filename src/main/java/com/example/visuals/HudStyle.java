package com.example.visuals;

import net.minecraft.class_327;
import net.minecraft.class_332;

/**
 * Единый стиль всех HUD-элементов: скруглённые панели, тень, рамка, акцентная линия.
 * Прозрачность (alpha 0.1..1.0) умножается на альфу каждого цвета.
 */
public final class HudStyle {
    private HudStyle() {}

    /** Цвета для значений (FPS, пинг, прочность). */
    public static final int GOOD = 0xFF6CE08A;
    public static final int MID  = 0xFFF2D45C;
    public static final int BAD  = 0xFFFF6B6B;

    /** Радиус скругления панелей. */
    public static final int R = 3;

    // ---------- цвет ----------

    /** Ограничивает прозрачность из настроек: 0.1..1.0. */
    public static float clampAlpha(double a) {
        return (float) Math.max(0.1, Math.min(1.0, a));
    }

    /** Умножает альфу цвета ARGB на a (0..1). */
    public static int withAlpha(int argb, float a) {
        int al = Math.round(((argb >>> 24) & 0xFF) * Math.max(0f, Math.min(1f, a)));
        return (al << 24) | (argb & 0xFFFFFF);
    }

    /** Красный -> жёлтый -> зелёный по доле 0..1. */
    public static int ratioColor(float ratio) {
        float r = Math.max(0f, Math.min(1f, ratio));
        int rr = (int) (235 * Math.min(1f, (1f - r) * 2f));
        int gg = (int) (225 * Math.min(1f, r * 2f));
        return 0xFF000000 | (rr << 16) | (gg << 8) | 0x46;
    }

    /** Цвета по порогам: 100-75 салатовый, 75-50 зелёный, 50-25 жёлтый, 25-0 красный. */
    public static int durabilityColor(float ratio) {
        if (ratio > 0.75f) return 0xFF9DFF3A; // салатовое
        if (ratio > 0.50f) return 0xFF2EC83C; // зелёное
        if (ratio > 0.25f) return 0xFFFFD81F; // жёлтое
        return 0xFFFF3B30;                    // красное
    }

    // ---------- примитивы ----------

    /** Сколько пикселей срезать слева/справа в строке i (от края) у угла радиуса r. */
    private static int inset(int r, int i) {
        double dy = r - i - 0.25;
        double dx = Math.sqrt(Math.max(0.0, r * r - dy * dy));
        return Math.max(0, (int) Math.round(r - dx));
    }

    /** Залитый прямоугольник со скруглёнными углами (без наложения пикселей - прозрачность не "двоится"). */
    public static void roundFill(class_332 g, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) return;
        r = Math.min(r, Math.min(w, h) / 2);
        if (r <= 0) {
            g.method_25294(x, y, x + w, y + h, color);
            return;
        }
        g.method_25294(x, y + r, x + w, y + h - r, color);
        for (int i = 0; i < r; i++) {
            int o = inset(r, i);
            g.method_25294(x + o, y + i, x + w - o, y + i + 1, color);
            g.method_25294(x + o, y + h - 1 - i, x + w - o, y + h - i, color);
        }
    }

    /** Строка рамки номер row (сверху) для прямоугольника со скруглением r. */
    private static void ringRow(class_332 g, int x, int y, int w, int h, int r, int row, int color) {
        int o = inset(r, row < r ? row : h - 1 - row);
        if (row == 0 || row == h - 1) {
            g.method_25294(x + o, y + row, x + w - o, y + row + 1, color);
            return;
        }
        int ir = Math.max(0, r - 1);
        int ih = h - 2;
        int irow = row - 1;
        int io = 0;
        if (ir > 0) {
            if (irow < ir) io = inset(ir, irow);
            else if (irow >= ih - ir) io = inset(ir, ih - 1 - irow);
        }
        int left = 1 + io;
        g.method_25294(x + o, y + row, x + left, y + row + 1, color);
        g.method_25294(x + w - left, y + row, x + w - o, y + row + 1, color);
    }

    /** Рамка толщиной 1px со скруглёнными углами. */
    public static void roundRing(class_332 g, int x, int y, int w, int h, int r, int color) {
        if (w < 4 || h < 4) return;
        r = Math.min(r, Math.min(w, h) / 2 - 1);
        if (r <= 0) {
            rectRing(g, x, y, w, h, color);
            return;
        }
        for (int i = 0; i < r; i++) {
            ringRow(g, x, y, w, h, r, i, color);
            ringRow(g, x, y, w, h, r, h - 1 - i, color);
        }
        g.method_25294(x, y + r, x + 1, y + h - r, color);
        g.method_25294(x + w - 1, y + r, x + w, y + h - r, color);
    }

    /** Обычная прямоугольная рамка 1px. */
    public static void rectRing(class_332 g, int x, int y, int w, int h, int color) {
        g.method_25294(x, y, x + w, y + 1, color);
        g.method_25294(x, y + h - 1, x + w, y + h, color);
        g.method_25294(x, y + 1, x + 1, y + h - 1, color);
        g.method_25294(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    // ---------- панели ----------

    /** Панель: тень, фон, рамка в цвет акцента, яркая линия сверху. a - прозрачность. */
    public static void panel(class_332 g, int x, int y, int w, int h, float a, int accent) {
        roundFill(g, x + 1, y + 2, w, h, R, withAlpha(0x38000000, a));
        roundFill(g, x, y, w, h, R, withAlpha(Theme.BG, a));
        roundRing(g, x, y, w, h, R, withAlpha(withAlpha(accent, 0.45f), a));
        g.method_25294(x + R, y, x + w - R, y + 1, withAlpha(accent, a));
    }

    /** Полоса заголовка с мягким градиентом от акцента. */
    public static void header(class_332 g, int x, int y, int w, int h, float a, int accent) {
        g.method_25296(x + 1, y + 1, x + w - 1, y + h,
                withAlpha(withAlpha(accent, 0.28f), a), withAlpha(withAlpha(accent, 0.02f), a));
    }

    /** Тонкий разделитель. */
    public static void divider(class_332 g, int x1, int x2, int y, int color, float a) {
        g.method_25294(x1, y, x2, y + 1, withAlpha(color, a));
    }

    // ---------- текст ----------

    public static void text(class_332 g, class_327 tr, String s, int x, int y, int color, float a, boolean shadow) {
        int c = withAlpha(color, a);
        if (shadow) g.method_25303(tr, s, x, y, c);
        else g.method_51433(tr, s, x, y, c, false);
    }

    /** Текст, прижатый правым краем к right. */
    public static void textRight(class_332 g, class_327 tr, String s, int right, int y, int color, float a, boolean shadow) {
        text(g, tr, s, right - tr.method_1727(s), y, color, a, shadow);
    }

    // ---------- элементы ----------

    /** Полоска прочности/значения со скруглением. */
    public static void bar(class_332 g, int x, int y, int w, int h, float ratio, int color, float a) {
        roundFill(g, x, y, w, h, 1, withAlpha(0xA0000000, a));
        int filled = Math.round(w * Math.max(0f, Math.min(1f, ratio)));
        if (filled > 0) roundFill(g, x, y, filled, h, 1, withAlpha(color, a));
    }

    /** Клавиша для Keystrokes. */
    public static void keyBox(class_332 g, class_327 tr, int x, int y, int w, int h,
                              String label, boolean pressed, int accent, float a) {
        int bg = pressed ? withAlpha(accent, a * 0.92f) : withAlpha(0xA0101018, a);
        roundFill(g, x, y, w, h, 2, bg);
        roundRing(g, x, y, w, h, 2, pressed ? withAlpha(0xFFFFFFFF, a * 0.9f) : withAlpha(0x40FFFFFF, a));
        if (!label.isEmpty()) {
            int tw = tr.method_1727(label);
            text(g, tr, label, x + (w - tw) / 2, y + (h - 8) / 2,
                    pressed ? 0xFF101018 : 0xFFFFFFFF, a, !pressed);
        }
    }

    /** Смешивает два цвета ARGB (t 0..1). */
    public static int mix(int c0, int c1, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int a = (int) (((c0 >>> 24) & 255) + (((c1 >>> 24) & 255) - ((c0 >>> 24) & 255)) * t);
        int r = (int) (((c0 >> 16) & 255) + (((c1 >> 16) & 255) - ((c0 >> 16) & 255)) * t);
        int g = (int) (((c0 >> 8) & 255) + (((c1 >> 8) & 255) - ((c0 >> 8) & 255)) * t);
        int b = (int) ((c0 & 255) + ((c1 & 255) - (c0 & 255)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** Плашка: тень, фон, тонкая рамка и цветная полоска слева (barColor). */
    public static void pill(class_332 g, int x, int y, int w, int h, float a, int barColor) {
        roundFill(g, x + 1, y + 1, w, h, 2, withAlpha(0x30000000, a));
        roundFill(g, x, y, w, h, 2, withAlpha(Theme.BG, a));
        roundRing(g, x, y, w, h, 2, withAlpha(withAlpha(barColor, 0.35f), a));
        roundFill(g, x + 1, y + 2, 2, h - 4, 1, withAlpha(barColor, a));
    }
}
