package com.example.visuals.mixin;

import com.example.visuals.Freelook;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class FreelookCameraMixin {

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    // В Camera.update() setRotation вызывается в нескольких местах (обычный поворот, вид спереди, сон в кровати),
    // и номер нужного вызова меняется от версии к версии. Поэтому ставим свой поворот после КАЖДОГО вызова:
    // последний вызов всегда наш, а смещение камеры назад (move) считается уже по нашему повороту.
    @Inject(method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V", shift = At.Shift.AFTER))
    private void visuals$freelook(CallbackInfo ci) {
        if (!Freelook.overriding()) return;
        Freelook.frame();
        this.setRotation(Freelook.yaw, Freelook.pitch);
    }
}
