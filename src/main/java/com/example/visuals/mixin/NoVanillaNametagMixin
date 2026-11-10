package com.example.visuals.mixin;

import com.example.visuals.Config;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Nametags+: прячет ванильные подписи игроков, когда включены свои. */
@Mixin(EntityRenderer.class)
public class NoVanillaNametagMixin {
    @Inject(method = "hasLabel(Lnet/minecraft/entity/Entity;D)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void visuals$noLabel(Entity entity, double squaredDistance, CallbackInfoReturnable<Boolean> cir) {
        if (Config.ntOn && Config.ntHide && entity instanceof PlayerEntity) cir.setReturnValue(false);
    }
}
