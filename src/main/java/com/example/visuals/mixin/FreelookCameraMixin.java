package com.example.visuals.mixin;

import com.example.visuals.Freelook;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Camera.class)
public class FreelookCameraMixin {

    // Первый вызов setRotation в Camera.update() ставит поворот камеры по игроку.
    // Подменяем yaw и pitch на свои, пока работает Freelook.
    @ModifyArg(method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V", ordinal = 0),
            index = 0)
    private float visuals$freelookYaw(float original) {
        if (!Freelook.overriding()) return original;
        Freelook.frame();
        return Freelook.yaw;
    }

    @ModifyArg(method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V", ordinal = 0),
            index = 1)
    private float visuals$freelookPitch(float original) {
        return Freelook.overriding() ? Freelook.pitch : original;
    }
}
