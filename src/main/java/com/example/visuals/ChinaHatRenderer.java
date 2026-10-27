package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class ChinaHatRenderer {
    
    public static void render(PlayerEntity player, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (!Config.chinaHat) return;
        
        // Проверяем, друг это или ты сам
        boolean isFriend = Config.friends.contains(player.getName().getString());
        if (player != MinecraftClient.getInstance().player && !isFriend) {
            return;
        }

        matrices.push();
        
        // Вращаем матрицу так же, как голову
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-player.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(player.getPitch()));
        
        // Смещаем наверх (примерная высота головы)
        matrices.translate(0, -0.45f, 0);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getDebugTriangleFan());
        
        float radius = 0.4f;
        float height = 0.3f;
        int segments = 360; // Как в их коде (360 шагов по 1 градусу)
        
        int color = player == MinecraftClient.getInstance().player 
                ? ColorHelper.getArgb(255, 133, 156, 255) // Синий для себя
                : ColorHelper.getArgb(255, 0, 155, 0);   // Зеленый для друзей
        
        // 1) Рисуем конус
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (2 * Math.PI * i / segments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / segments);
            
            float x1 = MathHelper.sin(angle1) * radius;
            float z1 = MathHelper.cos(angle1) * radius;
            float x2 = MathHelper.sin(angle2) * radius;
            float z2 = MathHelper.cos(angle2) * radius;
            
            // Боковая поверхность
            buffer.vertex(matrix, 0, height, 0).color(color);
            buffer.vertex(matrix, x1, 0, z1).color(color);
            buffer.vertex(matrix, x2, 0, z2).color(color);
        }
        
        // 2) Рисуем обод (кольцо)
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (2 * Math.PI * i / segments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / segments);
            
            float x1 = MathHelper.sin(angle1) * (radius + 0.01f);
            float z1 = MathHelper.cos(angle1) * (radius + 0.01f);
            float x2 = MathHelper.sin(angle2) * (radius + 0.01f);
            float z2 = MathHelper.cos(angle2) * (radius + 0.01f);
            
            float x3 = MathHelper.sin(angle1) * (radius - 0.01f);
            float z3 = MathHelper.cos(angle1) * (radius - 0.01f);
            float x4 = MathHelper.sin(angle2) * (radius - 0.01f);
            float z4 = MathHelper.cos(angle2) * (radius - 0.01f);
            
            buffer.vertex(matrix, x1, 0, z1).color(color);
            buffer.vertex(matrix, x2, 0, z2).color(color);
            buffer.vertex(matrix, x3, 0, z3).color(color);
            
            buffer.vertex(matrix, x2, 0, z2).color(color);
            buffer.vertex(matrix, x4, 0, z4).color(color);
            buffer.vertex(matrix, x3, 0, z3).color(color);
        }
        
        matrices.pop();
    }
}
