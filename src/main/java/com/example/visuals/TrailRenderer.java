package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrailRenderer {
    private static final Map<UUID, Long> lastSpawn = new HashMap<>();

    public static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null || !Config.trails) return;

        long now = System.currentTimeMillis();

        for (PlayerEntity player : mc.world.getPlayers()) {
            boolean isMoving = player.getVelocity().lengthSquared() > 0.001;
            if (!isMoving) continue;

            if (Config.friends.contains(player.getName().getString()) || player == mc.player) {
                long last = lastSpawn.getOrDefault(player.getUuid(), 0L);
                
                if (now - last > 50) {
                    // Используем particleManager.addParticle - публичный метод
                    mc.particleManager.addParticle(
                            ParticleTypes.END_ROD,
                            player.getX(),
                            player.getY() + 0.1,
                            player.getZ(),
                            0, 0, 0
                    );
                    lastSpawn.put(player.getUuid(), now);
                }
            }
        }
    }
}
