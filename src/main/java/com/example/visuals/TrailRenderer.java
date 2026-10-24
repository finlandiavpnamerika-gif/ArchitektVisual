package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TrailRenderer {
    private static final ConcurrentHashMap<UUID, List<Vec3d>> TRAILS = new ConcurrentHashMap<>();

    public static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || !Config.trails) {
            TRAILS.clear();
            return;
        }
        
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            
            List<Vec3d> trail = TRAILS.computeIfAbsent(player.getUuid(), k -> new java.util.ArrayList<>());
            Vec3d pos = new Vec3d(player.getX(), player.getY(), player.getZ());
            
            if (trail.isEmpty() || trail.get(trail.size() - 1).squaredDistanceTo(pos) > 0.1) {
                trail.add(pos);
            }
            
            int maxSize = (int) (20 * Config.trailsLength);
            while (trail.size() > maxSize) {
                trail.remove(0);
            }
        }
    }

    public static void render(net.minecraft.client.util.math.MatrixStack matrices, net.minecraft.client.render.VertexConsumerProvider vertexConsumers, double camX, double camY, double camZ) {
        // В 1.21.11 рендер линий сломан, поэтому мы просто спавним частицы
        // Это безопасно и не вызывает крашей
        if (!Config.trails || TRAILS.isEmpty()) return;
        
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return;
        
        int color = Config.COLORS[Config.trailsColor];
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        
        for (List<Vec3d> trail : TRAILS.values()) {
            for (Vec3d pos : trail) {
                // Спавним частицу в позиции трейла (видно только клиенту)
                mc.world.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 0, 0, 0);
            }
        }
    }
}
