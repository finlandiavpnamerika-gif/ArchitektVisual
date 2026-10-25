package com.example.visuals;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.util.UUID;

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
        
        this.nameField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 40, 200, 20, Text.literal("Введите ник"));
        this.nameField.setMaxLength(16);
        this.addDrawableChild(this.nameField);

        // Кнопка "Добавить в список"
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Добавить"), button -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty() && !Config.alts.contains(name)) {
                Config.alts.add(name);
                Config.save();
                nameField.setText("");
            }
        }).dimensions(this.width / 2 - 100, 65, 95, 20).build());

        // Кнопка "Войти" (Смена ника)
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Войти"), button -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                changeLocalUsername(name);
            }
        }).dimensions(this.width / 2 + 5, 65, 95, 20).build());

        // Кнопка "Назад"
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Назад"), button -> {
            if (this.client != null) this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 100, this.height - 40, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        
        int startY = 100;
        context.drawTextWithShadow(this.textRenderer, "Сохраненные ники (нажми на ник):", this.width / 2 - 100, startY, 0xFFFFFFFF);
        startY += 15;
        
        // Рисуем список ников и кнопки для них
        int buttonY = startY;
        for (String alt : Config.alts) {
            // Рисуем текст
            context.drawTextWithShadow(this.textRenderer, "- " + alt, this.width / 2 - 100, buttonY + 5, 0xFF7CE38B);
            
            // Добавляем кнопку "Войти" рядом с ником
            final String finalAlt = alt;
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Войти"), button -> {
                changeLocalUsername(finalAlt);
            }).dimensions(this.width / 2 + 50, buttonY, 50, 16).build());
            
            buttonY += 20;
        }
    }

    private void changeLocalUsername(String newName) {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return;

            Field gameProfileField = mc.player.getClass().getDeclaredField("gameProfile");
            gameProfileField.setAccessible(true);
            GameProfile profile = (GameProfile) gameProfileField.get(mc.player);
            
            Field nameField = GameProfile.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(profile, newName);
            
            Field idField = GameProfile.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(profile, UUID.nameUUIDFromBytes(("OfflinePlayer:" + newName).getBytes()));

            mc.player.sendMessage(Text.literal("§aНик (локально) изменен на: " + newName), false);
        } catch (Exception e) {
            if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendMessage(Text.literal("§cОшибка: " + e.getMessage()), false);
            }
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
