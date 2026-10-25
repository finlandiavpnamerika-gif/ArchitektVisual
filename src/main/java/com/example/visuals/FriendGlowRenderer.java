package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class FriendGlowRenderer {
    
    // Проверяет, является ли игрок другом
    public static boolean isFriend(String name) {
        return Config.friends.contains(name);
    }

    // Красит ник в зелёный, если это друг
    public static Text highlightName(Text original) {
        String name = original.getString();
        if (isFriend(name)) {
            MutableText colored = Text.literal(name).formatted(Formatting.GREEN);
            return colored;
        }
        return original;
    }
}
