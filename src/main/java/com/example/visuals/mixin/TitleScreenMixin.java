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

    // Путь к картинке внутри мода (внутренний ресурспак)
    private static final Identifier BG = Identifier.of("minecraft", "textures/gui/title/background.png");

    @Inject(method = "render", at = @At("HEAD"))
    private void visuals$drawBackground(DrawContext g, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!Config.customMainMenu) return;

        TitleScreen self = (TitleScreen) (Object) this;
        
        // Правильный метод для 1.21.11: blit + RenderPipelines
        g.blit(
                RenderPipelines.GUI_TEXTURED, 
                BG, 
                0, 0,           // x, y
                0f, 0f,         // u, v
                self.width, self.height, // width, height
                1376, 768       // textureWidth, textureHeight
        );
    }
}
