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
        
        // Используем getLines() — этот метод точно есть
        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getLines());
        
        float radius = 0.4f;
        float height = 0.3f;
        int segments = 24; // 24 линии по кругу
        
        int color = player == MinecraftClient.getInstance().player 
                ? ColorHelper.getArgb(255, 133, 156, 255)
                : ColorHelper.getArgb(255, 0, 155, 0);
        
        // Рисуем линии от основания к вершине (конус)
        for (int i = 0; i < segments; i++) {
            float angle = (float) (2 * Math.PI * i / segments);
            float x = MathHelper.sin(angle) * radius;
            float z = MathHelper.cos(angle) * radius;
            
            // Линия от вершины к основанию
            buffer.vertex(matrix, 0, height, 0).color(color);
            buffer.vertex(matrix, x, 0, z).color(color);
        }
        
        // Рисуем основание (круг)
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (2 * Math.PI * i / segments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / segments);
            
            float x1 = MathHelper.sin(angle1) * radius;
            float z1 = MathHelper.cos(angle1) * radius;
            float x2 = MathHelper.sin(angle2) * radius;
            float z2 = MathHelper.cos(angle2) * radius;
            
            buffer.vertex(matrix, x1, 0, z1).color(color);
            buffer.vertex(matrix, x2, 0, z2).color(color);
        }
        
        matrices.pop();
    }
}
