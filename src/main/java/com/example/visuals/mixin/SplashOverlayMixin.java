package com.example.visuals.mixin;

import com.example.visuals.Config;
import com.example.visuals.SplashArt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashOverlay.class)
public class SplashOverlayMixin {

    // TAIL: ванильный render выполняется целиком и закрывает оверлей после загрузки,
    // а наш экран рисуется поверх него
    @Inject(method = "render", at = @At("TAIL"))
    private void visuals$renderSplash(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!Config.customMainMenu) return;
        SplashArt.draw(context, MinecraftClient.getInstance());
    }
}
