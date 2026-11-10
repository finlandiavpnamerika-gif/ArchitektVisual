package com.example.visuals.mixin;

import com.example.visuals.Config;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** То же для LivingEntityRenderer, где метод переопределён (игроки рисуются через него). */
@Mixin(LivingEntityRenderer.class)
public class NoVanillaNametagLivingMixin {
    @Inject(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void visuals$noLabel(LivingEntity entity, double squaredDistance, CallbackInfoReturnable<Boolean> cir) {
        if (Config.ntOn && Config.ntHide && entity instanceof PlayerEntity) cir.setReturnValue(false);
    }
}
