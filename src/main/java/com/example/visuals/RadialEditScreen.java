package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

/**
 * Редактор кругового меню в стиле ArchitektVisuals.
 * Сверху клавиша колеса, ниже форма [Название] [Команда] [Добавить], ниже список секторов
 * (стрелка вверх - поднять сектор, крестик - удалить). Порядок списка = порядок на колесе по часовой, от верха.
 */
public class RadialEditScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget labelField;
    private TextFieldWidget cmdField;

    // черновик живёт в экране: после выбора клавиши экран пересоздаёт поля, а текст остаётся
    private String draftLabel = "";
    private String draftText = "";

    private int scroll = 0;
    private String note = null;
    private long noteUntil = 0;

    private final long openedAt = System.currentTimeMillis();
    private int mx, my;
    private boolean down, clicked, prevDown = true;
    private int px, py, pw, ph;

    public RadialEditScreen(Screen parent) {
        super(Text.literal("Radial menu"));
        this.parent = parent;
    }

    private void layout() {
        pw = Math.min(width - 20, 440);
        ph = Math.min(height - 44, 330);
        px = (width - pw) / 2;
        py = Math.max(8, (height - 36 - ph) / 2);
    }

    @Override
    protected void init() {
        super.init();
        layout();
        int lw = 96;
        int fx2 = px + 12 + lw + 6;
        int fw2 = pw - 24 - lw - 6 - 6 - 78;
        labelField = new TextFieldWidget(this.textRenderer, px + 12, py + 70, lw, 20, Text.literal("Название"));
        labelField.setMaxLength(24);
        labelField.setText(draftLabel);
        this.addDrawableChild(labelField);
        cmdField = new TextFieldWidget(this.textRenderer, fx2, py + 70, fw2, 20, Text.literal("Команда"));
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

    private String validateRadialKey(int key) {
        if (key == Config.radialKey) return null;
        String err = ItemFeatures.validateKey(key, null);
        if (err != null) return err;
        for (Macro m : Config.macros) {
            if (m.key == key) return "Клавиша уже занята макросом";
        }
        return null;
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
        draftLabel = labelField.getText();
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
        g.drawCenteredTextWithShadow(textRenderer, Text.literal("Radial Menu").formatted(Formatting.BOLD),
                px + pw / 2, top + 9, Theme.TEXT);
        g.fill(px + 8, top + 28, px + pw - 8, top + 29, Theme.ACCENT_DIM);

        // ---------- клавиша колеса ----------
        int ky = top + 36;
        g.drawTextWithShadow(textRenderer, "Клавиша колеса:", px + 12, ky + 6, Theme.TEXT);
        String keyLabel = Config.radialKey == -1 ? "Не задана" : "[" + Keys.name(Config.radialKey) + "]";
        int kx = px + 12 + 104;
        int kw = 86;
        boolean hovKey = hit(kx, ky, kw, 20);
        rbox(g, kx, ky, kw, 20, hovKey ? Theme.ROW_HOVER : Theme.ROW);
        g.drawCenteredTextWithShadow(textRenderer, keyLabel, kx + kw / 2, ky + 6,
                Config.radialKey == -1 ? Theme.TEXT_DIM : Theme.ACCENT);
        g.drawTextWithShadow(textRenderer, "зажми - наведи - отпусти", kx + kw + 8, ky + 6, Theme.TEXT_DIM);
        if (clicked && hovKey) {
            client.execute(() -> client.setScreen(new BindScreen(this, "Radial: клавиша колеса", k -> {
                String err = validateRadialKey(k);
                if (err != null) return err;
                Config.radialKey = k;
                RadialMenu.resync();
                Config.save();
                return null;
            })));
        }

        // ---------- форма добавления ----------
        int fy = top + 64;
        labelField.setY(fy);
        cmdField.setY(fy);
        labelField.render(g, mouseX, mouseY, delta);
        cmdField.render(g, mouseX, mouseY, delta);
        if (labelField.getText().isEmpty() && !labelField.isFocused()) {
            g.drawTextWithShadow(textRenderer, "Название", labelField.getX() + 4, fy + 6, 0xFF777777);
        }
        if (cmdField.getText().isEmpty() && !cmdField.isFocused()) {
            g.drawTextWithShadow(textRenderer, "Команда (/spawn) или сообщение", cmdField.getX() + 4, fy + 6, 0xFF777777);
        }

        int addW = 70;
        int ax = px + pw - 12 - addW;
        boolean ready = !cmdField.getText().trim().isEmpty();
        boolean hovAdd = hit(ax, fy, addW, 20);
        rbox(g, ax, fy, addW, 20, ready ? (hovAdd ? Theme.ACCENT : Theme.ACCENT_DIM) : Theme.ROW);
        g.drawCenteredTextWithShadow(textRenderer, "+ Добавить", ax + addW / 2, fy + 6, ready ? Theme.TEXT : Theme.TEXT_DIM);
        if (clicked && hovAdd) {
            if (!ready) {
                say("Введи команду или сообщение");
            } else if (Config.radialItems.size() >= RadialMenu.MAX_ITEMS) {
                say("Максимум " + RadialMenu.MAX_ITEMS + " секторов");
            } else {
                Config.radialItems.add(new RadialItem(labelField.getText().trim(), cmdField.getText().trim()));
                Config.save();
                draftLabel = "";
                draftText = "";
                labelField.setText("");
                cmdField.setText("");
                say("Сектор добавлен");
            }
        }

        g.drawTextWithShadow(textRenderer, "С «/» в начале - команда, иначе - сообщение в чат. Название необязательно",
                px + 12, fy + 26, Theme.TEXT_DIM);

        // ---------- список ----------
        int listTop = fy + 56;
        int rowH = 24;
        int n = Config.radialItems.size();
        int maxRows = Math.max(1, (top + ph - 10 - listTop) / rowH);
        scroll = Math.max(0, Math.min(scroll, Math.max(0, n - maxRows)));

        g.drawTextWithShadow(textRenderer, "Секторы (" + n + "/" + RadialMenu.MAX_ITEMS + ")", px + 12, listTop - 14, Theme.TEXT_DIM);
        if (n > maxRows) {
            String r = (scroll + 1) + "-" + Math.min(n, scroll + maxRows) + " / " + n + "  колёсико";
            g.drawTextWithShadow(textRenderer, r, px + pw - 12 - textRenderer.getWidth(r), listTop - 14, Theme.TEXT_DIM);
        }

        if (n == 0) {
            g.drawCenteredTextWithShadow(textRenderer, "Пока пусто: добавь первую команду", px + pw / 2, listTop + 12, Theme.TEXT_DIM);
        }

        RadialItem toRemove = null;
        int toMoveUp = -1;
        for (int i = scroll; i < n && i < scroll + maxRows; i++) {
            RadialItem it = Config.radialItems.get(i);
            int ry = listTop + (i - scroll) * rowH;
            int rx = px + 12, rw = pw - 24;
            boolean hov = hit(rx, ry, rw, 22);
            rbox(g, rx, ry, rw, 22, hov ? Theme.ROW_HOVER : Theme.ROW);
            g.fill(rx, ry + 2, rx + 2, ry + 20, Theme.ACCENT);

            // номер сектора
            String num = String.valueOf(i + 1);
            int bw = textRenderer.getWidth(num) + 12;
            rbox(g, rx + 6, ry + 3, bw, 16, Theme.ACCENT_DIM);
            g.drawCenteredTextWithShadow(textRenderer, num, rx + 6 + bw / 2, ry + 7, Theme.TEXT);

            // название и команда
            int tx = rx + 6 + bw + 8;
            int tw = rw - (tx - rx) - 50;
            if (it.label.isEmpty()) {
                g.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(it.text, tw), tx, ry + 7, Theme.TEXT);
            } else {
                String l = textRenderer.trimToWidth(it.label, tw / 2);
                g.drawTextWithShadow(textRenderer, l, tx, ry + 7, Theme.TEXT);
                int cx2 = tx + textRenderer.getWidth(l) + 8;
                g.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(it.text, Math.max(10, tw - (cx2 - tx))),
                        cx2, ry + 7, Theme.TEXT_DIM);
            }

            // поднять
            if (i > 0) {
                boolean hovUp = hit(rx + rw - 42, ry + 2, 18, 18);
                g.drawTextWithShadow(textRenderer, "▲", rx + rw - 38, ry + 7, hovUp ? Theme.ACCENT : Theme.TEXT_DIM);
                if (clicked && hovUp) toMoveUp = i;
            }

            // удаление
            boolean hovX = hit(rx + rw - 22, ry + 2, 18, 18);
            g.drawTextWithShadow(textRenderer, "✕", rx + rw - 16, ry + 7, hovX ? 0xFFFF5555 : Theme.TEXT_DIM);
            if (clicked && hovX) toRemove = it;
        }
        if (toRemove != null) {
            Config.radialItems.remove(toRemove);
            Config.save();
            say("Сектор удалён");
        } else if (toMoveUp > 0) {
            RadialItem a = Config.radialItems.get(toMoveUp);
            Config.radialItems.set(toMoveUp, Config.radialItems.get(toMoveUp - 1));
            Config.radialItems.set(toMoveUp - 1, a);
            Config.save();
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
        Config.save();
        if (client != null) client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() { return false; }
}
