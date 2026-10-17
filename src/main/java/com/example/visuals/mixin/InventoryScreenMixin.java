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

    // Твоя картинка с девушкой
    private static final Identifier BG = Identifier.of("visualsmod", "textures/gui/inventory_bg.png");

    // 1. Убираем ванильное затемнение (чтобы не перекрывало нашу картинку)
    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void visuals$removeVanillaBg(DrawContext g, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!Config.customMainMenu) return;
        
        // Рисуем картинку вместо ванильного размытия
        g.drawTexture(
                RenderPipelines.GUI_TEXTURED, 
                BG, 
                0, 0, 
                0f, 0f, 
                ((InventoryScreen)(Object)this).width, 
                ((InventoryScreen)(Object)this).height, 
                1376, 768 
        );
        
        // Отменяем стандартный рендер фона, чтобы он не рисовал своё
        ci.cancel(); 
    }
}
