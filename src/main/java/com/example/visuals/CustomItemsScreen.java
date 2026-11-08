package com.example.visuals;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

/**
 * Редактор CustomItems: слева список правил "предмет -> модель", справа настройки выбранного правила.
 * Предмет выбирается через инвентарь (ItemPickScreen).
 */
public class CustomItemsScreen extends Screen {
    private final Screen parent;
    private final Map<String, Float> anims = new HashMap<>();
    private int mx, my;
    private boolean down, clicked, prevDown = true;
    private int active = -1, idc;
    private int sel = 0;
    private float scrollL = 0, scrollR = 0, maxL = 0, maxR = 0;
    private int px, py, pw, ph, divX;
    private int cx, cy, cw;                 // курсор правой колонки
    private int clipY0 = 0, clipY1 = 100000;
    private final long openedAt = System.currentTimeMillis();

    public CustomItemsScreen(Screen parent) {
        super(Text.literal("CustomItems"));
        this.parent = parent;
    }

    // ---------- примитивы ----------
    private boolean hit(int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h && my >= clipY0 && my < clipY1;
    }

    private float anim(String k, boolean target) {
        float goal = target ? 1f : 0f, cur = anims.getOrDefault(k, goal);
        cur += (goal - cur) * 0.3f;
        if (Math.abs(goal - cur) < 0.01f) cur = goal;
        anims.put(k, cur);
        return cur;
    }

    private static int mix(int a, int b, float t) { return HudStyle.mix(a, b, t); }
    private static int al(int c, int a) { return (a << 24) | (c & 0xFFFFFF); }

    private void box(DrawContext g, int x, int y, int w, int h, int color) { HudStyle.roundFill(g, x, y, w, h, 3, color); }

    private void text(DrawContext g, String s, int x, int y, int color) { g.drawTextWithShadow(textRenderer, s, x, y, color); }

    private void textC(DrawContext g, String s, int x, int y, int w, int color) {
        text(g, s, x + (w - textRenderer.getWidth(s)) / 2, y, color);
    }

    private boolean button(DrawContext g, int x, int y, int w, int h, String label, int accent, String key) {
        boolean hov = hit(x, y, w, h);
        float a = anim("b:" + key, hov);
        box(g, x, y, w, h, mix(al(accent, 0x30), al(accent, 0x70), a));
        HudStyle.roundRing(g, x, y, w, h, 3, al(accent, 0x60 + (int) (0x50 * a)));
        textC(g, label, x, y + (h - 8) / 2, w, Theme.TEXT);
        return clicked && hov;
    }

    private ItemStack stackOf(String id) {
        Identifier ident = Identifier.tryParse(id);
        Item it = ident == null ? null : Registries.ITEM.get(ident);
        return it == null ? ItemStack.EMPTY : it.getDefaultStack();
    }

    private String nameOf(String id) {
        ItemStack s = stackOf(id);
        return s.isEmpty() ? id : s.getName().getString();
    }

    // ---------- элементы настроек ----------
    private boolean toggleRow(DrawContext g, String label, boolean v) {
        boolean hov = hit(cx, cy, cw, 18);
        box(g, cx, cy, cw, 18, mix(0x28000000, Theme.ROW_HOVER, anim("t:" + label, hov) * 0.5f));
        text(g, label, cx + 8, cy + 5, v ? Theme.TEXT : Theme.TEXT_DIM);
        float p = anim("tg:" + label + sel, v);
        int x = cx + cw - 30, y = cy + 3;
        HudStyle.roundFill(g, x, y, 24, 12, 6, mix(Theme.TOGGLE_OFF, Theme.TOGGLE_ON, p));
        HudStyle.roundFill(g, x + 2 + Math.round(p * 12), y + 2, 8, 8, 4, 0xFFFFFFFF);
        if (clicked && hov) v = !v;
        cy += 20;
        return v;
    }

