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

    @Inject(method = "render", at = @At("HEAD"))
    private void visuals$drawInventoryBg(DrawContext g, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!Config.customMainMenu) return;

        InventoryScreen self = (InventoryScreen) (Object) this;
        
        // Размеры картинки совпадают с размером инвентаря
        int invWidth = 176;
        int invHeight = 166;

        // Центрируем на экране
        int x = (self.width - invWidth) / 2;
        int y = (self.height - invHeight) / 2;

        // Рисуем картинку ровно 176x166. 
        // Последние два числа (176, 166) — это РЕАЛЬНЫЙ размер твоей картинки.
        g.drawTexture(
                RenderPipelines.GUI_TEXTURED, 
                BG, 
                x, y, 
                0f, 0f, 
                invWidth, invHeight, 
                176, 166 
        );
    }
}
