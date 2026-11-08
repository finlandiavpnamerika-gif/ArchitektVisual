package com.example.visuals;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Конфиги: сохранить текущие настройки (вместе с HUD) под именем, загрузить, удалить,
 * поделиться через буфер обмена (экспорт/импорт).
 */
public class ConfigsScreen extends Screen {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM HH:mm").withZone(ZoneId.systemDefault());

    private final Screen parent;
    private final Map<String, Float> anims = new HashMap<>();
    private TextFieldWidget nameField;
    private String draftName = "";
    private List<ProfileManager.Info> items;
    private String sel = null;
    private float scroll = 0, maxScroll = 0;
    private String note = ""; private long noteUntil = 0; private boolean noteBad = false;
    private String confirmKey = ""; private long confirmUntil = 0;

    private final long openedAt = System.currentTimeMillis();
    private int mx, my;
    private boolean down, clicked, prevDown = true;
    private int px, py, pw, ph;

    public ConfigsScreen(Screen parent) {
        super(Text.literal("Конфиги"));
        this.parent = parent;
        refresh();
    }

    private void refresh() { items = ProfileManager.list(); }

    private void say(String s, boolean bad) { note = s; noteBad = bad; noteUntil = System.currentTimeMillis() + 3000; }

    private void layout() {
        pw = Math.min(width - 16, 440);
        ph = Math.min(height - 16, 290);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
    }

    @Override
    protected void init() {
        super.init();
        layout();
        nameField = new TextFieldWidget(this.textRenderer, px + 12, py + 36, pw - 24 - 124, 20, Text.literal("Имя конфига"));
        nameField.setMaxLength(32);
        nameField.setText(draftName);
        this.addDrawableChild(nameField);
    }

    // ---------- примитивы ----------
    private boolean hit(int x, int y, int w, int h) { return mx >= x && mx < x + w && my >= y && my < y + h; }

    private float anim(String k, boolean target) {
        float goal = target ? 1f : 0f, cur = anims.getOrDefault(k, goal);
        cur += (goal - cur) * 0.3f;
        if (Math.abs(goal - cur) < 0.01f) cur = goal;
        anims.put(k, cur);
        return cur;
    }

    private static int al(int c, int a) { return (a << 24) | (c & 0xFFFFFF); }

    private void text(DrawContext g, String s, int x, int y, int c) { g.drawTextWithShadow(textRenderer, s, x, y, c); }

    private boolean button(DrawContext g, int x, int y, int w, int h, String label, int accent, String key, boolean enabled) {
        boolean hov = enabled && hit(x, y, w, h);
        float a = anim("b:" + key, hov);
        int base = enabled ? al(accent, 0x30) : 0x25000000;
        HudStyle.roundFill(g, x, y, w, h, 3, HudStyle.mix(base, al(accent, 0x70), a));
        HudStyle.roundRing(g, x, y, w, h, 3, al(accent, enabled ? 0x60 + (int) (0x50 * a) : 0x25));
        text(g, label, x + (w - textRenderer.getWidth(label)) / 2, y + (h - 8) / 2, enabled ? Theme.TEXT : Theme.TEXT_DIM);
        return enabled && clicked && hov;
    }

    private boolean confirm(String key) { return key.equals(confirmKey) && System.currentTimeMillis() < confirmUntil; }

