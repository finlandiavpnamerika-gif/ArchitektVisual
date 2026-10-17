package com.example.visuals;

/** Единый стиль ArchitektVisuals: зелёный неон + минимализм. */
public final class Theme {
    private Theme() {}

    // ================== ЦВЕТА (ArchitektVisuals) ==================

    /** Неон-зелёный акцент (#7CE38B). */
    public static final int ACCENT = 0xFF7CE38B;

    /** Акцент потемнее (для линий, теней). */
    public static final int ACCENT_DIM = 0x807CE38B;

    /** Зелёный тёмный (#4CAF50). */
    public static final int ACCENT_DARK = 0xFF4CAF50;

    /** Свечение (glow) — прозрачный зелёный. */
    public static final int GLOW = 0x407CE38B;

    /** Фон панелей меню (тёмный, полупрозрачный). */
    public static final int PANEL_BG = 0xC0101010;

    /** Граница панелей. */
    public static final int PANEL_BORDER = 0x407CE38B;

    /** Фон строки модуля. */
    public static final int ROW = 0x40000000;

    /** Фон строки при наведении. */
    public static final int ROW_HOVER = 0x60FFFFFF;

    /** Фон включённого модуля. */
    public static final int ROW_ON = 0x307CE38B;

    /** Текст обычный. */
    public static final int TEXT = 0xFFFFFFFF;

    /** Текст приглушённый. */
    public static final int TEXT_DIM = 0xFFAAAAAA;

    /** Галочка включённого модуля. */
    public static final int CHECK = 0xFF7CE38B;

    /** Цвет слайдера (заполнение). */
    public static final int SLIDER_FILL = 0xFF7CE38B;

    /** Цвет слайдера (фон). */
    public static final int SLIDER_BG = 0x40000000;

    /** Тумблер включён. */
    public static final int TOGGLE_ON = 0xFF7CE38B;

    /** Тумблер выключен. */
    public static final int TOGGLE_OFF = 0xFF444444;

    // ================== РАЗМЕРЫ МЕНЮ ==================

    public static final int PANEL_WIDTH = 130;
    public static final int PANEL_GAP = 6;
    public static final int TITLE_HEIGHT = 22;
    public static final int ROW_HEIGHT = 16;
    public static final int ROW_INDENT = 6;
    public static final int TOP_MARGIN = 12;

    // ================== ЦВЕТА HUD ==================

    /** Фон HUD-плашек. */
    public static final int HUD_BG = 0x90000000;

    /** Полоска слева у HUD-плашек. */
    public static final int HUD_BAR = 0xFF7CE38B;

    /** Тень текста в HUD. */
    public static final boolean HUD_SHADOW = true;

    // ================== ИКОНКИ КАТЕГОРИЙ ==================

    public static final String ICON_VISUAL = "\u2726";     // ✦
    public static final String ICON_WORLD = "\u25C9";      // ◉
    public static final String ICON_PARTICLES = "\u2727";  // ✧
    public static final String ICON_UTILS = "\u2699";      // ⚙
    public static final String ICON_CLIENT = "\u25C8";     // ◈

    // ================== ГАЛОЧКА ==================

    public static final String CHECK_MARK = "\u2714";      // ✔
}
