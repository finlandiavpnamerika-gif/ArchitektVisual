package com.example.visuals;

import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class AltManagerScreen extends Screen {
    private static final int PER_PAGE = 5;

    private static final int WHITE = 0xFFFFFFFF;
    private static final int GREEN = 0xFF55FF55;

    // геометрия строки списка: [ ник ][ Войти ][ X ]
    private static final int ROW_W = 220;
    private static final int NICK_W = 124;
    private static final int LOGIN_W = 64;
    private static final int DEL_W = 24;
    private static final int GAP = 4;
    private static final int ROW_H = 20;
    private static final int ROW_STEP = 24;
    private static final int LIST_Y = 82;

    private final Screen parent;
    private int page;
    private TextFieldWidget input;
    private String status;

    public AltManagerScreen(Screen parent) {
        this(parent, 0, "");
    }

    public AltManagerScreen(Screen parent, int page) {
        this(parent, page, "");
    }

    public AltManagerScreen(Screen parent, int page, String status) {
        super(Text.literal("Alt Manager"));
        this.parent = parent;
        this.page = page;
        this.status = status;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int left = cx - ROW_W / 2;
        List<String> accounts = AltManager.getAccounts();
        int maxPage = Math.max(0, (accounts.size() - 1) / PER_PAGE);
        page = Math.max(0, Math.min(page, maxPage));

        input = new TextFieldWidget(this.textRenderer, left, 48, ROW_W - 70, 20, Text.literal("Ник"));
        input.setMaxLength(16);
        this.addDrawableChild(input);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Добавить"), b -> addAlt())
                .dimensions(left + ROW_W - 66, 48, 66, 20).build());

        String current = AltManager.currentName();
        int start = page * PER_PAGE;
        for (int i = 0; i < PER_PAGE; i++) {
            int idx = start + i;
            if (idx >= accounts.size()) break;
            String name = accounts.get(idx);
            int y = LIST_Y + i * ROW_STEP;
            boolean active = name.equals(current);

            // Сам ник рисуется в render() (зелёным, если это текущий аккаунт),
            // здесь только кнопки.
            ButtonWidget login = ButtonWidget.builder(
                            Text.literal(active ? "§aВошёл" : "Войти"), b -> login(name))
                    .dimensions(left + NICK_W + GAP, y, LOGIN_W, ROW_H).build();
            login.active = !active;
            this.addDrawableChild(login);

            this.addDrawableChild(ButtonWidget.builder(Text.literal("§cX"), b -> delete(name))
                    .dimensions(left + NICK_W + GAP + LOGIN_W + GAP, y, DEL_W, ROW_H).build());
        }

        int bottom = this.height - 28;

        ButtonWidget prev = ButtonWidget.builder(Text.literal("<"), b -> changePage(-1))
                .dimensions(cx - 100, bottom, 30, 20).build();
        prev.active = page > 0;
        this.addDrawableChild(prev);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Назад"), b -> this.close())
                .dimensions(cx - 65, bottom, 130, 20).build());

        ButtonWidget next = ButtonWidget.builder(Text.literal(">"), b -> changePage(1))
                .dimensions(cx + 70, bottom, 30, 20).build();
        next.active = page < maxPage;
        this.addDrawableChild(next);
    }

    private void login(String name) {
        status = AltManager.use(name)
                ? "§aВы вошли как §f" + name
                : "§cНе удалось сменить ник (подробности в latest.log)";
        reopen();
    }

    private void delete(String name) {
        AltManager.remove(name);
        status = "";
        reopen();
    }

    private void changePage(int delta) {
        page += delta;
        status = "";
        reopen();
    }

    private void addAlt() {
        String name = input.getText().trim();
        if (!AltManager.isValid(name)) {
            status = "§cНеверный ник";
            return;
        }
        if (!AltManager.add(name)) {
            status = "§eТакой ник уже есть";
            return;
        }
        status = "§aДобавлен §f" + name + "§a - нажмите «Войти»";
        page = Integer.MAX_VALUE; // прыгаем на последнюю страницу, init() обрежет
        reopen();
    }

    private void reopen() {
        this.client.setScreen(new AltManagerScreen(parent, page, status));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int cx = this.width / 2;
        int left = cx - ROW_W / 2;
        String current = AltManager.currentName();

        context.drawCenteredTextWithShadow(this.textRenderer, "§lAlt Manager", cx, 12, WHITE);
        context.drawCenteredTextWithShadow(this.textRenderer, "§7Текущий ник: §a" + current, cx, 24, WHITE);
        context.drawText(this.textRenderer, "§7Ник (3-16: A-Z, 0-9, _)", left, 38, WHITE, false);
        if (!status.isEmpty()) {
            context.drawText(this.textRenderer, status, left, 71, WHITE, false);
        }

        List<String> accounts = AltManager.getAccounts();
        int start = page * PER_PAGE;
        for (int i = 0; i < PER_PAGE; i++) {
            int idx = start + i;
            if (idx >= accounts.size()) break;
            String name = accounts.get(idx);
            boolean active = name.equals(current);
            int y = LIST_Y + i * ROW_STEP;

            // плашка с ником: у текущего аккаунта зелёная полоска слева и зелёный ник
            context.fill(left, y, left + NICK_W, y + ROW_H, active ? 0xB0103A10 : 0xB0000000);
            context.fill(left, y, left + 2, y + ROW_H, active ? GREEN : 0xFF555555);
            context.drawText(this.textRenderer, name, left + 8, y + 6, active ? GREEN : WHITE, true);
        }

        if (accounts.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, "§7Список пуст - добавьте ник выше", cx, 100, WHITE);
        }
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
