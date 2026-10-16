package com.example.visuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

/** Клик-ГУИ в стиле референса: 5 отдельных панелей с иконками и галочками. */
public class MenuScreen extends Screen {
    private static final Set<String> open = new HashSet<>();

    private int mx, my;
    private boolean down, clicked, prevDown = true;
    private int active = -1;
    private int idc;
    private int cx, cy, cw;

    public MenuScreen() {
        super(Text.literal("ArchitektVisuals"));
    }

    private boolean hit(int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private int mh(String id, int options) {
        return Theme.TITLE_HEIGHT + 4 + (open.contains(id) ? options * Theme.ROW_HEIGHT + 2 : 0);
    }

    /** Панель с иконкой и заголовком. */
    private void panel(DrawContext g, String icon, String title, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, Theme.PANEL_BG);
        g.fill(x, y, x + 1, y + h, Theme.ACCENT);
        g.fill(x + w - 1, y, x + w, y + h, Theme.PANEL_BORDER);

        String head = (Config.showIcons ? icon + " " : "") + title;
        g.drawTextWithShadow(textRenderer, head, x + 6, y + 5, Theme.TEXT);
        g.fill(x + 4, y + Theme.TITLE_HEIGHT, x + w - 4, y + Theme.TITLE_HEIGHT + 1, Theme.ACCENT_DIM);

        cx = x + 4;
        cw = w - 8;
        cy = y + Theme.TITLE_HEIGHT + 4;
    }

    /** Модуль-переключатель: строка + галочка справа. */
    private boolean module(DrawContext g, String id, String label, boolean on, int options) {
        boolean hov = hit(cx, cy, cw, Theme.ROW_HEIGHT);
        HudUtils.rowBg(g, cx, cy, cw, Theme.ROW_HEIGHT, hov, on);
        g.drawTextWithShadow(textRenderer, label, cx + 4, cy + 4, on ? Theme.TEXT : Theme.TEXT_DIM);

        if (Config.showCheckmarks && on) {
            g.drawTextWithShadow(textRenderer, Theme.CHECK_MARK, cx + cw - 10, cy + 4, Theme.CHECK);
        } else if (options > 0) {
            String arrow = open.contains(id) ? "^" : "v";
            g.drawTextWithShadow(textRenderer, arrow, cx + cw - 10, cy + 4, Theme.TEXT_DIM);
        }

        if (clicked && hov) {
            if (options > 0 && mx < cx + cw - 14) {
                if (!open.remove(id)) open.add(id);
            } else {
                on = !on;
            }
        }
        cy += Theme.ROW_HEIGHT;
        return on;
    }

    /** Раскрывающаяся категория без тумблера (для Theme). */
    private boolean expander(DrawContext g, String id, String label) {
        boolean hov = hit(cx, cy, cw, Theme.ROW_HEIGHT);
        HudUtils.rowBg(g, cx, cy, cw, Theme.ROW_HEIGHT, hov, false);
        g.drawTextWithShadow(textRenderer, label, cx + 4, cy + 4, Theme.TEXT_DIM);
        g.drawTextWithShadow(textRenderer, open.contains(id) ? "^" : "v",
                cx + cw - 10, cy + 4, Theme.TEXT_DIM);
        if (clicked && hov) {
            if (!open.remove(id)) open.add(id);
        }
        cy += Theme.ROW_HEIGHT;
        return open.contains(id);
    }

    private boolean cbool(DrawContext g, String label, boolean v) {
        boolean hov = hit(cx, cy, cw, Theme.ROW_HEIGHT);
        HudUtils.rowBg(g, cx, cy, cw, Theme.ROW_HEIGHT, hov, false);
        g.drawTextWithShadow(textRenderer, label, cx + 4, cy + 4, Theme.TEXT_DIM);
        if (v) {
            g.drawTextWithShadow(textRenderer, Theme.CHECK_MARK, cx + cw - 10, cy + 4, Theme.CHECK);
        }
        if (clicked && hov) v = !v;
        cy += Theme.ROW_HEIGHT;
        return v;
    }

    private double cslider(DrawContext g, String label, double min, double max, double v, boolean isInt) {
        int id = idc++;
        boolean hov = hit(cx, cy, cw, Theme.ROW_HEIGHT);
        if (clicked && hov) active = id;
        if (active == id && down) {
            double t = Math.max(0.0, Math.min(1.0, (double) (mx - cx) / (double) cw));
            v = min + t * (max - min);
            v = isInt ? (double) Math.round(v) : Math.round(v * 10.0) / 10.0;
        }
        double t = (v - min) / (max - min);
        g.fill(cx, cy, cx + cw, cy + Theme.ROW_HEIGHT, Theme.SLIDER_BG);
        g.fill(cx, cy, cx + (int) (cw * t), cy + Theme.ROW_HEIGHT, Theme.SLIDER_FILL);
        String val = isInt ? String.valueOf((int) v) : String.valueOf(v);
        String txt = label + ": " + val;
        int tw = textRenderer.getWidth(txt);
        g.drawTextWithShadow(textRenderer, txt, cx + (cw - tw) / 2, cy + 4, Theme.TEXT);
        cy += Theme.ROW_HEIGHT;
        return v;
    }

