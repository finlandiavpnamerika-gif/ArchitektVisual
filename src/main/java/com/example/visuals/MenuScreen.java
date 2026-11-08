package com.example.visuals;

import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class MenuScreen extends Screen {
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

    public MenuScreen() { super(Text.literal("ArchitektVisuals")); }

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
    private void text(DrawContext g, String s, int x, int y, int color) { g.drawTextWithShadow(textRenderer, s, x, y, ff(color)); }

    private void box(DrawContext g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + h, ff(color));
    }

    // Прямоугольник со срезанными углами
    private void rbox(DrawContext g, int x, int y, int w, int h, int color) {
        rr(g, x, y, w, h, Math.min(Theme.CORNER, Math.min(w, h) / 2), color);
    }

    // Настоящий скруглённый прямоугольник (радиус r)
    private void rr(DrawContext g, int x, int y, int w, int h, int r, int color) {
        int c = ff(color);
        if (r <= 0 || w < 2 * r || h < 2 * r) { g.fill(x, y, x + w, y + h, c); return; }
        g.fill(x, y + r, x + w, y + h - r, c);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = r - (int) Math.round(Math.sqrt(r * r - dy * dy));
            g.fill(x + inset, y + i, x + w - inset, y + i + 1, c);
            g.fill(x + inset, y + h - 1 - i, x + w - inset, y + h - i, c);
        }
    }

    // Мягкая тень под скруглённым блоком
    private void shadow(DrawContext g, int x, int y, int w, int h, int r) {
        for (int i = 3; i >= 1; i--) rr(g, x - i, y - i + 2, w + 2 * i, h + 2 * i, r + i, 0x10000000);
    }

    // Горизонтальная линия, плавно исчезающая по краям
    private void fadeLine(DrawContext g, int x, int y, int w, int color, int maxAlpha) {
        for (int i = 0; i < w; i += 4) {
            double t = (i + 2.0) / w;
            int a = (int) (maxAlpha * Math.sin(Math.PI * t));
            g.fill(x + i, y, x + Math.min(w, i + 4), y + 1, ff(withAlpha(color, Math.max(0, a))));
        }
    }

    private void panel(DrawContext g, String title, int x, int y, int w, int h) {
        shadow(g, x, y, w, h, 6);
        rr(g, x, y, w, h, 6, Theme.PANEL_BG);
        g.fillGradient(x + 3, y + 1, x + w - 3, y + 24,
                ff(withAlpha(Theme.ACCENT, 70)), ff(withAlpha(Theme.ACCENT, 0)));
        fadeLine(g, x + 6, y, w - 12, Theme.ACCENT, 255);
        g.drawCenteredTextWithShadow(textRenderer, Text.literal(title).formatted(Formatting.BOLD),
                x + w / 2, y + 8, ff(Theme.TEXT));
        fadeLine(g, x + 10, y + 22, w - 20, Theme.ACCENT, 160);
        cx = x + 6; cw = w - 12; cy = y + 28;
    }

    // Тумблер с плавным движением ползунка
    private void sw(DrawContext g, int x, int y, String key, boolean on) {
        float p = anim(key, on);
        if (p > 0.05f) rr(g, x - 2, y - 2, 28, 16, 8, withAlpha(Theme.ACCENT, (int) (46 * p)));
        rr(g, x, y, 24, 12, 6, mix(Theme.TOGGLE_OFF, Theme.TOGGLE_ON, p));
        int kx = x + 2 + Math.round(p * 12);
        rr(g, kx, y + 2, 8, 8, 4, 0xFFFFFFFF);
    }

    private int mh(String id, int options) { return 24 + (open.contains(id) ? options * 20 + 4 : 0); }

    // ---------- элементы ----------
    private boolean module(DrawContext g, String id, String label, String hint, boolean on, int options) {
        curId = id;
        boolean hov = hit(cx, cy, cw, 22);
        float hv = anim("h:" + id, hov);
        int rowCol = mix(Theme.ROW, Theme.ROW_HOVER, hv * 0.6f);
        if (on) rowCol = mix(rowCol, withAlpha(Theme.ACCENT, 0x40), 0.45f);
        rbox(g, cx, cy, cw, 22, rowCol);

        if (on) rr(g, cx, cy + 4, 3, 14, 1, Theme.ACCENT); // Полоска слева, если включено

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
    private void soon(DrawContext g, String label) {
        rbox(g, cx, cy, cw, 22, Theme.ROW);
        text(g, label, cx + 8, cy + 7, Theme.TEXT_DIM);
        String s = "скоро";
        text(g, s, cx + cw - 8 - textRenderer.getWidth(s), cy + 7, Theme.ACCENT);
        cy += 24;
    }

    private boolean cbool(DrawContext g, String label, boolean v) {
        boolean hov = hit(cx, cy, cw, 18);
        rbox(g, cx, cy, cw, 18, mix(0x20000000, Theme.ROW_HOVER, anim("hb:" + curId + label, hov) * 0.6f));
        text(g, label, cx + 8, cy + 5, v ? Theme.TEXT : Theme.TEXT_DIM);
        sw(g, cx + cw - 28, cy + 3, "c:" + curId + "/" + label, v);
        if (clicked && hov) v = !v;
        cy += 20;
        return v;
    }

    private double cslider(DrawContext g, String label, double min, double max, double v, boolean isInt) {
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
        text(g, val, cx + cw - 6 - textRenderer.getWidth(val), cy + 2, Theme.TEXT);
        rr(g, cx + 6, cy + 12, tw, 4, 2, 0x45FFFFFF);
        if (fw > 1) {
            int c0 = mix(Theme.ACCENT, 0xFF14141C, 0.5f), c1 = Theme.ACCENT;
            for (int i = 0; i < fw; i += 4) {
                int c = mix(c0, c1, (float) i / Math.max(1, tw));
                g.fill(cx + 6 + i, cy + 12, cx + 6 + Math.min(fw, i + 4), cy + 16, ff(0xFF000000 | (c & 0xFFFFFF)));
            }
            rr(g, cx + 6 + fw - 3, cy + 10, 6, 8, 3, 0xFFFFFFFF);
        }
        cy += 20;
        return v;
    }

    private int ccycle(DrawContext g, String label, String[] names, int idx) {
        boolean hov = hit(cx, cy, cw, 18);
        rbox(g, cx, cy, cw, 18, mix(0x20000000, Theme.ROW_HOVER, anim("hc:" + curId + label, hov) * 0.6f));
        text(g, label + ": " + names[Math.max(0, Math.min(idx, names.length - 1))], cx + 8, cy + 5, Theme.TEXT_DIM);
        text(g, "›", cx + cw - 12, cy + 5, Theme.ACCENT);
        if (clicked && hov) idx = (idx + 1) % names.length;
        cy += 20;
        return idx;
    }

    // Палитра: градиентная полоска, возвращает позицию маркера 0..1
    private double cgrad(DrawContext g, double t, java.util.function.DoubleToIntFunction colorAt) {
        int id = idc++;
        boolean hov = hit(cx, cy, cw, 16);
        if (clicked && hov) active = id;
        if (active == id && down) t = Math.max(0.0, Math.min(1.0, (double) (mx - cx) / (double) cw));

        for (int x = 0; x < cw; x += 4) {
            int x2 = Math.min(x + 4, cw);
            g.fill(cx + x, cy, cx + x2, cy + 16, ff(0xFF000000 | colorAt.applyAsInt((x + 2.0) / cw)));
        }
        int mxp = cx + (int) (cw * t);
        g.fill(mxp - 2, cy - 1, mxp + 1, cy + 17, ff(0xFF000000));   // обводка маркера
        g.fill(mxp - 1, cy, mxp, cy + 16, ff(0xFFFFFFFF));           // маркер
        cy += 20;
        return t;
    }

    // Палитра с подписью поверх полоски
    private double cgradL(DrawContext g, String label, double t, java.util.function.DoubleToIntFunction colorAt) {
        int y0 = cy;
        double r = cgrad(g, t, colorAt);
        g.drawCenteredTextWithShadow(textRenderer, label, cx + cw / 2, y0 + 4, ff(0xFFFFFFFF));
        return r;
    }

    private static int hsv(double h, double s, double v) {
        return net.minecraft.util.math.MathHelper.hsvToRgb((float) h, (float) s, (float) v) & 0xFFFFFF;
    }

    private void hint2(DrawContext g, String l1, String l2) {
        int max = cw - 4;
        text(g, textRenderer.trimToWidth(l1, max), cx + 4, cy + 2, Theme.TEXT_DIM);
        text(g, textRenderer.trimToWidth(l2, max), cx + 4, cy + 12, Theme.TEXT_DIM);
        cy += 24;
    }

    private int listRow(DrawContext g, int key, String label) {
        boolean hov = hit(cx, cy, cw, 18);
        boolean overX = hit(cx + cw - 16, cy, 16, 18);
        rbox(g, cx, cy, cw, 18, hov ? Theme.ROW_HOVER : 0x20000000);
        String full = "[" + Keys.name(key) + "] " + label;
        text(g, textRenderer.trimToWidth(full, cw - 22), cx + 6, cy + 5, key == -1 ? 0xFFFF5555 : Theme.TEXT);
        text(g, "✕", cx + cw - 12, cy + 5, overX ? 0xFFFF5555 : Theme.TEXT_DIM);
        int result = 0;
        if (clicked && hov) result = overX ? 2 : 1;
        cy += 20;
        return result;
    }

    // Строка с названием предмета и крестиком удаления (2 = нажат крестик)
    private int nameRow(DrawContext g, String label) {
        boolean hov = hit(cx, cy, cw, 18);
        boolean overX = hit(cx + cw - 16, cy, 16, 18);
        rbox(g, cx, cy, cw, 18, hov ? Theme.ROW_HOVER : 0x20000000);
        text(g, textRenderer.trimToWidth(label, cw - 22), cx + 6, cy + 5, Theme.TEXT);
        text(g, "✕", cx + cw - 12, cy + 5, overX ? 0xFFFF5555 : Theme.TEXT_DIM);
        int result = (clicked && overX) ? 2 : 0;
        cy += 20;
        return result;
    }

    private void screenButton(DrawContext g, String label, Supplier<Screen> screen) {
        boolean hov = hit(cx, cy, cw, 22);
        rbox(g, cx, cy, cw, 22, hov ? Theme.ACCENT_DIM : Theme.ROW);
        text(g, label, cx + 8, cy + 7, Theme.TEXT);
        if (clicked && hov) client.setScreen(screen.get());
        cy += 24;
    }

    @Override
    public void render(DrawContext g, int mouseX, int mouseY, float delta) {
        mx = mouseX; my = mouseY;
        long window = client.getWindow().getHandle();
        down = GLFW.glfwGetMouseButton(window, 0) == GLFW.GLFW_PRESS;
        clicked = down && !prevDown;
        prevDown = down;
        if (!down) active = -1;
        idc = 0;

        // Плавное появление
        float p = Math.min(1f, (System.currentTimeMillis() - openedAt) / 200f);
        fade = 1f - (1f - p) * (1f - p);
        int slide = Math.round((1f - fade) * 10f);

        // Фон: затемнение с градиентом и лёгкий дождь
        g.fillGradient(0, 0, width, height, 0x80080810, 0xC0000000);
        RainyScene.drawRain(g, width, height, 0.3f);

        // Sidebar-стиль меню (переключается кнопкой снизу)
        if (Config.menuStyle == 1) { renderSidebar(g); drawBottom(g); return; }

        int cols = 4;
        int colW = Math.max(110, Math.min(150, (width - 30) / cols));
        int gap = 10;
        int x0 = (width - (colW * cols + gap * (cols - 1))) / 2;
        int y0 = 14 + slide;
        int step = colW + gap;

        // ========== ПЕРВЫЙ РЯД ==========

        // 1. Utilities
        int hUtil = 34 + mh("sprint", 2) + mh("zoom", 2) + mh("respawn", 0) + mh("freelook", 0);
        panel(g, "Utilities", x0, y0, colW, hUtil);
        Config.sprint = module(g, "sprint", "AutoSprint", "", Config.sprint, 1);
        if (open.contains("sprint")) { Config.sprintAllDirs = cbool(g, "Все стороны", Config.sprintAllDirs); cy += 2;  resetRow(g, "sprint");}
        Config.zoom = module(g, "zoom", "Zoom", "C", Config.zoom, 1);
        if (open.contains("zoom")) { Config.zoomFov = cslider(g, "FOV", 10, 60, Config.zoomFov, true); cy += 2;  resetRow(g, "zoom");}
        Config.autoRespawn = module(g, "respawn", "AutoRespawn", "", Config.autoRespawn, 0);
        Config.thirdPerson = module(g, "freelook", "Freelook", "", Config.thirdPerson, 0);

        // 2. Visuals
        int hVis = 34 + mh("fb", 2) + mh("cross", 5) + mh("trails", Config.trailsCustom ? 6 : 4) + mh("zones", 2) + mh("tesp", 7) + mh("sky", 6) + mh("part", 10) + mh("dmg", 6) + mh("fog", 7) + mh("thud", 6) + mh("hv", 11) + mh("ci", 8);
        panel(g, "Visuals", x0 + step, y0, colW, hVis);
        Config.fullbright = module(g, "fb", "Fullbright", "", Config.fullbright, 1);
        if (open.contains("fb")) { Config.gamma = cslider(g, "Яркость", 1, 15, Config.gamma, false); cy += 2;  resetRow(g, "fb");}

        Config.customCrosshair = module(g, "cross", "Crosshair", "", Config.customCrosshair, 4);
        if (open.contains("cross")) {
            Config.crosshairColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.crosshairColor);
            Config.crosshairSize = (int) cslider(g, "Размер", 2, 20, Config.crosshairSize, true);
            Config.crosshairThickness = (int) cslider(g, "Толщина", 1, 5, Config.crosshairThickness, true);
            Config.crosshairDot = cbool(g, "Точка", Config.crosshairDot);
            resetRow(g, "cross");
        }

        Config.trails = module(g, "trails", "Trails", "", Config.trails, Config.trailsCustom ? 6 : 4);
        if (open.contains("trails")) {
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
            resetRow(g, "trails");
        }

        Config.zones = module(g, "zones", "FunTime Zones", "", Config.zones, 1);
        if (open.contains("zones")) {
            Config.zonesHeld = cbool(g, "Чужие в руке", Config.zonesHeld);
            resetRow(g, "zones");
        }

        Config.targetEsp = module(g, "tesp", "Target ESP", "", Config.targetEsp, 6);
        if (open.contains("tesp")) {
            Config.targetEspShape = ccycle(g, "Фигура", TargetEsp.SHAPES, Config.targetEspShape);
            Config.targetEspHue = cgradL(g, "Цвет", Config.targetEspHue, pos -> hsv(pos, 0.85, 1.0));
            Config.targetEspHitHue = cgradL(g, "Цвет при ударе", Config.targetEspHitHue, pos -> hsv(pos, 0.85, 1.0));
            Config.targetEspSize = cslider(g, "Размер", 0.5, 2.0, Config.targetEspSize, false);
            Config.targetEspSpeed = cslider(g, "Скорость", 0.2, 3.0, Config.targetEspSpeed, false);
            Config.targetEspAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.targetEspAlpha, false);
            resetRow(g, "tesp");
        }

        Config.customItems = module(g, "ci", "CustomItems", "", Config.customItems, 7);
        if (open.contains("ci")) {
            Config.ciScale = cslider(g, "Размер", 0.2, 2.0, Config.ciScale, false);
            Config.ciX = cslider(g, "Смещение X (см)", -60, 60, Config.ciX, true);
            Config.ciY = cslider(g, "Смещение Y (см)", -60, 60, Config.ciY, true);
            Config.ciZ = cslider(g, "Смещение Z (см)", -60, 60, Config.ciZ, true);
            Config.ciRX = cslider(g, "Поворот X (°)", -180, 180, Config.ciRX, true);
            Config.ciRY = cslider(g, "Поворот Y (°)", -180, 180, Config.ciRY, true);
            Config.ciRZ = cslider(g, "Поворот Z (°)", -180, 180, Config.ciRZ, true);
            resetRow(g, "ci");
        }

        Config.handView = module(g, "hv", "Hand View", "", Config.handView, 10);
        if (open.contains("hv")) {
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
            resetRow(g, "hv");
        }

        Config.targetHud = module(g, "thud", "TargetHUD", "", Config.targetHud, 5);
        if (open.contains("thud")) {
            Config.thScale = cslider(g, "Размер", 0.6, 2.0, Config.thScale, false);
            Config.thX = cslider(g, "Позиция X (%)", 5, 95, Config.thX, true);
            Config.thY = cslider(g, "Позиция Y (%)", 5, 90, Config.thY, true);
            Config.thHue = cgradL(g, "Цвет", Config.thHue, pos -> hsv(pos, 0.75, 1.0));
            Config.thDist = cbool(g, "Расстояние", Config.thDist);
            resetRow(g, "thud");
        }

        Config.customFog = module(g, "fog", "CustomFog", "", Config.customFog, 6);
        if (open.contains("fog")) {
            Config.fogOff = cbool(g, "Без тумана", Config.fogOff);
            Config.fogStart = cslider(g, "Начало (блоки)", 0, 200, Config.fogStart, true);
            Config.fogEnd = cslider(g, "Конец (блоки)", 16, 400, Config.fogEnd, true);
            Config.fogHue = cgradL(g, "Цвет", Config.fogHue, pos -> hsv(pos, 1.0, 1.0));
            Config.fogSat = cgradL(g, "Насыщенность", Config.fogSat, pos -> hsv(Config.fogHue, pos, Math.max(0.35, Config.fogVal)));
            Config.fogVal = cgradL(g, "Яркость", Config.fogVal, pos -> hsv(Config.fogHue, Config.fogSat, pos));
            resetRow(g, "fog");
        }

        Config.damageFever = module(g, "dmg", "DamageViewer", "", Config.damageFever, 5);
        if (open.contains("dmg")) {
            Config.dfMine = cbool(g, "Только мои удары", Config.dfMine);
            Config.dfDecimals = cbool(g, "Дробные числа", Config.dfDecimals);
            Config.dfHue = cgradL(g, "Цвет", Config.dfHue, pos -> hsv(pos, 0.85, 1.0));
            Config.dfSize = cslider(g, "Размер", 0.5, 2.5, Config.dfSize, false);
            Config.dfLife = cslider(g, "Время (сек)", 0.5, 3.0, Config.dfLife, false);
            resetRow(g, "dmg");
        }

        Config.particles = module(g, "part", "Particles", "", Config.particles, 9);
        if (open.contains("part")) {
            Config.partMode = ccycle(g, "Режим", Particles.MODES, Config.partMode);
            Config.partShape = ccycle(g, "Форма", Particles.SHAPES, Config.partShape);
            Config.partHue = cgradL(g, "Цвет", Config.partHue, pos -> hsv(pos, 0.75, 1.0));
            Config.partRainbow = cbool(g, "Радуга", Config.partRainbow);
            Config.partCount = cslider(g, "Количество", 5, 60, Config.partCount, true);
            Config.partSize = cslider(g, "Размер", 0.05, 0.4, Config.partSize, false);
            Config.partLife = cslider(g, "Время жизни", 0.3, 3.0, Config.partLife, false);
            Config.partGravity = cslider(g, "Гравитация", -2.0, 10.0, Config.partGravity, false);
            Config.partAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.partAlpha, false);
            resetRow(g, "part");
        }

        Config.skyShader = module(g, "sky", "Sky Shader", "", Config.skyShader, 5);
        if (open.contains("sky")) {
            Config.skyPreset = ccycle(g, "Небо", SkyRenderer.PRESETS, Config.skyPreset);
            Config.skyAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.skyAlpha, false);
            Config.skySpeed = cslider(g, "Скорость", 0.2, 3.0, Config.skySpeed, false);
            Config.skyNightOnly = cbool(g, "Только ночью", Config.skyNightOnly);
            Config.skyStars = cbool(g, "Звёзды", Config.skyStars);
            resetRow(g, "sky");
        }

        // 3. HUD
        int hHud = 34 + mh("hud", 10) + mh("array", 7) + mh("armor", 7) + mh("potion", 10) + mh("keys", 8);
        panel(g, "HUD", x0 + step * 2, y0, colW, hHud);

        Config.hud = module(g, "hud", "Info HUD", "", Config.hud, 9);
        if (open.contains("hud")) {
            Config.hudFps = cbool(g, "FPS", Config.hudFps);
            Config.hudPing = cbool(g, "Ping", Config.hudPing);
            Config.hudCoords = cbool(g, "Координаты", Config.hudCoords);
            Config.hudFacing = cbool(g, "Направление", Config.hudFacing);
            Config.hudScale = cslider(g, "Размер", 0.5, 3.0, Config.hudScale, false);
            Config.hudAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.hudAlpha, false);
            Config.hudColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.hudColor);
            Config.hudX = (int) cslider(g, "X", 0, 400, Config.hudX, true);
            Config.hudY = (int) cslider(g, "Y", 0, 250, Config.hudY, true);
            cy += 2;
            resetRow(g, "hud");
        }

        Config.arrayList = module(g, "array", "ArrayList", "", Config.arrayList, 6);
        if (open.contains("array")) {
            Config.arrayListColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.arrayListColor);
            Config.arrayListShadow = cbool(g, "Тень", Config.arrayListShadow);
            Config.arrayListScale = cslider(g, "Размер", 0.5, 2.0, Config.arrayListScale, false);
            Config.arrayListAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.arrayListAlpha, false);
            Config.arrayListX = (int) cslider(g, "X", 0, 400, Config.arrayListX, true);
            Config.arrayListY = (int) cslider(g, "Y", 0, 250, Config.arrayListY, true);
            cy += 2;
            resetRow(g, "array");
        }

        Config.armorHud = module(g, "armor", "Armor", "", Config.armorHud, 6);
        if (open.contains("armor")) {
            Config.armorHudShowTool = cbool(g, "Инструмент", Config.armorHudShowTool);
            Config.armorHudShowDurability = cbool(g, "Прочность", Config.armorHudShowDurability);
            Config.armorHudScale = cslider(g, "Размер", 0.5, 2.0, Config.armorHudScale, false);
            Config.armorHudAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.armorHudAlpha, false);
            Config.armorHudX = (int) cslider(g, "X", 0, 400, Config.armorHudX, true);
            Config.armorHudY = (int) cslider(g, "Y", 0, 400, Config.armorHudY, true);
            cy += 2;
            resetRow(g, "armor");
        }

        Config.potionHud = module(g, "potion", "Potion HUD", "", Config.potionHud, 9);
        if (open.contains("potion")) {
            Config.potionIcons = cbool(g, "Иконки", Config.potionIcons);
            Config.potionBars = cbool(g, "Полоска времени", Config.potionBars);
            Config.potionLevel = cbool(g, "Уровень (II, III)", Config.potionLevel);
            Config.potionColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.potionColor);
            Config.potionScale = cslider(g, "Размер", 0.5, 2.0, Config.potionScale, false);
            Config.potionAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.potionAlpha, false);
            Config.potionX = (int) cslider(g, "X", 0, 400, Config.potionX, true);
            Config.potionY = (int) cslider(g, "Y", 0, 250, Config.potionY, true);
            resetRow(g, "potion");
        }

        Config.keystrokes = module(g, "keys", "Keystrokes", "", Config.keystrokes, 7);
        if (open.contains("keys")) {
            Config.keystrokesShowMouse = cbool(g, "ЛКМ/ПКМ", Config.keystrokesShowMouse);
            Config.keystrokesShowSpace = cbool(g, "Space", Config.keystrokesShowSpace);
            Config.keystrokesScale = cslider(g, "Размер", 0.5, 2.0, Config.keystrokesScale, false);
            Config.keystrokesAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.keystrokesAlpha, false);
            Config.keystrokesColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.keystrokesColor);
            Config.keystrokesX = (int) cslider(g, "X", 0, 400, Config.keystrokesX, true);
            Config.keystrokesY = (int) cslider(g, "Y", 0, 400, Config.keystrokesY, true);
            cy += 2;
            resetRow(g, "keys");
        }

        // 4. Client (+48: строка Friends и кнопка под ней)
        int hClient = 34 + mh("autoswap", Config.swapPairs.size() + 1) + mh("itembind", Config.binds.size() + 1) + 48;
        panel(g, "Client", x0 + step * 3, y0, colW, hClient);

        Config.autoswap = module(g, "autoswap", "AutoSwap", ItemFeatures.swapKeyName(), Config.autoswap, Config.swapPairs.size() + 1);
        if (open.contains("autoswap")) {
            SwapPair toRebind = null, toRemove = null;
            for (SwapPair sp : Config.swapPairs) {
                int r = listRow(g, sp.key, sp.a + " <-> " + sp.b);
                if (r == 1) toRebind = sp; else if (r == 2) toRemove = sp;
            }
            hint2(g, "Наведи на предмет", "и жми [" + ItemFeatures.swapKeyName() + "]");
            if (toRemove != null) { Config.swapPairs.remove(toRemove); Config.save(); }
            else if (toRebind != null) {
                final SwapPair sp = toRebind;
                client.execute(() -> client.setScreen(new BindScreen(this, "AutoSwap: " + sp.a + " <-> " + sp.b, k -> ItemFeatures.setKey(sp, k))));
            }
        }

        Config.itembind = module(g, "itembind", "ItemBind", ItemFeatures.bindKeyName(), Config.itembind, Config.binds.size() + 1);
        if (open.contains("itembind")) {
            BoundItem toRebind = null, toRemove = null;
            for (BoundItem b : Config.binds) {
                int r = listRow(g, b.key, b.name);
                if (r == 1) toRebind = b; else if (r == 2) toRemove = b;
            }
            hint2(g, "Наведи на предмет", "и жми [" + ItemFeatures.bindKeyName() + "]");
            if (toRemove != null) { Config.binds.remove(toRemove); Config.save(); }
            else if (toRebind != null) {
                final BoundItem b = toRebind;
                client.execute(() -> client.setScreen(new BindScreen(this, "ItemBind: " + b.name, k -> ItemFeatures.setKey(b, k))));
            }
        }

        module(g, "friends", "Friends", "", true, 0);
        screenButton(g, "-> Открыть Friends", () -> new FriendsScreen(this));

        // ========== ВТОРОЙ РЯД ==========
        int maxH1 = Math.max(Math.max(hUtil, hVis), Math.max(hHud, hClient));
        int y1 = y0 + maxH1 + 10;

        // 5. Time & Weather
        int hTw = 34 + mh("time", 3) + mh("weather", 2);
        panel(g, "Time & Weather", x0, y1, colW, hTw);
        Config.customTime = module(g, "time", "Время", "", Config.customTime, 2);
        if (open.contains("time")) {
            Config.timeValue = (int) cslider(g, "Время", 0, 24000, Config.timeValue, true);
            cy += 20;
            resetRow(g, "time");
        }
        Config.customWeather = module(g, "weather", "Погода", "", Config.customWeather, 1);
        if (open.contains("weather")) {
            Config.weatherType = ccycle(g, "Тип", new String[]{"Ясно", "Дождь", "Гроза"}, Config.weatherType);
            resetRow(g, "weather");
        }

        // 6. Extra Visuals 1
        int hEx1 = 34 + mh("hat", Config.hatCustom ? 6 : 4) + mh("pred", 0);
        panel(g, "Extra Visuals 1", x0 + step, y1, colW, hEx1);
        Config.chinaHat = module(g, "hat", "China Hat", "", Config.chinaHat, Config.hatCustom ? 6 : 4);
        if (open.contains("hat")) {
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
            resetRow(g, "hat");
        }
        Config.predictions = module(g, "pred", "Predictions", "", Config.predictions, 0);

        // 7. Extra Visuals 2
        int hEx2 = 34 + mh("physic", 2) + mh("shulker", 0) + mh("hl", 3 * Config.highlights.size() + 1);
        panel(g, "Extra Visuals 2", x0 + step * 2, y1, colW, hEx2);
        Config.itemPhysic = module(g, "physic", "Item Physic", "", Config.itemPhysic, 1);
        if (open.contains("physic")) { Config.physicLift = cslider(g, "Высота (см)", -15, 15, Config.physicLift, true); cy += 2;  resetRow(g, "physic");}
        Config.shulkerPreview = module(g, "shulker", "Shulker Prev", "", Config.shulkerPreview, 0);

        Config.highlight = module(g, "hl", "Item Highlight", ItemHighlight.keyName(), Config.highlight, 3 * Config.highlights.size() + 1);
        if (open.contains("hl")) {
            ItemHighlight.Entry toRemove = null;
            for (ItemHighlight.Entry e : Config.highlights) {
                if (nameRow(g, e.name) == 2) toRemove = e;
                e.hue = cgrad(g, e.hue, pos -> hsv(pos, 0.85, 1.0));
                e.alpha = cslider(g, "Прозрачность", 0.1, 1.0, e.alpha, false);
            }
            hint2(g, "Наведи на предмет", "и жми [" + ItemHighlight.keyName() + "]");
            if (toRemove != null) { Config.highlights.remove(toRemove); Config.save(); }
        }

        // 8. Misc (+24: кнопка под Macros)
        int hMisc = 34 + mh("macro", 0) + mh("stream", 0) + 24;
        panel(g, "Misc", x0 + step * 3, y1, colW, hMisc);
        module(g, "macro", "Macros", "", true, 0);
        screenButton(g, "-> Настроить макросы", () -> new MacrosScreen(this));
        Config.streamerMode = module(g, "stream", "StreamerMode", "", Config.streamerMode, 0);

        drawBottom(g);
    }

    // =====================================================================
    //  Нижние кнопки: закрыть + переключатель стиля меню
    // =====================================================================
    private boolean btn(DrawContext g, int x, int y, int w, int h, String label, String key) {
        boolean hov = hit(x, y, w, h);
        float p = anim("b:" + key, hov);
        shadow(g, x, y, w, h, 5);
        rr(g, x, y, w, h, 5, mix(Theme.PANEL_BG, withAlpha(Theme.ACCENT, 0xB0), p));
        fadeLine(g, x + 5, y, w - 10, Theme.ACCENT, 255);
        g.drawCenteredTextWithShadow(textRenderer, label, x + w / 2, y + (h - 8) / 2, ff(Theme.TEXT));
        return clicked && hov;
    }

    private void drawBottom(DrawContext g) {
        int bh = 20, gap = 8;
        int bw = Math.max(90, Math.min(150, (width - 24 - gap * 2) / 3));
        int total = bw * 3 + gap * 2;
        int bx = (width - total) / 2, by = height - bh - 10;

        if (btn(g, bx, by, bw, bh, "ArchitektVisuals | Закрыть", "close")) close();
        if (btn(g, bx + bw + gap, by, bw, bh, "Стиль: " + (Config.menuStyle == 1 ? "Sidebar" : "Classic"), "style")) {
            Config.menuStyle = Config.menuStyle == 1 ? 0 : 1;
            Config.save();
        }
        if (btn(g, bx + (bw + gap) * 2, by, bw, bh,
                "Тема: " + Theme.NAMES[Math.max(0, Math.min(Config.menuTheme, Theme.NAMES.length - 1))], "theme")) {
            Config.menuTheme = (Config.menuTheme + 1) % Theme.NAMES.length;
            Theme.THEME_ID = Config.menuTheme;
            Theme.apply();
            Config.save();
        }
    }

    // =====================================================================
    //  SIDEBAR-стиль: слева категории, в центре модули, справа настройки
    // =====================================================================
    private static int sbCat = 0;
    private static String sbSel = "";

    private static final String[] SB_CATS = {"Visuals", "Interface", "Utilities", "Client", "World"};
    private static final String[][] SB_MODS = {
            {"fb", "cross", "trails", "zones", "tesp", "sky", "fog", "part", "dmg", "thud", "hv", "ci", "hat", "pred", "physic", "shulker", "hl"},
            {"hud", "array", "armor", "potion", "keys"},
            {"sprint", "zoom", "respawn", "freelook", "stream"},
            {"autoswap", "itembind", "friends", "macro"},
            {"time", "weather"}
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
            case "hat" -> "China Hat";
            case "pred" -> "Predictions";
            case "physic" -> "Item Physic";
            case "shulker" -> "Shulker Prev";
            case "hl" -> "Item Highlight";
            case "hud" -> "Info HUD";
            case "array" -> "ArrayList";
            case "armor" -> "Armor";
            case "potion" -> "Potion HUD";
            case "keys" -> "Keystrokes";
            case "sprint" -> "AutoSprint";
            case "zoom" -> "Zoom";
            case "respawn" -> "AutoRespawn";
            case "freelook" -> "Freelook";
            case "stream" -> "StreamerMode";
            case "autoswap" -> "AutoSwap";
            case "itembind" -> "ItemBind";
            case "friends" -> "Friends";
            case "macro" -> "Macros";
            case "time" -> "Время";
            case "weather" -> "Погода";
            default -> id;
        };
    }

    private static boolean sbHasSwitch(String id) {
        return !id.equals("friends") && !id.equals("macro");
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
            case "hat" -> Config.chinaHat;
            case "pred" -> Config.predictions;
            case "physic" -> Config.itemPhysic;
            case "shulker" -> Config.shulkerPreview;
            case "hl" -> Config.highlight;
            case "hud" -> Config.hud;
            case "array" -> Config.arrayList;
            case "armor" -> Config.armorHud;
            case "potion" -> Config.potionHud;
            case "keys" -> Config.keystrokes;
            case "sprint" -> Config.sprint;
            case "zoom" -> Config.zoom;
            case "respawn" -> Config.autoRespawn;
            case "freelook" -> Config.thirdPerson;
            case "stream" -> Config.streamerMode;
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
            case "hat": Config.chinaHat = v; break;
            case "pred": Config.predictions = v; break;
            case "physic": Config.itemPhysic = v; break;
            case "shulker": Config.shulkerPreview = v; break;
            case "hl": Config.highlight = v; break;
            case "hud": Config.hud = v; break;
            case "array": Config.arrayList = v; break;
            case "armor": Config.armorHud = v; break;
            case "potion": Config.potionHud = v; break;
            case "keys": Config.keystrokes = v; break;
            case "sprint": Config.sprint = v; break;
            case "zoom": Config.zoom = v; break;
            case "respawn": Config.autoRespawn = v; break;
            case "freelook": Config.thirdPerson = v; break;
            case "stream": Config.streamerMode = v; break;
            case "autoswap": Config.autoswap = v; break;
            case "itembind": Config.itembind = v; break;
            case "time": Config.customTime = v; break;
            case "weather": Config.customWeather = v; break;
            default: break;
        }
    }

    // строка без переключателя (для Friends / Macros)
    private boolean plainRow(DrawContext g, String label) {
        boolean hov = hit(cx, cy, cw, 22);
        rbox(g, cx, cy, cw, 22, hov ? Theme.ROW_HOVER : Theme.ROW);
        text(g, label, cx + 8, cy + 7, Theme.TEXT);
        text(g, ">", cx + cw - 14, cy + 7, Theme.TEXT_DIM);
        cy += 24;
        return clicked && hov;
    }

    private void renderSidebar(DrawContext g) {
        int pw = Math.min(width - 16, 470);
        int ph = Math.min(height - 44, 300);
        int px = (width - pw) / 2;
        int py = Math.max(6, (height - 36 - ph) / 2);

        // подложка
        shadow(g, px, py, pw, ph, 8);
        rr(g, px, py, pw, ph, 8, Theme.PANEL_BG);
        g.fillGradient(px + 4, py + 1, px + pw - 4, py + 40,
                ff(withAlpha(Theme.ACCENT, 50)), ff(withAlpha(Theme.ACCENT, 0)));
        fadeLine(g, px + 8, py, pw - 16, Theme.ACCENT, 255);

        int sbW = 104;
        int midW = Math.max(120, (pw - sbW) * 2 / 5);
        int rightW = pw - sbW - midW;
        g.fill(px + sbW, py + 8, px + sbW + 1, py + ph - 8, ff(Theme.ACCENT_DIM));
        g.fill(px + sbW + midW, py + 8, px + sbW + midW + 1, py + ph - 8, ff(Theme.ACCENT_DIM));

        // ---------- левая колонка: название и категории ----------
        g.drawCenteredTextWithShadow(textRenderer, Text.literal("Architekt").formatted(Formatting.BOLD),
                px + sbW / 2, py + 14, ff(Theme.ACCENT));
        g.drawCenteredTextWithShadow(textRenderer, "Visuals", px + sbW / 2, py + 26, ff(Theme.TEXT));

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
        g.enableScissor(mx0 - 2, vTop, mx0 - 2 + midW - 12, vTop + vH);
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
        g.disableScissor();
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
        g.enableScissor(rx0 - 2, vTop, rx0 - 2 + rightW - 14, vTop + vH);
        sbSettings(g, sbSel);
        g.disableScissor();
        mx = smx; my = smy; clicked = scl;
        scrollbar(g, rx0 + rightW - 18, vTop, vH, cy - startR, 2);
    }

    // ---- прокрутка колонок Sidebar ----
    private static float sbScrollMid = 0, sbScrollRight = 0;
    private static int sbMaxMid = 0, sbMaxRight = 0, sbDrag = 0, sbSplitX = 0, sbLastCat = -1;
    private static String sbLastSel = "";

    private void scrollbar(DrawContext g, int x, int top, int h, int content, int which) {
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

    // колесо мыши (без @Override: сигнатура зависит от версии)
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (Config.menuStyle != 1) return false;
        if (mouseX < sbSplitX) sbScrollMid = Math.max(0, Math.min(sbMaxMid, sbScrollMid - (float) verticalAmount * 22f));
        else sbScrollRight = Math.max(0, Math.min(sbMaxRight, sbScrollRight - (float) verticalAmount * 22f));
        return true;
    }

    private void resetRow(DrawContext g, String id) {
        boolean hov = hit(cx, cy, cw, 18);
        rbox(g, cx, cy, cw, 18, mix(0x30000000, 0x50FF5555, anim("rs:" + id, hov)));
        String t = "Сбросить настройки";
        text(g, t, cx + (cw - textRenderer.getWidth(t)) / 2, cy + 5, hov ? 0xFFFF7777 : Theme.TEXT_DIM);
        if (clicked && hov) { ModuleDefaults.reset(id); Config.save(); }
        cy += 20;
    }

    private void noOptions(DrawContext g) {
        text(g, "Нет настроек", cx + 2, cy + 2, Theme.TEXT_DIM);
        cy += 20;
    }

    private void sbSettings(DrawContext g, String id) {
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
            case "ci" -> {
                Config.ciScale = cslider(g, "Размер", 0.2, 2.0, Config.ciScale, false);
                Config.ciX = cslider(g, "Смещение X (см)", -60, 60, Config.ciX, true);
                Config.ciY = cslider(g, "Смещение Y (см)", -60, 60, Config.ciY, true);
                Config.ciZ = cslider(g, "Смещение Z (см)", -60, 60, Config.ciZ, true);
                Config.ciRX = cslider(g, "Поворот X (°)", -180, 180, Config.ciRX, true);
                Config.ciRY = cslider(g, "Поворот Y (°)", -180, 180, Config.ciRY, true);
                Config.ciRZ = cslider(g, "Поворот Z (°)", -180, 180, Config.ciRZ, true);
            }
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
                Config.arrayListColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.arrayListColor);
                Config.arrayListShadow = cbool(g, "Тень", Config.arrayListShadow);
                Config.arrayListScale = cslider(g, "Размер", 0.5, 2.0, Config.arrayListScale, false);
                Config.arrayListAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.arrayListAlpha, false);
                Config.arrayListX = (int) cslider(g, "X", 0, 400, Config.arrayListX, true);
                Config.arrayListY = (int) cslider(g, "Y", 0, 250, Config.arrayListY, true);
            }
            case "potion" -> {
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
                Config.armorHudShowTool = cbool(g, "Инструмент", Config.armorHudShowTool);
                Config.armorHudShowDurability = cbool(g, "Прочность", Config.armorHudShowDurability);
                Config.armorHudScale = cslider(g, "Размер", 0.5, 2.0, Config.armorHudScale, false);
                Config.armorHudAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.armorHudAlpha, false);
                Config.armorHudX = (int) cslider(g, "X", 0, 400, Config.armorHudX, true);
                Config.armorHudY = (int) cslider(g, "Y", 0, 400, Config.armorHudY, true);
            }
            case "keys" -> {
                Config.keystrokesShowMouse = cbool(g, "ЛКМ/ПКМ", Config.keystrokesShowMouse);
                Config.keystrokesShowSpace = cbool(g, "Space", Config.keystrokesShowSpace);
                Config.keystrokesScale = cslider(g, "Размер", 0.5, 2.0, Config.keystrokesScale, false);
                Config.keystrokesAlpha = cslider(g, "Прозрачность", 0.1, 1.0, Config.keystrokesAlpha, false);
                Config.keystrokesColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.keystrokesColor);
                Config.keystrokesX = (int) cslider(g, "X", 0, 400, Config.keystrokesX, true);
                Config.keystrokesY = (int) cslider(g, "Y", 0, 400, Config.keystrokesY, true);
            }
            case "sprint" -> Config.sprintAllDirs = cbool(g, "Все стороны", Config.sprintAllDirs);
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
                    client.execute(() -> client.setScreen(new BindScreen(this, "AutoSwap: " + sp.a + " <-> " + sp.b, k -> ItemFeatures.setKey(sp, k))));
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
                    client.execute(() -> client.setScreen(new BindScreen(this, "ItemBind: " + b.name, k -> ItemFeatures.setKey(b, k))));
                }
            }
            case "friends" -> screenButton(g, "-> Открыть Friends", () -> new FriendsScreen(this));
            case "macro" -> screenButton(g, "-> Настроить макросы", () -> new MacrosScreen(this));
            case "time" -> Config.timeValue = (int) cslider(g, "Время", 0, 24000, Config.timeValue, true);
            case "weather" -> Config.weatherType = ccycle(g, "Тип", new String[]{"Ясно", "Дождь", "Гроза"}, Config.weatherType);
            default -> noOptions(g);
        }
        if (ModuleDefaults.has(id)) resetRow(g, id);
    }

    @Override
    public void removed() { Config.save(); }
}
