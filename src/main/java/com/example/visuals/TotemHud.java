package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/** Totem Counter: сколько тотемов бессмертия в инвентаре и второй руке. */
public final class TotemHud {
    private TotemHud() {}

    public static int count(MinecraftClient mc) {
        if (mc.player == null) return 0;
        int n = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isOf(Items.TOTEM_OF_UNDYING)) n += s.getCount();
        }
        ItemStack off = mc.player.getOffHandStack();
        if (off.isOf(Items.TOTEM_OF_UNDYING)) n += off.getCount();
        return n;
    }

    public static int[] draw(DrawContext g, MinecraftClient mc, float a) {
        int n = count(mc);
        if (n == 0 && Config.totemHideZero && !(mc.currentScreen instanceof ChatScreen)) return null;
        TextRenderer tr = mc.textRenderer;
        int accent = Config.COLORS[Config.totemColor];
        String txt = "x" + n;
        int w = 26 + tr.getWidth(txt) + 6, h = 22;

        HudStyle.panel(g, 0, 0, w, h, a, accent);
        HudStyle.header(g, 0, 0, w, 10, a, accent);
        g.drawItem(new ItemStack(Items.TOTEM_OF_UNDYING), 4, 3);
        int col = n == 0 ? HudStyle.BAD : n <= 1 ? HudStyle.MID : Theme.TEXT;
        HudStyle.text(g, tr, txt, 26, 7, col, a, true);
        return new int[] {w, h};
    }
}
