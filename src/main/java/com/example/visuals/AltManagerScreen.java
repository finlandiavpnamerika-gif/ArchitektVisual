package com.example.visuals;

import com.example.visuals.mixin.MinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.session.Session;
import net.minecraft.text.Text;

import java.nio.charset.StandardCharsets;
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
        
        this.nameField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, this.height / 2 - 20, 200, 20, Text.literal("Введите ник"));
        this.nameField.setMaxLength(16);
        this.addDrawableChild(this.nameField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Войти"), button -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                applyAltAccount(name);
            }
        }).dimensions(this.width / 2 - 100, this.height / 2 + 10, 95, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Назад"), button -> {
            if (this.client != null) this.client.setScreen(parent);
        }).dimensions(this.width / 2 + 5, this.height / 2 + 10, 95, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        
        context.drawCenteredTextWithShadow(this.textRenderer, "§lAlt Manager", this.width / 2, this.height / 2 - 50, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "§7Введите ник и нажмите Войти", this.width / 2, this.height / 2 - 35, 0xAAAAAA);
    }

    private void applyAltAccount(String newName) {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            
            UUID uuid = UUID.nameUUIDFromBytes(newName.getBytes(StandardCharsets.UTF_8));
            
            Session newSession = new Session(
                    newName, 
                    uuid, 
                    "", 
                    Optional.empty(), 
                    Optional.empty()
            );
            
            ((MinecraftClientAccessor) mc).setSession(newSession);

            if (mc.player != null) {
                mc.player.sendMessage(Text.literal("§aНик изменен на: " + newName + ". Перезайди на сервер!"), false);
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
