package com.example.visuals.mixin;

import com.example.visuals.Config;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gl.RenderPipelines;
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
        
        // ВАЖНО: 10 аргументов (RenderPipeline, Identifier, x, y, u, v, w, h, texW, texH)
        g.blit(
                RenderPipelines.GUI_TEXTURED,  // 1. Пайплайн
                BG,                            // 2. Текстура
                0, 0,                          // 3, 4. Позиция на экране (x, y)
                0f, 0f,                        // 5, 6. Откуда брать (u, v)
                self.width, self.height,       // 7, 8. Размер на экране
                1376, 768                      // 9, 10. РЕАЛЬНЫЙ размер картинки (ширина, высота)
        );
    }
}
