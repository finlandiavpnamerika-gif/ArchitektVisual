package com.example.visuals.mixin;

import com.example.visuals.Config;
import com.example.visuals.FakePlayer;
import net.minecraft.class_2394;
import net.minecraft.class_2960;
import net.minecraft.class_677;
import net.minecraft.class_702;
import net.minecraft.class_703;
import net.minecraft.class_7923;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** NoOverlay: убирает частицы тотема бессмертия и фейерверков. */
@Mixin(class_702.class)
public class NoOverlayParticleMixin {

    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void visuals$noParticle(class_2394 effect, double x, double y, double z,
                                    double vx, double vy, double vz, CallbackInfoReturnable<class_703> cir) {
        if (!Config.noOverlay) return;
        class_2960 id = class_7923.field_41180.method_10221(effect.method_10295());
        if (id == null) return;
        String p = id.method_12832();
        if (Config.noTotemParticles && FakePlayer.popTicks <= 0 && p.equals("totem_of_undying")) { cir.setReturnValue(null); return; }
        if (Config.noFireworkParticles && (p.equals("firework") || p.equals("flash"))) cir.setReturnValue(null);
    }

    @Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void visuals$noFireworkObj(class_703 particle, CallbackInfo ci) {
        if (Config.noOverlay && Config.noFireworkParticles
                && particle.getClass().getEnclosingClass() == class_677.class) {
            ci.cancel();
        }
    }
}
