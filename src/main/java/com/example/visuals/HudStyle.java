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
    public static final int R = 4;

    /** Тёмная «сланцевая» поверхность панелей и тонкая светлая рамка (стиль Pulse). */
    public static final int SURFACE = 0xD2191B28;
    public static final int BORDER  = 0x22FFFFFF;
    /** Нейтральный текст второго плана. */
    public static final int MUTED   = 0xFF9498AA;

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

    /** Панель: мягкая тень, тёмная поверхность и тонкая светлая рамка. a - прозрачность. accent сохранён для совместимости. */
    public static void panel(class_332 g, int x, int y, int w, int h, float a, int accent) {
        roundFill(g, x - 1, y + 1, w + 2, h + 3, R + 1, withAlpha(0x14000000, a));
        roundFill(g, x, y + 2, w, h, R, withAlpha(0x30000000, a));
        roundFill(g, x, y, w, h, R, withAlpha(SURFACE, a));
        roundRing(g, x, y, w, h, R, withAlpha(BORDER, a));
    }

    /** Верхняя полоса панели: едва заметный светлый градиент (акцент больше не заливает шапку). */
    public static void header(class_332 g, int x, int y, int w, int h, float a, int accent) {
        g.method_25296(x + 1, y + 1, x + w - 1, y + h,
                withAlpha(0x12FFFFFF, a), withAlpha(0x00FFFFFF, a));
    }

    /** Тонкий разделитель. */
    public static void divider(class_332 g, int x1, int x2, int y, int color, float a) {
        g.method_25294(x1, y, x2, y + 1, withAlpha(color, a * 0.45f));
    }

    // ---------- заголовок, чипы, аватар, капсулы ----------

    /** Иконка заголовка по названию панели. */
    public static String iconFor(String title) {
        String t = title == null ? "" : title.toLowerCase();
        if (t.contains("server") || t.contains("near") || t.contains("player")) return Theme.ICON_WORLD;
        if (t.contains("clock") || t.contains("stat") || t.contains("damage") || t.contains("combat")) return Theme.ICON_UTILS;
        return Theme.ICON_VISUAL;
    }

    public static int titleWidth(class_327 tr, String icon, String text) {
        return tr.method_1727(icon) + 4 + tr.method_1727(text);
    }

    /** Заголовок панели: иконка цвета акцента + белый текст. @return правый край заголовка. */
    public static int title(class_332 g, class_327 tr, String icon, String text, int x, int y, float a, int accent) {
        text(g, tr, icon, x, y, accent, a, true);
        int tx = x + tr.method_1727(icon) + 4;
        text(g, tr, text, tx, y, Theme.TEXT, a, true);
        return tx + tr.method_1727(text);
    }

    public static int chipWidth(class_327 tr, String s) {
        return tr.method_1727(s) + 8;
    }

    /** Маленькая тонированная метка (высота 11). @return её ширину. */
    public static int chip(class_332 g, class_327 tr, String s, int x, int y, int color, float a) {
        int w = chipWidth(tr, s);
        roundFill(g, x, y, w, 11, 3, withAlpha(withAlpha(color, 0.22f), a));
        text(g, tr, s, x + 4, y + 2, color, a, false);
        return w;
    }

    /** Полностью скруглённая плашка (капсула): тень, поверхность, рамка. */
    public static void capsule(class_332 g, int x, int y, int w, int h, float a) {
        int r = h / 2;
        roundFill(g, x - 1, y + 1, w + 2, h + 3, r + 1, withAlpha(0x14000000, a));
        roundFill(g, x, y + 2, w, h, r, withAlpha(0x30000000, a));
        roundFill(g, x, y, w, h, r, withAlpha(SURFACE, a));
        roundRing(g, x, y, w, h, r, withAlpha(BORDER, a));
    }

    /** Квадратный аватар с первой буквой ника. */
    public static void avatar(class_332 g, class_327 tr, String name, int x, int y, int size, float a, int accent) {
        roundFill(g, x, y, size, size, 4, withAlpha(withAlpha(accent, 0.30f), a));
        roundRing(g, x, y, size, size, 4, withAlpha(withAlpha(accent, 0.65f), a));
        String ch = name == null || name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
        text(g, tr, ch, x + (size - tr.method_1727(ch)) / 2, y + (size - 8) / 2, Theme.TEXT, a, true);
    }

    /** Фирменная «пульс»-линия 16x9. (x, y) - левый верхний угол. */
    public static void pulseLogo(class_332 g, int x, int y, int color, float a) {
        int[] xs = {0, 4, 6, 9, 12, 14, 16};
        int[] ys = {5, 5, 0, 8, 3, 5, 5};
        int c = withAlpha(color, a);
        for (int k = 0; k + 1 < xs.length; k++) {
            int steps = Math.max(Math.abs(xs[k + 1] - xs[k]), Math.abs(ys[k + 1] - ys[k]));
            for (int t = 0; t <= steps; t++) {
                int px = x + xs[k] + (xs[k + 1] - xs[k]) * t / Math.max(1, steps);
                int py = y + ys[k] + (ys[k + 1] - ys[k]) * t / Math.max(1, steps);
                g.method_25294(px, py, px + 1, py + 1, c);
            }
        }
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
        int r = Math.max(1, h / 2);
        roundFill(g, x, y, w, h, r, withAlpha(0x34FFFFFF, a));
        int filled = Math.round(w * Math.max(0f, Math.min(1f, ratio)));
        if (filled > 0) roundFill(g, x, y, Math.max(filled, Math.min(w, 2 * r)), h, r, withAlpha(color, a));
    }

    /** Клавиша для Keystrokes. */
    public static void keyBox(class_332 g, class_327 tr, int x, int y, int w, int h,
                              String label, boolean pressed, int accent, float a) {
        int bg = pressed ? withAlpha(accent, a * 0.95f) : withAlpha(SURFACE, a);
        roundFill(g, x, y, w, h, 3, bg);
        roundRing(g, x, y, w, h, 3, pressed ? withAlpha(0x66FFFFFF, a) : withAlpha(BORDER, a));
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
        roundFill(g, x + 1, y + 1, w, h, 3, withAlpha(0x30000000, a));
        roundFill(g, x, y, w, h, 3, withAlpha(SURFACE, a));
        roundRing(g, x, y, w, h, 3, withAlpha(BORDER, a));
        roundFill(g, x + 2, y + 2, 2, h - 4, 1, withAlpha(barColor, a));
    }
}
