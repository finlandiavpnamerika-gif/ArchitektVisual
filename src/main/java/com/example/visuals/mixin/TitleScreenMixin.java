package com.example.visuals.mixin;

import com.example.visuals.Config;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    // ВАЖНО: тут "minecraft", а не "visualsmod"!
    private static final Identifier BG = Identifier.of("minecraft", "textures/gui/title/background.png");

    @Inject(method = "render", at = @At("HEAD"))
    private void visuals$drawBackground(DrawContext g, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!Config.customMainMenu) return;

        TitleScreen self = (TitleScreen) (Object) this;
        
        // drawTexture без RenderPipeline (работает в 1.21.11)
        g.drawTexture(
                BG, 
                0, 0, 
                0f, 0f, 
                self.width, self.height, 
                1376, 768 
        );
    }
}
