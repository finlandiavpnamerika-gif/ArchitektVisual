package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.session.Session;
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
        
        // Поле ввода
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

        // Кнопка "Сменить ник на введенный"
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Войти"), button -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                changeUsername(name);
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
        
        // Список сохраненных альтов
        int startY = 100;
        context.drawTextWithShadow(this.textRenderer, "Сохраненные ники (нажми на ник):", this.width / 2 - 100, startY, 0xFFFFFFFF);
        startY += 15;
        for (String alt : Config.alts) {
            context.drawTextWithShadow(this.textRenderer, "- " + alt, this.width / 2 - 100, startY, 0xFF7CE38B);
            startY += 12;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int startY = 115; // 100 + 15
        for (String alt : Config.alts) {
            boolean hov = mouseX >= this.width / 2 - 100 && mouseX <= this.width / 2 + 100 && mouseY >= startY - 2 && mouseY <= startY + 10;
            if (hov && button == 0) {
                changeUsername(alt);
                return true;
            }
            startY += 12;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    // МЕТОД СМЕНЫ НИКА ЧЕРЕЗ РЕФЛЕКСИЮ
    private void changeUsername(String newName) {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            
            // 1. Создаем новую сессию (оффлайн)
            UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + newName).getBytes());
            Session newSession = new Session(newName, uuid, "0", "legacy");
            
            // 2. Внедряем её в MinecraftClient через рефлексию
            Field sessionField = MinecraftClient.class.getDeclaredField("session");
            sessionField.setAccessible(true);
            sessionField.set(mc, newSession);
            
            // 3. Обновляем ник в текущем профиле игрока
            if (mc.player != null) {
                Field gameProfileField = mc.player.getClass().getDeclaredField("gameProfile");
                gameProfileField.setAccessible(true);
                com.mojang.authlib.GameProfile profile = (com.mojang.authlib.GameProfile) gameProfileField.get(mc.player);
                Field nameField = com.mojang.authlib.GameProfile.class.getDeclaredField("name");
                nameField.setAccessible(true);
                nameField.set(profile, newName);
            }

            if (mc.player != null) {
                mc.player.sendMessage(Text.literal("§aНик изменен на: " + newName + " (Перезайди на сервер!)"), false);
            }
        } catch (Exception e) {
            if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendMessage(Text.literal("§cОшибка смены ника: " + e.getMessage()), false);
            }
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