    private int ccycle(DrawContext g, String label, String[] names, int idx) {
        boolean hov = hit(cx, cy, cw, Theme.ROW_HEIGHT);
        HudUtils.rowBg(g, cx, cy, cw, Theme.ROW_HEIGHT, hov, false);
        int safe = Math.max(0, Math.min(names.length - 1, idx));
        g.drawTextWithShadow(textRenderer, label + ": " + names[safe], cx + 4, cy + 4, Theme.TEXT_DIM);
        g.fill(cx + cw - 12, cy + 4, cx + cw - 4, cy + 12,
                Config.COLORS[Math.min(safe, Config.COLORS.length - 1)]);
        if (clicked && hov) idx = (safe + 1) % names.length;
        cy += Theme.ROW_HEIGHT;
        return idx;
    }

    private void hint2(DrawContext g, String l1, String l2) {
        int max = cw - 4;
        g.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(l1, max), cx + 2, cy + 1, Theme.TEXT_DIM);
        g.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(l2, max), cx + 2, cy + 9, Theme.TEXT_DIM);
        cy += Theme.ROW_HEIGHT + 4;
    }

    private int listRow(DrawContext g, int key, String label) {
        boolean hov = hit(cx, cy, cw, Theme.ROW_HEIGHT);
        boolean overX = hit(cx + cw - 14, cy, 14, Theme.ROW_HEIGHT);
        HudUtils.rowBg(g, cx, cy, cw, Theme.ROW_HEIGHT, hov, false);
        String full = "[" + Keys.name(key) + "] " + label;
        g.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(full, cw - 18),
                cx + 4, cy + 4, key == -1 ? 0xFFFF5555 : Theme.TEXT_DIM);
        g.drawTextWithShadow(textRenderer, "x", cx + cw - 10, cy + 4, overX ? 0xFFFF5555 : Theme.TEXT_DIM);
        int result = 0;
        if (clicked && hov) result = overX ? 2 : 1;
        cy += Theme.ROW_HEIGHT;
        return result;
    }

    @Override
    public void render(DrawContext g, int mouseX, int mouseY, float delta) {
        mx = mouseX;
        my = mouseY;
        long window = client.getWindow().getHandle();
        down = GLFW.glfwGetMouseButton(window, 0) == GLFW.GLFW_PRESS;
        clicked = down && !prevDown;
        prevDown = down;
        if (!down) active = -1;
        idc = 0;

        g.fill(0, 0, width, height, 0x80000000);

        int pw = Theme.PANEL_WIDTH;
        int gap = Theme.PANEL_GAP;
        int total = pw * 5 + gap * 4;
        int x0 = (width - total) / 2;
        int y0 = Theme.TOP_MARGIN;

        // ========== VISUAL ==========
        int hVis = mh("fb", 1) + mh("hud", 9) + mh("arraylist", 5);
        panel(g, Theme.ICON_VISUAL, "Visual", x0, y0, pw, hVis);
        Config.fullbright = module(g, "fb", "Fullbright", Config.fullbright, 1);
        if (open.contains("fb")) {
            Config.gamma = cslider(g, "Яркость", 1, 15, Config.gamma, false);
        }
        Config.hud = module(g, "hud", "HUD", Config.hud, 9);
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
        }
        Config.arrayList = module(g, "arraylist", "ArrayList", Config.arrayList, 5);
        if (open.contains("arraylist")) {
            Config.arrayListColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.arrayListColor);
            Config.arrayListShadow = cbool(g, "Тень", Config.arrayListShadow);
            Config.arrayListX = (int) cslider(g, "X", 0, 400, Config.arrayListX, true);
            Config.arrayListY = (int) cslider(g, "Y", 0, 250, Config.arrayListY, true);
        }

        // ========== WORLD ==========
        int hWorld = mh("dist", 1);
        panel(g, Theme.ICON_WORLD, "World", x0 + (pw + gap), y0, pw, hWorld);
        Config.limitDist = module(g, "dist", "Limit Distance", Config.limitDist, 1);
        if (open.contains("dist")) {
            Config.viewDist = cslider(g, "Чанки", 2, 32, Config.viewDist, true);
        }

        // ========== PARTICLES (заготовка) ==========
        int hPart = mh("particles", 0);
        panel(g, Theme.ICON_PARTICLES, "Particles", x0 + (pw + gap) * 2, y0, pw, hPart);
        cy += 2;
        g.drawTextWithShadow(textRenderer, "Скоро...", cx + 4, cy, Theme.TEXT_DIM);

        // ========== UTILS ==========
        int hUtil = mh("sprint", 1) + mh("zoom", 1) + mh("reconnect", 1);
        panel(g, Theme.ICON_UTILS, "Utils", x0 + (pw + gap) * 3, y0, pw, hUtil);
        Config.sprint = module(g, "sprint", "AutoSprint", Config.sprint, 1);
        if (open.contains("sprint")) {
            Config.sprintAllDirs = cbool(g, "Все стороны", Config.sprintAllDirs);
        }
        Config.zoom = module(g, "zoom", "Zoom", Config.zoom, 1);
        if (open.contains("zoom")) {
            Config.zoomFov = cslider(g, "FOV", 10, 60, Config.zoomFov, true);
        }
        Config.autoReconnect = module(g, "reconnect", "AutoReconnect", Config.autoReconnect, 1);
        if (open.contains("reconnect")) {
            Config.autoReconnectDelay = cslider(g, "Секунд", 1, 60, Config.autoReconnectDelay, true);
        }

        // ========== CLIENT ==========
        int hClient = mh("autoswap", Config.swapPairs.size() + 1)
                + mh("itembind", Config.binds.size() + 1)
                + mh("keystrokes", 5)
                + mh("armor", 5)
                + mh("theme", 3);
        panel(g, Theme.ICON_CLIENT, "Client", x0 + (pw + gap) * 4, y0, pw, hClient);

        Config.autoswap = module(g, "autoswap", "AutoSwap", Config.autoswap, Config.swapPairs.size() + 1);
        if (open.contains("autoswap")) {
            SwapPair toRebind = null, toRemove = null;
            for (SwapPair p : Config.swapPairs) {
                int r = listRow(g, p.key, p.a + "<->" + p.b);
                if (r == 1) toRebind = p; else if (r == 2) toRemove = p;
            }
            hint2(g, "Наведи на предмет", "и жми [" + ItemFeatures.swapKeyName() + "]");
            if (toRemove != null) {
                Config.swapPairs.remove(toRemove);
                Config.save();
            } else if (toRebind != null) {
                final SwapPair p = toRebind;
                client.send(() -> client.setScreen(new BindScreen(this,
                        "AutoSwap: " + p.a + " <-> " + p.b, k -> ItemFeatures.setKey(p, k))));
            }
        }

        Config.itembind = module(g, "itembind", "ItemBind", Config.itembind, Config.binds.size() + 1);
        if (open.contains("itembind")) {
            BoundItem toRebind = null, toRemove = null;
            for (BoundItem b : Config.binds) {
                int r = listRow(g, b.key, b.name);
                if (r == 1) toRebind = b; else if (r == 2) toRemove = b;
            }
            hint2(g, "Наведи на предмет", "и жми [" + ItemFeatures.bindKeyName() + "]");
            if (toRemove != null) {
                Config.binds.remove(toRemove);
                Config.save();
            } else if (toRebind != null) {
                final BoundItem b = toRebind;
                client.send(() -> client.setScreen(new BindScreen(this,
                        "ItemBind: " + b.name, k -> ItemFeatures.setKey(b, k))));
            }
        }

        Config.keystrokes = module(g, "keystrokes", "Keystrokes", Config.keystrokes, 5);
        if (open.contains("keystrokes")) {
            Config.keystrokesShowMouse = cbool(g, "ЛКМ/ПКМ", Config.keystrokesShowMouse);
            Config.keystrokesShowSpace = cbool(g, "Space", Config.keystrokesShowSpace);
            Config.keystrokesScale = cslider(g, "Размер", 0.5, 2.0, Config.keystrokesScale, false);
            Config.keystrokesColor = ccycle(g, "Цвет", Config.COLOR_NAMES, Config.keystrokesColor);
            Config.keystrokesX = (int) cslider(g, "X", 0, 400, Config.keystrokesX, true);
        }

        Config.armorHud = module(g, "armor", "Armor + Tool", Config.armorHud, 5);
        if (open.contains("armor")) {
            Config.armorHudShowTool = cbool(g, "Инструмент", Config.armorHudShowTool);
            Config.armorHudShowDurability = cbool(g, "Прочность", Config.armorHudShowDurability);
            Config.armorHudScale = cslider(g, "Размер", 0.5, 2.0, Config.armorHudScale, false);
            Config.armorHudX = (int) cslider(g, "X", 0, 400, Config.armorHudX, true);
            Config.armorHudY = (int) cslider(g, "Y", 0, 400, Config.armorHudY, true);
        }

        // ===== Theme — раскрывающаяся категория без тумблера =====
        boolean themeOpen = expander(g, "theme", "Theme");
        if (themeOpen) {
            Config.themeColor = ccycle(g, "Акцент", Config.COLOR_NAMES, Config.themeColor);
            Config.showIcons = cbool(g, "Иконки", Config.showIcons);
            Config.showCheckmarks = cbool(g, "Галочки", Config.showCheckmarks);
        }

        // ========== Кнопка закрытия ==========
        int bw = 120;
        int bh = 16;
        int bx = (width - bw) / 2;
        int by = height - bh - 6;
        boolean hov = hit(bx, by, bw, bh);
        g.fill(bx, by, bx + bw, by + bh, hov ? Theme.ACCENT : Theme.PANEL_BG);
        g.drawCenteredTextWithShadow(textRenderer, "Закрыть", width / 2, by + 4, Theme.TEXT);
        if (clicked && hov) close();
    }

    @Override
    public void removed() {
        Config.save();
    }
}
