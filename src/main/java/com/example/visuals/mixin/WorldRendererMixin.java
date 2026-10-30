package com.example.visuals.mixin;

import com.example.visuals.ChinaHatRenderer;
import com.example.visuals.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    // Вызывается в конце WorldRenderer.render() и рисует China Hat.
    // Аргументы метода не нужны, всё берём из MinecraftClient.
    @Inject(method = "render", at = @At("RETURN"))
    private void visuals$renderChinaHat(CallbackInfo ci) {
        if (!Config.chinaHat) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        Camera camera = mc.gameRenderer.getCamera();
        if (camera == null || !camera.isReady()) return;

        // Матрица вида: только поворот камеры (позиции считаются относительно камеры)
        Matrix4f positionMatrix = new Matrix4f().rotation(camera.getRotation().conjugate(new Quaternionf()));

        VertexConsumerProvider.Immediate consumers = mc.getBufferBuilders().getEntityVertexConsumers();
        float tickProgress = mc.getRenderTickCounter().getTickProgress(false);

        ChinaHatRenderer.render(camera, positionMatrix, consumers, tickProgress);
    }
}
