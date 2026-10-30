package com.example.visuals;

import com.example.visuals.mixin.SimpleOptionAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Features {
    private Features() {}

    private static double origGamma = -1.0;
    private static int origFov = -1;
    private static int origView = -1;
    private static boolean freelookApplied = false;

    public static void tick(MinecraftClient mc) {
        if (mc.options == null) return;
        GameOptions o = mc.options;

        // --- ЛОГИКА МОДУЛЕЙ ---

        // AutoRespawn
        if (mc.currentScreen instanceof DeathScreen && Config.autoRespawn && mc.player != null) {
            mc.player.requestRespawn();
            mc.setScreen(null);
        }

        // Macros
        long window = mc.getWindow().getHandle();
        if (mc.currentScreen == null && mc.player != null) {
            for (String macro : Config.macros) {
                try {
                    String[] parts = macro.split(":");
                    if (parts.length == 2) {
                        int key = Integer.parseInt(parts[0]);
                        String cmd = parts[1];
                        if (GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS) {
                            if (System.currentTimeMillis() % 500 < 50) {
                                mc.player.networkHandler.sendChatCommand(cmd.startsWith("/") ? cmd.substring(1) : cmd);
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
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

        // ---- Freelook (третье лицо, пока модуль включён) ----
        if (Config.thirdPerson) {
            if (!freelookApplied) {
                o.setPerspective(Perspective.THIRD_PERSON_BACK);
                freelookApplied = true;
            }
        } else if (freelookApplied) {
            o.setPerspective(Perspective.FIRST_PERSON);
            freelookApplied = false;
        }
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

    private static String time(StatusEffectInstance e) {
        if (e.isInfinite()) return "inf";
        int s = e.getDuration() / 20;
        return String.format(Locale.ROOT, "%02d:%02d", s / 60, s % 60);
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

        if (!Config.hud) return;

        TextRenderer tr = mc.textRenderer;

        List<String> lines = new ArrayList<>();
        if (Config.hudFps) lines.add("FPS: " + mc.getCurrentFps());
        if (Config.hudPing) {
            PlayerListEntry entry = mc.getNetworkHandler() == null
                    ? null : mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            lines.add("Ping: " + (entry == null ? 0 : entry.getLatency()) + " ms");
        }
        if (Config.hudCoords) {
            lines.add(String.format(Locale.ROOT, "X: %.1f / Y: %.1f / Z: %.1f",
                    mc.player.getX(), mc.player.getY(), mc.player.getZ()));
        }
        if (Config.hudFacing) {
            String f = mc.player.getHorizontalFacing().name().toLowerCase(Locale.ROOT);
            lines.add(Character.toUpperCase(f.charAt(0)) + f.substring(1));
        }

        List<String> effects = new ArrayList<>();
        if (Config.hudEffects) {
            for (StatusEffectInstance e : mc.player.getStatusEffects()) {
                String name = e.getEffectType().value().getName().getString();
                if (e.getAmplifier() > 0) name = name + " " + (e.getAmplifier() + 1);
                effects.add(name + "  " + time(e));
            }
        }

        String title = "ArchitektVisuals";
        int w = tr.getWidth(title);
        for (String s : lines) w = Math.max(w, tr.getWidth(s));
        for (String s : effects) w = Math.max(w, tr.getWidth(s));
        int h = 14 + lines.size() * 10 + (effects.isEmpty() ? 0 : 14 + effects.size() * 10) + 4;

        Matrix3x2fStack m = g.getMatrices();
        m.pushMatrix();
        m.translate((float) Config.hudX, (float) Config.hudY);
        m.scale((float) Config.hudScale, (float) Config.hudScale);

        int color = Config.COLORS[Config.hudColor];
        g.fill(0, 0, w + 10, h, 0x90000000);
        g.fill(0, 0, 2, h, color);

        int y = 3;
        g.drawTextWithShadow(tr, title, 6, y, color);
        y += 12;
        for (String s : lines) {
            g.drawTextWithShadow(tr, s, 6, y, -1);
            y += 10;
        }
        if (!effects.isEmpty()) {
            y += 3;
            g.drawTextWithShadow(tr, "EFFECTS", 6, y, color);
            y += 11;
            for (String s : effects) {
                g.drawTextWithShadow(tr, s, 6, y, -1);
                y += 10;
            }
        }
        m.popMatrix();
    }
}
