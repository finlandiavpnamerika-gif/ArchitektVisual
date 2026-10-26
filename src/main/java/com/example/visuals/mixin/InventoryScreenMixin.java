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
        
        // Классический размер окна инвентаря в Minecraft
        int invWidth = 176;
        int invHeight = 166;

        // Центрируем ровно по экрану
        int x = (self.width - invWidth) / 2;
        int y = (self.height - invHeight) / 2;

        // Рисуем картинку. 1376x768 - это реальный размер твоего файла.
        g.drawTexture(
                RenderPipelines.GUI_TEXTURED, 
                BG, 
                x, y, 
                0f, 0f, 
                invWidth, invHeight, 
                1376, 768 
        );
    }
}
