package com.example.visuals.mixin;

import com.example.visuals.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Свой экран загрузки в стиле ArchitektVisuals. */
@Mixin(SplashOverlay.class)
public class SplashOverlayMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void visuals$renderSplash(DrawContext g, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!Config.customMainMenu) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        int w = mc.getWindow().getScaledWidth();
        int h = mc.getWindow().getScaledHeight();

        // Тёмный фон с зелёным оттенком
        g.fill(0, 0, w, h, 0xFF0A0F0A);

        // Центральный текст
        String title = "ArchitektVisuals";
        int tw = mc.textRenderer.getWidth(title);
        g.drawTextWithShadow(mc.textRenderer, title,
                (w - tw) / 2, h / 2 - 20, 0xFF7CE38B);

        // Подзаголовок
        String sub = "Loading...";
        int sw = mc.textRenderer.getWidth(sub);
        g.drawTextWithShadow(mc.textRenderer, sub,
                (w - sw) / 2, h / 2 + 4, 0xFFAAAAAA);

        // Полоса загрузки (простая)
        int barW = 200;
        int barH = 6;
        int barX = (w - barW) / 2;
        int barY = h / 2 + 30;

        // Фон полосы
        g.fill(barX, barY, barX + barW, barY + barH, 0xFF202020);
        // Рамка
        g.fill(barX, barY, barX + barW, barY + 1, 0xFF7CE38B);
        g.fill(barX, barY + barH - 1, barX + barW, barY + barH, 0xFF7CE38B);
        g.fill(barX, barY, barX + 1, barY + barH, 0xFF7CE38B);
        g.fill(barX + barW - 1, barY, barX + barW, barY + barH, 0xFF7CE38B);

        // Заполнение (простая анимация от времени)
        long time = System.currentTimeMillis() % 2000;
        float progress = time / 2000f;
        int fillW = (int) (barW * progress);
        g.fill(barX + 2, barY + 2, barX + fillW - 2, barY + barH - 2, 0xFF7CE38B);

        // Нижняя зелёная полоса
        g.fill(0, h - 4, w, h, 0xFF7CE38B);

        ci.cancel();
    }
}
