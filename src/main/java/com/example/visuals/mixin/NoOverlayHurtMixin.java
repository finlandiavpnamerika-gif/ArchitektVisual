package com.example.visuals.mixin;

import com.example.visuals.Config;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NoOverlay: убирает тряску камеры при получении урона. */
@Mixin(GameRenderer.class)
public class NoOverlayHurtMixin {
    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true, require = 0)
    private void visuals$noHurtShake(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (Config.noOverlay && Config.noHurtShake) ci.cancel();
    }
}
