package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.SimpleParticleType; // ПРАВИЛЬНЫЙ КЛАСС
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

        // Используем SimpleParticleType (бывший DefaultParticleType)
        SimpleParticleType particle;
        switch (Config.trailsColor) {
            case 0: particle = ParticleTypes.DRAGON_BREATH; break; // Фиолетовый
            case 1: particle = ParticleTypes.ELECTRIC_SPARK; break; // Голубой
            case 2: particle = ParticleTypes.HAPPY_VILLAGER; break; // Зелёный
            case 3: particle = ParticleTypes.FIREWORK; break;       // Жёлтый
            case 4: particle = ParticleTypes.CRIT; break;           // Красный
            default: particle = ParticleTypes.END_ROD; break;
        }

        for (PlayerEntity player : mc.world.getPlayers()) {
            boolean isMoving = player.getVelocity().lengthSquared() > 0.001;
            if (!isMoving) continue;

            if (Config.friends.contains(player.getName().getString()) || player == mc.player) {
                long last = lastSpawn.getOrDefault(player.getUuid(), 0L);
                
                // Спавним каждые 5 мс
                if (now - last > 5) {
                    // Заполняем весь рост (от 0 до 1.8 блока)
                    for (int i = 0; i < 6; i++) {
                        double yOffset = Math.random() * 1.8;
                        
                        mc.particleManager.addParticle(
                                particle,
                                player.getX() + (Math.random() - 0.5) * 0.3,
                                player.getY() + yOffset,
                                player.getZ() + (Math.random() - 0.5) * 0.3,
                                0, 0, 0
                        );
                    }
                    lastSpawn.put(player.getUuid(), now);
                }
            }
        }
    }
}
