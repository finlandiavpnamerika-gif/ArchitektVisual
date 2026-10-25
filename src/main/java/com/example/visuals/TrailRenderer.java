package com.example.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

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

    // Рисуем линии через BufferBuilder (стандартный метод, работает в 1.21.11)
    public static void render(MatrixStack matrices, double camX, double camY, double camZ) {
        if (!Config.trails || TRAILS.isEmpty()) return;

        int color = Config.COLORS[Config.trailsColor];
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = 1.0f;

        // Стандартный BufferBuilder для линий
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        for (List<Vec3d> trail : TRAILS.values()) {
            for (int i = 0; i < trail.size() - 1; i++) {
                Vec3d p1 = trail.get(i);
                Vec3d p2 = trail.get(i + 1);

                // Координаты относительно камеры
                float x1 = (float) (p1.x - camX);
                float y1 = (float) (p1.y - camY);
                float z1 = (float) (p1.z - camZ);
                float x2 = (float) (p2.x - camX);
                float y2 = (float) (p2.y - camY);
                float z2 = (float) (p2.z - camZ);

                buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a);
                buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a);
            }
        }

        // Завершаем и рисуем
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }
}
