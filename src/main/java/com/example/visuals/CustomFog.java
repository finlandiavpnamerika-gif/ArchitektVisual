package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;

/**
 * CustomFog: настройки своего тумана (начало, конец, цвет). Подмена значений делается в FogRendererMixin.
 * Под водой, в лаве и при слепоте/тьме остаётся обычный туман.
 */
public final class CustomFog {
    private CustomFog() {}

    public static boolean active() {
        if (!Config.customFog) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return false;
        if (mc.player.isSubmergedInWater() || mc.player.isInLava()) return false;
        return !(mc.player.hasStatusEffect(StatusEffects.BLINDNESS) || mc.player.hasStatusEffect(StatusEffects.DARKNESS));
    }

    /** режим "Без тумана" */
    public static boolean none() { return Config.fogOff && active(); }

    public static final float FAR = 100000f;

    public static float start() { return (float) Math.max(0.0, Config.fogStart); }

    public static float end() { return (float) Math.max(Config.fogEnd, Config.fogStart + 4.0); }

    public static int rgb() {
        return MathHelper.hsvToRgb((float) Config.fogHue, (float) Config.fogSat, (float) Config.fogVal) & 0xFFFFFF;
    }
}
