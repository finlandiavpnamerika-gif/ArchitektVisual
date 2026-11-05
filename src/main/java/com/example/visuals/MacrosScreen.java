package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

/**
 * Меню макросов в стиле ArchitektVisuals.
 * Сверху форма: [Клавиша] [Команда или сообщение] [Добавить]. Ниже список с переключателями и удалением.
 * Клавишу выбираем нажатием, а не вводом числа. Клик по значку клавиши в списке - переназначить.
 */
public class MacrosScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget cmdField;

    // черновик живёт в экране: после выбора клавиши экран пересоздаёт поля, а текст остаётся
    private String draftText = "";
    private int draftKey = -1;

    private int scroll = 0;
    private String note = null;
    private long noteUntil = 0;

    private final long openedAt = System.currentTimeMillis();
    private int mx, my;
    private boolean down, clicked, prevDown = true;
    private int px, py, pw, ph;

    public MacrosScreen(Screen parent) {
        super(Text.literal("Macros"));
        this.parent = parent;
    }

    private void layout() {
        pw = Math.min(width - 20, 440);
        ph = Math.min(height - 44, 300);
        px = (width - pw) / 2;
        py = Math.max(8, (height - 36 - ph) / 2);
    }

    @Override
    protected void init() {
        super.init();
        layout();
        int fx = px + 12 + 104;
        int fw = pw - 24 - 104 - 78;
        cmdField = new TextFieldWidget(this.textRenderer, fx, py + 46, fw, 20, Text.literal("Команда"));
        cmdField.setMaxLength(256);
        cmdField.setText(draftText);
        this.addDrawableChild(cmdField);
    }

    // ---------- примитивы ----------
    private boolean hit(int x, int y, int w, int h) { return mx >= x && mx < x + w && my >= y && my < y + h; }

    private void rbox(DrawContext g, int x, int y, int w, int h, int c) {
        g.fill(x + 1, y, x + w - 1, y + h, c);
        g.fill(x, y + 1, x + 1, y + h - 1, c);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, c);
    }

    private static int withAlpha(int color, int alpha) { return (alpha << 24) | (color & 0xFFFFFF); }

    private void say(String s) {
        note = s;
        noteUntil = System.currentTimeMillis() + 2500;
    }

    private String validate(int key, Macro self) {
        String err = ItemFeatures.validateKey(key, self);
        if (err != null) return err;
        for (Macro m : Config.macros) {
            if (m != self && m.key == key) return "Клавиша уже занята другим макросом";
        }
        return null;
    }

    private void openKeyPicker(String subject, java.util.function.IntFunction<String> acceptor) {
        client.execute(() -> client.setScreen(new BindScreen(this, subject, acceptor)));
    }

    // ---------- отрисовка ----------
    @Override
    public void render(DrawContext g, int mouseX, int mouseY, float delta) {
        mx = mouseX; my = mouseY;
        long window = MinecraftClient.getInstance().getWindow().getHandle();
        down = GLFW.glfwGetMouseButton(window, 0) == GLFW.GLFW_PRESS;
        clicked = down && !prevDown;
        prevDown = down;
        layout();
        draftText = cmdField.getText();

        // фон как у главного меню
        g.fillGradient(0, 0, width, height, 0x80080810, 0xC0000000);
        RainyScene.drawRain(g, width, height, 0.6f);

        float p = Math.min(1f, (System.currentTimeMillis() - openedAt) / 200f);
        int slide = Math.round((1f - p) * (1f - p) * 10f);
        int top = py + slide;

        // панель
        rbox(g, px + 2, top + 3, pw, ph, 0x45000000);
        rbox(g, px, top, pw, ph, Theme.PANEL_BG);
        g.fillGradient(px + 1, top + 1, px + pw - 1, top + 28,
                withAlpha(Theme.ACCENT, 48), withAlpha(Theme.ACCENT, 4));
        g.fill(px + 1, top, px + pw - 1, top + 1, Theme.ACCENT);
        g.drawCenteredTextWithShadow(textRenderer, Text.literal("Macros").formatted(Formatting.BOLD),
                px + pw / 2, top + 9, Theme.TEXT);
        g.fill(px + 8, top + 28, px + pw - 8, top + 29, Theme.ACCENT_DIM);

        // ---------- форма добавления ----------
        int fy = top + 40;
        int keyW = 100;
        String keyLabel = draftKey == -1 ? "Выбрать клавишу" : "[" + Keys.name(draftKey) + "]";
        boolean hovKey = hit(px + 12, fy, keyW, 20);
        rbox(g, px + 12, fy, keyW, 20, hovKey ? Theme.ROW_HOVER : Theme.ROW);
        g.drawCenteredTextWithShadow(textRenderer, keyLabel, px + 12 + keyW / 2, fy + 6,
                draftKey == -1 ? Theme.TEXT_DIM : Theme.ACCENT);
        if (clicked && hovKey) {
            openKeyPicker("Macro: выбери клавишу", k -> {
                String err = validate(k, null);
                if (err != null) return err;
                draftKey = k;
                return null;
            });
        }

        // поле ввода (позиция задана в init; сдвигаем вместе с анимацией)
        cmdField.setY(fy);
        cmdField.render(g, mouseX, mouseY, delta);
        if (cmdField.getText().isEmpty() && !cmdField.isFocused()) {
            g.drawTextWithShadow(textRenderer, "Команда (/spawn) или сообщение", cmdField.getX() + 4, fy + 6, 0xFF777777);
        }

        // кнопка "Добавить"
        int addW = 70;
        int ax = px + pw - 12 - addW;
        boolean ready = draftKey != -1 && !cmdField.getText().trim().isEmpty();
        boolean hovAdd = hit(ax, fy, addW, 20);
        rbox(g, ax, fy, addW, 20, ready ? (hovAdd ? Theme.ACCENT : Theme.ACCENT_DIM) : Theme.ROW);
        g.drawCenteredTextWithShadow(textRenderer, "+ Добавить", ax + addW / 2, fy + 6, ready ? Theme.TEXT : Theme.TEXT_DIM);
        if (clicked && hovAdd) {
            if (ready) {
                Config.macros.add(new Macro(draftKey, cmdField.getText().trim()));
                Config.save();
                draftKey = -1;
                draftText = "";
                cmdField.setText("");
                say("Макрос добавлен");
            } else {
                say(draftKey == -1 ? "Сначала выбери клавишу" : "Введи команду или сообщение");
            }
        }

        // подсказка
        g.drawTextWithShadow(textRenderer, "С «/» в начале - команда, иначе - сообщение в чат",
                px + 12, fy + 26, Theme.TEXT_DIM);

        // ---------- список ----------
        int listTop = fy + 44;
        int rowH = 24;
        int n = Config.macros.size();
        int maxRows = Math.max(1, (top + ph - 10 - listTop) / rowH);
        scroll = Math.max(0, Math.min(scroll, Math.max(0, n - maxRows)));

        g.drawTextWithShadow(textRenderer, "Список макросов (" + n + ")", px + 12, listTop - 14, Theme.TEXT_DIM);
        if (n > maxRows) {
            String r = (scroll + 1) + "-" + Math.min(n, scroll + maxRows) + " / " + n + "  колёсико";
            g.drawTextWithShadow(textRenderer, r, px + pw - 12 - textRenderer.getWidth(r), listTop - 14, Theme.TEXT_DIM);
        }

        if (n == 0) {
            g.drawCenteredTextWithShadow(textRenderer, "Пока нет макросов", px + pw / 2, listTop + 12, Theme.TEXT_DIM);
        }

        Macro toRemove = null;
        for (int i = scroll; i < n && i < scroll + maxRows; i++) {
            Macro m = Config.macros.get(i);
            int ry = listTop + (i - scroll) * rowH;
            int rx = px + 12, rw = pw - 24;
            boolean hov = hit(rx, ry, rw, 22);
            rbox(g, rx, ry, rw, 22, hov ? Theme.ROW_HOVER : Theme.ROW);
            if (m.enabled) g.fill(rx, ry + 2, rx + 2, ry + 20, Theme.ACCENT);

            // значок клавиши (клик - переназначить)
            String kn = m.key == -1 ? "-" : Keys.name(m.key);
            int bw = textRenderer.getWidth(kn) + 12;
            boolean hovBadge = hit(rx + 6, ry + 3, bw, 16);
            rbox(g, rx + 6, ry + 3, bw, 16, hovBadge ? Theme.ACCENT : Theme.ACCENT_DIM);
            g.drawCenteredTextWithShadow(textRenderer, kn, rx + 6 + bw / 2, ry + 7, Theme.TEXT);
            if (clicked && hovBadge) {
                final Macro mm = m;
                openKeyPicker("Macro: " + mm.text, k -> {
                    String err = validate(k, mm);
                    if (err != null) return err;
                    mm.key = k;
                    mm.wasDown = true;
                    Config.save();
                    return null;
                });
            }

            // текст команды
            int tx = rx + 6 + bw + 8;
            int tw = rw - (tx - rx) - 62;
            g.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(m.text, tw), tx, ry + 7,
                    m.enabled ? Theme.TEXT : Theme.TEXT_DIM);

            // переключатель
            int sx = rx + rw - 54;
            boolean hovSw = hit(sx - 2, ry + 2, 28, 18);
            rbox(g, sx, ry + 5, 24, 12, m.enabled ? Theme.TOGGLE_ON : Theme.TOGGLE_OFF);
            rbox(g, sx + (m.enabled ? 14 : 2), ry + 7, 8, 8, 0xFFFFFFFF);
            if (clicked && hovSw) { m.enabled = !m.enabled; Config.save(); }

            // удаление
            boolean hovX = hit(rx + rw - 22, ry + 2, 18, 18);
            g.drawTextWithShadow(textRenderer, "✕", rx + rw - 16, ry + 7, hovX ? 0xFFFF5555 : Theme.TEXT_DIM);
            if (clicked && hovX) toRemove = m;
        }
        if (toRemove != null) {
            Config.macros.remove(toRemove);
            Config.save();
            say("Макрос удалён");
        }

        // уведомление
        if (note != null && System.currentTimeMillis() < noteUntil) {
            int w = textRenderer.getWidth(note) + 14;
            g.fill(px + pw / 2 - w / 2, top + ph + 6, px + pw / 2 + w / 2, top + ph + 22, 0xC0000000);
            g.drawCenteredTextWithShadow(textRenderer, note, px + pw / 2, top + ph + 10, Theme.TEXT);
        }

        // кнопка "Назад"
        int bw2 = 160, bh = 20;
        int bx = (width - bw2) / 2, by = height - bh - 10;
        boolean hovB = hit(bx, by, bw2, bh);
        rbox(g, bx + 1, by + 2, bw2, bh, 0x40000000);
        rbox(g, bx, by, bw2, bh, hovB ? Theme.ACCENT : Theme.PANEL_BG);
        g.fill(bx + 1, by, bx + bw2 - 1, by + 1, Theme.ACCENT);
        g.drawCenteredTextWithShadow(textRenderer, "Назад", width / 2, by + 6, Theme.TEXT);
        if (clicked && hovB) close();
    }

    // колёсико мыши листает список (без @Override: если сигнатура в версии другая, метод просто не вызовется)
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll -= (int) Math.signum(verticalAmount);
        if (scroll < 0) scroll = 0;
        return true;
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() { return false; }
}
