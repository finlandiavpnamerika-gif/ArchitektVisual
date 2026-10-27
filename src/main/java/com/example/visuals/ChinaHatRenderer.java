package com.example.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class ChinaHatRenderer {
    
    public static void render(PlayerEntity player, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (!Config.chinaHat) return;
        
        // Проверяем, друг это или ты сам
        if (player != MinecraftClient.getInstance().player && !Config.friends.contains(player.getName().getString())) {
            return;
        }

        matrices.push();
        
        // Поднимаем шляпу на голову
        matrices.translate(0, player.getHeight() + 0.1f, 0);
        
        // Наклоняем шляпу (чтобы она была ровно на голове)
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-player.getYaw()));
        
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getDebugLineStrip(1.0));
        
        float radius = 0.4f;
        float height = 0.5f;
        int segments = 16;
        int color = ColorHelper.getArgb(255, 255, 100, 100); // Красный
        
        // Рисуем основание (круг)
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (2 * Math.PI * i / segments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / segments);
            
            float x1 = MathHelper.cos(angle1) * radius;
            float z1 = MathHelper.sin(angle1) * radius;
            float x2 = MathHelper.cos(angle2) * radius;
            float z2 = MathHelper.sin(angle2) * radius;
            
            buffer.vertex(matrix, x1, 0, z1).color(color);
            buffer.vertex(matrix, x2, 0, z2).color(color);
        }
        
        // Рисуем линии от основания к вершине
        for (int i = 0; i < segments; i++) {
            float angle = (float) (2 * Math.PI * i / segments);
            float x = MathHelper.cos(angle) * radius;
            float z = MathHelper.sin(angle) * radius;
            
            buffer.vertex(matrix, x, 0, z).color(color);
            buffer.vertex(matrix, 0, height, 0).color(color);
        }
        
        matrices.pop();
    }
}
