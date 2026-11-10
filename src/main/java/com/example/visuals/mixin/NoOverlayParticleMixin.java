package com.example.visuals.mixin;

import com.example.visuals.Config;
import net.minecraft.client.particle.FireworksSparkParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** NoOverlay: убирает частицы тотема бессмертия и фейерверков. */
@Mixin(ParticleManager.class)
public class NoOverlayParticleMixin {

    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void visuals$noParticle(ParticleEffect effect, double x, double y, double z,
                                    double vx, double vy, double vz, CallbackInfoReturnable<Particle> cir) {
        if (!Config.noOverlay) return;
        Identifier id = Registries.PARTICLE_TYPE.getId(effect.getType());
        if (id == null) return;
        String p = id.getPath();
        if (Config.noTotemParticles && p.equals("totem_of_undying")) { cir.setReturnValue(null); return; }
        if (Config.noFireworkParticles && (p.equals("firework") || p.equals("flash"))) cir.setReturnValue(null);
    }

    @Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void visuals$noFireworkObj(Particle particle, CallbackInfo ci) {
        if (Config.noOverlay && Config.noFireworkParticles
                && particle.getClass().getEnclosingClass() == FireworksSparkParticle.class) {
            ci.cancel();
        }
    }
}
