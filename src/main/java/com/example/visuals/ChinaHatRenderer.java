package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class ChinaHatRenderer {

    // Текстура для шляпы (замени на свою, если нужно)
    private static final Identifier HAT_TEXTURE = Identifier.of("minecraft", "textures/misc/white.png");

    public static void render(PlayerEntity player, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (!Config.chinaHat) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        // Проверка на друга
        boolean isFriend = Config.friends.contains(player.getName().getString());
        if (player != mc.player && !isFriend) return;

        // Не рисуем на себе в первом лице
        if (player == mc.player && mc.options.getPerspective().isFirstPerson()) return;

        matrices.push();

        // Позиционируем над головой
        double heightOffset = player.getInventory().getStack(39).isEmpty() ? 0.48 : 0.45;
        matrices.translate(0.0, player.getHeight() + (float) heightOffset, 0.0);

        // Повороты из байткода (Y 180, X 90)
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // Получаем VertexConsumer. Используем RenderLayer.getEntitySolid с текстурой
        // (В 1.21.11 этот метод существует, но требует Identifier)
        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntitySolid(HAT_TEXTURE));

        // Определение цвета
        int color;
        if (player == mc.player) {
            color = Config.chinaHatColor;
        } else if (isFriend) {
            color = 0xFF009B00; // Зеленый для друзей
        } else {
            matrices.pop();
            return;
        }

        // Рисуем простой квадрат (спрайт) вместо сложного конуса
        float size = 0.5f;
        float y = 0.0f;

        // Вершины квадрата
        vertex(buffer, matrix, -size, y, -size, color);
        vertex(buffer, matrix, -size, y,  size, color);
        vertex(buffer, matrix,  size, y,  size, color);
        vertex(buffer, matrix,  size, y, -size, color);

        matrices.pop();

        // Flush (если это Immediate провайдер)
        if (vertexConsumers instanceof VertexConsumerProvider.Immediate immediate) {
            immediate.draw();
        }
    }

    private static void vertex(VertexConsumer buffer, Matrix4f matrix, float x, float y, float z, int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        buffer.vertex(matrix, x, y, z).color(r, g, b, a);
    }
}
