package com.example.visuals;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/** Общие функции рендера для всех HUD-элементов. Единый стиль. */
public final class HudUtils {
    private HudUtils() {}

    /** Рисует HUD-плашку с полоской слева. */
    public static void panel(DrawContext g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, Theme.HUD_BG);
        g.fill(x, y, x + 2, y + h, Theme.HUD_BAR);
    }

    /** Рисует HUD-плашку с полоской слева и заголовком. */
    public static void panelWithTitle(DrawContext g, TextRenderer tr, int x, int y, int w, int h, String title) {
        panel(g, x, y, w, h);
        g.drawTextWithShadow(tr, title, x + 6, y + 3, Theme.HUD_BAR);
    }

    /** Рисует строку текста с тенью или без. */
    public static void text(DrawContext g, TextRenderer tr, String s, int x, int y, int color) {
        if (Theme.HUD_SHADOW) {
            g.drawTextWithShadow(tr, s, x, y, color);
        } else {
            g.drawText(tr, s, x, y, color, false);
        }
    }

    /** Закруглённый прямоугольник (эмуляция через срез углов). */
    public static void roundedRect(DrawContext g, int x, int y, int w, int h, int color) {
        g.fill(x + 1, y, x + w - 1, y + h, color);
        g.fill(x, y + 1, x + w, y + h - 1, color);
    }

    /** Квадрат с рамкой (для Keystrokes). */
    public static void keyBox(DrawContext g, TextRenderer tr, int x, int y, int size,
                              String label, boolean pressed, int accent) {
        int bg = pressed ? accent : 0x80000000;
        int border = pressed ? 0xFFFFFFFF : 0x40FFFFFF;

        g.fill(x, y, x + size, y + size, border);
        g.fill(x + 1, y + 1, x + size - 1, y + size - 1, bg);

        if (!label.isEmpty()) {
            int color = pressed ? 0xFF000000 : 0xFFFFFFFF;
            int tw = tr.getWidth(label);
            g.drawTextWithShadow(tr, label,
                    x + (size - tw) / 2,
                    y + (size - 8) / 2,
                    color);
        }
    }

    /** Полоска прочности (для Armor HUD). */
    public static void durabilityBar(DrawContext g, int x, int y, int w, float ratio) {
        g.fill(x, y, x + w, y + 2, 0xFF000000);
        int color = ratio > 0.5f ? 0xFF00FF00 : ratio > 0.25f ? 0xFFFFFF00 : 0xFFFF0000;
        int filled = (int) (w * Math.max(0f, Math.min(1f, ratio)));
        g.fill(x, y, x + filled, y + 2, color);
    }

    /** Подсветка строки модуля (hover). */
    public static void rowBg(DrawContext g, int x, int y, int w, int h, boolean hover, boolean on) {
        int bg = on ? Theme.ROW_ON : Theme.ROW;
        if (hover) bg = Theme.ROW_HOVER;
        g.fill(x, y, x + w, y + h, bg);
    }

    /** Акцентная линия под заголовком. */
    public static void accentLine(DrawContext g, int x, int y, int w) {
        g.fill(x, y, x + w, y + 1, Theme.ACCENT);
    }
}
