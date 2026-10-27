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
    
    @Inject(method = "render", at = @At("TAIL"))
    private void visuals$renderShulker(DrawContext g, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ShulkerPreview.render(g, (HandledScreen<?>) (Object) this, mouseX, mouseY);
    }
}
