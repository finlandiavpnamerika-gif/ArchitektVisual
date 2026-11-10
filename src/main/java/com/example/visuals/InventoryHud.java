package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

/** Inventory HUD: содержимое инвентаря (27 слотов, по желанию + хотбар) прямо на экране. */
public final class InventoryHud {
    private InventoryHud() {}

    private static final int SLOT = 18;
    private static final int PAD = 4;
    private static final int HEADER = 12;

    public static int[] draw(DrawContext g, MinecraftClient mc, float a) {
        if (mc.player == null) return null;
        TextRenderer tr = mc.textRenderer;
        int accent = Config.COLORS[Config.invColor];
        int rows = Config.invHotbar ? 4 : 3;
        int w = PAD * 2 + SLOT * 9;
        int h = HEADER + PAD + rows * SLOT + 2;

        HudStyle.panel(g, 0, 0, w, h, a, accent);
        HudStyle.header(g, 0, 0, w, HEADER - 2, a, accent);
        HudStyle.text(g, tr, "Inventory", PAD + 1, 2, accent, a, true);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < 9; c++) {
                int x = PAD + c * SLOT, y = HEADER + r * SLOT;
                // r 0..2 = слоты 9..35, r 3 = хотбар 0..8
                int slot = r < 3 ? 9 + r * 9 + c : c;
                HudStyle.roundFill(g, x, y, SLOT - 1, SLOT - 1, 2, HudStyle.withAlpha(0x40000000, a));
                ItemStack st = mc.player.getInventory().getStack(slot);
                if (!st.isEmpty()) {
                    g.drawItem(st, x + 1, y + 1);
                    g.drawStackOverlay(tr, st, x + 1, y + 1);
                }
            }
        }
        return new int[] {w, h};
    }
}
