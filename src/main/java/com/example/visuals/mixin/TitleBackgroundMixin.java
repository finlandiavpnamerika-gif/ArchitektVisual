package com.example.visuals.mixin;

import com.example.visuals.Config;
import com.example.visuals.RainyScene;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class TitleBackgroundMixin {

    // Подменяем ванильную панораму на нашу сцену, но только на главном меню.
    // require = 0: если цель не найдётся, игра не упадёт, фон просто останется ванильным.
    @Inject(method = "renderPanoramaBackground", at = @At("HEAD"), cancellable = true, require = 0)
    private void visuals$rainyBackground(DrawContext context, float deltaTicks, CallbackInfo ci) {
        if (!Config.customMainMenu) return;
        if (!((Object) this instanceof TitleScreen)) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        RainyScene.drawScene(context, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
        ci.cancel();
    }
}
