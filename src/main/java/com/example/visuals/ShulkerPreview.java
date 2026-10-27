package com.example.visuals;

import com.example.visuals.mixin.HandledScreenAccessor;
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
        
        // Используем наш Accessor, чтобы получить защищенные поля x и y
        HandledScreenAccessor accessor = (HandledScreenAccessor) screen;
        int screenX = accessor.visuals$getX();
        int screenY = accessor.visuals$getY();
        
        // Ищем слот, на который наведена мышь
        Slot hoveredSlot = null;
        for (Slot slot : screen.getScreenHandler().slots) {
            if (mouseX >= screenX + slot.x && mouseX < screenX + slot.x + 16 &&
                mouseY >= screenY + slot.y && mouseY < screenY + slot.y + 16) {
                hoveredSlot = slot;
                break;
            }
        }

        if (hoveredSlot == null || !hoveredSlot.hasStack()) return;
        
        ItemStack stack = hoveredSlot.getStack();
        if (stack.getItem() != Items.SHULKER_BOX && !stack.getItem().toString().contains("shulker")) return;

        ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
        if (container == null) return;

        // Рисуем фон
        int x = mouseX + 12;
        int y = mouseY - 12;
        g.fill(x, y, x + 160, y + 60, 0xD0000000);
        g.fill(x, y, x + 160, y + 2, 0xFF7CE38B);

        // Рисуем слоты
        int index = 0;
        for (ItemStack item : container.iterateNonEmpty()) {
            if (index >= 27) break;
            int ix = x + 5 + (index % 9) * 17;
            int iy = y + 5 + (index / 9) * 17;
            
            g.drawItem(item, ix, iy);
            g.drawStackOverlay(MinecraftClient.getInstance().textRenderer, item, ix, iy);
            
            index++;
        }
    }
}
