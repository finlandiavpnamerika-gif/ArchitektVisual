package com.example.visuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class MenuScreen extends Screen {
    private static final int ACC = -4883201;
    private static final int ACC_DIM = -9814091;
    private static final int BG = -435155944;
    private static final int ROW = -15068122;
    private static final int ROW2 = -15593958;
    private static final int OFF = -11910056;
    private static final int WHITE = -1;

    private static final Set<String> open = new HashSet<>();
    private int mx, my;
    private boolean down, clicked, prevDown = true;
    private int active = -1;
    private int idc;
    private int cx, cy, cw;

    public MenuScreen() { super(Text.literal("ArchitektVisuals")); }

    private boolean hit(int x, int y, int w, int h) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    private void text(DrawContext g, String s, int x, int y, int color) { g.drawTextWithShadow(textRenderer, s, x, y, color); }
    private void box(DrawContext g, int x, int y, int w, int h, int outer, int inner) { g.fill(x, y, x + w, y + h, outer); g.fill(x + 1, y + 1, x + w - 1, y + h - 1, inner); }
    private void sw(DrawContext g, int x, int y, boolean on) {
        g.fill(x, y, x + 22, y + 10, on ? ACC : OFF);
        int k = on ? x + 12 : x + 2;
        g.fill(k, y + 2, k + 8, y + 8, WHITE);
    }

    private int mh(String id, int options) { return 24 + (open.contains(id) ? options * 20 + 2 : 0); }

    private void panel(DrawContext g, String title, int x, int y, int w, int h) {
        box(g, x, y, w, h, ACC_DIM, BG);
        g.drawCenteredTextWithShadow(textRenderer, Text.literal(title).formatted(Formatting.BOLD), x + w / 2, y + 8, WHITE);
        g.fill(x + 6, y + 22, x + w - 6, y + 24, ACC);
        cx = x + 6; cw = w - 12; cy = y + 30;
    }

    private boolean module(DrawContext g, String id, String label, String hint, boolean on, int options) {
        boolean hov = hit(cx, cy, cw, 22);
        g.fill(cx, cy, cx + cw, cy + 22, hov ? -14410952 : ROW);
        text(g, label + (hint.isEmpty() ? "" : " [" + hint + "]"), cx + 5, cy + 7, WHITE);
        if (options > 0) text(g, open.contains(id) ? "^" : "v", cx + cw - 38, cy + 7, -5592406);
        sw(g, cx + cw - 28, cy + 6, on);
        if (clicked && hov) {
            if (mx >= cx + cw - 32) on = !on;
            else if (options > 0) { if (!open.remove(id)) open.add(id); }
        }
        cy += 24;
        return on;
    }

    private boolean cbool(DrawContext g, String label, boolean v) {
        boolean hov = hit(cx, cy, cw, 18);
        g.fill(cx, cy, cx + cw, cy + 18, hov ? -15002584 : ROW2);
        text(g, label, cx + 5, cy + 5, -2236963);
        sw(g, cx + cw - 28, cy + 4, v);
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
        double t = (v - min) / (max - min);
        g.fill(cx, cy, cx + cw, cy + 18, ROW2);
        g.fill(cx, cy, cx + (int) (cw * t), cy + 18, ACC_DIM);
        String val = isInt ? String.valueOf((int) v) : String.valueOf(v);
        g.drawCenteredTextWithShadow(textRenderer, label + ": " + val, cx + cw / 2, cy + 5, WHITE);
        cy += 20;
        return v;
    }

    private int ccycle(DrawContext g, String label, String[] names, int idx) {
        boolean hov = hit(cx, cy, cw, 18);
        g.fill(cx, cy, cx + cw, cy + 18, hov ? -15002584 : ROW2);
        text(g, label + ": " + names[idx], cx + 5, cy + 5, -2236963);
        g.fill(cx + cw - 16, cy + 4, cx + cw - 6, cy + 14, Config.COLORS[Math.min(idx, Config.COLORS.length - 1)]);
        if (clicked && hov) idx = (idx + 1) % names.length;
        cy += 20;
        return idx;
    }

    private void hint2(DrawContext g, String l1, String l2) {
        int max = cw - 4;
        text(g, textRenderer.trimToWidth(l1, max), cx + 2, cy + 1, -5592406);
        text(g, textRenderer.trimToWidth(l2, max), cx + 2, cy + 10, -5592406);
        cy += 20;
    }

    private int listRow(DrawContext g, int key, String label) {
        boolean hov = hit(cx, cy, cw, 18);
        boolean overX = hit(cx + cw - 16, cy, 16, 18);
        g.fill(cx, cy, cx + cw, cy + 18, hov ? -15002584 : ROW2);
        String full = "[" + Keys.name(key) + "] " + label;
        text(g, textRenderer.trimToWidth(full, cw - 22), cx + 4, cy + 5, key == -1 ? -43691 : -2236963);
        text(g, "x", cx + cw - 11, cy + 5, overX ? -43691 : -5592406);
        int result = 0;
        if (clicked && hov) result = overX ? 2 : 1;
        cy += 20;
        return result;
    }

    private boolean screenButton(DrawContext g, String label, Screen screen) {
        boolean hov = hit(cx, cy, cw, 22);
        g.fill(cx, cy, cx + cw, cy + 22, hov ? -14410952 : ROW);
        text(g, label, cx + 5, cy + 7, WHITE);
        if (clicked && hov) client.setScreen(screen);
        cy += 24;
        return false;
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

        g.fill(0, 0, width, height, 0x90000000);

        int cols = 4;
        int colW = Math.max(90, Math.min(150, (width - 30) / cols));
        int gap = 10;
        int x0 = (width - (colW * cols + gap * (cols - 1))) / 2;
        int y0 = 14;
        int step = colW + gap;

        // 1. Utilities
        int hUtil = 34 + mh("sprint", 1) + mh("zoom", 1);
        panel(g, "Utilities", x0, y0, colW, hUtil);
        Config.sprint = module(g, "sprint", "AutoSprint", "", Config.sprint, 1);
        if (open.contains("sprint")) { Config.sprintAllDirs = cbool(g, "Все стороны", Config.sprintAllDirs); cy += 2; }
        Config.zoom = module(g, "zoom", "Zoom", "C", Config.zoom, 1);
        if (open.contains("zoom")) { Config.zoomFov = cslider(g, "FOV", 10, 60, Config.zoomFov, true); cy += 2; }

        // 2. Visuals
        int hVis = 34 + mh("fb", 1) + mh("hud", 9) + mh("arraylist", 5) + mh("trails", 2) + mh("sky", 2) + mh("time", 2) + mh("weather", 1) + mh("crosshair", 4);
        panel(g, "Visuals", x0 + step, y0, colW, hVis);
        
        Config.fullbright = module(g, "fb", "Fullbright", "", Config.fullbright, 1);
        if (open.contains("fb")) { Config.gamma = cslider(g, "Яркость", 1, 15, Config.gamma, false); cy += 2; }
        
        Config.hud = module(g, "hud", "HUD", "", Config.hud, 9);
        if (open.contains("hud")) {
            Config.hudFps = cbool(g, "FPS", Config.hudFps);
            Config.hudPing = cbool(g, "Ping", Config.hudPing);
            Config.hudCoords = cbool(g, "Координаты", Config.hudCoords);
            Config.hudFacing = cbool(g, "Направление", Config.hudFacing);
            Config.hudEffects = cbool(g, "Эффекты", Config.hudEffects);
            Config.hudScale = cslider(g, "Размер", 0.5, 3.0, Config.hudScale, false);
            Config.hudColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.hudColor);
            Config.hudX = (int) cslider(g, "X", 0, 400, Config.hudX, true);
            Config.hudY = (int) cslider(g, "Y", 0, 250, Config.hudY, true);
            cy += 2;
        }
        
        Config.arrayList = module(g, "arraylist", "ArrayList", "", Config.arrayList, 5);
        if (open.contains("arraylist")) {
            Config.arrayListColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.arrayListColor);
            Config.arrayListShadow = cbool(g, "Тень", Config.arrayListShadow);
            Config.arrayListX = (int) cslider(g, "X", 0, 400, Config.arrayListX, true);
            Config.arrayListY = (int) cslider(g, "Y", 0, 250, Config.arrayListY, true);
            cy += 2;
        }

        Config.trails = module(g, "trails", "Trails", "", Config.trails, 2);
        if (open.contains("trails")) {
            Config.trailsColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.trailsColor);
            Config.trailsLength = cslider(g, "Длина", 0.5, 3.0, Config.trailsLength, false);
            cy += 2;
        }

        Config.customSky = module(g, "sky", "Custom Sky", "", Config.customSky, 2);
        if (open.contains("sky")) {
            Config.skyLines = cbool(g, "Линии", Config.skyLines);
            Config.skySpeed = cslider(g, "Скорость", 0.5, 5.0, Config.skySpeed, false);
            cy += 2;
        }

        Config.customTime = module(g, "time", "Время", "", Config.customTime, 2);
        if (open.contains("time")) {
            Config.timeValue = (int) cslider(g, "Время (0-24000)", 0, 24000, Config.timeValue, true);
            cy += 2;
        }

        Config.customWeather = module(g, "weather", "Погода", "", Config.customWeather, 1);
        if (open.contains("weather")) {
            Config.weatherType = ccycle(g, "Тип", new String[]{"Ясно", "Дождь", "Гроза"}, Config.weatherType);
            cy += 2;
        }

        // Crosshair
        Config.customCrosshair = module(g, "crosshair", "Crosshair", "", Config.customCrosshair, 4);
        if (open.contains("crosshair")) {
            Config.crosshairColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.crosshairColor);
            Config.crosshairSize = (int) cslider(g, "Размер", 2, 20, Config.crosshairSize, true);
            Config.crosshairThickness = (int) cslider(g, "Толщина", 1, 5, Config.crosshairThickness, true);
            Config.crosshairDot = cbool(g, "Точка", Config.crosshairDot);
            cy += 2;
        }

        // 3. Performance
        int hPerf = 34 + mh("dist", 1);
        panel(g, "Performance", x0 + step * 2, y0, colW, hPerf);
        Config.limitDist = module(g, "dist", "Лимит дальности", "", Config.limitDist, 1);
        if (open.contains("dist")) { Config.viewDist = cslider(g, "Чанки", 2, 32, Config.viewDist, true); cy += 2; }

        // 4. Client
        int hItems = 34 + mh("autoswap", Config.swapPairs.size() + 1) + mh("itembind", Config.binds.size() + 1);
        panel(g, "Client", x0 + step * 3, y0, colW, hItems);

        Config.autoswap = module(g, "autoswap", "AutoSwap", ItemFeatures.swapKeyName(), Config.autoswap, Config.swapPairs.size() + 1);
        if (open.contains("autoswap")) {
            SwapPair toRebind = null, toRemove = null;
            for (SwapPair p : Config.swapPairs) {
                int r = listRow(g, p.key, p.a + " <-> " + p.b);
                if (r == 1) toRebind = p; else if (r == 2) toRemove = p;
            }
            hint2(g, "Наведи на предмет", "и жми [" + ItemFeatures.swapKeyName() + "]");
            cy += 2;
            if (toRemove != null) { Config.swapPairs.remove(toRemove); Config.save(); }
            else if (toRebind != null) {
                final SwapPair p = toRebind;
                client.send(() -> client.setScreen(new BindScreen(this, "AutoSwap: " + p.a + " <-> " + p.b, k -> ItemFeatures.setKey(p, k))));
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
            cy += 2;
            if (toRemove != null) { Config.binds.remove(toRemove); Config.save(); }
            else if (toRebind != null) {
                final BoundItem b = toRebind;
                client.send(() -> client.setScreen(new BindScreen(this, "ItemBind: " + b.name, k -> ItemFeatures.setKey(b, k))));
            }
        }

        // === ВТОРОЙ РЯД ===
        int maxH1 = Math.max(Math.max(hUtil, hVis), Math.max(hPerf, hItems));
        int y1 = y0 + maxH1 + 10;

        // 5. Extra HUD
        int hExtra = 34 + mh("keystrokes", 5) + mh("armor", 5);
        panel(g, "Extra HUD", x0, y1, colW, hExtra);

        Config.keystrokes = module(g, "keystrokes", "Keystrokes", "", Config.keystrokes, 5);
        if (open.contains("keystrokes")) {
            Config.keystrokesShowMouse = cbool(g, "ЛКМ/ПКМ", Config.keystrokesShowMouse);
            Config.
