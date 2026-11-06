package com.example.visuals;

import com.example.visuals.mixin.HandledScreenAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

/** Превью содержимого шалкера при наведении в инвентаре. Рисуется НАД курсором, чтобы не перекрываться ванильной подсказкой. */
public class ShulkerPreview {

    private static final int W = 9 * 18 + 8, H = 3 * 18 + 8 + 12;

    public static void render(DrawContext g, HandledScreen<?> screen, int mouseX, int mouseY) {
        if (!Config.shulkerPreview) return;

        HandledScreenAccessor accessor = (HandledScreenAccessor) screen;
        int screenX = accessor.visuals$getX();
        int screenY = accessor.visuals$getY();

        Slot hovered = null;
        for (Slot slot : screen.getScreenHandler().slots) {
            if (mouseX >= screenX + slot.x && mouseX < screenX + slot.x + 16 &&
                mouseY >= screenY + slot.y && mouseY < screenY + slot.y + 16) {
                hovered = slot;
                break;
            }
        }
        if (hovered == null || !hovered.hasStack()) return;

        ItemStack stack = hovered.getStack();
        if (!(Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock)) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer tr = mc.textRenderer;
        ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);

        int sw = mc.getWindow().getScaledWidth();
        int x = Math.max(4, Math.min(sw - W - 4, mouseX - W / 2));
        int y = mouseY - H - 16;
        if (y < 4) y = Math.min(mc.getWindow().getScaledHeight() - H - 4, mouseY + 40);

        // фон + рамка + заголовок
        g.fill(x - 1, y - 1, x + W + 1, y + H + 1, 0xFF7CE38B);
        g.fill(x, y, x + W, y + H, 0xF0101216);
        g.fill(x, y, x + W, y + 12, 0xFF1B1F26);
        g.drawTextWithShadow(tr, stack.getName(), x + 4, y + 2, 0xFFFFFFFF);

        // пустая сетка 9x3
        for (int i = 0; i < 27; i++) {
            int ix = x + 4 + (i % 9) * 18, iy = y + 16 + (i / 9) * 18;
            g.fill(ix, iy, ix + 17, iy + 17, 0x33FFFFFF);
        }

        if (container == null) {
            g.drawCenteredTextWithShadow(tr, "Сервер не передаёт содержимое", x + W / 2, y + 16 + 27, 0xFFFFAA55);
            return;
        }

        int index = 0;
        for (ItemStack item : container.iterateNonEmpty()) {
            if (index >= 27) break;
            int ix = x + 5 + (index % 9) * 18, iy = y + 17 + (index / 9) * 18;
            g.drawItem(item, ix, iy);
            g.drawStackOverlay(tr, item, ix, iy);
            index++;
        }
        if (index == 0) g.drawCenteredTextWithShadow(tr, "Пусто", x + W / 2, y + 16 + 27, 0xFFAAAAAA);
    }
}
