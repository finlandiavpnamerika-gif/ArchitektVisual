package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TrailRenderer {
    // Храним последние позиции игроков
    private static final ConcurrentHashMap<UUID, List<Vec3d>> TRAILS = new ConcurrentHashMap<>();

    public static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || !Config.trails) {
            TRAILS.clear();
            return;
        }
        
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue; // Можно рисовать и за собой, если убрать эту строку
            
            List<Vec3d> trail = TRAILS.computeIfAbsent(player.getUuid(), k -> new ArrayList<>());
            Vec3d pos = player.getPos();
            
            if (trail.isEmpty() || trail.get(trail.size() - 1).squaredDistanceTo(pos) > 0.01) {
                trail.add(pos);
            }
            
            // Ограничиваем длину трейла
            int maxSize = (int) (20 * Config.trailsLength);
            while (trail.size() > maxSize) {
                trail.remove(0);
            }
        }
    }

    public static void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double camX, double camY, double camZ) {
        if (!Config.trails || TRAILS.isEmpty()) return;
        
        int color = Config.COLORS[Config.trailsColor];
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        
        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getLines());
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        
        for (List<Vec3d> trail : TRAILS.values()) {
            for (int i = 0; i < trail.size() - 1; i++) {
                Vec3d p1 = trail.get(i);
                Vec3d p2 = trail.get(i + 1);
                
                // Вычисляем альфу (прозрачность к концу)
                float alpha = (float) i / trail.size();
                int a = (int)(alpha * 255) << 24;
                
                buffer.vertex(matrix, (float)(p1.x - camX), (float)(p1.y - camY), (float)(p1.z - camZ))
                      .color(a | (int)(r*255) << 16 | (int)(g*255) << 8 | (int)(b*255));
                buffer.vertex(matrix, (float)(p2.x - camX), (float)(p2.y - camY), (float)(p2.z - camZ))
                      .color(a | (int)(r*255) << 16 | (int)(g*255) << 8 | (int)(b*255));
            }
        }
    }
}
