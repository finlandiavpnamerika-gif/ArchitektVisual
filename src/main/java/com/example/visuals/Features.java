package com.example.visuals;

import com.example.visuals.mixin.SimpleOptionAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Features {
    private Features() {}

    private static double origGamma = -1.0;
    private static int origFov = -1;
    private static int origView = -1;

    public static void tick(MinecraftClient mc) {
        if (mc.options == null) return;
        GameOptions o = mc.options;

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

        // Лимит дальности
        SimpleOption<Integer> view = o.getViewDistance();
        if (Config.limitDist) {
            if (origView < 0) origView = view.getValue();
            int target = (int) Config.viewDist;
            if (view.getValue() != target) view.setValue(target);
        } else if (origView >= 0) {
            view.setValue(origView);
            origView = -1;
        }

        // AutoSprint
        if (mc.player != null && Config.sprint && mc.currentScreen == null
                && !mc.player.isSneaking() && !mc.player.isUsingItem()
                && mc.player.getHungerManager().getFoodLevel() > 6) {
            boolean fwd = o.forwardKey.isPressed();
            boolean any = fwd || o.backKey.isPressed() || o.leftKey.isPressed() || o.rightKey.isPressed();
            if (Config.sprintAllDirs ? any : fwd) {
                mc.player.setSprinting(true);
            }
        }

        // Время
        if (mc.world != null && Config.customTime) {
            mc.world.setTimeOfDay(Config.timeValue);
        }

        // Погода
        if (mc.world != null && Config.customWeather) {
            if (Config.weatherType == 0) {
                mc.world.setRainGradient(0f);
                mc.world.setThunderGradient(0f);
            } else if (Config.weatherType == 1) {
                mc.world.setRainGradient(1f);
                mc.world.setThunderGradient(0f);
            } else if (Config.weatherType == 2) {
                mc.world.setRainGradient(1f);
                mc.world.setThunderGradient(1f);
            }
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

        // === КАСТОМНОЕ НЕБО ===
        if (Config.customSky && mc.player.getPitch() < -45.0f) {
            int w = mc.getWindow().getScaledWidth();
            int h = mc.getWindow().getScaledHeight();
            
            g.fillGradient(0, 0, w, h / 2, Config.skyColor1, Config.skyColor2);
            g.fillGradient(0, h / 2, w, h, Config.skyColor2, Config.skyColor1);

            if (Config.skyLines) {
                long time = System.currentTimeMillis();
                int offset = (int) ((time / 50) * Config.skySpeed % w);
                for (int i = 0; i < w; i += 100) {
                    int x = (i + offset) % w;
                    g.fill(x, 0, x + 2, h, 0x30FFFFFF); 
                }
            }
        }

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
