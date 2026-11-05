package com.example.visuals;

import com.example.visuals.mixin.SimpleOptionAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.RenderTickCounter;
import org.lwjgl.glfw.GLFW;

public final class Features {
    private Features() {}

    private static double origGamma = -1.0;
    private static int origFov = -1;
    private static int origView = -1;

    public static void tick(MinecraftClient mc) {
        if (mc.options == null) return;
        GameOptions o = mc.options;

        // --- ЛОГИКА МОДУЛЕЙ ---

        // AutoRespawn
        if (mc.currentScreen instanceof DeathScreen && Config.autoRespawn && mc.player != null) {
            mc.player.requestRespawn();
            mc.setScreen(null);
        }

        // Fullbright
        SimpleOption<Double> gamma = o.getGamma();
        if (Config.fullbright) {
            if (origGamma < 0) origGamma = gamma.getValue();
            if (gamma.getValue() != Config.gamma) setRawGamma(gamma, Config.gamma);
        } else if (origGamma >= 0) {
            gamma.setValue(origGamma);
            origGamma = -1.0;
        }

        // Zoom
        SimpleOption<Integer> fov = o.getFov();
        boolean zooming = Config.zoom && VisualsClient.zoomKey.isPressed() && mc.currentScreen == null;
        if (zooming) {
            if (origFov < 0) origFov = fov.getValue();
            int target = (int) Config.zoomFov;
            if (fov.getValue() != target) fov.setValue(target);
        } else if (origFov >= 0) {
            fov.setValue(origFov);
            origFov = -1;
        }

        // Время и Погода
        if (mc.world != null && Config.customTime) mc.world.getLevelProperties().setTimeOfDay(Config.timeValue);
        if (mc.world != null && Config.customWeather) {
            if (Config.weatherType == 0) { mc.world.setRainGradient(0f); mc.world.setThunderGradient(0f); }
            else if (Config.weatherType == 1) { mc.world.setRainGradient(1f); mc.world.setThunderGradient(0f); }
            else if (Config.weatherType == 2) { mc.world.setRainGradient(1f); mc.world.setThunderGradient(1f); }
        }

        // ---- AutoSprint ----
        if (Config.sprint && mc.player != null && mc.currentScreen == null) {
            boolean fwd = o.forwardKey.isPressed();
            boolean any = fwd || o.backKey.isPressed() || o.leftKey.isPressed() || o.rightKey.isPressed();
            boolean move = Config.sprintAllDirs ? any : fwd;
            if (move && !mc.player.isSneaking() && !mc.player.isUsingItem()) {
                mc.player.setSprinting(true);
            }
        }

        // ---- Freelook (зажал клавишу - камера крутится отдельно от игрока) ----
        Freelook.tick(mc);
    }

    @SuppressWarnings("unchecked")
    private static void setRawGamma(SimpleOption<Double> opt, double v) {
        ((SimpleOptionAccessor<Double>) (Object) opt).visuals$setRawValue(v);
    }

    public static void restore(MinecraftClient mc) {
        try {
            if (origGamma >= 0) mc.options.getGamma().setValue(origGamma);
            if (origFov >= 0) mc.options.getFov().setValue(origFov);
            if (origView >= 0) mc.options.getViewDistance().setValue(origView);
        } catch (Exception ignored) {}
        origGamma = -1.0;
        origFov = -1;
        origView = -1;
    }

    public static void hud(DrawContext g, RenderTickCounter tick) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;

        // ===== АДАПТИВНЫЙ ПРИЦЕЛ =====
        if (Config.customCrosshair) {
            int cx = mc.getWindow().getScaledWidth() / 2;
            int cy = mc.getWindow().getScaledHeight() / 2;
            int color = Config.COLORS[Config.crosshairColor];
            int size = Config.crosshairSize;
            int thick = Config.crosshairThickness;

            if (mc.player.isSprinting()) size += 4;

            if (Config.crosshairDot) g.fill(cx - thick / 2, cy - thick / 2, cx + thick / 2, cy + thick / 2, color);
            g.fill(cx - thick / 2, cy - size, cx + thick / 2, cy - thick, color);
            g.fill(cx - thick / 2, cy + thick, cx + thick / 2, cy + size, color);
            g.fill(cx - size, cy - thick / 2, cx - thick, cy + thick / 2, color);
            g.fill(cx + thick, cy - thick / 2, cx + size, cy + thick / 2, color);
        }

        // ===== PREDICTIONS (траектория + время) =====
        Predictions.render(g, mc, tick.getTickProgress(false));

        // ===== HUD: Info, ArrayList, Armor, Keystrokes (перетаскиваются мышью при открытом чате) =====
        HudManager.render(g, mc);
    }
}
