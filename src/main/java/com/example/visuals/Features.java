package com.example.visuals;

import com.example.visuals.mixin.SimpleOptionAccessor;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_1293;
import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_418;
import net.minecraft.class_5498;
import net.minecraft.class_640;
import net.minecraft.class_7172;
import net.minecraft.class_9779;

public final class Features {
    private Features() {}

    private static double origGamma = -1.0;
    private static int origFov = -1;
    private static int origView = -1;
    private static boolean freelookApplied = false;

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

        // ---- Freelook (третье лицо, пока модуль включён) ----
        if (Config.thirdPerson) {
            if (!freelookApplied) {
                o.method_31043(class_5498.field_26665);
                freelookApplied = true;
            }
        } else if (freelookApplied) {
            o.method_31043(class_5498.field_26664);
            freelookApplied = false;
        }
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

    private static String time(class_1293 e) {
        if (e.method_48559()) return "inf";
        int s = e.method_5584() / 20;
        return String.format(Locale.ROOT, "%02d:%02d", s / 60, s % 60);
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

        if (!Config.hud) return;

        class_327 tr = mc.field_1772;

        List<String> lines = new ArrayList<>();
        if (Config.hudFps) lines.add("FPS: " + mc.method_47599());
        if (Config.hudPing) {
            class_640 entry = mc.method_1562() == null
                    ? null : mc.method_1562().method_2871(mc.field_1724.method_5667());
            lines.add("Ping: " + (entry == null ? 0 : entry.method_2959()) + " ms");
        }
        if (Config.hudCoords) {
            lines.add(String.format(Locale.ROOT, "X: %.1f / Y: %.1f / Z: %.1f",
                    mc.field_1724.method_23317(), mc.field_1724.method_23318(), mc.field_1724.method_23321()));
        }
        if (Config.hudFacing) {
            String f = mc.field_1724.method_5735().name().toLowerCase(Locale.ROOT);
            lines.add(Character.toUpperCase(f.charAt(0)) + f.substring(1));
        }

        List<String> effects = new ArrayList<>();
        if (Config.hudEffects) {
            for (class_1293 e : mc.field_1724.method_6026()) {
                String name = e.method_5579().comp_349().method_5560().getString();
                if (e.method_5578() > 0) name = name + " " + (e.method_5578() + 1);
                effects.add(name + "  " + time(e));
            }
        }

        String title = "ArchitektVisuals";
        int w = tr.method_1727(title);
        for (String s : lines) w = Math.max(w, tr.method_1727(s));
        for (String s : effects) w = Math.max(w, tr.method_1727(s));
        int h = 14 + lines.size() * 10 + (effects.isEmpty() ? 0 : 14 + effects.size() * 10) + 4;

        Matrix3x2fStack m = g.method_51448();
        m.pushMatrix();
        m.translate((float) Config.hudX, (float) Config.hudY);
        m.scale((float) Config.hudScale, (float) Config.hudScale);

        int color = Config.COLORS[Config.hudColor];
        g.method_25294(0, 0, w + 10, h, 0x90000000);
        g.method_25294(0, 0, 2, h, color);

        int y = 3;
        g.method_25303(tr, title, 6, y, color);
        y += 12;
        for (String s : lines) {
            g.method_25303(tr, s, 6, y, -1);
            y += 10;
        }
        if (!effects.isEmpty()) {
            y += 3;
            g.method_25303(tr, "EFFECTS", 6, y, color);
            y += 11;
            for (String s : effects) {
                g.method_25303(tr, s, 6, y, -1);
                y += 10;
            }
        }
        m.popMatrix();
    }
}
