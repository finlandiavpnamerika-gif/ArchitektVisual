package com.example.visuals.mixin;

import com.example.visuals.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatHud.class)
public class ChatHudMixin {

    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), argsOnly = true)
    private Text visuals$highlightChat(Text message) {
        if (message == null) return null;
        
        String text = message.getString();
        String result = text;
        
        // Streamer Mode
        if (Config.streamerMode && MinecraftClient.getInstance().player != null) {
            String myName = MinecraftClient.getInstance().player.getName().getString();
            result = result.replaceAll("(?i)" + myName, "Protected");
            for (String friend : Config.friends) {
                result = result.replaceAll("(?i)" + friend, "Protected");
            }
        }

        // Подсветка друзей
        for (String friend : Config.friends) {
            if (text.contains(friend)) {
                return Text.literal(result).formatted(net.minecraft.util.Formatting.GREEN);
            }
        }
        
        return Text.literal(result);
    }
}
