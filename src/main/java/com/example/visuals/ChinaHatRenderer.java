package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class ChinaHatRenderer {
    
    public static void render(PlayerEntity player, MatrixStack matrices, float tickDelta) {
        if (!Config.chinaHat) return;

        // Проверяем, друг это или ты сам
        if (player != MinecraftClient.getInstance().player && !Config.friends.contains(player.getName().getString())) {
            return;
        }

        // Получаем позицию над головой
        double x = player.getLerpedPos(tickDelta).x;
        double y = player.getLerpedPos(tickDelta).y + player.getHeight() + 0.2;
        double z = player.getLerpedPos(tickDelta).z;

        // Цвет шляпы (из настроек или фиксированный)
        int color = ColorHelper.getArgb(255, 255, 100, 100); // Красный
        float radius = 0.4f;
        float height = 0.5f;
        int segments = 16; // Чем больше, тем ровнее конус

        Vec3d center = new Vec3d(x, y, z);
        
        // Рисуем основание (круг)
        for (int i = 0; i < segments; i++) {
            double angle1 = 2 * Math.PI * i / segments;
            double angle2 = 2 * Math.PI * (i + 1) / segments;
            
            Vec3d p1 = new Vec3d(x + Math.cos(angle1) * radius, y, z + Math.sin(angle1) * radius);
            Vec3d p2 = new Vec3d(x + Math.cos(angle2) * radius, y, z + Math.sin(angle2) * radius);
            
            Gizmos.line(p1, p2, color);
        }

        // Рисуем линии от основания к вершине (конус)
        Vec3d tip = new Vec3d(x, y + height, z);
        for (int i = 0; i < segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            Vec3d basePoint = new Vec3d(x + Math.cos(angle) * radius, y, z + Math.sin(angle) * radius);
            Gizmos.line(basePoint, tip, color);
        }
    }
}