    private int cycle(DrawContext g, String label, String[] names, int idx) {
        boolean hov = hit(cx, cy, cw, 18);
        box(g, cx, cy, cw, 18, mix(0x28000000, Theme.ROW_HOVER, anim("c:" + label, hov) * 0.6f));
        idx = Math.max(0, Math.min(idx, names.length - 1));
        text(g, label + ": " + names[idx], cx + 8, cy + 5, Theme.TEXT);
        text(g, "›", cx + cw - 12, cy + 5, Theme.ACCENT);
        if (clicked && hov) idx = (idx + 1) % names.length;
        cy += 20;
        return idx;
    }

    private double slider(DrawContext g, String label, double min, double max, double v, double step, int dec) {
        int id = idc++;
        boolean hov = hit(cx, cy, cw, 22);
        if (clicked && hov) active = id;
        if (active == id && down) {
            double t = Math.max(0, Math.min(1, (double) (mx - cx - 6) / (cw - 12)));
            v = Math.round((min + t * (max - min)) / step) * step;
        }
        v = Math.max(min, Math.min(max, v));
        box(g, cx, cy, cw, 22, hov || active == id ? 0x38000000 : 0x28000000);
        text(g, label, cx + 6, cy + 3, Theme.TEXT_DIM);
        String val = dec == 0 ? String.valueOf((int) Math.round(v)) : String.format(java.util.Locale.ROOT, "%." + dec + "f", v);
        text(g, val, cx + cw - 6 - textRenderer.getWidth(val), cy + 3, Theme.TEXT);
        int tw = cw - 12, fw = (int) (tw * (v - min) / (max - min));
        HudStyle.roundFill(g, cx + 6, cy + 15, tw, 3, 1, 0x45FFFFFF);
        if (fw > 0) HudStyle.roundFill(g, cx + 6, cy + 15, fw, 3, 1, Theme.ACCENT);
        HudStyle.roundFill(g, cx + 6 + fw - 3, cy + 13, 7, 7, 3, active == id ? 0xFFFFFFFF : 0xFFE8E8E8);
        cy += 24;
        return v;
    }

    private double hueBar(DrawContext g, String label, double t) {
        int id = idc++;
        boolean hov = hit(cx, cy, cw, 18);
        if (clicked && hov) active = id;
        if (active == id && down) t = Math.max(0, Math.min(1, (double) (mx - cx) / cw));
        for (int x = 0; x < cw; x += 3)
            g.fill(cx + x, cy, cx + Math.min(cw, x + 3), cy + 18, 0xFF000000 | (MathHelper.hsvToRgb((x + 1.5f) / cw, 0.85f, 1f) & 0xFFFFFF));
        int m = cx + (int) (cw * t);
        g.fill(m - 2, cy - 1, m + 1, cy + 19, 0xFF000000);
        g.fill(m - 1, cy, m, cy + 18, 0xFFFFFFFF);
        textC(g, label, cx, cy + 5, cw, 0xFFFFFFFF);
        cy += 22;
        return t;
    }

    // ---------- экран ----------
    @Override
    public void render(DrawContext g, int mouseX, int mouseY, float delta) {
        mx = mouseX; my = mouseY;
        long window = client.getWindow().getHandle();
        down = GLFW.glfwGetMouseButton(window, 0) == GLFW.GLFW_PRESS;
        clicked = down && !prevDown;
        prevDown = down;
        if (!down) active = -1;
        idc = 0;
        clipY0 = 0; clipY1 = 100000;

        g.fillGradient(0, 0, width, height, 0xA0080810, 0xD0000000);
        g.fillGradient(0, height / 2, width, height, 0x00000000, al(Theme.ACCENT, 0x30));

        pw = Math.min(width - 16, 460);
        ph = Math.min(height - 16, 280);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        int slide = Math.round((1f - Math.min(1f, (System.currentTimeMillis() - openedAt) / 180f)) * 8f);
        py += slide;

        HudStyle.roundFill(g, px + 1, py + 2, pw, ph, 6, 0x40000000);
        HudStyle.roundFill(g, px, py, pw, ph, 6, Theme.PANEL_BG);
        g.fillGradient(px + 3, py + 1, px + pw - 3, py + 30, al(Theme.ACCENT, 80), al(Theme.ACCENT, 0));
        HudStyle.roundRing(g, px, py, pw, ph, 6, al(Theme.ACCENT, 0x70));

        // шапка
        text(g, "CustomItems", px + 10, py + 9, Theme.ACCENT);
        Config.customItems = headerToggle(g, px + 90, py + 6, Config.customItems);
        if (button(g, px + pw - 62, py + 5, 54, 18, "← Назад", Theme.ACCENT, "back")) close();

        int top = py + 30, bottom = py + ph - 8;
        divX = px + 150;
        g.fill(divX, top, divX + 1, bottom, al(Theme.ACCENT, 0x50));

        leftColumn(g, px + 8, top, 134, bottom);
        rightColumn(g, divX + 8, top, px + pw - 14 - (divX + 8), bottom);
    }

