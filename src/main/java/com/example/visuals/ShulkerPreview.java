package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;

public class ShulkerPreview {
    
    public static void render(DrawContext g, HandledScreen<?> screen, int mouseX, int mouseY) {
        if (!Config.shulkerPreview) return;
        
        Slot slot = screen.getScreenHandler().getSlotAt(mouseX, mouseY);
        if (slot == null || !slot.hasStack()) return;
        
        ItemStack stack = slot.getStack();
        if (stack.getItem() != Items.SHULKER_BOX && !stack.getItem().toString().contains("shulker")) return;

        ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
        if (container == null) return;

        // Рисуем фон
        int x = mouseX + 12;
        int y = mouseY - 12;
        g.fill(x, y, x + 160, y + 60, 0xD0000000);
        g.fill(x, y, x + 160, y + 2, 0xFF7CE38B); // Зеленая полоска

        // Рисуем слоты
        int index = 0;
        for (ItemStack item : container.iterateNonEmpty()) {
            if (index >= 27) break;
            int ix = x + 5 + (index % 9) * 17;
            int iy = y + 5 + (index / 9) * 17;
            g.drawItem(item, ix, iy);
            g.drawItemInSlot(MinecraftClient.getInstance().textRenderer, item, ix, iy);
            index++;
        }
    }
}
