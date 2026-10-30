package com.example.visuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;

public class AltManagerScreen extends Screen {
    private static final int PER_PAGE = 5;

    private final Screen parent;
    private int page;
    private TextFieldWidget input;
    private String status = "";

    public AltManagerScreen(Screen parent) {
        this(parent, 0);
    }

    public AltManagerScreen(Screen parent, int page) {
        super(Text.literal("Alt Manager"));
        this.parent = parent;
        this.page = page;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        List<String> accounts = AltManager.getAccounts();
        int maxPage = Math.max(0, (accounts.size() - 1) / PER_PAGE);
        page = Math.max(0, Math.min(page, maxPage));

        input = new TextFieldWidget(this.textRenderer, cx - 100, 48, 140, 20, Text.literal("Ник"));
        input.setMaxLength(16);
        this.addDrawableChild(input);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Добавить"), b -> addAlt())
                .dimensions(cx + 45, 48, 55, 20).build());

        String current = AltManager.currentName();
        int start = page * PER_PAGE;
        for (int i = 0; i < PER_PAGE; i++) {
            int idx = start + i;
            if (idx >= accounts.size()) break;
            String name = accounts.get(idx);
            int y = 82 + i * 24;

            String label = name.equals(current) ? "§a● " + name : name;
            this.addDrawableChild(ButtonWidget.builder(Text.literal(label), b -> {
                AltManager.use(name);
                reopen();
            }).dimensions(cx - 100, y, 176, 20).build());

            this.addDrawableChild(ButtonWidget.builder(Text.literal("§cX"), b -> {
                AltManager.remove(name);
                reopen();
            }).dimensions(cx + 80, y, 20, 20).build());
        }

        int bottom = this.height - 28;

        ButtonWidget prev = ButtonWidget.builder(Text.literal("<"), b -> {
            page--;
            reopen();
        }).dimensions(cx - 100, bottom, 30, 20).build();
        prev.active = page > 0;
        this.addDrawableChild(prev);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Назад"), b -> this.close())
                .dimensions(cx - 65, bottom, 130, 20).build());

        ButtonWidget next = ButtonWidget.builder(Text.literal(">"), b -> {
            page++;
            reopen();
        }).dimensions(cx + 70, bottom, 30, 20).build();
        next.active = page < maxPage;
        this.addDrawableChild(next);
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
        page = Integer.MAX_VALUE; // прыгаем на последнюю страницу, init() обрежет
        reopen();
    }

    private void reopen() {
        this.client.setScreen(new AltManagerScreen(parent, page));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int cx = this.width / 2;
        context.drawCenteredTextWithShadow(this.textRenderer, "§lAlt Manager", cx, 12, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                "§7Текущий ник: §f" + AltManager.currentName(), cx, 24, 0xFFFFFFFF);
        context.drawText(this.textRenderer, "§7Ник (3-16: A-Z, 0-9, _)", cx - 100, 38, 0xFFFFFFFF, false);
        if (!status.isEmpty()) {
            context.drawText(this.textRenderer, status, cx - 100, 71, 0xFFFFFFFF, false);
        }
        if (AltManager.getAccounts().isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, "§8Список пуст", cx, 100, 0xFFFFFFFF);
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
