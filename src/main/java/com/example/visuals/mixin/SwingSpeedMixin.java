package com.example.visuals.mixin;

import com.example.visuals.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Swing Animation: меняет длительность взмаха руки (скорость анимации удара). Только визуально. */
@Mixin(LivingEntity.class)
public class SwingSpeedMixin {
    @Inject(method = "getHandSwingDuration", at = @At("RETURN"), cancellable = true, require = 0)
    private void visuals$swingSpeed(CallbackInfoReturnable<Integer> cir) {
        if (!Config.swingAnim) return;
        LivingEntity self = (LivingEntity) (Object) this;
        if (Config.swingSelfOnly && self != MinecraftClient.getInstance().player) return;
        int base = cir.getReturnValue();
        cir.setReturnValue(Math.max(1, Math.round(base / (float) Math.max(0.1, Config.swingSpeed))));
    }
}
