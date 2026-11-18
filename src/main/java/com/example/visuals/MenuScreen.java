package com.example.visuals;

import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.class_124;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_3532;
import net.minecraft.class_437;
import net.minecraft.class_7923;

public class MenuScreen extends class_437 {
    private static final Set<String> open = new HashSet<>();
    private static final Map<String, Float> anims = new HashMap<>();
    private final long openedAt = System.currentTimeMillis();
    private float fade = 0f;
    private String curId = "";
    private int mx, my;
    private boolean down, clicked, prevDown = true;
    private int active = -1;
    private int idc;
    private int cx, cy, cw;

    public MenuScreen() { super(class_2561.method_43470("ArchitektVisuals")); }

    // ---------- цвета и анимации ----------
    private int ff(int color) {
        int a = color >>> 24;
        int na = Math.max(Math.min(a, 8), Math.round(a * fade));
        return (na << 24) | (color & 0xFFFFFF);
    }

    private static int withAlpha(int color, int alpha) { return (alpha << 24) | (color & 0xFFFFFF); }

    private static int mix(int a, int b, float t) {
        int aa = (int) (((a >>> 24) & 255) + (((b >>> 24) & 255) - ((a >>> 24) & 255)) * t);
        int rr = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
        int gr = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
        int bb = (int) ((a & 255) + ((b & 255) - (a & 255)) * t);
        return (aa << 24) | (rr << 16) | (gr << 8) | bb;
    }

    private float anim(String key, boolean target) {
        float goal = target ? 1f : 0f;
        float cur = anims.getOrDefault(key, goal);
        cur += (goal - cur) * 0.28f;
        if (Math.abs(goal - cur) < 0.01f) cur = goal;
        anims.put(key, cur);
        return cur;
    }

    // ---------- примитивы ----------
    private boolean hit(int x, int y, int w, int h) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    private void text(class_332 g, String s, int x, int y, int color) { g.method_25303(field_22793, s, x, y, ff(color)); }

    private void box(class_332 g, int x, int y, int w, int h, int color) {
        g.method_25294(x, y, x + w, y + h, ff(color));
    }

    // Прямоугольник со срезанными углами
    private void rbox(class_332 g, int x, int y, int w, int h, int color) {
        rr(g, x, y, w, h, Math.min(Theme.CORNER, Math.min(w, h) / 2), color);
    }

