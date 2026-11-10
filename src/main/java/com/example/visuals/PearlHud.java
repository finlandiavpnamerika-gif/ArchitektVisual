package com.example.visuals;

import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/** Pearl Cooldown: оставшееся время перезарядки эндер-жемчуга. */
public final class PearlHud {
    private PearlHud() {}

    public static int[] draw(DrawContext g, MinecraftClient mc, float a) {
        if (mc.player == null) return null;
        float prog = mc.player.getItemCooldownManager().getCooldownProgress(new ItemStack(Items.ENDER_PEARL), 0f);
        boolean editing = mc.currentScreen instanceof ChatScreen;
        if (prog <= 0f && Config.pcdHideIdle && !editing) return null;

        TextRenderer tr = mc.textRenderer;
        int accent = Config.COLORS[Config.pcdColor];
        String txt = prog > 0f ? String.format(Locale.ROOT, "%.1fs", prog * 1.0f) : "Ready";
        int w = 26 + Math.max(tr.getWidth(txt), 28) + 6, h = 22;

        HudStyle.panel(g, 0, 0, w, h, a, accent);
        HudStyle.header(g, 0, 0, w, 10, a, accent);
        g.drawItem(new ItemStack(Items.ENDER_PEARL), 4, 3);
        HudStyle.text(g, tr, txt, 26, 4, prog > 0f ? HudStyle.MID : HudStyle.GOOD, a, true);
        HudStyle.bar(g, 26, 15, w - 32, 3, prog > 0f ? 1f - prog : 1f, accent, a);
        return new int[] {w, h};
    }
}
