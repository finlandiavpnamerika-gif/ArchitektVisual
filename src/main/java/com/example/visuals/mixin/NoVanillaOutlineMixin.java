package com.example.visuals.mixin;

import com.example.visuals.Config;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Block Outline: прячет ванильную рамку блока, когда включена своя. */
@Mixin(WorldRenderer.class)
public class NoVanillaOutlineMixin {
    @Inject(method = "renderTargetBlockOutline", at = @At("HEAD"), cancellable = true, require = 0)
    private void visuals$hideOutline(CallbackInfo ci) {
        if (Config.bout && Config.boHideVanilla) ci.cancel();
    }
}
