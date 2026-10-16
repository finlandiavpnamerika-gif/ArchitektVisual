package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.List;

/** Armor + Tool HUD: иконки брони, инструмента и полоски прочности. */
public final class ArmorHud {
    private ArmorHud() {}

    private static final int SLOT = 18;

    public static void render(DrawContext g, MinecraftClient mc) {
        if (!Config.armorHud) return;
        if (mc.player == null || mc.options.hudHidden) return;

        TextRenderer tr = mc.textRenderer;

        List<ItemStack> items = new ArrayList<>();
        items.add(mc.player.getEquippedStack(EquipmentSlot.HEAD));
        items.add(mc.player.getEquippedStack(EquipmentSlot.CHEST));
        items.add(mc.player.getEquippedStack(EquipmentSlot.LEGS));
        items.add(mc.player.getEquippedStack(EquipmentSlot.FEET));
        if (Config.armorHudShowTool) {
            items.add(mc.player.getMainHandStack());
        }

        Matrix3x2fStack m = g.getMatrices();
        m.pushMatrix();
        m.translate((float) Config.armorHudX, (float) Config.armorHudY);
        m.scale((float) Config.armorHudScale, (float) Config.armorHudScale);

        int y = 0;
        for (ItemStack st : items) {
            if (st.isEmpty()) {
                y += SLOT;
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

                HudUtils.durabilityBar(g, 0, y + 16, 16, ratio);

                // Цифра прочности
                String txt = String.valueOf(left);
                g.drawTextWithShadow(tr, txt, 18, y + 8, Theme.TEXT);
            }

            y += SLOT;
        }

        m.popMatrix();
    }
}
