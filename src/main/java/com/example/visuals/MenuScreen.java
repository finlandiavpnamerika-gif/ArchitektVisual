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
        int c = ff(color);
        g.fill(x + 1, y, x + w - 1, y + h, c);
        g.fill(x, y + 1, x + 1, y + h - 1, c);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, c);
    }

    private void panel(DrawContext g, String title, int x, int y, int w, int h) {
        rbox(g, x + 2, y + 3, w, h, 0x45000000);
        rbox(g, x, y, w, h, Theme.PANEL_BG);
        g.fillGradient(x + 1, y + 1, x + w - 1, y + 22,
                ff(withAlpha(Theme.ACCENT, 48)), ff(withAlpha(Theme.ACCENT, 4)));
        g.fill(x + 1, y, x + w - 1, y + 1, ff(Theme.ACCENT));
        g.drawCenteredTextWithShadow(textRenderer, Text.literal(title).formatted(Formatting.BOLD),
                x + w / 2, y + 8, ff(Theme.TEXT));
        g.fill(x + 8, y + 22, x + w - 8, y + 23, ff(Theme.ACCENT_DIM));
        cx = x + 6; cw = w - 12; cy = y + 28;
    }

    // Тумблер с плавным движением ползунка
    private void sw(DrawContext g, int x, int y, String key, boolean on) {
        float p = anim(key, on);
        rbox(g, x, y, 24, 12, mix(Theme.TOGGLE_OFF, Theme.TOGGLE_ON, p));
        int kx = x + 2 + Math.round(p * 12);
        rbox(g, kx, y + 2, 8, 8, 0xFFFFFFFF);
    }

    private int mh(String id, int options) { return 24 + (open.contains(id) ? options * 20 + 4 : 0); }

    // ---------- элементы ----------
    private boolean module(DrawContext g, String id, String label, String hint, boolean on, int options) {
        curId = id;
        boolean hov = hit(cx, cy, cw, 22);
        rbox(g, cx, cy, cw, 22, hov ? Theme.ROW_HOVER : Theme.ROW);

        if (on) g.fill(cx, cy + 2, cx + 2, cy + 20, ff(Theme.ACCENT)); // Полоска слева, если включено

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
        rbox(g, cx, cy, cw, 18, hov ? Theme.ROW_HOVER : 0x20000000);
        text(g, label, cx + 8, cy + 5, Theme.TEXT_DIM);
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
        double t = (v - min) / (max - min);
        int fw = (int) (cw * t);

        rbox(g, cx, cy, cw, 18, hov ? 0x30000000 : 0x20000000);
        if (fw > 2) {
            rbox(g, cx, cy, fw, 18, Theme.SLIDER_FILL);
            g.fill(cx + fw - 2, cy + 2, cx + fw, cy + 16, ff(0xCCFFFFFF));
        }

        String val = isInt ? String.valueOf((int) v) : String.valueOf(v);
        g.drawCenteredTextWithShadow(textRenderer, label + ": " + val, cx + cw / 2, cy + 5, ff(Theme.TEXT));
        cy += 20;
        return v;
    }

    private int ccycle(DrawContext g, String label, String[] names, int idx) {
        boolean hov = hit(cx, cy, cw, 18);
        rbox(g, cx, cy, cw, 18, hov ? Theme.ROW_HOVER : 0x20000000);
        text(g, label + ": " + names[Math.max(0, Math.min(idx, names.length - 1))], cx + 8, cy + 5, Theme.TEXT_DIM);
        rbox(g, cx + cw - 16, cy + 4, 8, 10, Theme.ACCENT);
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

        for (int x = 0; x < cw; x += 2) {
            int x2 = Math.min(x + 2, cw);
            g.fill(cx + x, cy, cx + x2, cy + 16, ff(0xFF000000 | colorAt.applyAsInt((x + 1.0) / cw)));
        }
        int mxp = cx + (int) (cw * t);
        g.fill(mxp - 2, cy - 1, mxp + 1, cy + 17, ff(0xFF000000));   // обводка маркера
        g.fill(mxp - 1, cy, mxp, cy + 16, ff(0xFFFFFFFF));           // маркер
        cy += 20;
        return t;
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
        RainyScene.drawRain(g, width, height, 0.6f);

        int cols = 4;
        int colW = Math.max(110, Math.min(150, (width - 30) / cols));
        int gap = 10;
        int x0 = (width - (colW * cols + gap * (cols - 1))) / 2;
        int y0 = 14 + slide;
        int step = colW + gap;

        // ========== ПЕРВЫЙ РЯД ==========

        // 1. Utilities
        int hUtil = 34 + mh("sprint", 1) + mh("zoom", 1) + mh("respawn", 0) + mh("freelook", 0);
        panel(g, "Utilities", x0, y0, colW, hUtil);
        Config.sprint = module(g, "sprint", "AutoSprint", "", Config.sprint, 1);
        if (open.contains("sprint")) { Config.sprintAllDirs = cbool(g, "Все стороны", Config.sprintAllDirs); cy += 2; }
        Config.zoom = module(g, "zoom", "Zoom", "C", Config.zoom, 1);
        if (open.contains("zoom")) { Config.zoomFov = cslider(g, "FOV", 10, 60, Config.zoomFov, true); cy += 2; }
        Config.autoRespawn = module(g, "respawn", "AutoRespawn", "", Config.autoRespawn, 0);
        Config.thirdPerson = module(g, "freelook", "Freelook", "", Config.thirdPerson, 0);

        // 2. Visuals
        int hVis = 34 + mh("fb", 1) + mh("cross", 4) + mh("trails", Config.trailsCustom ? 6 : 4);
        panel(g, "Visuals", x0 + step, y0, colW, hVis);
        Config.fullbright = module(g, "fb", "Fullbright", "", Config.fullbright, 1);
        if (open.contains("fb")) { Config.gamma = cslider(g, "Яркость", 1, 15, Config.gamma, false); cy += 2; }

        Config.customCrosshair = module(g, "cross", "Crosshair", "", Config.customCrosshair, 4);
        if (open.contains("cross")) {
            Config.crosshairColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.crosshairColor);
            Config.crosshairSize = (int) cslider(g, "Размер", 2, 20, Config.crosshairSize, true);
            Config.crosshairThickness = (int) cslider(g, "Толщина", 1, 5, Config.crosshairThickness, true);
            Config.crosshairDot = cbool(g, "Точка", Config.crosshairDot);
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
        }

        // 3. HUD
        int hHud = 34 + mh("hud", 9) + mh("array", 4) + mh("armor", 5) + mh("keys", 5);
        panel(g, "HUD", x0 + step * 2, y0, colW, hHud);

        Config.hud = module(g, "hud", "Info HUD", "", Config.hud, 9);
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

        Config.arrayList = module(g, "array", "ArrayList", "", Config.arrayList, 4);
        if (open.contains("array")) {
            Config.arrayListColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.arrayListColor);
            Config.arrayListShadow = cbool(g, "Тень", Config.arrayListShadow);
            Config.arrayListX = (int) cslider(g, "X", 0, 400, Config.arrayListX, true);
            Config.arrayListY = (int) cslider(g, "Y", 0, 250, Config.arrayListY, true);
            cy += 2;
        }

        Config.armorHud = module(g, "armor", "Armor", "", Config.armorHud, 5);
        if (open.contains("armor")) {
            Config.armorHudShowTool = cbool(g, "Инструмент", Config.armorHudShowTool);
            Config.armorHudShowDurability = cbool(g, "Прочность", Config.armorHudShowDurability);
            Config.armorHudScale = cslider(g, "Размер", 0.5, 2.0, Config.armorHudScale, false);
            Config.armorHudX = (int) cslider(g, "X", 0, 400, Config.armorHudX, true);
            Config.armorHudY = (int) cslider(g, "Y", 0, 400, Config.armorHudY, true);
            cy += 2;
        }

        Config.keystrokes = module(g, "keys", "Keystrokes", "", Config.keystrokes, 5);
        if (open.contains("keys")) {
            Config.keystrokesShowMouse = cbool(g, "ЛКМ/ПКМ", Config.keystrokesShowMouse);
            Config.keystrokesShowSpace = cbool(g, "Space", Config.keystrokesShowSpace);
            Config.keystrokesScale = cslider(g, "Размер", 0.5, 2.0, Config.keystrokesScale, false);
            Config.keystrokesColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.keystrokesColor);
            Config.keystrokesX = (int) cslider(g, "X", 0, 400, Config.keystrokesX, true);
            Config.keystrokesY = (int) cslider(g, "Y", 0, 400, Config.keystrokesY, true);
            cy += 2;
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
        int hTw = 34 + mh("time", 2) + mh("weather", 1);
        panel(g, "Time & Weather", x0, y1, colW, hTw);
        Config.customTime = module(g, "time", "Время", "", Config.customTime, 2);
        if (open.contains("time")) {
            Config.timeValue = (int) cslider(g, "Время", 0, 24000, Config.timeValue, true);
            cy += 20;
        }
        Config.customWeather = module(g, "weather", "Погода", "", Config.customWeather, 1);
        if (open.contains("weather")) {
            Config.weatherType = ccycle(g, "Тип", new String[]{"Ясно", "Дождь", "Гроза"}, Config.weatherType);
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
        }
        Config.predictions = module(g, "pred", "Predictions", "", Config.predictions, 0);

        // 7. Extra Visuals 2
        int hEx2 = 34 + mh("physic", 0) + mh("shulker", 0);
        panel(g, "Extra Visuals 2", x0 + step * 2, y1, colW, hEx2);
        Config.itemPhysic = module(g, "physic", "Item Physic", "", Config.itemPhysic, 0);
        Config.shulkerPreview = module(g, "shulker", "Shulker Prev", "", Config.shulkerPreview, 0);

        // 8. Misc (+24: кнопка под Macros)
        int hMisc = 34 + mh("macro", 0) + mh("stream", 0) + 24;
        panel(g, "Misc", x0 + step * 3, y1, colW, hMisc);
        module(g, "macro", "Macros", "", true, 0);
        screenButton(g, "-> Настроить макросы", () -> new MacrosScreen(this));
        Config.streamerMode = module(g, "stream", "StreamerMode", "", Config.streamerMode, 0);

        // Кнопка закрытия
        int bw = 160; int bh = 20;
        int bx = (width - bw) / 2; int by = height - bh - 10;
        boolean hov = hit(bx, by, bw, bh);
        rbox(g, bx + 1, by + 2, bw, bh, 0x40000000);
        rbox(g, bx, by, bw, bh, hov ? Theme.ACCENT : Theme.PANEL_BG);
        g.fill(bx + 1, by, bx + bw - 1, by + 1, ff(Theme.ACCENT));
        g.drawCenteredTextWithShadow(textRenderer, "ArchitektVisuals | Закрыть", width / 2, by + 6, ff(Theme.TEXT));
        if (clicked && hov) close();
    }

    @Override
    public void removed() { Config.save(); }
}
