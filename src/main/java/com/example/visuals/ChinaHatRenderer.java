package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class ChinaHatRenderer {

    public static void render(PlayerEntity player, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (!Config.chinaHat) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        boolean isFriend = Config.friends.contains(player.getName().getString());
        if (player != mc.player && !isFriend) return;

        // Пропускаем рендер на себе в первом лице (мешает обзору)
        if (player == mc.player && mc.options.getPerspective().isFirstPerson()) return;

        matrices.push();

        // === Позиционирование (как в байткоде: method_22703 у ModelPart головы) ===
        // Байткод использует modelPart головы, но мы упрощаем: ставим над хитбоксом
        double heightOffset = player.getInventory().getStack(39).isEmpty() ? 0.48 : 0.45;
        matrices.translate(0.0, player.getHeight() + (float) heightOffset, 0.0);

        // === Повороты из байткода (строки 143-168) ===
        // POSITIVE_Y.rotationDegrees(180.0f) → разворот на 180
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
        // POSITIVE_X.rotationDegrees(90.0f) → наклон
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // === Получаем VertexConsumer через RenderLayer.getEntitySolid ===
        // Байткод: class_1921.method_49042() возвращает RenderLayer.getEntitySolid(texture)
        // Для шляпы используем текстуру скина игрока
        Identifier skinTexture = mc.getPlayerSkinProvider().getSkinTextures(player.getUuid()).texture();
        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntitySolid(skinTexture));

        // Цвет: свой игрок — из конфига, друг — зелёный, остальные — прозрачные (не рендерим)
        int color;
        if (player == mc.player) {
            color = Config.chinaHatColor; // ARGB int из твоего конфига
        } else if (isFriend) {
            color = 0xFF009B00; // зелёный для друзей
        } else {
            matrices.pop();
            return;
        }

        long time = System.currentTimeMillis();
        float radius = (float) (player.getBoundingBox().maxX - player.getBoundingBox().minX) / 2.0f;

        // === Часть 1: Верхний конус (строки 251-414 байткода) ===
        // Цикл 0..359 с шагом 8 градусов, рисует треугольники от вершины к основанию
        for (int i = 0; i < 360; i++) {
            float angle1 = (float) Math.toRadians(i * 8.0f);
            float angle2 = (float) Math.toRadians((i + 1) * 8.0f);

            int gradientColor = applyGradient(color, i * 8, time);

            float x1 = MathHelper.sin(angle1) * radius;
            float z1 = MathHelper.cos(angle1) * radius;
            float x2 = MathHelper.sin(angle2) * radius;
            float z2 = MathHelper.cos(angle2) * radius;

            // Треугольник 1: вершина -> точка на окружности -> следующая точка
            vertex(buffer, matrix, x1, 0.0f, z1, gradientColor);
            vertex(buffer, matrix, x2, 0.0f, z2, gradientColor);
            vertex(buffer, matrix, 0.0f, 0.3f, 0.0f, color); // вершина конуса

            // Дублируем для заполнения (как в байткоде)
            vertex(buffer, matrix, 0.0f, 0.3f, 0.0f, color);
        }

        // === Часть 2: Ободок (строки 414-697 байткода) ===
        // Второй цикл: 1..180, создаёт полосу вокруг основания
        float outerRadius = radius + 0.01f;
        float innerRadius = radius - 0.01f;

        for (int i = 1; i <= 180; i++) {
            float angle = (float) Math.toRadians(i * 8.0f);
            int bandColor = applyGradient(color, (i - 1) * 8, time);

            float cosA = MathHelper.cos(angle);
            float sinA = MathHelper.sin(angle);

            float outerX = sinA * outerRadius;
            float outerZ = cosA * outerRadius;
            float innerX = sinA * innerRadius;
            float innerZ = cosA * innerRadius;

            // Рисуем quad (4 вершины) для полосы
            vertex(buffer, matrix, outerX, -0.01f, outerZ, bandColor);
            vertex(buffer, matrix, outerX, 0.0f, outerZ, bandColor);
            vertex(buffer, matrix, innerX, 0.0f, innerZ, bandColor);
            vertex(buffer, matrix, innerX, -0.01f, innerZ, bandColor);
        }

        matrices.pop();

        // === Flush (строки 702-728 байткода) ===
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

    private static int applyGradient(int baseColor, int angle, long time) {
        // Упрощённая версия GradientUtil.a — пульсация альфы
        int a = (baseColor >> 24) & 0xFF;
        int r = (baseColor >> 16) & 0xFF;
        int g = (baseColor >> 8) & 0xFF;
        int b = baseColor & 0xFF;

        float pulse = 0.75f + 0.25f * MathHelper.sin((time % 2000) / 2000.0f * (float) Math.PI * 2.0f + angle * 0.01f);
        int newAlpha = (int) (a * pulse);

        return (newAlpha << 24) | (r << 16) | (g << 8) | b;
    }
}
