package com.example.visuals.mixin;

import com.example.visuals.Config;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Свой фон инвентаря в стиле ArchitektVisuals. */
@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void visuals$drawInventoryBg(DrawContext g, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!Config.customMainMenu) return;

        InventoryScreen self = (InventoryScreen) (Object) this;
        int w = self.width;
        int h = self.height;

        // Тёмный полупрозрачный фон с зелёным оттенком
        g.fill(0, 0, w, h, 0xA0050A05);

        // Верхняя зелёная полоса
        g.fill(0, 0, w, 2, 0xFF7CE38B);
        // Нижняя зелёная полоса
        g.fill(0, h - 2, w, h, 0xFF7CE38B);
        // Левая зелёная полоса
        g.fill(0, 0, 2, h, 0xFF7CE38B);
        // Правая зелёная полоса
        g.fill(w - 2, 0, w, h, 0xFF7CE38B);
    }
}
