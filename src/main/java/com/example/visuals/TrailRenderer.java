package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrailRenderer {
    // Храним время последнего спавна частицы для каждого игрока, чтобы не спамить
    private static final Map<UUID, Long> lastSpawn = new HashMap<>();

    public static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || !Config.trails) return;

        long now = System.currentTimeMillis();

        for (PlayerEntity player : mc.world.getPlayers()) {
            // Проверяем, движется ли игрок (чтобы частицы не спамились, когда он стоит)
            boolean isMoving = player.getVelocity().lengthSquared() > 0.001;
            if (!isMoving) continue;

            // Если это ты или твой друг
            if (Config.friends.contains(player.getName().getString()) || player == mc.player) {
                long last = lastSpawn.getOrDefault(player.getUuid(), 0L);
                
                // Спавним частицу каждые 50 мс (20 раз в секунду)
                if (now - last > 50) {
                    mc.world.addParticle(
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
