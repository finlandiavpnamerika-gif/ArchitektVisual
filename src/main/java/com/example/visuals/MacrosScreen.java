package com.example.visuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class MacrosScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget keyField;
    private TextFieldWidget cmdField;

    public MacrosScreen(Screen parent) {
        super(Text.literal("Macros"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.keyField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 40, 200, 20, Text.literal("Клавиша (например: 86)"));
        this.addDrawableChild(this.keyField);
        
        this.cmdField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 70, 200, 20, Text.literal("Команда (без /)"));
        this.addDrawableChild(this.cmdField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Добавить"), button -> {
            if (!keyField.getText().isEmpty() && !cmdField.getText().isEmpty()) {
                Config.macros.add(keyField.getText() + ":" + cmdField.getText());
                Config.save();
                keyField.setText("");
                cmdField.setText("");
            }
        }).dimensions(this.width / 2 - 100, 100, 95, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Назад"), button -> {
            if (this.client != null) this.client.setScreen(parent);
        }).dimensions(this.width / 2 + 5, 100, 95, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, "§lMacros", this.width / 2, 15, 0xFFFFFF);
        
        int y = 130;
        context.drawTextWithShadow(this.textRenderer, "Список (клавиша:команда):", 10, y, 0xAAAAAA);
        y += 15;
        for (String m : Config.macros) {
            context.drawTextWithShadow(this.textRenderer, "- " + m, 10, y, 0x7CE38B);
            y += 12;
        }
    }

    @Override
    public boolean shouldPause() { return false; }
}
