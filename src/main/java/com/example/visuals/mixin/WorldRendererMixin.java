package com.example.visuals.mixin;

import com.example.visuals.ChinaHatRenderer;
import com.example.visuals.Config;
import com.example.visuals.FuntimeHelper;
import com.example.visuals.HaloRenderer;
import com.example.visuals.JumpCircle;
import com.example.visuals.Particles;
import com.example.visuals.SkyRenderer;
import com.example.visuals.TargetEsp;
import com.example.visuals.TrailRenderer;
import com.example.visuals.WorldEsp;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_4597;
import net.minecraft.class_761;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_761.class)
public class WorldRendererMixin {

    // Вызывается в конце WorldRenderer.render(): рисует China Hat и Trails.
    @Inject(method = "render", at = @At("RETURN"))
    private void visuals$renderChinaHat(CallbackInfo ci) {
        if (!Config.chinaHat && !Config.halo && !Config.trails && !Config.zones && !Config.targetEsp && !Config.skyShader && !Config.particles && !WorldEsp.any()) return;

        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || mc.field_1724 == null) return;

        class_4184 camera = mc.field_1773.method_19418();
        if (camera == null || !camera.method_19332()) return;

        // Матрица вида: только поворот камеры (позиции считаются относительно камеры)
        Matrix4f positionMatrix = new Matrix4f().rotation(camera.method_23767().conjugate(new Quaternionf()));

        class_4597.class_4598 consumers = mc.method_22940().method_23000();
        float tickProgress = mc.method_61966().method_60637(false);

        if (Config.skyShader) SkyRenderer.render(camera, positionMatrix, consumers, tickProgress);   // небо первым
        if (Config.chinaHat) ChinaHatRenderer.render(camera, positionMatrix, consumers, tickProgress);
        if (Config.halo) HaloRenderer.render(camera, positionMatrix, consumers, tickProgress);
        if (Config.trails) TrailRenderer.render(camera, positionMatrix, consumers, tickProgress);
        if (Config.zones) FuntimeHelper.render(camera, positionMatrix, consumers, tickProgress);
        if (Config.targetEsp) TargetEsp.render(camera, positionMatrix, consumers, tickProgress);
        if (Config.jumpCircle) JumpCircle.render(camera, positionMatrix, consumers, tickProgress);
        WorldEsp.render(camera, positionMatrix, consumers, tickProgress);
        Particles.render(camera, positionMatrix, consumers, tickProgress);
    }
}
