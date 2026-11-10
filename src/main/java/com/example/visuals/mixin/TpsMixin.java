package com.example.visuals.mixin;

import com.example.visuals.Tps;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Считает TPS по пакетам обновления времени мира. */
@Mixin(ClientPlayNetworkHandler.class)
public class TpsMixin {
    @Inject(method = "onWorldTimeUpdate", at = @At("HEAD"), require = 0)
    private void visuals$tps(WorldTimeUpdateS2CPacket packet, CallbackInfo ci) {
        Tps.onPacket();
    }
}
