package com.example.visuals.mixin;

import com.example.visuals.Config;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    private static final Identifier BG = Identifier.of("visualsmod", "textures/gui/main_menu_bg.png");

    @Inject(method = "render", at = @At("HEAD"))
    private void visuals$drawBackground(DrawContext g, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!Config.customMainMenu) return;

        TitleScreen self = (TitleScreen) (Object) this;
        
        // В 1.21.11: blit(RenderPipeline, Identifier, x, y, u, v, width, height, texWidth, texHeight)
        g.blit(
                RenderPipelines.GUI_TEXTURED, 
                BG, 
                0, 0, 
                0f, 0f, 
                self.width, self.height, 
                1376, 768 
        );
    }
}
