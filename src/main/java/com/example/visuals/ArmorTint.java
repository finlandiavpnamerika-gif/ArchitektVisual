package com.example.visuals;

import net.minecraft.item.ItemStack;

/** Цвет-множитель для брони на моделях игроков по прочности предмета. */
public final class ArmorTint {
    private ArmorTint() {}

    /** ARGB; -1 = не красить. */
    public static int current = -1;

    public static void begin(ItemStack st) {
        current = -1;
        if (!Config.armorTint || st.isEmpty() || !st.isDamageable()) return;
        int max = Math.max(1, st.getMaxDamage());
        float r = (float) (max - st.getDamage()) / max;
        int c = HudStyle.durabilityColor(r);
        float k = 0.30f; // подмешиваем белый, чтобы тёмная броня не становилась чёрной
        int rr = (int) (255 * k + (1 - k) * ((c >> 16) & 255));
        int gg = (int) (255 * k + (1 - k) * ((c >> 8) & 255));
        int bb = (int) (255 * k + (1 - k) * (c & 255));
        current = 0xFF000000 | (rr << 16) | (gg << 8) | bb;
    }

    public static void end() { current = -1; }
}