    // ---------- экран ----------
    @Override
    public void render(DrawContext g, int mouseX, int mouseY, float delta) {
        mx = mouseX; my = mouseY;
        long window = MinecraftClient.getInstance().getWindow().getHandle();
        down = GLFW.glfwGetMouseButton(window, 0) == GLFW.GLFW_PRESS;
        clicked = down && !prevDown;
        prevDown = down;
        layout();
        draftName = nameField.getText();

        g.fillGradient(0, 0, width, height, 0xA0080810, 0xD0000000);
        g.fillGradient(0, height / 2, width, height, 0x00000000, al(Theme.ACCENT, 0x30));

        int slide = Math.round((1f - Math.min(1f, (System.currentTimeMillis() - openedAt) / 180f)) * 8f);
        int oy = py;
        py += slide;
        nameField.setY(py + 36);

        HudStyle.roundFill(g, px + 1, py + 2, pw, ph, 6, 0x40000000);
        HudStyle.roundFill(g, px, py, pw, ph, 6, Theme.PANEL_BG);
        g.fillGradient(px + 3, py + 1, px + pw - 3, py + 30, al(Theme.ACCENT, 80), al(Theme.ACCENT, 0));
        HudStyle.roundRing(g, px, py, pw, ph, 6, al(Theme.ACCENT, 0x70));

        text(g, "Конфиги", px + 10, py + 9, Theme.ACCENT);
        String cur = (ProfileManager.current.isEmpty() ? "" : "● " + ProfileManager.current + "   ")
                + (ProfileManager.autoName().isEmpty() ? "" : "★ " + ProfileManager.autoName());
        text(g, cur, px + 70, py + 9, Theme.TEXT_DIM);
        if (button(g, px + pw - 62, py + 5, 54, 18, "← Назад", Theme.ACCENT, "back", true)) close();

        // имя + сохранить
        nameField.render(g, mouseX, mouseY, delta);
        if (nameField.getText().isEmpty() && !nameField.isFocused())
            text(g, "Имя нового конфига", nameField.getX() + 4, py + 42, 0xFF777777);
        String clean = ProfileManager.clean(nameField.getText());
        boolean exists = ProfileManager.exists(clean);
        if (button(g, px + pw - 12 - 116, py + 36, 116, 20, exists ? "Перезаписать" : "+ Сохранить", Theme.ACCENT, "save", !clean.isEmpty())) {
            if (ProfileManager.save(clean)) { say("Сохранено: " + clean, false); sel = clean; refresh(); }
            else say("Не удалось сохранить", true);
        }

        // список
        int top = py + 62, bottom = py + ph - 84;
        int lx = px + 12, lw = pw - 24;
        maxScroll = Math.max(0, items.size() * 24 - (bottom - top));
        scroll = Math.max(0, Math.min(scroll, maxScroll));
        HudStyle.roundFill(g, lx - 2, top - 2, lw + 4, bottom - top + 4, 3, 0x20000000);
        if (items.isEmpty()) {
            g.drawCenteredTextWithShadow(textRenderer, "Пока нет конфигов", px + pw / 2, top + 18, Theme.TEXT_DIM);
            g.drawCenteredTextWithShadow(textRenderer, "Введи имя и нажми «Сохранить»", px + pw / 2, top + 32, Theme.TEXT_DIM);
        }
        g.enableScissor(lx - 2, top, lx + lw + 2, bottom);
        int y = top - Math.round(scroll);
        for (ProfileManager.Info it : items) {
            boolean hov = hit(lx, y, lw, 22) && my >= top && my < bottom;
            boolean on = it.name().equals(sel);
            float a = anim("r:" + it.name(), hov);
            HudStyle.roundFill(g, lx, y, lw, 22, 3, on ? al(Theme.ACCENT, 0x50) : HudStyle.mix(0x28000000, Theme.ROW_HOVER, a * 0.6f));
            if (on) HudStyle.roundFill(g, lx, y + 4, 2, 14, 1, Theme.ACCENT);
            boolean isCur = it.name().equals(ProfileManager.current);
            text(g, textRenderer.trimToWidth(it.name(), lw - 120), lx + 8, y + 7, Theme.TEXT);
            if (isCur) text(g, "●", lx + lw - 112, y + 7, Theme.ACCENT);
            if (it.name().equals(ProfileManager.autoName())) text(g, "★", lx + lw - 124, y + 7, 0xFFFFD34D);
            String when = FMT.format(Instant.ofEpochMilli(it.modified()));
            text(g, when, lx + lw - 8 - textRenderer.getWidth(when), y + 7, Theme.TEXT_DIM);
            if (clicked && hov) {
                sel = it.name();
                nameField.setText(sel);
                draftName = sel;
            }
            y += 24;
        }
        g.disableScissor();
        if (maxScroll > 0) {
            int h = bottom - top, thumb = Math.max(14, (int) (h * h / (h + maxScroll)));
            int ty = top + Math.round((h - thumb) * (scroll / maxScroll));
            HudStyle.roundFill(g, lx + lw + 3, top, 2, h, 1, 0x30FFFFFF);
            HudStyle.roundFill(g, lx + lw + 3, ty, 2, thumb, 1, Theme.ACCENT_DIM);
        }

        // статус
        if (System.currentTimeMillis() < noteUntil)
            g.drawCenteredTextWithShadow(textRenderer, note, px + pw / 2, bottom + 6, noteBad ? 0xFFFF7777 : 0xFF77FF99);
        else
            g.drawCenteredTextWithShadow(textRenderer, "В конфиг входят все модули и HUD. Друзья и макросы не меняются.",
                    px + pw / 2, bottom + 6, Theme.TEXT_DIM);

        // кнопки действий
        int by = py + ph - 72, gap = 4, bw = (pw - 24 - gap * 3) / 4, bx = px + 12;
        boolean has = sel != null && ProfileManager.exists(sel);
        if (button(g, bx, by, bw, 20, "Загрузить", 0xFF66DD88, "load", has)) {
            if (ProfileManager.load(sel)) say("Загружено: " + sel, false); else say("Не удалось загрузить", true);
        }
        if (button(g, bx + (bw + gap), by, bw, 20, "Экспорт", Theme.ACCENT, "exp", has)) {
            String json = ProfileManager.export(sel);
            if (json != null) { MinecraftClient.getInstance().keyboard.setClipboard(json); say("Скопировано в буфер обмена", false); }
            else say("Не удалось экспортировать", true);
        }
        if (button(g, bx + (bw + gap) * 2, by, bw, 20, "Импорт", Theme.ACCENT, "imp", true)) {
            String name = ProfileManager.importJson(MinecraftClient.getInstance().keyboard.getClipboard(), clean.isEmpty() ? "Импорт" : clean);
            if (name != null) { sel = name; refresh(); say("Импортировано: " + name, false); }
            else say("В буфере обмена нет конфига", true);
        }
        String delKey = "del:" + sel;
        if (button(g, bx + (bw + gap) * 3, by, bw, 20, confirm(delKey) ? "Точно?" : "Удалить", 0xFFFF6666, "del", has)) {
            if (confirm(delKey)) {
                if (sel.equals(ProfileManager.autoName())) ProfileManager.setAuto("");
                ProfileManager.delete(sel);
                say("Удалено: " + sel, false);
                sel = null; refresh(); confirmKey = "";
            } else { confirmKey = delKey; confirmUntil = System.currentTimeMillis() + 2500; }
        }
        // авто-конфиг: выбранный загружается сам при запуске игры (и при заходе в мир, если включено)
        int half = (pw - 24 - gap) / 2;
        boolean isAuto = has && sel.equals(ProfileManager.autoName());
        String autoLabel = isAuto ? "★ Авто: снять" : "★ Сделать авто-конфигом";
        if (button(g, bx, by + 24, half, 20, autoLabel, 0xFFFFD34D, "auto", has)) {
            ProfileManager.setAuto(isAuto ? "" : sel);
            say(isAuto ? "Авто-конфиг снят" : "Авто-конфиг: " + sel, false);
        }
        boolean onJoin = ProfileManager.autoOnJoin();
        if (button(g, bx + half + gap, by + 24, half, 20, "При заходе в мир: " + (onJoin ? "вкл" : "выкл"), onJoin ? 0xFF66DD88 : Theme.ACCENT, "join", true)) {
            ProfileManager.setAutoOnJoin(!onJoin);
        }
        String rstKey = "reset";
        if (button(g, bx, by + 48, pw - 24, 18, confirm(rstKey) ? "Точно сбросить всё?" : "Сбросить все настройки по умолчанию", 0xFFFF9955, "rst", true)) {
            if (confirm(rstKey)) { ProfileManager.resetAll(); say("Всё сброшено", false); confirmKey = ""; }
            else { confirmKey = rstKey; confirmUntil = System.currentTimeMillis() + 2500; }
        }
        py = oy;
    }

    // колесо мыши (без @Override: сигнатура зависит от версии)
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll = Math.max(0, Math.min(maxScroll, scroll - (float) verticalAmount * 24f));
        return true;
    }

    @Override
    public void close() { MinecraftClient.getInstance().setScreen(parent); }

    @Override
    public boolean shouldPause() { return false; }
}
