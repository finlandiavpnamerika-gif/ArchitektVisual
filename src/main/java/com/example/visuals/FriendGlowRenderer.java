package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;

public class FriendGlowRenderer {
    public static void renderNameTag(DrawContext g, PlayerEntity player, int x, int y) {
        if (Config.friends.contains(player.getName().getString())) {
            MinecraftClient mc = MinecraftClient.getInstance();
            TextRenderer tr = mc.textRenderer;
            
            // Рисуем зелёную рамку вокруг ника
            String name = player.getName().getString();
            int width = tr.getWidth(name);
            
            // Свечение (рамка)
            g.fill(x - width / 2 - 2, y - 2, x + width / 2 + 2, y + 10, 0xFF7CE38B); // Зелёный фон
            g.fill(x - width / 2 - 1, y - 1, x + width / 2 + 1, y + 9, 0xFF000000); // Черный фон внутри
            
            // Сам ник
            g.drawTextWithShadow(tr, name, x - width / 2, y, 0xFF7CE38B);
        }
    }
}
