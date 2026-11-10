package com.example.visuals;

import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Tooltips Plus: дополнительные строки в подсказках предметов (прочность, id, размер стака). */
public final class TooltipsPlus {
    private TooltipsPlus() {}

    public static void add(ItemStack stack, boolean advanced, List<Text> lines) {
        if (!Config.tipOn || stack == null || stack.isEmpty()) return;

        if (Config.tipDur && !advanced && stack.isDamageable()) {
            int max = stack.getMaxDamage(), left = max - stack.getDamage();
            int pct = Math.round(100f * left / Math.max(1, max));
            Formatting f = pct >= 60 ? Formatting.GREEN : pct >= 25 ? Formatting.YELLOW : Formatting.RED;
            lines.add(Text.literal("Прочность: " + left + "/" + max + " (" + pct + "%)").formatted(f));
        }
        if (Config.tipStack && stack.getMaxCount() > 1) {
            lines.add(Text.literal("Стак: " + stack.getCount() + "/" + stack.getMaxCount()).formatted(Formatting.GRAY));
        }
        if (Config.tipId && !advanced) {
            lines.add(Text.literal(Registries.ITEM.getId(stack.getItem()).toString()).formatted(Formatting.DARK_GRAY));
        }
    }
}
