package com.example.visuals;

import com.example.visuals.mixin.MinecraftClientAccessor;
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
        
        this.nameField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 40, 200, 20, Text.literal("Введите ник"));
        this.nameField.setMaxLength(16);
        this.addDrawableChild(this.nameField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Добавить"), button -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty() && !Config.alts.contains(name)) {
                Config.alts.add(name);
                Config.save();
                nameField.setText("");
            }
        }).dimensions(this.width / 2 - 100, 65, 95, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Войти"), button -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                changeUsername(name);
            }
        }).dimensions(this.width / 2 + 5, 65, 95, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Назад"), button -> {
            if (this.client != null) this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 100, this.height - 40, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        
        int startY = 100;
        context.drawTextWithShadow(this.textRenderer, "Сохраненные ники:", this.width / 2 - 100, startY, 0xFFFFFFFF);
        startY += 15;
        
        for (String alt : Config.alts) {
            int finalStartY = startY;
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Войти: " + alt), button -> {
                changeUsername(alt);
            }).dimensions(this.width / 2 - 100, finalStartY, 200, 16).build());
            
            startY += 20;
        }
    }

    private void changeUsername(String newName) {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return;

            UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + newName).getBytes());
            
            // Убрали AccountType.LEGACY, так как его больше нет в 1.21.11
            Session newSession = new Session(newName, uuid, "0", Optional.empty(), Optional.empty());
            
            ((MinecraftClientAccessor) mc).setSession(newSession);
            
            Field gameProfileField = mc.player.getClass().getDeclaredField("gameProfile");
            gameProfileField.setAccessible(true);
            GameProfile profile = (GameProfile) gameProfileField.get(mc.player);
            
            Field nameField = GameProfile.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(profile, newName);
            
            Field idField = GameProfile.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(profile, uuid);

            mc.player.sendMessage(Text.literal("§aНик изменен на: " + newName + ". Перезайди на сервер!"), false);
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
