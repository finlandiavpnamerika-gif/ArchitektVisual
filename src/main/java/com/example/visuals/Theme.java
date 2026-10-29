package com.example.visuals;

/** Единый стиль ArchitektVisuals: всё меняется здесь. */
public final class Theme {
    private Theme() {}

    // ================== ВЫБОР ПАЛИТРЫ ==================
    // 0 = неон-зелёный, 1 = фиолетовый Pulse, 2 = синий, 3 = минимализм (белый)
    public static int THEME_ID = 1;

    // ================== ПАЛИТРЫ ==================
    private static final int[][] PALETTES = {
        // ACCENT,     ACCENT_DIM,  BG,          ROW,         ROW_HOVER,   TEXT,        TEXT_DIM
        { 0xFF7CE38B, 0x807CE38B, 0xC0101010, 0x40000000, 0x60FFFFFF, 0xFFFFFFFF, 0xFFAAAAAA }, // 0 зелёный
        { 0xFF9C7CFF, 0x809C7CFF, 0xC0120A1E, 0x40000000, 0x60B07CFF, 0xFFFFFFFF, 0xFFB0A0D0 }, // 1 фиолетовый
        { 0xFF5A9CFF, 0x805A9CFF, 0xC00A0F1A, 0x40000000, 0x605A9CFF, 0xFFFFFFFF, 0xFFA0B0D0 }, // 2 синий
        { 0xFFFFFFFF, 0x80FFFFFF, 0xC0101010, 0x40000000, 0x60FFFFFF, 0xFFFFFFFF, 0xFFAAAAAA }  // 3 белый
    };

    // ================== ЦВЕТА (берутся из палитры) ==================
    public static int ACCENT     = PALETTES[THEME_ID][0];
    public static int ACCENT_DIM = PALETTES[THEME_ID][1];
    public static int BG         = PALETTES[THEME_ID][2];
    public static int ROW        = PALETTES[THEME_ID][3];
    public static int ROW_HOVER  = PALETTES[THEME_ID][4];
    public static int TEXT       = PALETTES[THEME_ID][5];
    public static int TEXT_DIM   = PALETTES[THEME_ID][6];

    // ================== ДОП. ЦВЕТА (можно менять вручную) ==================
    public static int ACCENT_DARK = 0xFF4CAF50;
    public static int GLOW        = 0x40FFFFFF;
    public static int PANEL_BG    = BG;
    public static int PANEL_BORDER = ACCENT_DIM;
    public static int ROW_ON      = 0x30FFFFFF;
    public static int CHECK       = ACCENT;
    public static int SLIDER_FILL = ACCENT;
    public static int SLIDER_BG   = 0x40000000;
    public static int TOGGLE_ON   = ACCENT;
    public static int TOGGLE_OFF  = 0xFF444444;

    // ================== РАЗМЕРЫ МЕНЮ ==================
    public static int PANEL_WIDTH  = 130;
    public static int PANEL_GAP    = 6;
    public static int TITLE_HEIGHT = 22;
    public static int ROW_HEIGHT   = 16;
    public static int ROW_INDENT   = 6;
    public static int TOP_MARGIN   = 12;
    public static int CORNER       = 3;   // скругление (0 = без)

    // ================== HUD ==================
    public static int HUD_BG    = 0x90000000;
    public static int HUD_BAR   = ACCENT;
    public static float HUD_SCALE = 1.0f;
    public static boolean HUD_SHADOW = true;

    // ================== ИКОНКИ / СИМВОЛЫ ==================
    public static String ICON_VISUAL    = "\u2726";
    public static String ICON_WORLD     = "\u25C9";
    public static String ICON_PARTICLES = "\u2727";
    public static String ICON_UTILS     = "\u2699";
    public static String ICON_CLIENT    = "\u25C8";
    public static String CHECK_MARK     = "\u2714";

    /** Применить выбранную палитру (вызывать при старте после Config.load). */
    public static void apply() {
        int id = Math.max(0, Math.min(THEME_ID, PALETTES.length - 1));
        ACCENT     = PALETTES[id][0];
        ACCENT_DIM = PALETTES[id][1];
        BG         = PALETTES[id][2];
        ROW        = PALETTES[id][3];
        ROW_HOVER  = PALETTES[id][4];
        TEXT       = PALETTES[id][5];
        TEXT_DIM   = PALETTES[id][6];
        PANEL_BG   = BG;
        PANEL_BORDER = ACCENT_DIM;
        CHECK      = ACCENT;
        SLIDER_FILL = ACCENT;
        TOGGLE_ON  = ACCENT;
        HUD_BAR    = ACCENT;
    }
}
