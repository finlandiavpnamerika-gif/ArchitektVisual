package com.example.visuals;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

/** Armor + Tool HUD: иконки брони и предмета в руке, цифра и полоска прочности. */
public final class ArmorHud {
    private ArmorHud() {}

    private static final int PAD = 4;
    private static final int ICON = 16;
    private static final int ROW = 18;
    private static final int TOOL_GAP = 4;   // отступ перед предметом в руке
    private static final int MIN_BAR = 26;

    /** Рисует в (0,0), возвращает {w, h}. */
    public static int[] draw(DrawContext g, MinecraftClient mc, float a) {
        TextRenderer tr = mc.textRenderer;

        List<ItemStack> items = new ArrayList<>();
        items.add(mc.player.getEquippedStack(EquipmentSlot.HEAD));   // шлем
        items.add(mc.player.getEquippedStack(EquipmentSlot.CHEST));   // нагрудник
        items.add(mc.player.getEquippedStack(EquipmentSlot.LEGS));   // поножи
        items.add(mc.player.getEquippedStack(EquipmentSlot.FEET));   // ботинки
        boolean tool = Config.armorHudShowTool;
        if (tool) items.add(mc.player.getMainHandStack());

        boolean showDur = Config.armorHudShowDurability;

        // ширина колонки с цифрой/полоской
        int barW = MIN_BAR;
        if (showDur) {
            for (ItemStack st : items) {
                if (!st.isEmpty() && st.isDamageable()) {
                    barW = Math.max(barW, tr.getWidth(String.valueOf(st.getMaxDamage() - st.getDamage())));
                }
            }
        }

        int w = PAD * 2 + ICON + (showDur ? 4 + barW : 0);
        int h = PAD * 2 + items.size() * ROW - (ROW - ICON) + (tool ? TOOL_GAP : 0);

        HudStyle.panel(g, 0, 0, w, h, a, Theme.ACCENT);

        for (int i = 0; i < items.size(); i++) {
            ItemStack st = items.get(i);
            int y = PAD + i * ROW + (tool && i == 4 ? TOOL_GAP : 0);

            if (tool && i == 4) {
                HudStyle.divider(g, PAD, w - PAD, y - TOOL_GAP / 2 - 1, 0x30FFFFFF, a);
            }

            if (st.isEmpty()) {
                // пустой слот: еле заметная плашка, чтобы раскладка не прыгала
                HudStyle.roundFill(g, PAD, y, ICON, ICON, 2, HudStyle.withAlpha(0x22FFFFFF, a));
                continue;
            }

            g.drawItem(st, PAD, y);   // иконка (прозрачность на неё не действует)

            if (showDur && st.isDamageable()) {
                int max = st.getMaxDamage();
                int left = max - st.getDamage();
                float ratio = max > 0 ? (float) left / max : 0f;
                int col = HudStyle.ratioColor(ratio);
                int tx = PAD + ICON + 4;

                HudStyle.text(g, tr, String.valueOf(left), tx, y + 1, col, a, true);
                HudStyle.bar(g, tx, y + 11, barW, 3, ratio, col, a);
            }
        }
        return new int[] {w, h};
    }
}
