package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing; // Новый класс Gizmos

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TrailRenderer {
    private static final Map<UUID, List<Vec3d>> TRAILS = new ConcurrentHashMap<>();
    private static final int MAX_POINTS = 60;

    public static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null || !Config.trails) return;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (Config.friends.contains(player.getName().getString()) || player == mc.player) {
                List<Vec3d> trail = TRAILS.computeIfAbsent(player.getUuid(), k -> new ArrayList<>());
                Vec3d pos = new Vec3d(player.getX(), player.getY() + 0.1, player.getZ());
                
                if (trail.isEmpty() || trail.get(trail.size() - 1).squaredDistanceTo(pos) > 0.01) {
                    trail.add(pos);
                }
                while (trail.size() > MAX_POINTS) trail.remove(0);
            }
        }
    }

    // Вызывается из события рендера
    public static void render() {
        if (!Config.trails || TRAILS.isEmpty()) return;

        int color = Config.COLORS[Config.trailsColor];
        
        for (List<Vec3d> trail : TRAILS.values()) {
            for (int i = 0; i < trail.size() - 1; i++) {
                Vec3d p1 = trail.get(i);
                Vec3d p2 = trail.get(i + 1);

                // GizmoDrawing.line(start, end, color) - самый простой способ
                GizmoDrawing.line(p1, p2, color);
            }
        }
    }
}