    private boolean headerToggle(DrawContext g, int x, int y, boolean v) {
        boolean hov = hit(x, y, 28, 16);
        float p = anim("hdr", v);
        HudStyle.roundFill(g, x, y + 2, 24, 12, 6, mix(Theme.TOGGLE_OFF, Theme.TOGGLE_ON, p));
        HudStyle.roundFill(g, x + 2 + Math.round(p * 12), y + 4, 8, 8, 4, 0xFFFFFFFF);
        if (clicked && hov) v = !v;
        text(g, v ? "вкл" : "выкл", x + 30, y + 4, v ? Theme.TEXT : Theme.TEXT_DIM);
        return v;
    }

    private void leftColumn(DrawContext g, int x, int top, int w, int bottom) {
        int listBottom = bottom - 24;
        maxL = Math.max(0, Config.ciRules.size() * 24 - (listBottom - top));
        scrollL = Math.max(0, Math.min(scrollL, maxL));
        int y = top - Math.round(scrollL);
        g.enableScissor(x - 2, top, x + w + 2, listBottom);
        clipY0 = top; clipY1 = listBottom;
        int removeIdx = -1;
        if (sel >= Config.ciRules.size()) sel = Config.ciRules.size() - 1;
        for (int i = 0; i < Config.ciRules.size(); i++) {
            CiRule r = Config.ciRules.get(i);
            boolean hov = hit(x, y, w, 22);
            boolean overX = hit(x + w - 14, y, 14, 22);
            boolean on = i == sel;
            float a = anim("l:" + i, hov);
            box(g, x, y, w, 22, on ? al(Theme.ACCENT, 0x50) : mix(0x28000000, Theme.ROW_HOVER, a * 0.6f));
            if (on) HudStyle.roundFill(g, x, y + 4, 2, 14, 1, Theme.ACCENT);
            g.drawItem(stackOf(r.item), x + 5, y + 3);
            String nm = textRenderer.trimToWidth(nameOf(r.item), w - 46);
            text(g, nm, x + 24, y + 2, r.enabled ? Theme.TEXT : Theme.TEXT_DIM);
            String md = CustomItems.MODELS[Math.max(0, Math.min(r.model, CustomItems.MODELS.length - 1))];
            text(g, textRenderer.trimToWidth("→ " + md, w - 46), x + 24, y + 12, Theme.ACCENT);
            text(g, "✕", x + w - 11, y + 7, overX ? 0xFFFF5555 : Theme.TEXT_DIM);
            if (clicked && hov) { if (overX) removeIdx = i; else sel = i; }
            y += 24;
        }
        g.disableScissor();
        clipY0 = 0; clipY1 = 100000;
        if (removeIdx >= 0) { Config.ciRules.remove(removeIdx); if (sel >= Config.ciRules.size()) sel = Config.ciRules.size() - 1; Config.save(); }
        if (Config.ciRules.isEmpty()) text(g, "Нет правил", x + 4, top + 4, Theme.TEXT_DIM);
        if (maxL > 0) scrollbar(g, x + w + 1, top, listBottom - top, maxL, scrollL);

        if (button(g, x, bottom - 20, w, 20, "＋ Добавить предмет", Theme.ACCENT, "add")) {
            client.setScreen(new ItemPickScreen(this, id -> {
                CiRule r = new CiRule(id, 0);
                Config.ciRules.add(r);
                sel = Config.ciRules.size() - 1;
                Config.save();
            }));
        }
    }

