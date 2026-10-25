package com.example.visuals.mixin;

import com.example.visuals.Config;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatHud.class)
public class ChatHudMixin {

    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), argsOnly = true)
    private Text visuals$highlightChat(Text message) {
        if (message == null) return null;
        
        String text = message.getString();
        
        // Ищем друзей в тексте сообщения
        for (String friend : Config.friends) {
            if (text.contains(friend)) {
                // Если нашли друга — красим весь текст в зелёный
                return Text.literal(text).formatted(Formatting.GREEN);
            }
        }
        return message;
    }
}
