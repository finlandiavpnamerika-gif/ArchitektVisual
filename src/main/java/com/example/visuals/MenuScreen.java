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

        // --- Utilities ---
        int hUtil = 34 + mh("sprint", 1) + mh("zoom", 1);
        panel(g, "Utilities", x0, y0, colW, hUtil);
        Config.sprint = module(g, "sprint", "AutoSprint", "", Config.sprint, 1);
        if (open.contains("sprint")) { /* cbool */ cy += 20; }
        Config.zoom = module(g, "zoom", "Zoom", "C", Config.zoom, 1);
        if (open.contains("zoom")) { Config.zoomFov = cslider(g, "FOV", 10, 60, Config.zoomFov, true); }

        // --- Visuals (ДОБАВИЛИ TRAILS) ---
        int hVis = 34 + mh("fb", 1) + mh("hud", 9) + mh("arraylist", 5) + mh("trails", 2);
        panel(g, "Visuals", x0 + step, y0, colW, hVis);
        Config.fullbright = module(g, "fb", "Fullbright", "", Config.fullbright, 1);
        if (open.contains("fb")) { Config.gamma = cslider(g, "Яркость", 1, 15, Config.gamma, false); }
        
        Config.hud = module(g, "hud", "HUD", "", Config.hud, 9);
        if (open.contains("hud")) {
            Config.hudFps = cbool(g, "FPS", Config.hudFps); cy += 20; // Заглушка для cbool
            Config.hudCoords = cbool(g, "Координаты", Config.hudCoords); cy += 20;
        }
        
        Config.arrayList = module(g, "arraylist", "ArrayList", "", Config.arrayList, 5);
        if (open.contains("arraylist")) {
            Config.arrayListColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.arrayListColor);
            Config.arrayListX = (int) cslider(g, "X", 0, 400, Config.arrayListX, true);
        }

        Config.trails = module(g, "trails", "Trails", "", Config.trails, 2);
        if (open.contains("trails")) {
            Config.trailsColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.trailsColor);
            Config.trailsLength = cslider(g, "Длина", 0.5, 3.0, Config.trailsLength, false);
        }

        // --- Performance ---
        int hPerf = 34 + mh("dist", 1);
        panel(g, "Performance", x0 + step * 2, y0, colW, hPerf);
        Config.limitDist = module(g, "dist", "Лимит дальности", "", Config.limitDist, 1);
        if (open.contains("dist")) { Config.viewDist = cslider(g, "Чанки", 2, 32, Config.viewDist, true); }

        // --- Client (ДОБАВИЛИ FRIENDS и ALTS) ---
        int hItems = 34 + mh("autoswap", 1) + mh("itembind", 1) + mh("friends", 0) + mh("alts", 0);
        panel(g, "Client", x0 + step * 3, y0, colW, hItems);

        Config.autoswap = module(g, "autoswap", "AutoSwap", ItemFeatures.swapKeyName(), Config.autoswap, 1);
        Config.itembind = module(g, "itembind", "ItemBind", ItemFeatures.bindKeyName(), Config.itembind, 1);

        // Кнопки
        module(g, "friends", "Friends", "", true, 0);
        screenButton(g, "-> Открыть Friends", new FriendsScreen(this));

        module(g, "alts", "Alt Manager", "", true, 0);
        screenButton(g, "-> Открыть Alts", new AltManagerScreen(this));

        // --- Кнопка закрытия ---
        int bw = 140; int bh = 18;
        int bx = (width - bw) / 2; int by = height - bh - 8;
        boolean hov = hit(bx, by, bw, bh);
        box(g, bx, by, bw, bh, hov ? ACC : ACC_DIM, BG);
        g.drawCenteredTextWithShadow(textRenderer, "ArchitektVisuals | Закрыть", width / 2, by + 5, WHITE);
        if (clicked && hov) close();
    }

    // Заглушка для чекбокса
    private boolean cbool(DrawContext g, String label, boolean v) {
        boolean hov = hit(cx, cy, cw, 18);
        g.fill(cx, cy, cx + cw, cy + 18, ROW2);
        text(g, label, cx + 5, cy + 5, -2236963);
        sw(g, cx + cw - 28, cy + 4, v);
        if (clicked && hov) v = !v;
        cy += 20;
        return v;
    }

    @Override
    public void removed() { Config.save(); }
}