    private void scrollbar(DrawContext g, int x, int top, int h, float max, float sc) {
        int thumb = Math.max(14, (int) (h * h / (h + max)));
        int ty = top + Math.round((h - thumb) * (sc / max));
        HudStyle.roundFill(g, x, top, 2, h, 1, 0x30FFFFFF);
        HudStyle.roundFill(g, x, ty, 2, thumb, 1, Theme.ACCENT_DIM);
    }

    private void rightColumn(DrawContext g, int x, int top, int w, int bottom) {
        if (Config.ciRules.isEmpty() || sel < 0) {
            textC(g, "Добавь предмет слева", x, top + 30, w, Theme.TEXT_DIM);
            textC(g, "и выбери для него модель", x, top + 44, w, Theme.TEXT_DIM);
            return;
        }
        CiRule r = Config.ciRules.get(sel);
        maxR = Math.max(0, contentHeight(r) - (bottom - top));
        scrollR = Math.max(0, Math.min(scrollR, maxR));
        cx = x; cw = w; cy = top - Math.round(scrollR);
        g.enableScissor(x - 2, top, x + w + 4, bottom);
        clipY0 = top; clipY1 = bottom;

        // заголовок правила
        box(g, cx, cy, cw, 26, 0x30000000);
        g.drawItem(stackOf(r.item), cx + 5, cy + 5);
        text(g, textRenderer.trimToWidth(nameOf(r.item), cw - 120), cx + 26, cy + 4, Theme.TEXT);
        text(g, textRenderer.trimToWidth(r.item, cw - 120), cx + 26, cy + 15, Theme.TEXT_DIM);
        if (button(g, cx + cw - 88, cy + 3, 84, 20, "Сменить предмет", Theme.ACCENT, "swap")) {
            final CiRule rule = r;
            client.setScreen(new ItemPickScreen(this, id -> { rule.item = id; Config.save(); }));
        }
        cy += 30;

        r.enabled = toggleRow(g, "Правило включено", r.enabled);
        r.model = cycle(g, "Модель", CustomItems.MODELS, r.model);
        r.skin = cycle(g, "Текстура", CustomItems.SKINS, r.skin);
        if (r.skin == 1) {
            r.hue = hueBar(g, "Цвет", r.hue);
            r.sat = slider(g, "Насыщенность", 0, 1, r.sat, 0.05, 2);
        }
        cy += 2;
        r.scale = slider(g, "Размер", 0.2, 2.5, r.scale, 0.05, 2);
        r.x = slider(g, "Смещение X (см)", -60, 60, r.x, 1, 0);
        r.y = slider(g, "Смещение Y (см)", -60, 60, r.y, 1, 0);
        r.z = slider(g, "Смещение Z (см)", -60, 60, r.z, 1, 0);
        r.rx = slider(g, "Поворот X (°)", -180, 180, r.rx, 1, 0);
        r.ry = slider(g, "Поворот Y (°)", -180, 180, r.ry, 1, 0);
        r.rz = slider(g, "Поворот Z (°)", -180, 180, r.rz, 1, 0);
        cy += 2;
        if (button(g, cx, cy, cw, 18, "Сбросить положение и размер", 0xFFFF7777, "reset")) { r.resetTransform(); Config.save(); }
        cy += 22;

        g.disableScissor();
        clipY0 = 0; clipY1 = 100000;
        if (maxR > 0) scrollbar(g, x + w + 5, top, bottom - top, maxR, scrollR);
    }

    private int contentHeight(CiRule r) {
        return 30 + 20 * 3 + (r.skin == 1 ? 22 + 24 : 0) + 2 + 24 * 7 + 2 + 22;
    }

    // колесо мыши (без @Override: сигнатура зависит от версии)
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX < divX) scrollL = Math.max(0, Math.min(maxL, scrollL - (float) verticalAmount * 24f));
        else scrollR = Math.max(0, Math.min(maxR, scrollR - (float) verticalAmount * 24f));
        return true;
    }

    @Override
    public void close() { client.setScreen(parent); }

    @Override
    public void removed() { Config.save(); }

    @Override
    public boolean shouldPause() { return false; }
}
