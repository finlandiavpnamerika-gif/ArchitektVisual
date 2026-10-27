package com.example.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class ChinaHatRenderer {
    
    // Создаем свой пайплайн для линий (обходной путь для 1.21.11)
    private static final RenderPipeline DEBUG_LINES_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
                    .withLocation("pipeline/visuals_debug_lines")
                    .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.DEBUG_LINES)
                    .build()
    );
    
    private static final RenderLayer LINES_LAYER = RenderLayer.of(
            "visuals_lines",
            1536,
            DEBUG_LINES_PIPELINE,
            RenderLayer.MultiPhaseParameters.builder()
                    .lineWidth(new RenderPhase.LineWidth(java.util.OptionalDouble.of(1.0)))
                    .build(false)
    );

    public static void render(PlayerEntity player, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (!Config.chinaHat) return;
        
        boolean isFriend = Config.friends.contains(player.getName().getString());
        if (player != MinecraftClient.getInstance().player && !isFriend) {
            return;
        }

        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-player.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(player.getPitch()));
        matrices.translate(0, -0.45f, 0);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        
        // Инициализируем BufferBuilder через Tesselator (как в документации)
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        
        float radius = 0.4f;
        float height = 0.3f;
        int segments = 24;
        
        int color = player == MinecraftClient.getInstance().player 
                ? ColorHelper.getArgb(255, 133, 156, 255)
                : ColorHelper.getArgb(255, 0, 155, 0);
        
        // Рисуем линии от основания к вершине
        for (int i = 0; i < segments; i++) {
            float angle = (float) (2 * Math.PI * i / segments);
            float x = MathHelper.sin(angle) * radius;
            float z = MathHelper.cos(angle) * radius;
            
            buffer.vertex(matrix, 0, height, 0).color(color);
            buffer.vertex(matrix, x, 0, z).color(color);
        }
        
        // Рисуем основание
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
        
        // Завершаем и рисуем через наш RenderLayer (вместо BufferRenderer)
        LINES_LAYER.draw(buffer.end());
        
        matrices.pop();
    }
}
