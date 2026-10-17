package com.example.visuals.mixin;

import com.example.visuals.Config;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Свой фон и логотип в главном меню. */
@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    private static final Identifier BG =
            Identifier.of("visualsmod", "textures/gui/main_menu_bg.png");

    @Inject(method = "render", at = @At("HEAD"))
    private void visuals$drawBackground(DrawContext g, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!Config.customMainMenu) return;

        TitleScreen self = (TitleScreen) (Object) this;
        int w = self.width;
        int h = self.height;

        // Затемняем ванильный фон
        g.fill(0, 0, w, h, 0xC0000000);

        // Пробуем новый API — drawGuiTexture с RenderPipeline
        try {
            g.drawGuiTexture(
                    net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED,
                    BG,
                    0, 0, w, h
            );
        } catch (Throwable t) {
            // Если не сработало — пробуем другой вариант
            try {
                g.drawTexture(
                        net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED,
                        BG,
                        0, 0, 0f, 0f, w, h, w, h
                );
            } catch (Throwable t2) {
                // Фолбэк: просто тёмный фон
                g.fill(0, 0, w, h, 0xFF101010);
            }
        }
    }
}
