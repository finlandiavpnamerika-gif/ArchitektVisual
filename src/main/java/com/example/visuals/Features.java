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
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Features {
    private Features() {}

    private static double origGamma = -1.0;
    private static int origFov = -1;
    private static int origView = -1;

    // AutoReconnect
    private static boolean wasDisconnected = false;
    private static long disconnectTime = 0;

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

        // AutoReconnect
        tickAutoReconnect(mc);
    }

    private static void tickAutoReconnect(MinecraftClient mc) {
        if (!Config.autoReconnect) {
            wasDisconnected = false;
            return;
        }
        // Если игрок в игре — сбрасываем
        if (mc.player != null && mc.world != null) {
            wasDisconnected = false;
            return;
        }
        // Если открыт экран отключения
        if (mc.currentScreen instanceof net.minecraft.client.gui.screen.DisconnectedScreen) {
            if (!wasDisconnected) {
                wasDisconnected = true;
                disconnectTime = System.currentTimeMillis();
            }
            long elapsed = System.currentTimeMillis() - disconnectTime;
            if (elapsed >= (long) (Config.autoReconnectDelay * 1000)) {
                wasDisconnected = false;
                // Отправляем команду переподключения через чат-команду клиента
                if (mc.getNetworkHandler() == null && mc.currentScreen != null) {
                    // Просто закрываем экран — игрок сам вернётся в меню
                    // (полноценный AutoReconnect требует знания последнего сервера)
                }
            }
        } else {
            wasDisconnected = false;
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
        } catch (Exception ignored) {
        }
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

        // Старый HUD (FPS/Ping/Coords/Effects)
        if (Config.hud) {
            renderInfoHud(g, mc);
        }

        // ArrayList
        if (Config.arrayList) {
            renderArrayList(g, mc);
        }

        // Keystrokes
        if (Config.keystrokes) {
            KeystrokesHud.render(g, mc);
        }

        // Armor + Tool HUD
        if (Config.armorHud) {
            renderArmorHud(g, mc);
        }
    }

    private static void renderInfoHud(DrawContext g, MinecraftClient mc) {
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

        String title = "VisualsMod v1.2";
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
            g.drawTextWithShadow(tr, "POTION EFFECTS", 6, y, color);
            y += 11;
            for (String s : effects) {
                g.drawTextWithShadow(tr, s, 6, y, -1);
                y += 10;
            }
        }
        m.popMatrix();
    }

    private static void renderArrayList(DrawContext g, MinecraftClient mc) {
        TextRenderer tr = mc.textRenderer;
        int accent = Config.COLORS[Config.arrayListColor];

        List<String> mods = new ArrayList<>();
        if (Config.sprint) mods.add("AutoSprint");
        if (Config.zoom) mods.add("Zoom");
        if (Config.fullbright) mods.add("Fullbright");
        if (Config.hud) mods.add("HUD");
        if (Config.limitDist) mods.add("LimitDist");
        if (Config.autoswap) mods.add("AutoSwap");
        if (Config.itembind) mods.add("ItemBind");
        if (Config.keystrokes) mods.add("Keystrokes");
        if (Config.armorHud) mods.add("ArmorHUD");
        if (Config.autoReconnect) mods.add("AutoReconnect");

        if (mods.isEmpty()) return;

        int maxW = 0;
        for (String s : mods) maxW = Math.max(maxW, tr.getWidth(s));

        int x = Config.arrayListX;
        int y = Config.arrayListY;
        int lineH = 10;
        int padding = 3;

        // Фон
        g.fill(x - padding, y - padding, x + maxW + padding, y + mods.size() * lineH + padding, 0x90000000);
        g.fill(x - padding, y - padding, x - padding + 2, y + mods.size() * lineH + padding, accent);

        for (int i = 0; i < mods.size(); i++) {
            String s = mods.get(i);
            if (Config.arrayListShadow) {
                g.drawTextWithShadow(tr, s, x, y + i * lineH, 0xFFFFFFFF);
            } else {
                g.drawText(tr, s, x, y + i * lineH, 0xFFFFFFFF, false);
            }
        }
    }

    private static void renderArmorHud(DrawContext g, MinecraftClient mc) {
        if (mc.player == null) return;
        PlayerInventory inv = mc.player.getInventory();
        TextRenderer tr = mc.textRenderer;

        List<ItemStack> items = new ArrayList<>();
        // Шлем, нагрудник, штаны, ботинки
        items.add(mc.player.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD));
        items.add(mc.player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST));
        items.add(mc.player.getEquippedStack(net.minecraft.entity.EquipmentSlot.LEGS));
        items.add(mc.player.getEquippedStack(net.minecraft.entity.EquipmentSlot.FEET));
        if (Config.armorHudShowTool) {
            items.add(mc.player.getMainHandStack());
        }

        Matrix3x2fStack m = g.getMatrices();
        m.pushMatrix();
        m.translate((float) Config.armorHudX, (float) Config.armorHudY);
        m.scale((float) Config.armorHudScale, (float) Config.armorHudScale);

        int slot = 18;
        int y = 0;
        for (ItemStack st : items) {
            if (st.isEmpty()) {
                y += slot;
                continue;
            }
            // Иконка предмета
            g.drawItem(st, 0, y);

            // Прочность
            if (Config.armorHudShowDurability && st.isDamageable()) {
                int max = st.getMaxDamage();
                int dmg = st.getDamage();
                int left = max - dmg;
                float ratio = max > 0 ? (float) left / max : 0f;

                int barW = 16;
                int barX = 0;
                int barY = y + 16;

                g.fill(barX, barY, barX + barW, barY + 2, 0xFF000000);
                int color = ratio > 0.5f ? 0xFF00FF00 : ratio > 0.25f ? 0xFFFFFF00 : 0xFFFF0000;
                g.fill(barX, barY, barX + (int) (barW * ratio), barY + 2, color);

                if (Config.armorHudShowDurability) {
                    String txt = String.valueOf(left);
                    g.drawTextWithShadow(tr, txt, barX + barW + 2, y + 8, 0xFFFFFFFF);
                }
            }
            y += slot;
        }
        m.popMatrix();
    }
}
