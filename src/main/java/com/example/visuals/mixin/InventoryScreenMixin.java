package com.example.visuals.mixin;

import com.example.visuals.Config;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {

    private static final Identifier BG = Identifier.of("visualsmod", "textures/gui/inventory_bg.png");

    // Отключаем повторение текстуры (чтобы не было плитки)
    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void visuals$drawInventoryBg(DrawContext g, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!Config.customMainMenu) return;

        InventoryScreen self = (InventoryScreen) (Object) this;
        
        // Размер окна инвентаря (стандарт)
        int invWidth = 176;
        int invHeight = 166;

        // Центрируем
        int x = (self.width - invWidth) / 2;
        int y = (self.height - invHeight) / 2;

        // ВАЖНО: Вместо drawTexture используем drawGuiTexture, 
        // он не повторяет картинку, если она больше или меньше нужного размера
        g.drawGuiTexture(
                BG, 
                x, y, 
                invWidth, invHeight
        );
        
        ci.cancel(); 
    }
}
