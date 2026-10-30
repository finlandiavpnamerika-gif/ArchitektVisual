package com.example.visuals.mixin;

import com.example.visuals.Config;
import com.example.visuals.MainMenuUI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin() {
        super(null);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void visuals$init(CallbackInfo ci) {
        if (!Config.customMainMenu) return;
        this.clearChildren();
        MainMenuUI.onOpen(MinecraftClient.getInstance());
    }

    // Заменяем весь ванильный render: логотип, сплэш и копирайт больше не рисуются.
    // Фон (панорама, ваш миксин на renderPanoramaBackground) рисуется через renderBackground.
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void visuals$render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!Config.customMainMenu) return;
        ci.cancel();
        this.renderBackground(context, mouseX, mouseY, delta);
        MainMenuUI.render(context, MinecraftClient.getInstance(), this, this.textRenderer, mouseX, mouseY);
    }
}
