package com.example.visuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class FriendsScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget nameField;

    public FriendsScreen(Screen parent) {
        super(Text.literal("Friends Manager"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        
        // Поле ввода (по центру сверху)
        this.nameField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 50, 200, 20, Text.literal("Введите ник"));
        this.nameField.setMaxLength(16);
        this.addDrawableChild(this.nameField);

        // Кнопка "Добавить"
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Добавить друга"), button -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty() && !Config.friends.contains(name)) {
                Config.friends.add(name);
                Config.save();
                nameField.setText("");
            }
        }).dimensions(this.width / 2 - 100, 80, 200, 20).build());

        // Кнопка "Назад"
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Назад"), button -> {
            if (this.client != null) this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 100, this.height - 40, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta); // Рисует фон и кнопки
        
        // Рисуем список друзей вручную
        int startY = 120;
        for (String friend : Config.friends) {
            context.drawTextWithShadow(this.textRenderer, friend, this.width / 2 - 100, startY, 0xFF7CE38B);
            startY += 12;
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
