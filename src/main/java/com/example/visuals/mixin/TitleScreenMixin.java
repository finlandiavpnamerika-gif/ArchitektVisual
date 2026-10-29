package com.example.visuals.mixin;

import com.example.visuals.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin() {
        super(null);
    }

    private boolean prevMouseDown = false;

    @Inject(method = "init", at = @At("TAIL"))
    private void visuals$clearButtons(CallbackInfo ci) {
        if (!Config.customMainMenu) return;
        this.clearChildren();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void visuals$drawCustomUI(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!Config.customMainMenu) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        int w = this.width;
        int h = this.height;

        long window = mc.getWindow().getHandle();
        boolean mouseDown = GLFW.glfwGetMouseButton(window, 0) == GLFW.GLFW_PRESS;
        boolean clicked = mouseDown && !prevMouseDown;
        prevMouseDown = mouseDown;

        // Часы и дата
        LocalDateTime now = LocalDateTime.now();
        String timeString = now.format(DateTimeFormatter.ofPattern("HH:mm"));
        String dateString = now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", new java.util.Locale("ru")));
        dateString = dateString.substring(0, 1).toUpperCase() + dateString.substring(1);

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(w / 2.0f, h / 2.0f - 60);
        context.getMatrices().scale(3.0f, 3.0f);
        int timeWidth = this.textRenderer.getWidth(timeString);
        context.drawTextWithShadow(this.textRenderer, timeString, -timeWidth / 2, 0, 0xFFFFFFFF);
        context.getMatrices().popMatrix();

        int dateWidth = this.textRenderer.getWidth(dateString);
        context.drawTextWithShadow(this.textRenderer, dateString, (w - dateWidth) / 2, h / 2 + 10, 0xFFAAAAAA);

        // Кнопка "Продолжить"
        int widgetW = 220;
        int widgetH = 60;
        int widgetX = w - widgetW - 20;
        int widgetY = h - widgetH - 60;

        boolean hovPlay = mouseX >= widgetX && mouseX <= widgetX + widgetW && mouseY >= widgetY && mouseY <= widgetY + widgetH;
        
        context.fill(widgetX, widgetY, widgetX + widgetW, widgetY + widgetH, hovPlay ? 0xFF2A2A2A : 0xE0101010);
        context.fill(widgetX, widgetY, widgetX + widgetW, widgetY + 2, 0xFF7CE38B);

        context.drawTextWithShadow(this.textRenderer, "§7Одиночная игра", widgetX + 10, widgetY + 10, 0xFFFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "§fСоздать новый мир", widgetX + 10, widgetY + 25, 0xFFAAAAAA);
        
        int btnW = 60;
        int btnH = 20;
        int btnX = widgetX + widgetW - btnW - 10;
        int btnY = widgetY + widgetH - btnH - 10;
        context.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "Играть", btnX + btnW / 2, btnY + 6, 0xFF000000);

        if (clicked && hovPlay) {
            mc.setScreen(new SelectWorldScreen(this));
        }

        // Нижняя панель
        int dockW = 200;
        int dockH = 30;
        int dockX = (w - dockW) / 2;
        int dockY = h - dockH - 10;

        context.fill(dockX, dockY, dockX + dockW, dockY + dockH, 0xE0202020);
        context.fill(dockX, dockY, dockX + dockW, dockY + 2, 0xFF7CE38B);

        boolean hovSettings = mouseX >= dockX + 10 && mouseX <= dockX + 60 && mouseY >= dockY + 5 && mouseY <= dockY + 25;
        context.drawCenteredTextWithShadow(this.textRenderer, "Настройки", dockX + 35, dockY + 10, hovSettings ? 0xFF7CE38B : 0xFFFFFFFF);
        if (clicked && hovSettings) mc.setScreen(new OptionsScreen(this, mc.options));

        boolean hovMulti = mouseX >= dockX + 70 && mouseX <= dockX + 130 && mouseY >= dockY + 5 && mouseY <= dockY + 25;
        context.drawCenteredTextWithShadow(this.textRenderer, "Сеть", dockX + 100, dockY + 10, hovMulti ? 0xFF7CE38B : 0xFFFFFFFF);
        if (clicked && hovMulti) mc.setScreen(new MultiplayerScreen(this));

        boolean hovQuit = mouseX >= dockX + 140 && mouseX <= dockX + 190 && mouseY >= dockY + 5 && mouseY <= dockY + 25;
        context.drawCenteredTextWithShadow(this.textRenderer, "Выход", dockX + 165, dockY + 10, hovQuit ? 0xFFFF5555 : 0xFFFFFFFF);
        if (clicked && hovQuit) mc.scheduleStop();
    }
}
