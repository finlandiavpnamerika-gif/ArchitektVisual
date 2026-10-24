package com.example.visuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class AltManagerScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget nameField;

    public AltManagerScreen(Screen parent) {
        super(Text.literal("Alt Manager"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        
        // Поле ввода для ника
        this.nameField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 50, 200, 20, Text.literal("Введите ник"));
        this.nameField.setMaxLength(16);
        this.addDrawableChild(this.nameField);

        // Кнопка "Добавить"
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Добавить"), button -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty() && !Config.alts.contains(name)) {
                Config.alts.add(name);
                Config.save();
                nameField.setText("");
            }
        }).dimensions(this.width / 2 - 100, 80, 200, 20).build());

        // Кнопка "Войти" (пока просто выдает сообщение)
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Войти (Скоро)"), button -> {
            if (this.client != null && this.client.player != null) {
                this.client.player.sendMessage(Text.literal("§cСмена ника в разработке!"), true);
            }
        }).dimensions(this.width / 2 - 100, 110, 200, 20).build());

        // Кнопка "Назад"
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Назад"), button -> {
            if (this.client != null) this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 100, this.height - 40, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        
        // Рисуем список сохраненных альтов
        int startY = 150;
        context.drawTextWithShadow(this.textRenderer, "Сохраненные ники:", this.width / 2 - 100, startY, 0xFFFFFFFF);
        startY += 15;
        for (String alt : Config.alts) {
            context.drawTextWithShadow(this.textRenderer, "- " + alt, this.width / 2 - 100, startY, 0xFF7CE38B);
            startY += 12;
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
