package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.SimpleParticleType;
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

        SimpleParticleType particle = ParticleTypes.END_ROD;
        int color = Config.trailsColor;
        if (color == 0) particle = ParticleTypes.WITCH;
        else if (color == 1) particle = ParticleTypes.ELECTRIC_SPARK;
        else if (color == 2) particle = ParticleTypes.HAPPY_VILLAGER; // Если выберешь зелёный, будет много частиц
        else if (color == 3) particle = ParticleTypes.FIREWORK;
        else if (color == 4) particle = ParticleTypes.CRIT;

        for (PlayerEntity player : mc.world.getPlayers()) {
            // Проверка на движение (если игрок не двигается, пропускаем)
            boolean isMoving = player.prevX != player.getX() || player.prevZ != player.getZ() || player.prevY != player.getY();
            
            if (!isMoving) {
                lastSpawn.remove(player.getUuid());
                continue;
            }

            if (Config.friends.contains(player.getName().getString()) || player == mc.player) {
                long last = lastSpawn.getOrDefault(player.getUuid(), 0L);
                
                // Спавним раз в 100 мс (очень редко, чтобы не лагало)
                if (now - last > 100) {
                    mc.particleManager.addParticle(
                            particle,
                            player.getX() + (Math.random() - 0.5) * 0.2,
                            player.getY() + 0.5 + (Math.random() * 1.0),
                            player.getZ() + (Math.random() - 0.5) * 0.2,
                            0, 0, 0
                    );
                    lastSpawn.put(player.getUuid(), now);
                }
            }
        }
    }
}
