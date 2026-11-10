package com.example.visuals.mixin;

import com.example.visuals.Config;
import net.minecraft.client.render.CloudRenderer;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Custom Clouds: скрыть облака, перекрасить (с сохранением ванильной яркости по времени суток) и сменить высоту. */
@Mixin(CloudRenderer.class)
public class CloudRendererMixin {

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true, require = 0)
    private void visuals$hideClouds(CallbackInfo ci) {
        if (Config.cloudsOn && Config.cloudHide) ci.cancel();
    }

    @ModifyVariable(method = "renderClouds", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0)
    private int visuals$cloudColor(int color) {
        if (!Config.cloudsOn || !Config.cloudTint) return color;
        float h = Config.cloudRainbow ? (System.currentTimeMillis() % 20000L) / 20000f : (float) Config.cloudHue;
        int rgb = MathHelper.hsvToRgb(h, (float) Config.cloudSat, 1f);
        int a = color >>> 24;
        float br = Math.max(Math.max((color >> 16) & 255, (color >> 8) & 255), color & 255) / 255f;
        int r = (int) (((rgb >> 16) & 255) * br), g = (int) (((rgb >> 8) & 255) * br), b = (int) ((rgb & 255) * br);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @ModifyVariable(method = "renderClouds", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0)
    private float visuals$cloudHeight(float cloudHeight) {
        return Config.cloudsOn ? cloudHeight + (float) Config.cloudHeightOffset : cloudHeight;
    }
}