    // Настоящий скруглённый прямоугольник (радиус r)
    private void rr(class_332 g, int x, int y, int w, int h, int r, int color) {
        int c = ff(color);
        if (r <= 0 || w < 2 * r || h < 2 * r) { g.method_25294(x, y, x + w, y + h, c); return; }
        g.method_25294(x, y + r, x + w, y + h - r, c);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = r - (int) Math.round(Math.sqrt(r * r - dy * dy));
            g.method_25294(x + inset, y + i, x + w - inset, y + i + 1, c);
            g.method_25294(x + inset, y + h - 1 - i, x + w - inset, y + h - i, c);
        }
    }

    // Мягкая тень под скруглённым блоком
    private void shadow(class_332 g, int x, int y, int w, int h, int r) {
        for (int i = 3; i >= 1; i--) rr(g, x - i, y - i + 2, w + 2 * i, h + 2 * i, r + i, 0x10000000);
    }

    // Горизонтальная линия, плавно исчезающая по краям
    private void fadeLine(class_332 g, int x, int y, int w, int color, int maxAlpha) {
        for (int i = 0; i < w; i += 4) {
            double t = (i + 2.0) / w;
            int a = (int) (maxAlpha * Math.sin(Math.PI * t));
            g.method_25294(x + i, y, x + Math.min(w, i + 4), y + 1, ff(withAlpha(color, Math.max(0, a))));
        }
    }

    private void panel(class_332 g, String title, int x, int y, int w, int h) {
        shadow(g, x, y, w, h, 6);
        rr(g, x, y, w, h, 6, Theme.PANEL_BG);
        g.method_25296(x + 3, y + 1, x + w - 3, y + 26,
                ff(withAlpha(Theme.ACCENT, 90)), ff(withAlpha(Theme.ACCENT, 0)));
        if (h > 48) g.method_25296(x + 4, y + h - 26, x + w - 4, y + h - 4,
                ff(withAlpha(Theme.ACCENT, 0)), ff(withAlpha(Theme.ACCENT, 26)));            // тонировка низа
        HudStyle.roundRing(g, x, y, w, h, 6, ff(withAlpha(Theme.ACCENT, 0x70)));          // рамка
        fadeLine(g, x + 6, y, w - 12, Theme.ACCENT, 255);
        fadeLine(g, x + 6, y + 1, w - 12, 0xFFFFFFFF, 70);                                 // блик
        g.method_27534(field_22793, class_2561.method_43470(title).method_27692(class_124.field_1067),
                x + w / 2, y + 8, ff(Theme.TEXT));
        fadeLine(g, x + 10, y + 22, w - 20, Theme.ACCENT, 160);
        cx = x + 6; cw = w - 12; cy = y + 28;
    }

    // Тумблер с плавным движением ползунка
    private void sw(class_332 g, int x, int y, String key, boolean on) {
        float p = anim(key, on);
        if (p > 0.05f) rr(g, x - 2, y - 2, 28, 16, 8, withAlpha(Theme.ACCENT, (int) (46 * p)));
        rr(g, x, y, 24, 12, 6, mix(Theme.TOGGLE_OFF, Theme.TOGGLE_ON, p));
        HudStyle.roundRing(g, x, y, 24, 12, 6, ff(mix(0x30FFFFFF, withAlpha(0xFFFFFF, 0x90), p)));
        int kx = x + 2 + Math.round(p * 12);
        rr(g, kx - 1, y + 1, 10, 10, 5, withAlpha(0xFFFFFF, (int) (40 * p)));
        rr(g, kx, y + 2, 8, 8, 4, mix(0xFFCCCCCC, 0xFFFFFFFF, p));
    }

    private int mh(String id, int options) { return 24 + (open.contains(id) ? options * 20 + 4 : 0); }

    // ---------- элементы ----------
    private boolean module(class_332 g, String id, String label, String hint, boolean on, int options) {
        curId = id;
        boolean hov = hit(cx, cy, cw, 22);
        float hv = anim("h:" + id, hov);
        int rowCol = mix(Theme.ROW, Theme.ROW_HOVER, hv * 0.6f);
        rbox(g, cx, cy, cw, 22, rowCol);
        float onp = anim("on:" + id, on);
        if (onp > 0.02f) {   // градиент акцента слева направо
            for (int i = 0; i < cw - 2; i += 3) {
                float t = 1f - (float) i / (cw - 2);
                g.method_25294(cx + 1 + i, cy + 1, cx + 1 + Math.min(cw - 2, i + 3), cy + 21,
                        ff(withAlpha(Theme.ACCENT, (int) (0x58 * t * t * onp))));
            }
        }
        if (onp > 0.02f) {
            rr(g, cx - 1, cy + 3, 5, 16, 2, withAlpha(Theme.ACCENT, (int) (40 * onp)));    // свечение
            rr(g, cx, cy + 4, 3, Math.max(2, Math.round(14 * onp)), 1, Theme.ACCENT);     // полоска слева
        } else if (hv > 0.1f) rr(g, cx, cy + 8, 2, 6, 1, withAlpha(Theme.ACCENT, (int) (160 * hv)));

        text(g, label + (hint.isEmpty() ? "" : " [" + hint + "]"), cx + 8, cy + 7, on ? Theme.TEXT : Theme.TEXT_DIM);

        if (options > 0) text(g, open.contains(id) ? "▲" : "▼", cx + cw - 40, cy + 7, Theme.TEXT_DIM);

        sw(g, cx + cw - 28, cy + 5, "m:" + id, on);

        if (clicked && hov) {
            if (mx >= cx + cw - 32) on = !on;
            else if (options > 0) { if (!open.remove(id)) open.add(id); }
        }
        cy += 24;
        return on;
    }

    // Строка-заглушка "скоро" (без переключателя)
    private void soon(class_332 g, String label) {
        rbox(g, cx, cy, cw, 22, Theme.ROW);
        text(g, label, cx + 8, cy + 7, Theme.TEXT_DIM);
        String s = "скоро";
        text(g, s, cx + cw - 8 - field_22793.method_1727(s), cy + 7, Theme.ACCENT);
        cy += 24;
    }

    private boolean cbool(class_332 g, String label, boolean v) {
        boolean hov = hit(cx, cy, cw, 18);
        rbox(g, cx, cy, cw, 18, mix(0x20000000, Theme.ROW_HOVER, anim("hb:" + curId + label, hov) * 0.6f));
        text(g, label, cx + 8, cy + 5, v ? Theme.TEXT : Theme.TEXT_DIM);
        sw(g, cx + cw - 28, cy + 3, "c:" + curId + "/" + label, v);
        if (clicked && hov) v = !v;
        cy += 20;
        return v;
    }

    private double cslider(class_332 g, String label, double min, double max, double v, boolean isInt) {
        int id = idc++;
        boolean hov = hit(cx, cy, cw, 18);
        if (clicked && hov) active = id;
        if (active == id && down) {
            double t = Math.max(0.0, Math.min(1.0, (double) (mx - cx) / (double) cw));
            v = min + t * (max - min);
            v = isInt ? (double) Math.round(v) : Math.round(v * 10.0) / 10.0;
        }
        double t = Math.max(0.0, Math.min(1.0, (v - min) / (max - min)));
        int tw = cw - 12;
        int fw = (int) (tw * t);

        rbox(g, cx, cy, cw, 18, hov || active == id ? 0x30000000 : 0x20000000);
        String val = isInt ? String.valueOf((int) v) : String.valueOf(v);
        text(g, label, cx + 6, cy + 2, Theme.TEXT_DIM);
        text(g, val, cx + cw - 6 - field_22793.method_1727(val), cy + 2, Theme.TEXT);
        rr(g, cx + 6, cy + 12, tw, 4, 2, 0x45FFFFFF);
        if (fw > 1) {
            int c0 = mix(Theme.ACCENT, 0xFF14141C, 0.5f), c1 = Theme.ACCENT;
            for (int i = 0; i < fw; i += 4) {
                int c = mix(c0, c1, (float) i / Math.max(1, tw));
                g.method_25294(cx + 6 + i, cy + 12, cx + 6 + Math.min(fw, i + 4), cy + 16, ff(0xFF000000 | (c & 0xFFFFFF)));
            }
            rr(g, cx + 6 + fw - 6, cy + 8, 12, 12, 6, withAlpha(Theme.ACCENT, active == id ? 90 : 45));
            rr(g, cx + 6 + fw - 3, cy + 10, 6, 8, 3, 0xFFFFFFFF);
        }
        cy += 20;
        return v;
    }

    private int ccycle(class_332 g, String label, String[] names, int idx) {
        boolean hov = hit(cx, cy, cw, 18);
        rbox(g, cx, cy, cw, 18, mix(0x20000000, Theme.ROW_HOVER, anim("hc:" + curId + label, hov) * 0.6f));
        text(g, label + ": " + names[Math.max(0, Math.min(idx, names.length - 1))], cx + 8, cy + 5, Theme.TEXT_DIM);
        text(g, "›", cx + cw - 12, cy + 5, Theme.ACCENT);
        if (clicked && hov) idx = (idx + 1) % names.length;
        cy += 20;
        return idx;
    }

    // Палитра: градиентная полоска, возвращает позицию маркера 0..1
    private double cgrad(class_332 g, double t, java.util.function.DoubleToIntFunction colorAt) {
        int id = idc++;
        boolean hov = hit(cx, cy, cw, 16);
        if (clicked && hov) active = id;
        if (active == id && down) t = Math.max(0.0, Math.min(1.0, (double) (mx - cx) / (double) cw));

        for (int x = 0; x < cw; x += 4) {
            int x2 = Math.min(x + 4, cw);
            g.method_25294(cx + x, cy, cx + x2, cy + 16, ff(0xFF000000 | colorAt.applyAsInt((x + 2.0) / cw)));
        }
        int mxp = cx + (int) (cw * t);
        g.method_25294(mxp - 2, cy - 1, mxp + 1, cy + 17, ff(0xFF000000));   // обводка маркера
        g.method_25294(mxp - 1, cy, mxp, cy + 16, ff(0xFFFFFFFF));           // маркер
        cy += 20;
        return t;
    }

    // Палитра с подписью поверх полоски
    private double cgradL(class_332 g, String label, double t, java.util.function.DoubleToIntFunction colorAt) {
        int y0 = cy;
        double r = cgrad(g, t, colorAt);
        g.method_25300(field_22793, label, cx + cw / 2, y0 + 4, ff(0xFFFFFFFF));
        return r;
    }

    private static int hsv(double h, double s, double v) {
        return class_3532.method_15369((float) h, (float) s, (float) v) & 0xFFFFFF;
    }

    private void hint2(class_332 g, String l1, String l2) {
        int max = cw - 4;
        text(g, field_22793.method_27523(l1, max), cx + 4, cy + 2, Theme.TEXT_DIM);
        text(g, field_22793.method_27523(l2, max), cx + 4, cy + 12, Theme.TEXT_DIM);
        cy += 24;
    }

    private int listRow(class_332 g, int key, String label) {
        boolean hov = hit(cx, cy, cw, 18);
        boolean overX = hit(cx + cw - 16, cy, 16, 18);
        rbox(g, cx, cy, cw, 18, hov ? Theme.ROW_HOVER : 0x20000000);
        String full = "[" + Keys.name(key) + "] " + label;
        text(g, field_22793.method_27523(full, cw - 22), cx + 6, cy + 5, key == -1 ? 0xFFFF5555 : Theme.TEXT);
        text(g, "✕", cx + cw - 12, cy + 5, overX ? 0xFFFF5555 : Theme.TEXT_DIM);
        int result = 0;
        if (clicked && hov) result = overX ? 2 : 1;
        cy += 20;
        return result;
    }

    // Строка с названием предмета и крестиком удаления (2 = нажат крестик)
    private int nameRow(class_332 g, String label) {
        boolean hov = hit(cx, cy, cw, 18);
        boolean overX = hit(cx + cw - 16, cy, 16, 18);
        rbox(g, cx, cy, cw, 18, hov ? Theme.ROW_HOVER : 0x20000000);
        text(g, field_22793.method_27523(label, cw - 22), cx + 6, cy + 5, Theme.TEXT);
        text(g, "✕", cx + cw - 12, cy + 5, overX ? 0xFFFF5555 : Theme.TEXT_DIM);
        int result = (clicked && overX) ? 2 : 0;
        cy += 20;
        return result;
    }

    private void screenButton(class_332 g, String label, Supplier<class_437> screen) {
        boolean hov = hit(cx, cy, cw, 22);
        rbox(g, cx, cy, cw, 22, hov ? Theme.ACCENT_DIM : Theme.ROW);
        text(g, label, cx + 8, cy + 7, Theme.TEXT);
        if (clicked && hov) field_22787.method_1507(screen.get());
        cy += 24;
    }

    @Override
    public void method_25394(class_332 g, int mouseX, int mouseY, float delta) {
        mx = mouseX; my = mouseY;
        long window = field_22787.method_22683().method_4490();
        down = GLFW.glfwGetMouseButton(window, 0) == GLFW.GLFW_PRESS;
        clicked = down && !prevDown;
        prevDown = down;
        if (!down) active = -1;
        idc = 0;

        // Плавное появление
        float p = Math.min(1f, (System.currentTimeMillis() - openedAt) / 200f);
        fade = 1f - (1f - p) * (1f - p);

        // Фон: затемнение с градиентом и лёгкий дождь
        g.method_25296(0, 0, field_22789, field_22790, 0x80080810, 0xC0000000);
        g.method_25296(0, field_22790 / 2, field_22789, field_22790, 0x00000000, ff(withAlpha(Theme.ACCENT, 0x38)));   // свечение акцента снизу
        g.method_25296(0, 0, field_22789, field_22790 / 4, ff(withAlpha(Theme.ACCENT, 0x18)), 0x00000000);        // лёгкий блик сверху
        RainyScene.drawRain(g, field_22789, field_22790, 0.3f);

        if (Config.menuStyle == 1) renderPulse(g);
        else {
            if (pvSearch != null) pvSearch.method_46419(-100);   // поле поиска Pulse скрыто
            renderSidebar(g);
        }
        drawBottom(g);
    }

    // =====================================================================
    //  Нижние кнопки: закрыть + тема
    // =====================================================================
    private boolean btn(class_332 g, int x, int y, int w, int h, String label, String key) {
        boolean hov = hit(x, y, w, h);
        float p = anim("b:" + key, hov);
        shadow(g, x, y, w, h, 5);
        rr(g, x, y, w, h, 5, mix(Theme.PANEL_BG, withAlpha(Theme.ACCENT, 0xB0), p));
        fadeLine(g, x + 5, y, w - 10, Theme.ACCENT, 255);
        g.method_25300(field_22793, label, x + w / 2, y + (h - 8) / 2, ff(Theme.TEXT));
        return clicked && hov;
    }

    private void drawBottom(class_332 g) {
        int bh = 20, gap = 8;
        int bw = Math.max(80, Math.min(150, (field_22789 - 24 - gap * 2) / 3));
        int total = bw * 3 + gap * 2;
        int bx = (field_22789 - total) / 2, by = field_22790 - bh - 10;

        if (btn(g, bx, by, bw, bh, "ArchitektVisuals | Закрыть", "close")) method_25419();
        if (btn(g, bx + bw + gap, by, bw, bh,
                "Тема: " + Theme.NAMES[Math.max(0, Math.min(Config.menuTheme, Theme.NAMES.length - 1))], "theme")) {
            Config.menuTheme = (Config.menuTheme + 1) % Theme.NAMES.length;
            Theme.THEME_ID = Config.menuTheme;
            Theme.apply();
            Config.save();
        }
        if (btn(g, bx + (bw + gap) * 2, by, bw, bh,
                "Стиль меню: " + MENU_STYLES[Math.max(0, Math.min(Config.menuStyle, MENU_STYLES.length - 1))], "style")) {
            Config.menuStyle = (Config.menuStyle + 1) % MENU_STYLES.length;
            pvSel = "";
            if (pvSearch != null) pvSearch.method_1852("");
            Config.save();
        }
    }

    private static final String[] MENU_STYLES = {"Sidebar", "Pulse"};

    // =====================================================================
    //  SIDEBAR-стиль: слева категории, в центре модули, справа настройки
    // =====================================================================
    private static int sbCat = 0;
    private static String sbSel = "";

    private static final String[] SB_CATS = {"Visuals", "Interface", "Utilities", "Client", "World"};
    private static final String[][] SB_MODS = {
            {"fb", "cross", "trails", "zones", "tesp", "sky", "fog", "part", "dmg", "thud", "hv", "ci", "jc", "hat", "halo", "horns", "pred", "physic", "shulker", "hl", "crit", "kill", "atk", "wings", "aura", "cape", "chams"},
            {"hud", "array", "armor", "potion", "keys", "combo", "wm", "server", "clock", "inv", "totem", "stats", "dlog", "pcd", "cdh", "near", "nt", "tip"},
            {"sprint", "zoom", "respawn", "freelook", "noov", "hitsound", "swing", "iscroll", "stream", "fake"},
            {"autoswap", "itembind", "friends", "macro", "configs"},
            {"time", "weather", "hbox", "bout", "chunk", "iesp", "wp", "evpt", "ptr", "etr", "clouds"}
    };

    private static String sbLabel(String id) {
        return switch (id) {
            case "fb" -> "Fullbright";
            case "cross" -> "Crosshair";
            case "trails" -> "Trails";
            case "zones" -> "FunTime Zones";
            case "tesp" -> "Target ESP";
            case "sky" -> "Sky Shader";
            case "part" -> "Particles";
            case "dmg" -> "DamageViewer";
            case "fog" -> "CustomFog";
            case "thud" -> "TargetHUD";
            case "hv" -> "Hand View";
            case "ci" -> "CustomItems";
            case "jc" -> "JumpCircle";
            case "hat" -> "China Hat";
            case "halo" -> "Нимб";
            case "horns" -> "Рога";
            case "pred" -> "Predictions";
            case "physic" -> "Item Physic";
            case "shulker" -> "Shulker Prev";
            case "hl" -> "Item Highlight";
            case "hud" -> "Info HUD";
            case "array" -> "ArrayList";
            case "armor" -> "Armor";
            case "potion" -> "Potion HUD";
            case "keys" -> "Keystrokes";
            case "combo" -> "Combo Counter";
            case "server" -> "Server Info";
            case "wings" -> "Wings";
            case "aura" -> "Aura";
            case "cape" -> "Animated Cape";
            case "nt" -> "Nametags+";
            case "chams" -> "Chams";
            case "swing" -> "Swing Animation";
            case "iscroll" -> "Item Scroller";
            case "clouds" -> "Custom Clouds";
            case "tip" -> "Tooltips Plus";
            case "hbox" -> "Hitboxes";
            case "bout" -> "Block Outline";
            case "chunk" -> "Chunk Borders";
            case "iesp" -> "Item ESP";
            case "ptr" -> "Pearl Trail";
            case "etr" -> "Elytra Trail";
            case "pcd" -> "Pearl Cooldown";
            case "wp" -> "Waypoints";
            case "evpt" -> "Event Point";
            case "crit" -> "Crit Effects";
            case "kill" -> "Kill Effect";
            case "atk" -> "Attack Indicator";
            case "stats" -> "Combat Stats";
            case "dlog" -> "Damage Log";
            case "clock" -> "Time & Date";
            case "inv" -> "Inventory HUD";
            case "totem" -> "Totem Counter";
            case "near" -> "Players Near";
            case "cdh" -> "Cooldown HUD";
            case "wm" -> "Watermark";
            case "noov" -> "NoOverlay";
            case "hitsound" -> "Hit Sound";
            case "sprint" -> "AutoSprint";
            case "zoom" -> "Zoom";
            case "respawn" -> "AutoRespawn";
            case "freelook" -> "Freelook";
            case "stream" -> "StreamerMode";
            case "fake" -> "FakePlayer";
            case "autoswap" -> "AutoSwap";
            case "itembind" -> "ItemBind";
            case "friends" -> "Friends";
            case "macro" -> "Macros";
            case "configs" -> "Configs";
            case "time" -> "Время";
            case "weather" -> "Погода";
            default -> id;
        };
    }

    private static boolean sbHasSwitch(String id) {
        return !id.equals("friends") && !id.equals("macro") && !id.equals("configs");
    }

    private static boolean sbGet(String id) {
        return switch (id) {
            case "fb" -> Config.fullbright;
            case "cross" -> Config.customCrosshair;
            case "trails" -> Config.trails;
            case "zones" -> Config.zones;
            case "tesp" -> Config.targetEsp;
            case "sky" -> Config.skyShader;
            case "part" -> Config.particles;
            case "dmg" -> Config.damageFever;
            case "fog" -> Config.customFog;
            case "thud" -> Config.targetHud;
            case "hv" -> Config.handView;
            case "ci" -> Config.customItems;
            case "jc" -> Config.jumpCircle;
            case "hat" -> Config.chinaHat;
            case "halo" -> Config.halo;
            case "horns" -> Config.horns;
            case "pred" -> Config.predictions;
            case "physic" -> Config.itemPhysic;
            case "shulker" -> Config.shulkerPreview;
            case "hl" -> Config.highlight;
            case "hud" -> Config.hud;
            case "array" -> Config.arrayList;
            case "armor" -> Config.armorHud;
            case "potion" -> Config.potionHud;
            case "keys" -> Config.keystrokes;
            case "combo" -> Config.comboHud;
            case "server" -> Config.serverHud;
            case "wings" -> Config.wingsOn;
            case "aura" -> Config.auraOn;
            case "cape" -> Config.capeOn;
            case "nt" -> Config.ntOn;
            case "chams" -> Config.chamsOn;
            case "swing" -> Config.swingAnim;
            case "iscroll" -> Config.itemScroller;
            case "clouds" -> Config.cloudsOn;
            case "tip" -> Config.tipOn;
            case "hbox" -> Config.hbox;
            case "bout" -> Config.bout;
            case "chunk" -> Config.chunkB;
            case "iesp" -> Config.itemEsp;
            case "ptr" -> Config.ptrail;
            case "etr" -> Config.etrail;
            case "pcd" -> Config.pcdHud;
            case "wp" -> Config.wp;
            case "evpt" -> Config.eventPts;
            case "crit" -> Config.critFx;
            case "kill" -> Config.killFx;
            case "atk" -> Config.atkInd;
            case "stats" -> Config.statsHud;
            case "dlog" -> Config.dlHud;
            case "clock" -> Config.clockHud;
            case "inv" -> Config.invHud;
            case "totem" -> Config.totemHud;
            case "near" -> Config.nearHud;
            case "cdh" -> Config.cdHud;
            case "wm" -> Config.watermark;
            case "noov" -> Config.noOverlay;
            case "hitsound" -> Config.hitSound;
            case "sprint" -> Config.sprint;
            case "zoom" -> Config.zoom;
            case "respawn" -> Config.autoRespawn;
            case "freelook" -> Config.thirdPerson;
            case "stream" -> Config.streamerMode;
            case "fake" -> FakePlayer.enabled;
            case "autoswap" -> Config.autoswap;
            case "itembind" -> Config.itembind;
            case "time" -> Config.customTime;
            case "weather" -> Config.customWeather;
            default -> true;
        };
    }

    private static void sbSet(String id, boolean v) {
        switch (id) {
            case "fb": Config.fullbright = v; break;
            case "cross": Config.customCrosshair = v; break;
            case "trails": Config.trails = v; break;
            case "zones": Config.zones = v; break;
            case "tesp": Config.targetEsp = v; break;
            case "sky": Config.skyShader = v; break;
            case "part": Config.particles = v; break;
            case "dmg": Config.damageFever = v; break;
            case "fog": Config.customFog = v; break;
            case "thud": Config.targetHud = v; break;
            case "hv": Config.handView = v; break;
            case "ci": Config.customItems = v; break;
            case "jc": Config.jumpCircle = v; break;
            case "hat": Config.chinaHat = v; break;
            case "halo": Config.halo = v; break;
            case "horns": Config.horns = v; break;
            case "pred": Config.predictions = v; break;
            case "physic": Config.itemPhysic = v; break;
            case "shulker": Config.shulkerPreview = v; break;
            case "hl": Config.highlight = v; break;
            case "hud": Config.hud = v; break;
            case "array": Config.arrayList = v; break;
            case "armor": Config.armorHud = v; break;
            case "potion": Config.potionHud = v; break;
            case "keys": Config.keystrokes = v; break;
            case "combo": Config.comboHud = v; break;
            case "server": Config.serverHud = v; break;
            case "wings": Config.wingsOn = v; break;
            case "aura": Config.auraOn = v; break;
            case "cape": Config.capeOn = v; break;
            case "nt": Config.ntOn = v; break;
            case "chams": Config.chamsOn = v; break;
            case "swing": Config.swingAnim = v; break;
            case "iscroll": Config.itemScroller = v; break;
            case "clouds": Config.cloudsOn = v; break;
            case "tip": Config.tipOn = v; break;
            case "hbox": Config.hbox = v; break;
            case "bout": Config.bout = v; break;
            case "chunk": Config.chunkB = v; break;
            case "iesp": Config.itemEsp = v; break;
            case "ptr": Config.ptrail = v; break;
            case "etr": Config.etrail = v; break;
            case "pcd": Config.pcdHud = v; break;
            case "wp": Config.wp = v; break;
            case "evpt": Config.eventPts = v; break;
            case "crit": Config.critFx = v; break;
            case "kill": Config.killFx = v; break;
            case "atk": Config.atkInd = v; break;
            case "stats": Config.statsHud = v; break;
            case "dlog": Config.dlHud = v; break;
            case "clock": Config.clockHud = v; break;
            case "inv": Config.invHud = v; break;
            case "totem": Config.totemHud = v; break;
            case "near": Config.nearHud = v; break;
            case "cdh": Config.cdHud = v; break;
            case "wm": Config.watermark = v; break;
            case "noov": Config.noOverlay = v; break;
            case "hitsound": Config.hitSound = v; break;
            case "sprint": Config.sprint = v; break;
            case "zoom": Config.zoom = v; break;
            case "respawn": Config.autoRespawn = v; break;
                            case "freelook": Config.thirdPerson = v; break;
            case "stream": Config.streamerMode = v; break;
            case "fake": FakePlayer.enabled = v; break;
            case "autoswap": Config.autoswap = v; break;
            case "itembind": Config.itembind = v; break;
            case "time": Config.customTime = v; break;
            case "weather": Config.customWeather = v; break;
            default: break;
        }
    }

    // строка без переключателя (для Friends / Macros)
    private boolean plainRow(class_332 g, String label) {
        boolean hov = hit(cx, cy, cw, 22);
        rbox(g, cx, cy, cw, 22, hov ? Theme.ROW_HOVER : Theme.ROW);
        text(g, label, cx + 8, cy + 7, Theme.TEXT);
        text(g, ">", cx + cw - 14, cy + 7, Theme.TEXT_DIM);
        cy += 24;
        return clicked && hov;
    }

    private void renderSidebar(class_332 g) {
        int pw = Math.min(field_22789 - 16, 470);
        int ph = Math.min(field_22790 - 44, 300);
        int px = (field_22789 - pw) / 2;
        int py = Math.max(6, (field_22790 - 36 - ph) / 2);

        // подложка
        shadow(g, px, py, pw, ph, 8);
        rr(g, px, py, pw, ph, 8, Theme.PANEL_BG);
        g.method_25296(px + 4, py + 1, px + pw - 4, py + 40,
                ff(withAlpha(Theme.ACCENT, 50)), ff(withAlpha(Theme.ACCENT, 0)));
        fadeLine(g, px + 8, py, pw - 16, Theme.ACCENT, 255);

        int sbW = 104;
        int midW = Math.max(120, (pw - sbW) * 2 / 5);
        int rightW = pw - sbW - midW;
        g.method_25294(px + sbW, py + 8, px + sbW + 1, py + ph - 8, ff(Theme.ACCENT_DIM));
        g.method_25294(px + sbW + midW, py + 8, px + sbW + midW + 1, py + ph - 8, ff(Theme.ACCENT_DIM));

        // ---------- левая колонка: название и категории ----------
        g.method_27534(field_22793, class_2561.method_43470("Architekt").method_27692(class_124.field_1067),
                px + sbW / 2, py + 14, ff(Theme.ACCENT));
        g.method_25300(field_22793, "Visuals", px + sbW / 2, py + 26, ff(Theme.TEXT));

        int ry = py + 46;
        for (int i = 0; i < SB_CATS.length; i++) {
            int rx = px + 8, rw = sbW - 16;
            boolean hov = hit(rx, ry, rw, 22);
            if (clicked && hov) { sbCat = i; sbSel = ""; }
            rbox(g, rx, ry, rw, 22, sbCat == i ? Theme.ACCENT_DIM : (hov ? Theme.ROW_HOVER : 0x00000000));
            text(g, SB_CATS[i], rx + 10, ry + 7, sbCat == i ? Theme.TEXT : Theme.TEXT_DIM);
            ry += 26;
        }

        // ---------- центральная колонка: модули ----------
        sbCat = Math.max(0, Math.min(sbCat, SB_CATS.length - 1));
        String[] mods = SB_MODS[sbCat];
        boolean found = false;
        for (String m : mods) if (m.equals(sbSel)) found = true;
        if (!found) sbSel = mods[0];

        int mx0 = px + sbW + 8;
        int vTop = py + 30, vH = ph - 38;
        sbSplitX = px + sbW + midW;
        if (sbCat != sbLastCat) { sbLastCat = sbCat; sbScrollMid = 0; sbScrollRight = 0; }
        text(g, "Категория: " + SB_CATS[sbCat], mx0, py + 12, Theme.TEXT_DIM);

        // ---- центральная колонка (с прокруткой) ----
        sbScrollMid = Math.max(0, Math.min(sbScrollMid, sbMaxMid));
        cx = mx0; cw = midW - 22; cy = vTop - Math.round(sbScrollMid);
        int startMid = cy;
        int smx = mx, smy = my; boolean scl = clicked;
        if (active == -1 && !hit(mx0 - 2, vTop, midW - 12, vH)) { mx = -1000; my = -1000; clicked = false; }
        g.method_44379(mx0 - 2, vTop, mx0 - 2 + midW - 12, vTop + vH);
        for (String id : mods) {
            int rowY = cy;
            if (sbHasSwitch(id)) {
                boolean on = sbGet(id);
                if (clicked && hit(cx, cy, cw - 32, 22)) sbSel = id;
                boolean nv = module(g, id, sbLabel(id), "", on, 0);
                if (nv != on) sbSet(id, nv);
            } else {
                if (plainRow(g, sbLabel(id))) sbSel = id;
            }
            if (id.equals(sbSel)) {   // подсветка выбранного модуля
                rr(g, mx0, rowY, cw, 22, 3, withAlpha(Theme.ACCENT, 0x30));
                rr(g, mx0, rowY + 4, 3, 14, 1, Theme.ACCENT);
            }
        }
        g.method_44380();
        mx = smx; my = smy; clicked = scl;
        scrollbar(g, mx0 + midW - 18, vTop, vH, cy - startMid, 1);

        // ---- правая колонка: настройки выбранного модуля (с прокруткой) ----
        if (!sbSel.equals(sbLastSel)) { sbLastSel = sbSel; sbScrollRight = 0; }
        int rx0 = px + sbW + midW + 10;
        text(g, sbLabel(sbSel), rx0, py + 12, Theme.TEXT);
        sbScrollRight = Math.max(0, Math.min(sbScrollRight, sbMaxRight));
        cx = rx0; cw = rightW - 24; cy = vTop - Math.round(sbScrollRight); curId = sbSel;
        int startR = cy;
        smx = mx; smy = my; scl = clicked;
        if (active == -1 && !hit(rx0 - 2, vTop, rightW - 14, vH)) { mx = -1000; my = -1000; clicked = false; }
        g.method_44379(rx0 - 2, vTop, rx0 - 2 + rightW - 14, vTop + vH);
        sbSettings(g, sbSel);
        g.method_44380();
        mx = smx; my = smy; clicked = scl;
        scrollbar(g, rx0 + rightW - 18, vTop, vH, cy - startR, 2);
    }

    // ---- прокрутка колонок Sidebar ----
    private static float sbScrollMid = 0, sbScrollRight = 0;
    private static int sbMaxMid = 0, sbMaxRight = 0, sbDrag = 0, sbSplitX = 0, sbLastCat = -1;
    private static String sbLastSel = "";

    private void scrollbar(class_332 g, int x, int top, int h, int content, int which) {
        int max = Math.max(0, content - h);
        if (which == 1) sbMaxMid = max; else sbMaxRight = max;
        if (max <= 0) { if (which == 1) sbScrollMid = 0; else sbScrollRight = 0; return; }
        float sc = which == 1 ? sbScrollMid : sbScrollRight;
        int thumbH = Math.max(16, h * h / content);
        boolean over = hit(x - 4, top, 11, h);
        if (clicked && over) sbDrag = which;
        if (!down) sbDrag = 0;
        if (sbDrag == which) {
            float t = (my - top - thumbH / 2f) / (float) (h - thumbH);
            sc = Math.max(0f, Math.min((float) max, t * max));
            if (which == 1) sbScrollMid = sc; else sbScrollRight = sc;
        }
        int thumbY = top + Math.round((h - thumbH) * (sc / max));
        rr(g, x, top, 3, h, 1, 0x30FFFFFF);
        rr(g, x, thumbY, 3, thumbH, 1, (sbDrag == which || over) ? Theme.ACCENT : Theme.ACCENT_DIM);
    }

    // =====================================================================
    //  PULSE-стиль: вкладки сверху, поиск, сетка плиток 2 колонки, док снизу
    //  Клик по переключателю - вкл/выкл, клик по остальной части плитки - настройки модуля
    // =====================================================================
    private static final String[] PV_TABS = {"Visuals", "HUD", "Utilities", "Client", "World"};
    private static int pvCat = 0;
    private static String pvSel = "";
    private static String pvLastKey = "";
    private class_342 pvSearch;
    private int pvPx, pvPy, pvPw, pvPh;

    private void pvLayout() {
        pvPw = Math.min(field_22789 - 16, 380);
        int top = field_22790 >= 300 ? 40 : 8;
        int availBottom = field_22790 - 64 - 8;
        pvPh = Math.max(100, Math.min(280, availBottom - top));
        pvPx = (field_22789 - pvPw) / 2;
        pvPy = top + Math.max(0, (availBottom - top - pvPh) / 2);
    }

    @Override
    protected void method_25426() {
        super.method_25426();
        pvLayout();
        pvSearch = new class_342(this.field_22793, pvPx + pvPw - 124, -100, 112, 16, class_2561.method_43470("Поиск"));
        pvSearch.method_1880(24);
        this.method_37063(pvSearch);
    }

    // ломаная линия толщиной 1px
    private void pline(class_332 g, int[] xs, int[] ys, int color) {
        for (int k = 0; k + 1 < xs.length; k++) {
            int steps = Math.max(Math.abs(xs[k + 1] - xs[k]), Math.abs(ys[k + 1] - ys[k]));
            for (int t = 0; t <= steps; t++) {
                int x = xs[k] + (xs[k + 1] - xs[k]) * t / Math.max(1, steps);
                int y = ys[k] + (ys[k + 1] - ys[k]) * t / Math.max(1, steps);
                g.method_25294(x, y, x + 1, y + 1, ff(color));
            }
        }
    }

    private void pvLogo(class_332 g, int cxm, int y) {
        String name = "ArchitektVisuals";
        int tw = field_22793.method_1727(name) + 2;
        int total = 26 + tw;
        int x0 = cxm - total / 2;
        pline(g, new int[]{x0, x0 + 5, x0 + 8, x0 + 12, x0 + 16, x0 + 19, x0 + 24},
                new int[]{y + 6, y + 6, y - 1, y + 13, y + 3, y + 6, y + 6}, Theme.ACCENT);
        g.method_27534(field_22793, class_2561.method_43470(name).method_27692(class_124.field_1067),
                x0 + 26 + tw / 2, y + 2, ff(Theme.TEXT));
    }

    // одна плитка: подпись слева, переключатель справа
    private void pvTile(class_332 g, String id, int x, int y, int w, int h) {
        boolean hasSw = sbHasSwitch(id);
        boolean on = hasSw && sbGet(id);
        boolean hov = hit(x, y, w, h);
        float hv = anim("pv:h:" + id, hov);
        float onp = anim("pv:on:" + id, on);
        rr(g, x, y, w, h, 5, mix(0x58000000, withAlpha(Theme.ACCENT, 0x40), Math.min(1f, hv * 0.5f + onp * 0.25f)));
        HudStyle.roundRing(g, x, y, w, h, 5, ff(mix(0x18FFFFFF, withAlpha(Theme.ACCENT, 0x70), Math.max(onp * 0.8f, hv * 0.5f))));
        int labelW = w - (hasSw ? 46 : 28);
        text(g, field_22793.method_27523(sbLabel(id), labelW), x + 10, y + (h - 8) / 2,
                mix(0xFF6E6E80, Theme.TEXT, Math.max(onp, hv * 0.7f)));
        if (hasSw) sw(g, x + w - 30, y + (h - 12) / 2, "pv:" + id, on);
        else text(g, ">", x + w - 14, y + (h - 8) / 2, Theme.TEXT_DIM);
        if (clicked && hov) {
            if (hasSw && mx >= x + w - 34) sbSet(id, !on);
            else {
                pvSel = id;
                if (pvSearch != null) pvSearch.method_1852("");
            }
        }
    }

    private void pvDock(class_332 g) {
        String[] icons = {Theme.ICON_VISUAL, Theme.ICON_CLIENT, Theme.ICON_UTILS, Theme.ICON_WORLD};
        String[] names = {"Модули", "Friends", "Макросы", "Конфиги"};
        int bw = 28, bh = 22, gap = 6;
        int total = bw * icons.length + gap * (icons.length - 1);
        int dx = (field_22789 - total) / 2, dy = field_22790 - 64;
        shadow(g, dx - 8, dy - 4, total + 16, bh + 8, 8);
        rr(g, dx - 8, dy - 4, total + 16, bh + 8, 8, Theme.PANEL_BG);
        HudStyle.roundRing(g, dx - 8, dy - 4, total + 16, bh + 8, 8, ff(withAlpha(Theme.ACCENT, 0x40)));
        for (int i = 0; i < icons.length; i++) {
            int x = dx + i * (bw + gap);
            boolean hov = hit(x, dy, bw, bh);
            boolean act = i == 0 && pvSel.isEmpty();
            float p = anim("pvd:" + i, hov);
            rr(g, x, dy, bw, bh, 6, act ? withAlpha(Theme.ACCENT, 0xD0) : mix(0x30000000, withAlpha(Theme.ACCENT, 0x70), p));
            g.method_25300(field_22793, icons[i], x + bw / 2, dy + (bh - 8) / 2, ff(Theme.TEXT));
            if (hov) g.method_25300(field_22793, names[i], x + bw / 2, dy - 14, ff(Theme.TEXT_DIM));
            if (clicked && hov) {
                switch (i) {
                    case 0 -> { pvSel = ""; if (pvSearch != null) pvSearch.method_1852(""); }
                    case 1 -> field_22787.execute(() -> field_22787.method_1507(new FriendsScreen(this)));
                    case 2 -> field_22787.execute(() -> field_22787.method_1507(new MacrosScreen(this)));
                    default -> field_22787.execute(() -> field_22787.method_1507(new ConfigsScreen(this)));
                }
            }
        }
    }

    private void renderPulse(class_332 g) {
        pvLayout();
        int px = pvPx, py = pvPy, pw = pvPw, ph = pvPh;
        boolean overlay = !pvSel.isEmpty();

        if (py >= 34) pvLogo(g, field_22789 / 2, py - 28);

        shadow(g, px, py, pw, ph, 8);
        rr(g, px, py, pw, ph, 8, Theme.PANEL_BG);
        g.method_25296(px + 4, py + 1, px + pw - 4, py + 36,
                ff(withAlpha(Theme.ACCENT, 34)), ff(withAlpha(Theme.ACCENT, 0)));
        HudStyle.roundRing(g, px, py, pw, ph, 8, ff(withAlpha(Theme.ACCENT, 0x40)));
        fadeLine(g, px + 8, py, pw - 16, Theme.ACCENT, 255);
        fadeLine(g, px + 10, py + 28, pw - 20, Theme.ACCENT, 90);

        int vTop = py + 34, vH = ph - 40;

        // ---------------- настройки выбранного модуля ----------------
        if (overlay) {
            sbSplitX = 0;   // колесо мыши крутит правую (настройки) прокрутку
            if (pvSearch != null) pvSearch.method_46419(-100);

            boolean hovB = hit(px + 12, py + 7, 54, 16);
            rr(g, px + 12, py + 7, 54, 16, 5, hovB ? withAlpha(Theme.ACCENT, 0x80) : 0x30000000);
            g.method_25300(field_22793, "← Назад", px + 12 + 27, py + 12, ff(Theme.TEXT));
            if (clicked && hovB) { pvSel = ""; return; }

            text(g, sbLabel(pvSel), px + 76, py + 12, Theme.TEXT);
            if (sbHasSwitch(pvSel)) {
                boolean on = sbGet(pvSel);
                sw(g, px + pw - 40, py + 8, "pvo:" + pvSel, on);
                if (clicked && hit(px + pw - 44, py + 5, 32, 18)) sbSet(pvSel, !on);
            }

            if (!pvSel.equals(sbLastSel)) { sbLastSel = pvSel; sbScrollRight = 0; }
            sbScrollRight = Math.max(0, Math.min(sbScrollRight, sbMaxRight));
            cx = px + 16; cw = pw - 32 - 8; cy = vTop - Math.round(sbScrollRight); curId = pvSel;
            int startR = cy;
            int smx = mx, smy = my; boolean scl = clicked;
            if (active == -1 && !hit(px + 12, vTop, pw - 24, vH)) { mx = -1000; my = -1000; clicked = false; }
            g.method_44379(px + 12, vTop, px + pw - 12, vTop + vH);
            sbSettings(g, pvSel);
            g.method_44380();
            mx = smx; my = smy; clicked = scl;
            scrollbar(g, px + pw - 14, vTop, vH, cy - startR, 2);
            pvDock(g);
            return;
        }

        // ---------------- шапка: вкладки + поиск ----------------
        sbSplitX = Integer.MAX_VALUE;   // колесо мыши крутит сетку плиток
        if (pvSearch != null) {
            pvSearch.method_46419(py + 6);
            pvSearch.method_25394(g, mx, my, 0f);
            if (pvSearch.method_1882().isEmpty() && !pvSearch.method_25370())
                text(g, "Поиск", pvSearch.method_46426() + 5, py + 10, 0xFF777777);
        }
        String q = pvSearch == null ? "" : pvSearch.method_1882().trim().toLowerCase();
        boolean searching = !q.isEmpty();

        int tx = px + 14, ty = py + 10;
        for (int i = 0; i < PV_TABS.length; i++) {
            int tw = field_22793.method_1727(PV_TABS[i]);
            boolean act = i == pvCat && !searching;
            boolean hov = hit(tx - 3, ty - 3, tw + 6, 15);
            text(g, PV_TABS[i], tx, ty, act ? Theme.TEXT : (hov ? Theme.TEXT : Theme.TEXT_DIM));
            if (act) rr(g, tx, ty + 11, tw, 1, 0, Theme.ACCENT);
            if (clicked && hov) {
                pvCat = i;
                if (pvSearch != null) pvSearch.method_1852("");
            }
            tx += tw;
            if (i < PV_TABS.length - 1) { text(g, "/", tx + 5, ty, 0xFF55556A); tx += 14; }
        }

        // ---------------- сетка плиток ----------------
        java.util.List<String> ids = new java.util.ArrayList<>();
        if (!searching) { for (String m : SB_MODS[pvCat]) ids.add(m); }
        else for (String[] cat : SB_MODS) for (String m : cat)
            if (sbLabel(m).toLowerCase().contains(q) || m.contains(q)) ids.add(m);

        String key = pvCat + "|" + q;
        if (!key.equals(pvLastKey)) { pvLastKey = key; sbScrollMid = 0; }
        sbScrollMid = Math.max(0, Math.min(sbScrollMid, sbMaxMid));

        int x0 = px + 14, iw = pw - 28 - 6, colW = (iw - 6) / 2, th = 22, rowGap = 4;
        int smx = mx, smy = my; boolean scl = clicked;
        if (active == -1 && !hit(x0 - 2, vTop, iw + 8, vH)) { mx = -1000; my = -1000; clicked = false; }
        g.method_44379(x0 - 2, vTop, x0 + iw + 6, vTop + vH);
        for (int i = 0; i < ids.size(); i++) {
            int x = x0 + (i % 2) * (colW + 6);
            int y = vTop - Math.round(sbScrollMid) + (i / 2) * (th + rowGap);
            if (y + th < vTop || y > vTop + vH) continue;
            pvTile(g, ids.get(i), x, y, colW, th);
        }
        if (ids.isEmpty()) text(g, "Ничего не найдено", x0 + 4, vTop + 6, Theme.TEXT_DIM);
        g.method_44380();
        mx = smx; my = smy; clicked = scl;
        int rows = (ids.size() + 1) / 2;
        scrollbar(g, px + pw - 12, vTop, vH, rows * (th + rowGap), 1);

        pvDock(g);
    }

    // колесо мыши
    @Override
    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX < sbSplitX) sbScrollMid = Math.max(0, Math.min(sbMaxMid, sbScrollMid - (float) verticalAmount * 22f));
        else sbScrollRight = Math.max(0, Math.min(sbMaxRight, sbScrollRight - (float) verticalAmount * 22f));
        return true;
    }

    private void evptBlock(class_332 g) {
        Config.epBeam = cbool(g, "Луч в мире", Config.epBeam);
        Config.epMinutes = cslider(g, "Держать метку, мин", 1, 30, Config.epMinutes, true);
        Config.epScale = cslider(g, "Размер метки", 0.5, 2.0, Config.epScale, false);
    }

    private void wpBlock(class_332 g) {
        Config.wpBeam = cbool(g, "Луч в мире", Config.wpBeam);
        Config.wpLabels = cbool(g, "Метки на экране", Config.wpLabels);
        Config.wpDist = cbool(g, "Расстояние", Config.wpDist);
        Config.wpDeath = cbool(g, "Точка смерти", Config.wpDeath);
        Config.wpMax = cslider(g, "Макс. дальность", 50, 5000, Config.wpMax, true);
        Config.wpColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.wpColor);
        Config.wpScale = cslider(g, "Размер меток", 0.5, 2.0, Config.wpScale, false);
        if (actionRow(g, "+ Добавить здесь")) Waypoints.addHere(field_22787, null);
        if (actionRow(g, "Удалить все точки")) Waypoints.clear();
        Waypoints.Wp del = null;
        for (Waypoints.Wp w : Waypoints.LIST) {
            if (nameRow(g, w.name + " " + w.x + "," + w.y + "," + w.z) == 2) del = w;
        }
        if (del != null) { Waypoints.LIST.remove(del); Waypoints.save(); }
    }

    private void fakeBlock(class_332 g) {
        FakePlayer.totem = totemRow(g, FakePlayer.totem);
        if (actionRow(g, "Поставить передо мной")) FakePlayer.respawnInFront();
    }

    private class_1799 totemStack() {
        class_2960 ident = class_2960.method_12829("totem_of_undying");
        class_1792 it = ident == null ? null : class_7923.field_41178.method_63535(ident);
        return it == null ? class_1799.field_8037 : it.method_7854();
    }

    private boolean totemRow(class_332 g, boolean v) {
        boolean hov = hit(cx, cy, cw, 18);
        rbox(g, cx, cy, cw, 18, mix(0x20000000, Theme.ROW_HOVER, anim("ft:" + curId, hov) * 0.6f));
        g.method_51427(totemStack(), cx + 2, cy + 1);
        text(g, "Тотем в руке", cx + 22, cy + 5, v ? Theme.TEXT : Theme.TEXT_DIM);
        sw(g, cx + cw - 28, cy + 3, "c:" + curId + "/totem", v);
        if (clicked && hov) v = !v;
        cy += 20;
        return v;
    }

    private boolean actionRow(class_332 g, String label) {
        boolean hov = hit(cx, cy, cw, 18);
        rbox(g, cx, cy, cw, 18, mix(0x20000000, Theme.ROW_HOVER, anim("ar:" + curId + label, hov) * 0.6f));
        text(g, label, cx + 8, cy + 5, Theme.TEXT);
        boolean c = clicked && hov;
        cy += 20;
        return c;
    }

    private void resetRow(class_332 g, String id) {
        boolean hov = hit(cx, cy, cw, 18);
        rbox(g, cx, cy, cw, 18, mix(0x30000000, 0x50FF5555, anim("rs:" + id, hov)));
        String t = "Сбросить настройки";
        text(g, t, cx + (cw - field_22793.method_1727(t)) / 2, cy + 5, hov ? 0xFFFF7777 : Theme.TEXT_DIM);
        if (clicked && hov) { ModuleDefaults.reset(id); Config.save(); }
        cy += 20;
    }

    private void noOptions(class_332 g) {
        text(g, "Нет настроек", cx + 2, cy + 2, Theme.TEXT_DIM);
        cy += 20;
    }

    private void sbSettings(class_332 g, String id) {
        switch (id) {
            case "fb" -> Config.gamma = cslider(g, "Яркость", 1, 15, Config.gamma, false);
            case "cross" -> {
                Config.crosshairColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.crosshairColor);
                Config.crosshairSize = (int) cslider(g, "Размер", 2, 20, Config.crosshairSize, true);
                Config.crosshairThickness = (int) cslider(g, "Толщина", 1, 5, Config.crosshairThickness, true);
                Config.crosshairDot = cbool(g, "Точка", Config.crosshairDot);
            }
            case "trails" -> {
                Config.trailsCustom = cbool(g, "Своя палитра", Config.trailsCustom);
                if (Config.trailsCustom) {
                    Config.trailsHue = cgrad(g, Config.trailsHue, pos -> hsv(pos, 1.0, 1.0));
                    Config.trailsSat = cgrad(g, Config.trailsSat, pos -> hsv(Config.trailsHue, pos, Math.max(0.35, Config.trailsVal)));
                    Config.trailsVal = cgrad(g, Config.trailsVal, pos -> hsv(Config.trailsHue, Config.trailsSat, pos));
                } else {
                    Config.trailsColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.trailsColor);
                }
                Config.trailsLength = (int) cslider(g, "Длина (тики)", 10, 80, Config.trailsLength, true);
                Config.trailsHeight = cslider(g, "Высота", 0.1, 2.0, Config.trailsHeight, false);
            }
            case "zones" -> {
                Config.zonesHeld = cbool(g, "Показывать в руках", Config.zonesHeld);
                hint2(g, "Радиусы при предмете", "в руке, таймер трапки");
            }
            case "tesp" -> {
                Config.targetEspShape = ccycle(g, "Фигура", TargetEsp.SHAPES, Config.targetEspShape);
                Config.targetEspHue = cgradL(g, "Цвет", Config.targetEspHue, pos -> hsv(pos, 0.85, 1.0));
                Config.targetEspHitHue = cgradL(g, "Цвет при ударе", Config.targetEspHitHue, pos -> hsv(pos, 0.85, 1.0));
                Config.targetEspSize = cslider(g, "Размер", 0.5, 2.0, Config.targetEspSize, false);
                Config.targetEspSpeed = cslider(g, "Скорость", 0.2, 3.0, Config.targetEspSpeed, false);
                Config.targetEspAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.targetEspAlpha, false);
            }
            case "jc" -> {
                Config.jcStyle = ccycle(g, "Стиль", JumpCircle.STYLES, Config.jcStyle);
                Config.jcRainbow = cbool(g, "Радуга", Config.jcRainbow);
                Config.jcHue = cgradL(g, "Цвет", Config.jcHue, pos -> hsv(pos, 0.7, 1.0));
                Config.jcSize = cslider(g, "Размер", 0.5, 4.0, Config.jcSize, false);
                Config.jcWidth = cslider(g, "Толщина", 0.1, 1.0, Config.jcWidth, false);
                Config.jcLife = cslider(g, "Длительность (с)", 0.3, 3.0, Config.jcLife, false);
                Config.jcAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.jcAlpha, false);
                Config.jcLand = cbool(g, "При приземлении", Config.jcLand);
            }
            case "ci" -> screenButton(g, "-> Открыть редактор", () -> new CustomItemsScreen(this));
            case "fake" -> fakeBlock(g);
            case "hv" -> {
                Config.leftHanded = cbool(g, "Левша (главная рука слева)", Config.leftHanded);
                Config.svHand = ccycle(g, "Рука", HandView.HANDS, Config.svHand);
                int hh = Config.svHand;
                Config.sv[HandView.SCALE][hh] = cslider(g, "Размер", 0.2, 2.5, Config.sv[HandView.SCALE][hh], false);
                Config.sv[HandView.X][hh] = cslider(g, "Смещение X (см)", -60, 60, Config.sv[HandView.X][hh], true);
                Config.sv[HandView.Y][hh] = cslider(g, "Смещение Y (см)", -60, 60, Config.sv[HandView.Y][hh], true);
                Config.sv[HandView.Z][hh] = cslider(g, "Смещение Z (см)", -60, 60, Config.sv[HandView.Z][hh], true);
                Config.sv[HandView.RX][hh] = cslider(g, "Поворот X (°)", -180, 180, Config.sv[HandView.RX][hh], true);
                Config.sv[HandView.RY][hh] = cslider(g, "Поворот Y (°)", -180, 180, Config.sv[HandView.RY][hh], true);
                Config.sv[HandView.RZ][hh] = cslider(g, "Поворот Z (°)", -180, 180, Config.sv[HandView.RZ][hh], true);
                Config.svNoSwing[hh] = cbool(g, "Без замаха", Config.svNoSwing[hh]);
            }
            case "thud" -> {
                Config.thScale = cslider(g, "Размер", 0.6, 2.0, Config.thScale, false);
                Config.thX = cslider(g, "Позиция X (%)", 5, 95, Config.thX, true);
                Config.thY = cslider(g, "Позиция Y (%)", 5, 90, Config.thY, true);
                Config.thHue = cgradL(g, "Цвет", Config.thHue, pos -> hsv(pos, 0.75, 1.0));
                Config.thDist = cbool(g, "Расстояние", Config.thDist);
            }
            case "fog" -> {
                Config.fogOff = cbool(g, "Без тумана", Config.fogOff);
                Config.fogStart = cslider(g, "Начало (блоки)", 0, 200, Config.fogStart, true);
                Config.fogEnd = cslider(g, "Конец (блоки)", 16, 400, Config.fogEnd, true);
                Config.fogHue = cgradL(g, "Цвет", Config.fogHue, pos -> hsv(pos, 1.0, 1.0));
                Config.fogSat = cgradL(g, "Насыщенность", Config.fogSat, pos -> hsv(Config.fogHue, pos, Math.max(0.35, Config.fogVal)));
                Config.fogVal = cgradL(g, "Яркость", Config.fogVal, pos -> hsv(Config.fogHue, Config.fogSat, pos));
            }
            case "physic" -> Config.physicLift = cslider(g, "Высота (см)", -15, 15, Config.physicLift, true);
            case "dmg" -> {
                Config.dfMine = cbool(g, "Только мои удары", Config.dfMine);
                Config.dfDecimals = cbool(g, "Дробные числа", Config.dfDecimals);
                Config.dfHue = cgradL(g, "Цвет", Config.dfHue, pos -> hsv(pos, 0.85, 1.0));
                Config.dfSize = cslider(g, "Размер", 0.5, 2.5, Config.dfSize, false);
                Config.dfLife = cslider(g, "Время (сек)", 0.5, 3.0, Config.dfLife, false);
            }
            case "part" -> {
                Config.partMode = ccycle(g, "Режим", Particles.MODES, Config.partMode);
                Config.partShape = ccycle(g, "Форма", Particles.SHAPES, Config.partShape);
                Config.partHue = cgradL(g, "Цвет", Config.partHue, pos -> hsv(pos, 0.75, 1.0));
                Config.partRainbow = cbool(g, "Радуга", Config.partRainbow);
                Config.partCount = cslider(g, "Количество", 5, 60, Config.partCount, true);
                Config.partSize = cslider(g, "Размер", 0.05, 0.4, Config.partSize, false);
                Config.partLife = cslider(g, "Время жизни", 0.3, 3.0, Config.partLife, false);
                Config.partGravity = cslider(g, "Гравитация", -2.0, 10.0, Config.partGravity, false);
                Config.partAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.partAlpha, false);
            }
            case "sky" -> {
                Config.skyPreset = ccycle(g, "Небо", SkyRenderer.PRESETS, Config.skyPreset);
                Config.skyAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.skyAlpha, false);
                Config.skySpeed = cslider(g, "Скорость", 0.2, 3.0, Config.skySpeed, false);
                Config.skyNightOnly = cbool(g, "Только ночью", Config.skyNightOnly);
                Config.skyStars = cbool(g, "Звёзды", Config.skyStars);
            }
            case "hat" -> {
                Config.hatCustom = cbool(g, "Своя палитра", Config.hatCustom);
                if (Config.hatCustom) {
                    Config.hatHue = cgrad(g, Config.hatHue, pos -> hsv(pos, 1.0, 1.0));
                    Config.hatSat = cgrad(g, Config.hatSat, pos -> hsv(Config.hatHue, pos, Math.max(0.35, Config.hatVal)));
                    Config.hatVal = cgrad(g, Config.hatVal, pos -> hsv(Config.hatHue, Config.hatSat, pos));
                } else {
                    Config.chinaHatMode = ccycle(g, "Цвет", ChinaHatRenderer.COLOR_NAMES, Config.chinaHatMode);
                }
                Config.hatSize = cslider(g, "Размер", 0.5, 2.0, Config.hatSize, false);
                Config.hatHeight = cslider(g, "Высота", 0.5, 2.0, Config.hatHeight, false);
            }
            case "halo" -> {
                Config.haloCustom = cbool(g, "Своя палитра", Config.haloCustom);
                if (Config.haloCustom) {
                    Config.haloHue = cgrad(g, Config.haloHue, pos -> hsv(pos, 1.0, 1.0));
                    Config.haloSat = cgrad(g, Config.haloSat, pos -> hsv(Config.haloHue, pos, Math.max(0.35, Config.haloVal)));
                    Config.haloVal = cgrad(g, Config.haloVal, pos -> hsv(Config.haloHue, Config.haloSat, pos));
                } else {
                    Config.haloMode = ccycle(g, "Цвет", HaloRenderer.COLOR_NAMES, Config.haloMode);
                }
                Config.haloSize = cslider(g, "Размер", 0.5, 2.0, Config.haloSize, false);
                Config.haloHeight = cslider(g, "Высота", 0.5, 2.0, Config.haloHeight, false);
                Config.haloBob = cbool(g, "Парение", Config.haloBob);
            }
            case "horns" -> {
                Config.hornsCustom = cbool(g, "Своя палитра", Config.hornsCustom);
                if (Config.hornsCustom) {
                    Config.hornsHue = cgrad(g, Config.hornsHue, pos -> hsv(pos, 1.0, 1.0));
                    Config.hornsSat = cgrad(g, Config.hornsSat, pos -> hsv(Config.hornsHue, pos, Math.max(0.35, Config.hornsVal)));
                    Config.hornsVal = cgrad(g, Config.hornsVal, pos -> hsv(Config.hornsHue, Config.hornsSat, pos));
                } else {
                    Config.hornsMode = ccycle(g, "Цвет", HornsRenderer.COLOR_NAMES, Config.hornsMode);
                }
                Config.hornsSize = cslider(g, "Размер", 0.5, 2.0, Config.hornsSize, false);
                Config.hornsGlow = cbool(g, "Раскал", Config.hornsGlow);
            }
            case "hl" -> {
                ItemHighlight.Entry toRemove = null;
                for (ItemHighlight.Entry e : Config.highlights) {
                    if (nameRow(g, e.name) == 2) toRemove = e;
                    e.hue = cgrad(g, e.hue, pos -> hsv(pos, 0.85, 1.0));
                    e.alpha = cslider(g, "Прозрачность", 0.1, 1.0, e.alpha, false);
                }
                hint2(g, "Наведи на предмет", "и жми [" + ItemHighlight.keyName() + "]");
                if (toRemove != null) { Config.highlights.remove(toRemove); Config.save(); }
            }
            case "hud" -> {
                Config.hudHoriz = cbool(g, "Горизонтально", Config.hudHoriz);
                Config.hudFps = cbool(g, "FPS", Config.hudFps);
                Config.hudPing = cbool(g, "Ping", Config.hudPing);
                Config.hudCoords = cbool(g, "Координаты", Config.hudCoords);
                Config.hudFacing = cbool(g, "Направление", Config.hudFacing);
                Config.hudScale = cslider(g, "Размер", 0.5, 3.0, Config.hudScale, false);
                Config.hudAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.hudAlpha, false);
                Config.hudColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.hudColor);
                Config.hudX = (int) cslider(g, "X", 0, 400, Config.hudX, true);
                Config.hudY = (int) cslider(g, "Y", 0, 250, Config.hudY, true);
            }
            case "array" -> {
                Config.arrayListHoriz = cbool(g, "Горизонтально", Config.arrayListHoriz);
                Config.arrayListColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.arrayListColor);
                Config.arrayListShadow = cbool(g, "Тень", Config.arrayListShadow);
                Config.arrayListScale = cslider(g, "Размер", 0.5, 2.0, Config.arrayListScale, false);
                Config.arrayListAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.arrayListAlpha, false);
                Config.arrayListX = (int) cslider(g, "X", 0, 400, Config.arrayListX, true);
                Config.arrayListY = (int) cslider(g, "Y", 0, 250, Config.arrayListY, true);
            }
            case "potion" -> {
                Config.potionHoriz = cbool(g, "Горизонтально", Config.potionHoriz);
                Config.potionIcons = cbool(g, "Иконки", Config.potionIcons);
                Config.potionBars = cbool(g, "Полоска времени", Config.potionBars);
                Config.potionLevel = cbool(g, "Уровень (II, III)", Config.potionLevel);
                Config.potionColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.potionColor);
                Config.potionScale = cslider(g, "Размер", 0.5, 2.0, Config.potionScale, false);
                Config.potionAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.potionAlpha, false);
                Config.potionX = (int) cslider(g, "X", 0, 400, Config.potionX, true);
                Config.potionY = (int) cslider(g, "Y", 0, 250, Config.potionY, true);
            }
            case "armor" -> {
                Config.armorHudHoriz = cbool(g, "Горизонтально", Config.armorHudHoriz);
                Config.armorHudShowTool = cbool(g, "Инструмент", Config.armorHudShowTool);
                Config.armorHudShowDurability = cbool(g, "Прочность", Config.armorHudShowDurability);
                Config.armorHudColorIcons = cbool(g, "Цвет иконок", Config.armorHudColorIcons);
                Config.armorTint = cbool(g, "Цвет брони на игроках", Config.armorTint);
                Config.armorHudScale = cslider(g, "Размер", 0.5, 2.0, Config.armorHudScale, false);
                Config.armorHudAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.armorHudAlpha, false);
                Config.armorHudX = (int) cslider(g, "X", 0, 400, Config.armorHudX, true);
                Config.armorHudY = (int) cslider(g, "Y", 0, 400, Config.armorHudY, true);
            }
            case "keys" -> {
                Config.keystrokesHoriz = cbool(g, "Горизонтально", Config.keystrokesHoriz);
                Config.keystrokesShowMouse = cbool(g, "ЛКМ/ПКМ", Config.keystrokesShowMouse);
                Config.keystrokesShowSpace = cbool(g, "Space", Config.keystrokesShowSpace);
                Config.keystrokesScale = cslider(g, "Размер", 0.5, 2.0, Config.keystrokesScale, false);
                Config.keystrokesAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.keystrokesAlpha, false);
                Config.keystrokesColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.keystrokesColor);
                Config.keystrokesX = (int) cslider(g, "X", 0, 400, Config.keystrokesX, true);
                Config.keystrokesY = (int) cslider(g, "Y", 0, 400, Config.keystrokesY, true);
            }
            case "crit" -> {
                Config.critStyle = ccycle(g, "Стиль", CombatFx.CRIT_STYLES, Config.critStyle);
                Config.critCount = cslider(g, "Количество", 5, 60, Config.critCount, true);
            }
            case "kill" -> {
                Config.killFlash = cbool(g, "Вспышка", Config.killFlash);
                Config.killSound = cbool(g, "Звук", Config.killSound);
                Config.killParticles = ccycle(g, "Частицы", CombatFx.KILL_PARTICLES, Config.killParticles);
                Config.killColor = ccycle(g, "Цвет вспышки", Config.COLOR_NAMES, Config.killColor);
            }
            case "atk" -> {
                Config.atkStyle = ccycle(g, "Стиль", CombatFx.ATK_STYLES, Config.atkStyle);
                Config.atkColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.atkColor);
                Config.atkSize = cslider(g, "Размер", 1, 12, Config.atkSize, true);
                Config.atkAlways = cbool(g, "Всегда показывать", Config.atkAlways);
                Config.atkOffset = (int) cslider(g, "Смещение вниз", 0, 40, Config.atkOffset, true);
            }
            case "stats" -> {
                Config.statsAcc = cbool(g, "Точность", Config.statsAcc);
                Config.statsKills = cbool(g, "Киллы", Config.statsKills);
                Config.statsStreak = cbool(g, "Серия", Config.statsStreak);
                Config.statsDeaths = cbool(g, "Смерти", Config.statsDeaths);
                Config.statsKd = cbool(g, "K/D", Config.statsKd);
                Config.statsPlayersOnly = cbool(g, "Киллы только игроков", Config.statsPlayersOnly);
                Config.statsColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.statsColor);
                Config.statsScale = cslider(g, "Размер", 0.5, 2.5, Config.statsScale, false);
                Config.statsAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.statsAlpha, false);
                Config.statsX = (int) cslider(g, "X", 0, 400, Config.statsX, true);
                Config.statsY = (int) cslider(g, "Y", 0, 250, Config.statsY, true);
                if (actionRow(g, "Сбросить статистику")) Combat.resetStats();
            }
            case "dlog" -> {
                Config.dlDealt = cbool(g, "Нанесённый урон", Config.dlDealt);
                Config.dlTaken = cbool(g, "Полученный урон", Config.dlTaken);
                Config.dlLines = (int) cslider(g, "Строк", 3, 10, Config.dlLines, true);
                Config.dlLife = cslider(g, "Время (сек)", 2, 20, Config.dlLife, false);
                Config.dlColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.dlColor);
                Config.dlScale = cslider(g, "Размер", 0.5, 2.5, Config.dlScale, false);
                Config.dlAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.dlAlpha, false);
                Config.dlX = (int) cslider(g, "X", 0, 400, Config.dlX, true);
                Config.dlY = (int) cslider(g, "Y", 0, 250, Config.dlY, true);
            }
            case "hbox" -> {
                Config.hbPlayers = cbool(g, "Игроки", Config.hbPlayers);
                Config.hbMobs = cbool(g, "Мобы", Config.hbMobs);
                Config.hbOthers = cbool(g, "Прочие сущности", Config.hbOthers);
                Config.hbSelf = cbool(g, "Свой (в 3-м лице)", Config.hbSelf);
                Config.hbFill = cbool(g, "Заливка", Config.hbFill);
                Config.hbColorP = ccycle(g, "Цвет игроков", Config.COLOR_NAMES, Config.hbColorP);
                Config.hbColorM = ccycle(g, "Цвет мобов", Config.COLOR_NAMES, Config.hbColorM);
                Config.hbColorO = ccycle(g, "Цвет прочих", Config.COLOR_NAMES, Config.hbColorO);
                Config.hbWidth = cslider(g, "Толщина", 0.005, 0.06, Config.hbWidth, false);
                Config.hbRange = cslider(g, "Дальность", 8, 128, Config.hbRange, true);
                Config.hbAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.hbAlpha, false);
            }
            case "bout" -> {
                Config.boColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.boColor);
                Config.boRainbow = cbool(g, "Радуга", Config.boRainbow);
                Config.boWidth = cslider(g, "Толщина", 0.005, 0.06, Config.boWidth, false);
                Config.boFill = cbool(g, "Заливка", Config.boFill);
                Config.boFillAlpha = cslider(g, "Прозрачность заливки", 0.05, 0.8, Config.boFillAlpha, false);
                Config.boHideVanilla = cbool(g, "Скрыть ванильную рамку", Config.boHideVanilla);
            }
            case "chunk" -> {
                Config.cbRadius = (int) cslider(g, "Радиус (чанков)", 1, 4, Config.cbRadius, true);
                Config.cbHeight = cslider(g, "Высота показа", 16, 128, Config.cbHeight, true);
                Config.cbColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.cbColor);
                Config.cbWidth = cslider(g, "Толщина", 0.01, 0.08, Config.cbWidth, false);
                Config.cbLevels = cbool(g, "Уровни каждые 16", Config.cbLevels);
                Config.cbAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.cbAlpha, false);
            }
            case "iesp" -> {
                Config.ieRange = cslider(g, "Дальность", 8, 128, Config.ieRange, true);
                Config.ieValuable = cbool(g, "Только ценные", Config.ieValuable);
                Config.ieFill = cbool(g, "Заливка", Config.ieFill);
                Config.ieTracers = cbool(g, "Трейсеры", Config.ieTracers);
                Config.ieColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.ieColor);
                Config.ieColorHi = ccycle(g, "Цвет ценных", Config.COLOR_NAMES, Config.ieColorHi);
                Config.ieWidth = cslider(g, "Толщина", 0.005, 0.06, Config.ieWidth, false);
            }
            case "ptr" -> {
                Config.ptColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.ptColor);
                Config.ptRainbow = cbool(g, "Радуга", Config.ptRainbow);
                Config.ptLength = (int) cslider(g, "Длина (тики)", 10, 200, Config.ptLength, true);
                Config.ptWidth = cslider(g, "Толщина", 0.02, 0.2, Config.ptWidth, false);
            }
            case "etr" -> {
                Config.etColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.etColor);
                Config.etRainbow = cbool(g, "Радуга", Config.etRainbow);
                Config.etLength = (int) cslider(g, "Длина (тики)", 10, 200, Config.etLength, true);
                Config.etWidth = cslider(g, "Толщина", 0.04, 0.4, Config.etWidth, false);
                Config.etAll = cbool(g, "Все игроки", Config.etAll);
            }
            case "pcd" -> {
                Config.pcdHideIdle = cbool(g, "Скрывать, когда готов", Config.pcdHideIdle);
                Config.pcdColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.pcdColor);
                Config.pcdScale = cslider(g, "Размер", 0.5, 2.5, Config.pcdScale, false);
                Config.pcdAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.pcdAlpha, false);
                Config.pcdX = (int) cslider(g, "X", 0, 400, Config.pcdX, true);
                Config.pcdY = (int) cslider(g, "Y", 0, 250, Config.pcdY, true);
            }
            case "wp" -> wpBlock(g);
            case "evpt" -> evptBlock(g);
            case "wings" -> {
                Config.wingStyle = ccycle(g, "Стиль", Cosmetics.WING_STYLES, Config.wingStyle);
                Config.wingColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.wingColor);
                Config.wingRainbow = cbool(g, "Радуга", Config.wingRainbow);
                Config.wingSize = cslider(g, "Размер", 0.5, 2.0, Config.wingSize, false);
                Config.wingSpeed = cslider(g, "Скорость взмаха", 0.3, 3.0, Config.wingSpeed, false);
                Config.wingFriends = cbool(g, "Для друзей", Config.wingFriends);
            }
            case "aura" -> {
                Config.auraStyle = ccycle(g, "Стиль", Cosmetics.AURA_STYLES, Config.auraStyle);
                Config.auraColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.auraColor);
                Config.auraRainbow = cbool(g, "Радуга", Config.auraRainbow);
                Config.auraSize = cslider(g, "Размер", 0.5, 2.0, Config.auraSize, false);
                Config.auraSpeed = cslider(g, "Скорость", 0.3, 3.0, Config.auraSpeed, false);
                Config.auraFriends = cbool(g, "Для друзей", Config.auraFriends);
            }
            case "cape" -> {
                Config.capeColor = ccycle(g, "Цвет сверху", Config.COLOR_NAMES, Config.capeColor);
                Config.capeColor2 = ccycle(g, "Цвет снизу", Config.COLOR_NAMES, Config.capeColor2);
                Config.capeRainbow = cbool(g, "Радуга", Config.capeRainbow);
                Config.capeWave = cslider(g, "Волны", 0.0, 1.5, Config.capeWave, false);
                Config.capeFriends = cbool(g, "Для друзей", Config.capeFriends);
            }
            case "nt" -> {
                Config.ntRange = cslider(g, "Дальность", 16, 128, Config.ntRange, true);
                Config.ntScale = cslider(g, "Размер", 0.5, 2.0, Config.ntScale, false);
                Config.ntBg = cbool(g, "Фон", Config.ntBg);
                Config.ntHealth = cbool(g, "Здоровье", Config.ntHealth);
                Config.ntDist = cbool(g, "Расстояние", Config.ntDist);
                Config.ntDistScale = cbool(g, "Масштаб по расстоянию", Config.ntDistScale);
                Config.ntHide = cbool(g, "Скрыть ванильные", Config.ntHide);
            }
            case "chams" -> {
                Config.chPlayers = cbool(g, "Игроки", Config.chPlayers);
                Config.chMobs = cbool(g, "Мобы", Config.chMobs);
                Config.chFriends = cbool(g, "Друзья", Config.chFriends);
                Config.chSelf = cbool(g, "Свой (3-е лицо)", Config.chSelf);
            }
            case "swing" -> {
                Config.swingSpeed = cslider(g, "Скорость взмаха", 0.3, 3.0, Config.swingSpeed, false);
                Config.swingSelfOnly = cbool(g, "Только свой", Config.swingSelfOnly);
            }
            case "iscroll" -> {
                Config.scrollerWheel = cbool(g, "Колесо мыши", Config.scrollerWheel);
                Config.scrollerDrag = cbool(g, "Shift + протяжка", Config.scrollerDrag);
                Config.scrollerInvert = cbool(g, "Инвертировать колесо", Config.scrollerInvert);
                Config.scrollerDelay = cslider(g, "Задержка (мс)", 0, 200, Config.scrollerDelay, true);
            }
            case "clouds" -> {
                Config.cloudHide = cbool(g, "Скрыть облака", Config.cloudHide);
                Config.cloudTint = cbool(g, "Свой цвет", Config.cloudTint);
                Config.cloudHue = cslider(g, "Оттенок", 0.0, 1.0, Config.cloudHue, false);
                Config.cloudSat = cslider(g, "Насыщенность", 0.0, 1.0, Config.cloudSat, false);
                Config.cloudRainbow = cbool(g, "Радуга", Config.cloudRainbow);
                Config.cloudHeightOffset = cslider(g, "Высота (смещение)", -40, 120, Config.cloudHeightOffset, true);
            }
            case "tip" -> {
                Config.tipDur = cbool(g, "Прочность", Config.tipDur);
                Config.tipId = cbool(g, "ID предмета", Config.tipId);
                Config.tipStack = cbool(g, "Размер стака", Config.tipStack);
            }
            case "server" -> {
                Config.serverIp = cbool(g, "IP сервера", Config.serverIp);
                Config.serverTps = cbool(g, "TPS", Config.serverTps);
                Config.serverPing = cbool(g, "Ping", Config.serverPing);
                Config.serverColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.serverColor);
                Config.serverScale = cslider(g, "Размер", 0.5, 2.5, Config.serverScale, false);
                Config.serverAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.serverAlpha, false);
                Config.serverX = (int) cslider(g, "X", 0, 400, Config.serverX, true);
                Config.serverY = (int) cslider(g, "Y", 0, 250, Config.serverY, true);
            }
            case "clock" -> {
                Config.clockSeconds = cbool(g, "Секунды", Config.clockSeconds);
                Config.clockDate = cbool(g, "Дата", Config.clockDate);
                Config.clockGame = cbool(g, "Время в мире", Config.clockGame);
                Config.clockColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.clockColor);
                Config.clockScale = cslider(g, "Размер", 0.5, 2.5, Config.clockScale, false);
                Config.clockAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.clockAlpha, false);
                Config.clockX = (int) cslider(g, "X", 0, 400, Config.clockX, true);
                Config.clockY = (int) cslider(g, "Y", 0, 250, Config.clockY, true);
            }
            case "inv" -> {
                Config.invHotbar = cbool(g, "Показывать хотбар", Config.invHotbar);
                Config.invColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.invColor);
                Config.invScale = cslider(g, "Размер", 0.5, 2.0, Config.invScale, false);
                Config.invAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.invAlpha, false);
                Config.invX = (int) cslider(g, "X", 0, 400, Config.invX, true);
                Config.invY = (int) cslider(g, "Y", 0, 250, Config.invY, true);
            }
            case "totem" -> {
                Config.totemHideZero = cbool(g, "Скрывать при 0", Config.totemHideZero);
                Config.totemColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.totemColor);
                Config.totemScale = cslider(g, "Размер", 0.5, 2.5, Config.totemScale, false);
                Config.totemAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.totemAlpha, false);
                Config.totemX = (int) cslider(g, "X", 0, 400, Config.totemX, true);
                Config.totemY = (int) cslider(g, "Y", 0, 250, Config.totemY, true);
            }
            case "near" -> {
                Config.nearColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.nearColor);
                Config.nearRange = cslider(g, "Дистанция", 20, 300, Config.nearRange, true);
                Config.nearMax = cslider(g, "Макс. игроков", 1, 12, Config.nearMax, true);
                Config.nearScale = cslider(g, "Размер", 0.5, 2.5, Config.nearScale, false);
                Config.nearAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.nearAlpha, false);
                Config.nearX = (int) cslider(g, "X", 0, 400, Config.nearX, true);
                Config.nearY = (int) cslider(g, "Y", 0, 250, Config.nearY, true);
            }
            case "cdh" -> {
                Config.cdTime = cbool(g, "Показывать время", Config.cdTime);
                Config.cdColor = ccycle(g, "Цвет", CooldownHud.COLOR_NAMES, Config.cdColor);
                Config.cdAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.cdAlpha, false);
                Config.cdX = (int) cslider(g, "X", 0, 400, Config.cdX, true);
                Config.cdY = (int) cslider(g, "Y", 0, 250, Config.cdY, true);
            }
            case "combo" -> {
                Config.comboColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.comboColor);
                Config.comboTimeout = cslider(g, "Таймаут (сек)", 1.0, 10.0, Config.comboTimeout, false);
                Config.comboScale = cslider(g, "Размер", 0.5, 2.5, Config.comboScale, false);
                Config.comboAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.comboAlpha, false);
                Config.comboX = (int) cslider(g, "X", 0, 400, Config.comboX, true);
                Config.comboY = (int) cslider(g, "Y", 0, 250, Config.comboY, true);
            }
            case "wm" -> {
                Config.watermarkName = ccycle(g, "Название", WatermarkHud.NAMES, Config.watermarkName);
                Config.watermarkNick = cbool(g, "Ник", Config.watermarkNick);
                Config.watermarkFps = cbool(g, "FPS", Config.watermarkFps);
                Config.watermarkTime = cbool(g, "Время", Config.watermarkTime);
                Config.watermarkRainbow = cbool(g, "Радуга", Config.watermarkRainbow);
                Config.watermarkColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.watermarkColor);
                Config.watermarkScale = cslider(g, "Размер", 0.5, 2.5, Config.watermarkScale, false);
                Config.watermarkAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.watermarkAlpha, false);
                Config.hitSoundPitch = cslider(g, "Высота тона", 0.5, 2.0, Config.hitSoundPitch, false);
            }
            case "sprint" -> Config.sprintAllDirs = cbool(g, "Все стороны", Config.sprintAllDirs);
            case "freelook" -> {
                Config.freelookToggle = cbool(g, "Переключатель (вместо зажатия)", Config.freelookToggle);
                Config.freelookThird = cbool(g, "Третье лицо при включении", Config.freelookThird);
                Config.freelookSmooth = cbool(g, "Плавный возврат", Config.freelookSmooth);
                Config.freelookSens = cslider(g, "Чувствительность", 0.2, 2.0, Config.freelookSens, false);
            }
            case "zoom" -> Config.zoomFov = cslider(g, "FOV", 10, 60, Config.zoomFov, true);
            case "autoswap" -> {
                SwapPair toRebind = null, toRemove = null;
                for (SwapPair sp : Config.swapPairs) {
                    int r = listRow(g, sp.key, sp.a + " <-> " + sp.b);
                    if (r == 1) toRebind = sp; else if (r == 2) toRemove = sp;
                }
                hint2(g, "Наведи на предмет", "и жми [" + ItemFeatures.swapKeyName() + "]");
                if (toRemove != null) { Config.swapPairs.remove(toRemove); Config.save(); }
                else if (toRebind != null) {
                    final SwapPair sp = toRebind;
                    field_22787.execute(() -> field_22787.method_1507(new BindScreen(this, "AutoSwap: " + sp.a + " <-> " + sp.b, k -> ItemFeatures.setKey(sp, k))));
                }
            }
            case "itembind" -> {
                BoundItem toRebind = null, toRemove = null;
                for (BoundItem b : Config.binds) {
                    int r = listRow(g, b.key, b.name);
                    if (r == 1) toRebind = b; else if (r == 2) toRemove = b;
                }
                hint2(g, "Наведи на предмет", "и жми [" + ItemFeatures.bindKeyName() + "]");
                if (toRemove != null) { Config.binds.remove(toRemove); Config.save(); }
                else if (toRebind != null) {
                    final BoundItem b = toRebind;
                    field_22787.execute(() -> field_22787.method_1507(new BindScreen(this, "ItemBind: " + b.name, k -> ItemFeatures.setKey(b, k))));
                }
            }
            case "friends" -> screenButton(g, "-> Открыть Friends", () -> new FriendsScreen(this));
            case "macro" -> screenButton(g, "-> Настроить макросы", () -> new MacrosScreen(this));
            case "configs" -> screenButton(g, "-> Открыть конфиги", () -> new ConfigsScreen(this));
            case "time" -> Config.timeValue = (int) cslider(g, "Время", 0, 24000, Config.timeValue, true);
            case "weather" -> Config.weatherType = ccycle(g, "Тип", new String[]{"Ясно", "Дождь", "Гроза"}, Config.weatherType);
            default -> noOptions(g);
        }
        if (ModuleDefaults.has(id)) resetRow(g, id);
    }

    @Override
    public void method_25432() { Config.save(); }
}
