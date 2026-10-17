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

    /** Рисуем фон и логотип ПЕРЕД ванильным рендером. */
    @Inject(method = "render", at = @At("HEAD"))
    private void visuals$drawBackground(DrawContext g, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!Config.customMainMenu) return;

        TitleScreen self = (TitleScreen) (Object) this;
        int w = self.width;
        int h = self.height;

        // Затемняем ванильный фон
        g.fill(0, 0, w, h, 0xC0000000);

        // Рисуем свою картинку (растягиваем на весь экран)
        g.drawTexture(BG, 0, 0, 0, 0, w, h, w, h);
    }
}
