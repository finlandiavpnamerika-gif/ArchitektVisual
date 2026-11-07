package com.example.visuals.mixin;

import com.example.visuals.CustomFog;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public class FogRendererMixin {

    private static final String WRITE = "applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V";

    // цвет тумана (он же цвет горизонта)
    @Inject(method = "getFogColor", at = @At("RETURN"), require = 0)
    private void visuals$color(Camera camera, float tickProgress, ClientWorld world, int viewDistance, float skyDarkness,
                               CallbackInfoReturnable<Vector4f> cir) {
        if (!CustomFog.active()) return;
        int rgb = CustomFog.rgb();
        Vector4f c = cir.getReturnValue();
        c.set(((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f, c.w);
    }

    // начало и конец тумана: аргументы environmentalStart (float #0) и environmentalEnd (float #1)
    @ModifyVariable(method = WRITE, at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0)
    private float visuals$start(float original) {
        if (CustomFog.none()) return CustomFog.FAR;
        return CustomFog.active() ? CustomFog.start() : original;
    }

    @ModifyVariable(method = WRITE, at = @At("HEAD"), argsOnly = true, ordinal = 1, require = 0)
    private float visuals$end(float original) {
        if (CustomFog.none()) return CustomFog.FAR + 1000f;
        return CustomFog.active() ? CustomFog.end() : original;
    }

    // "Без тумана": убираем и туман дальности прорисовки, и туман неба/облаков
    @ModifyVariable(method = WRITE, at = @At("HEAD"), argsOnly = true, ordinal = 2, require = 0)
    private float visuals$rdStart(float original) { return CustomFog.none() ? CustomFog.FAR : original; }

    @ModifyVariable(method = WRITE, at = @At("HEAD"), argsOnly = true, ordinal = 3, require = 0)
    private float visuals$rdEnd(float original) { return CustomFog.none() ? CustomFog.FAR + 1000f : original; }

    @ModifyVariable(method = WRITE, at = @At("HEAD"), argsOnly = true, ordinal = 4, require = 0)
    private float visuals$skyEnd(float original) { return CustomFog.none() ? CustomFog.FAR + 1000f : original; }

    @ModifyVariable(method = WRITE, at = @At("HEAD"), argsOnly = true, ordinal = 5, require = 0)
    private float visuals$cloudEnd(float original) { return CustomFog.none() ? CustomFog.FAR + 1000f : original; }
}
