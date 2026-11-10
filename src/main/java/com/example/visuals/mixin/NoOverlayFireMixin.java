package com.example.visuals.mixin;

import com.example.visuals.Config;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NoOverlay: убирает огонь на экране. Обработчик static - работает и для static, и для обычного метода. */
@Mixin(InGameOverlayRenderer.class)
public class NoOverlayFireMixin {
    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true, require = 0)
    private static void visuals$noFire(CallbackInfo ci) {
        if (Config.noOverlay && Config.noFireOverlay) ci.cancel();
    }
}
