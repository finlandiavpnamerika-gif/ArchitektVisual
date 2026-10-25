package com.example.visuals;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.session.Session;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.util.Optional;
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

        // Кнопка "Сменить ник"
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
        context.drawTextWithShadow(this.textRenderer, "Сохраненные ники (нажми, чтобы войти):", this.width / 2 - 100, startY, 0xFFFFFFFF);
        startY += 15;
        for (String alt : Config.alts) {
            boolean hov = mouseX >= this.width / 2 - 100 && mouseX <= this.width / 2 + 100 && mouseY >= startY - 2 && mouseY <= startY + 10;
            context.drawTextWithShadow(this.textRenderer, (hov ? "§a> " : "§7- ") + alt, this.width / 2 - 100, startY, 0xFFFFFFFF);
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

    // МЕТОД СМЕНЫ НИКА
    private void changeUsername(String newName) {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            
            // Создаем UUID для оффлайн режима
            UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + newName).getBytes());
            
            // В 1.21.11 конструктор требует Optional
            Session newSession = new Session(newName, uuid, "0", Optional.empty(), Optional.empty(), Session.AccountType.LEGACY);
            
            // Подменяем сессию через рефлексию
            Field sessionField = MinecraftClient.class.getDeclaredField("session");
            sessionField.setAccessible(true);
            sessionField.set(mc, newSession);
            
            // Меняем ник в профиле игрока (чтобы отображалось над головой)
            if (mc.player != null) {
                Field gameProfileField = mc.player.getClass().getDeclaredField("gameProfile");
                gameProfileField.setAccessible(true);
                GameProfile profile = (GameProfile) gameProfileField.get(mc.player);
                
                Field nameField = GameProfile.class.getDeclaredField("name");
                nameField.setAccessible(true);
                nameField.set(profile, newName);
                
                Field idField = GameProfile.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(profile, uuid);
            }

            if (mc.player != null) {
                mc.player.sendMessage(Text.literal("§aНик изменен на: " + newName + " (Перезайди на сервер!)"), false);
            }
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
