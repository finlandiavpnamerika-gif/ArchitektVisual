package com.example.visuals.mixin;

import com.example.visuals.ShulkerPreview;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {

    // Отрисовка превью шалкера
    @Inject(method = "render", at = @At("TAIL"))
    private void visuals$renderShulker(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ShulkerPreview.render(context, (HandledScreen<?>) (Object) this, mouseX, mouseY);
    }
}
