package com.example.visuals;

/** Единый стиль всего мода: цвета, отступы, размеры. Меняешь тут — меняется везде. */
public final class Theme {
    private Theme() {}

    // ================== ЦВЕТА ==================

    /** Основной акцент (фиолетовый, как в референсе). */
    public static final int ACCENT = 0xFF8B5CF6;

    /** Акцент потемнее (для линий). */
    public static final int ACCENT_DIM = 0x708B5CF6;

    /** Фон панелей меню (полупрозрачный тёмный). */
    public static final int PANEL_BG = 0xC0000000;

    /** Граница панелей. */
    public static final int PANEL_BORDER = 0x40FFFFFF;

    /** Фон строки модуля. */
    public static final int ROW = 0x40000000;

    /** Фон строки при наведении. */
    public static final int ROW_HOVER = 0x60FFFFFF;

    /** Фон включённого модуля. */
    public static final int ROW_ON = 0x308B5CF6;

    /** Текст обычный. */
    public static final int TEXT = 0xFFFFFFFF;

    /** Текст приглушённый (подписи, подсказки). */
    public static final int TEXT_DIM = 0xFFAAAAAA;

    /** Галочка включённого модуля. */
    public static final int CHECK = 0xFF8B5CF6;

    /** Цвет слайдера (заполнение). */
    public static final int SLIDER_FILL = 0xFF8B5CF6;

    /** Цвет слайдера (фон). */
    public static final int SLIDER_BG = 0x40000000;

    /** Тумблер включён. */
    public static final int TOGGLE_ON = 0xFF8B5CF6;

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
    public static final int HUD_BAR = 0xFF8B5CF6;

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
