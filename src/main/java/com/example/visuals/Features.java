package com.example.visuals;

import com.example.visuals.mixin.SimpleOptionAccessor;
import org.lwjgl.glfw.GLFW;

import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_332;
import net.minecraft.class_418;
import net.minecraft.class_7172;
import net.minecraft.class_9779;

public final class Features {
    private Features() {}

    private static double origGamma = -1.0;
    private static int origFov = -1;
    private static int origView = -1;

    public static void tick(class_310 mc) {
        if (mc.field_1690 == null) return;
        class_315 o = mc.field_1690;

        // --- ЛОГИКА МОДУЛЕЙ ---

        // AutoRespawn
        if (mc.field_1755 instanceof class_418 && Config.autoRespawn && mc.field_1724 != null) {
            mc.field_1724.method_7331();
            mc.method_1507(null);
        }

        // Macros
        long window = mc.method_22683().method_4490();
        if (mc.field_1755 == null && mc.field_1724 != null) {
            for (String macro : Config.macros) {
                try {
                    String[] parts = macro.split(":");
                    if (parts.length == 2) {
                        int key = Integer.parseInt(parts[0]);
                        String cmd = parts[1];
                        if (GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS) {
                            if (System.currentTimeMillis() % 500 < 50) {
                                mc.field_1724.field_3944.method_45730(cmd.startsWith("/") ? cmd.substring(1) : cmd);
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        // Fullbright
        class_7172<Double> gamma = o.method_42473();
        if (Config.fullbright) {
            if (origGamma < 0) origGamma = gamma.method_41753();
            if (gamma.method_41753() != Config.gamma) setRawGamma(gamma, Config.gamma);
        } else if (origGamma >= 0) {
            gamma.method_41748(origGamma);
            origGamma = -1.0;
        }

        // Zoom
        class_7172<Integer> fov = o.method_41808();
        boolean zooming = Config.zoom && VisualsClient.zoomKey.method_1434() && mc.field_1755 == null;
        if (zooming) {
            if (origFov < 0) origFov = fov.method_41753();
            int target = (int) Config.zoomFov;
            if (fov.method_41753() != target) fov.method_41748(target);
        } else if (origFov >= 0) {
            fov.method_41748(origFov);
            origFov = -1;
        }

        // Время и Погода
        if (mc.field_1687 != null && Config.customTime) mc.field_1687.method_28104().method_165(Config.timeValue);
        if (mc.field_1687 != null && Config.customWeather) {
            if (Config.weatherType == 0) { mc.field_1687.method_8519(0f); mc.field_1687.method_8496(0f); }
            else if (Config.weatherType == 1) { mc.field_1687.method_8519(1f); mc.field_1687.method_8496(0f); }
            else if (Config.weatherType == 2) { mc.field_1687.method_8519(1f); mc.field_1687.method_8496(1f); }
        }

        // ---- AutoSprint ----
        if (Config.sprint && mc.field_1724 != null && mc.field_1755 == null) {
            boolean fwd = o.field_1894.method_1434();
            boolean any = fwd || o.field_1881.method_1434() || o.field_1913.method_1434() || o.field_1849.method_1434();
            boolean move = Config.sprintAllDirs ? any : fwd;
            if (move && !mc.field_1724.method_5715() && !mc.field_1724.method_6115()) {
                mc.field_1724.method_5728(true);
            }
        }

        // ---- Freelook (зажал клавишу - камера крутится отдельно от игрока) ----
        Freelook.tick(mc);
    }

    @SuppressWarnings("unchecked")
    private static void setRawGamma(class_7172<Double> opt, double v) {
        ((SimpleOptionAccessor<Double>) (Object) opt).visuals$setRawValue(v);
    }

    public static void restore(class_310 mc) {
        try {
            if (origGamma >= 0) mc.field_1690.method_42473().method_41748(origGamma);
            if (origFov >= 0) mc.field_1690.method_41808().method_41748(origFov);
            if (origView >= 0) mc.field_1690.method_42503().method_41748(origView);
        } catch (Exception ignored) {}
        origGamma = -1.0;
        origFov = -1;
        origView = -1;
    }

    public static void hud(class_332 g, class_9779 tick) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null || mc.field_1690.field_1842) return;

        // ===== АДАПТИВНЫЙ ПРИЦЕЛ =====
        if (Config.customCrosshair) {
            int cx = mc.method_22683().method_4486() / 2;
            int cy = mc.method_22683().method_4502() / 2;
            int color = Config.COLORS[Config.crosshairColor];
            int size = Config.crosshairSize;
            int thick = Config.crosshairThickness;

            if (mc.field_1724.method_5624()) size += 4;

            if (Config.crosshairDot) g.method_25294(cx - thick / 2, cy - thick / 2, cx + thick / 2, cy + thick / 2, color);
            g.method_25294(cx - thick / 2, cy - size, cx + thick / 2, cy - thick, color);
            g.method_25294(cx - thick / 2, cy + thick, cx + thick / 2, cy + size, color);
            g.method_25294(cx - size, cy - thick / 2, cx - thick, cy + thick / 2, color);
            g.method_25294(cx + thick, cy - thick / 2, cx + size, cy + thick / 2, color);
        }

        // ===== PREDICTIONS (траектория + время) =====
        Predictions.render(g, mc, tick.method_60637(false));

        // ===== HUD: Info, ArrayList, Armor, Keystrokes (перетаскиваются мышью при открытом чате) =====
        HudManager.render(g, mc);
    }
}
