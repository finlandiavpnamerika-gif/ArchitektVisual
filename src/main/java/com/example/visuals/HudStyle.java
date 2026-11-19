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

    /** Тёмная «сланцевая» поверхность панелей и тонкая светлая рамка. */
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
        return HudStyle.width(tr, icon) + 4 + HudStyle.width(tr, text);
    }

    /** Заголовок панели: иконка цвета акцента + белый текст. @return правый край заголовка. */
    public static int title(class_332 g, class_327 tr, String icon, String text, int x, int y, float a, int accent) {
        text(g, tr, icon, x, y, accent, a, true);
        int tx = x + HudStyle.width(tr, icon) + 4;
        text(g, tr, text, tx, y, Theme.TEXT, a, true);
        return tx + HudStyle.width(tr, text);
    }

    public static int chipWidth(class_327 tr, String s) {
        return HudStyle.width(tr, s) + 8;
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
        text(g, tr, ch, x + (size - HudStyle.width(tr, ch)) / 2, y + (size - 8) / 2, Theme.TEXT, a, true);
    }

    // ---------- логотип ----------

    /** Фирменный знак «A» (красный). Хранится как маска прозрачности 48x42 (по байту на пиксель, hex). */
    private static final int LOGO_SW = 48, LOGO_SH = 42;
    private static final int LOGO_RGB = 0xFB0101;
    private static final String LOGO_HEX =
            "000000000000000000000000000000000000000000000000000000000000000017000000000000000000000000000000" +
            "0000000000000000000000000000000000000000000000000000000000000383ED130000000000000000000000000000" +
            "000000000000000000000000000000000000000000000000000000000017BDFFFF880000000000000000000000000000" +
            "000000000000000000000000000000000000000000000000000000003FE6FFFFFFF10F00000000000000000000000000" +
            "00000000000000000000000000000000000000000000000000000178FBFFFFFFFFFF6E00000000000000000000000000" +
            "0000000000000000000000000000000000000000000000000013B5FFFFFFFFFFFFFFCE00000000000000000000000000" +
            "00000000000000000000000000000000000000000000000037E1FFFFFFFFFFFFFFFFFF27000000000000000000000000" +
            "00000000000000000000000000000000000000000000006DF9FFFFFFFFFFFFFFFFFFFF76000000000000000000000000" +
            "0000000000000000000000000000000000000000000DA8FFFFFFFFFFFFFFFFFFFFFFFFBE000000000000000000000000" +
            "00000000000000000000000000000000000000002BD7FFFFFFFFFFFFFDEAFFFFFFFFFFF7020000000000000000000000" +
            "000000000000000000000000000000000000005CF4FFFFFFFFFFFFE8468EFFFFFFFFFFFF2B0000000000000000000000" +
            "00000000000000000000000000000000000796FFFFFFFFFFFFFFBE190072FFFFFFFFFFFF5C0000000000000000001912" +
            "000000000000000000000000000000001FCAFFFFFFFFFFFFFE8603000059FFFFFFFFFFFF89000000000000000A78AA00" +
            "00000000000000000000000000000046ECFFFFFFFFFFFFF1500000000040FFFFFFFFFFFFAF00000000000E74E7E81A00" +
            "000000000000000000000000000179FCFFFFFFFFFFFFD929000000000028FFFFFFFFF6C2690000003395F1FFF3360000" +
            "0000000000000000000000000FB0FFFFFFFFFFFFFFB50F0000000000000EB18C613205000A438AD9FFFFFFED3A000000" +
            "00000000000000000000002BD8FFFFFFFFFFFFFE8804220F030000000006172F4D719CCDFBFFFFFFFFFFCD2400000000" +
            "0000000000000000000059F4FFFFFFFFFFFFF75C0072FFFFFDF5EFF0F8FEFFFFFFFFFFFFFFFFFFFFF77E060000000000" +
            "0000000000000000048DFFFFFFFFFFFFFFE8390028F7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFA9D1F00000000000000" +
            "0000000000000013BCFFFFFFFFFFFFFFD3200002C6FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFE4851D000000000000000000" +
            "0000000000002DDDFFFFFFFFFFFFFFB90E00006CFFFFFFFFFFFFFFFFFFFFFFFFFFE89A45030000000000000000000000" +
            "000000000050F3FFFFFFFFFFFFFF9B04000002A7EFFFFFFFFFFFFFFFFFE4B276340200196BB91D000000000000000000" +
            "000000017AFDFFFFFFFFFFFFFE7C000000000000011F40565F5A4C33120000001569C1FDFFFF3F000000000000000000" +
            "000007A3FFFFFFFFFFFFFFF95C00000000000000000000000000000000003FB9FBFFFFFFFFFF63000000000000000000" +
            "0015C5FFFFFFFFFFFFFFEC3D00000000000000000000000000000000000094FFFFFFFFFFFFFF84000000000000000000" +
            "16DEFFFFFFFFFFFFFFDE2700000000000000000000000000000000000000A5FFFFFFFFFFFFFFA6000000000000000000" +
            "003FC8FFFFFFFFFFCF190000000000000000000000000000000000000000B6FFFFFFFFFFFFFFC8000000000000000000" +
            "0000014FD0FFFFBE0E000000000000000000000000000000000000000000C7FFFFFFFFFFFFFFE9000000000000000000" +
            "0000000001528D0700000000000000000000000000000000000000000000D9FFFFFFFFFFFFFFFE0C0000000000000000" +
            "000000000000000000000000000000000000000000000000000000000000EAFFFFFFFFFFFFFFFF2F0000000000000000" +
            "000000000000000000000000000000000000000000000000000000000001FAFFFFFFFFFFFFFFFF530000000000000000" +
            "00000000000000000000000000000000000000000000000000000000000CFFFFFFFFFFFFFFFFFF780000000000000000" +
            "00000000000000000000000000000000000000000000000000000000001DFFFFFFFFFFFFFFFFFF9F0000000000000000" +
            "00000000000000000000000000000000000000000000000000000000002EFFFFFFFFFFFFFFFFFFC60000000000000000" +
            "00000000000000000000000000000000000000000000000000000000003EFFFFFFFFFFFFFFFFFFEF0000000000000000" +
            "00000000000000000000000000000000000000000000000000000000004DFFFFFFFFFFFFFFFFFFFF1900000000000000" +
            "00000000000000000000000000000000000000000000000000000000005DFFFFFFFFFFFFFFFFFFFF4300000000000000" +
            "00000000000000000000000000000000000000000000000000000000006DFFFFFFFFFFFFFFFFFFFF6D00000000000000" +
            "00000000000000000000000000000000000000000000000000000000006CFEFFFFFFFFFFFFFFFFFF9900000000000000" +
            "0000000000000000000000000000000000000000000000000000000000001C62A6E6FFFFFFFFFFFFC500000000000000" +
            "00000000000000000000000000000000000000000000000000000000000000000001255E94C8F5FFF102000000000000" +
            "000000000000000000000000000000000000000000000000000000000000000000000000000003234706000000000000";

    private static byte[] logoMask;
    private static int[] logoCache;
    private static int logoCacheW = -1, logoCacheH = -1;

    /** Ширина логотипа при высоте h (сохраняет пропорции). */
    public static int logoWidth(int h) {
        return Math.max(1, Math.round(h * (float) LOGO_SW / LOGO_SH));
    }

    /** Маска, уменьшенная до tw x th усреднением по области (получается мягкий край). */
    private static int[] logoPixels(int tw, int th) {
        if (logoCache != null && logoCacheW == tw && logoCacheH == th) return logoCache;
        if (logoMask == null) {
            byte[] m = new byte[LOGO_SW * LOGO_SH];
            for (int i = 0; i < m.length; i++) m[i] = (byte) Integer.parseInt(LOGO_HEX.substring(i * 2, i * 2 + 2), 16);
            logoMask = m;
        }
        int[] out = new int[tw * th];
        for (int ty = 0; ty < th; ty++) {
            float sy0 = ty * (float) LOGO_SH / th, sy1 = (ty + 1) * (float) LOGO_SH / th;
            for (int tx = 0; tx < tw; tx++) {
                float sx0 = tx * (float) LOGO_SW / tw, sx1 = (tx + 1) * (float) LOGO_SW / tw;
                float sum = 0f, wsum = 0f;
                for (int sy = (int) sy0; sy < Math.ceil(sy1) && sy < LOGO_SH; sy++) {
                    float wy = Math.min(sy + 1, sy1) - Math.max(sy, sy0);
                    for (int sx = (int) sx0; sx < Math.ceil(sx1) && sx < LOGO_SW; sx++) {
                        float wx = Math.min(sx + 1, sx1) - Math.max(sx, sx0);
                        float w = wx * wy;
                        sum += (logoMask[sy * LOGO_SW + sx] & 0xFF) * w;
                        wsum += w;
                    }
                }
                out[ty * tw + tx] = wsum <= 0f ? 0 : Math.min(255, Math.round(sum / wsum));
            }
        }
        logoCache = out; logoCacheW = tw; logoCacheH = th;
        return out;
    }

    /** Рисует логотип высотой h с левым верхним углом (x, y). Ширина - {@link #logoWidth(int)}. */
    public static void logo(class_332 g, int x, int y, int h, float a) {
        int tw = logoWidth(h);
        int[] px = logoPixels(tw, h);
        for (int ty = 0; ty < h; ty++) {
            int tx = 0;
            while (tx < tw) {
                int al = px[ty * tw + tx];
                int run = tx + 1;
                while (run < tw && px[ty * tw + run] == al) run++;
                if (al > 0) g.method_25294(x + tx, y + ty, x + run, y + ty + 1,
                        withAlpha(0xFF000000 | LOGO_RGB, a * al / 255f));
                tx = run;
            }
        }
    }

    // ---------- текст ----------

    /** Ширина строки в шрифте HUD. */
    public static int width(class_327 tr, String s) {
        return HudFont.width(tr, s);
    }

    /** Обрезает строку по ширине (с «…»), учитывая шрифт HUD. */
    public static String trim(class_327 tr, String s, int maxW) {
        if (width(tr, s) <= maxW) return s;
        int e = s.length();
        while (e > 0 && width(tr, s.substring(0, e) + "…") > maxW) e--;
        return s.substring(0, e) + "…";
    }

    public static void text(class_332 g, class_327 tr, String s, int x, int y, int color, float a, boolean shadow) {
        int c = withAlpha(color, a);
        if (HudFont.draw(g, tr, s, x, y, c, shadow)) return;
        if (shadow) g.method_25303(tr, s, x, y, c);
        else g.method_51433(tr, s, x, y, c, false);
    }

    /** Текст, прижатый правым краем к right. */
    public static void textRight(class_332 g, class_327 tr, String s, int right, int y, int color, float a, boolean shadow) {
        text(g, tr, s, right - HudStyle.width(tr, s), y, color, a, shadow);
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
            int tw = HudStyle.width(tr, label);
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
