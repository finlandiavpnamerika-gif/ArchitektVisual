package com.example.visuals.mixin;

import com.example.visuals.Config;
import com.example.visuals.EventPoints;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_338;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(class_338.class)
public class ChatHudMixin {

    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), argsOnly = true)
    private class_2561 visuals$highlightChat(class_2561 message) {
        if (message == null) return null;
        
        String text = message.getString();
        EventPoints.onChat(text);
        String result = text;
        
        // Streamer Mode
        if (Config.streamerMode && class_310.method_1551().field_1724 != null) {
            String myName = class_310.method_1551().field_1724.method_5477().getString();
            result = result.replaceAll("(?i)" + myName, "Protected");
            for (String friend : Config.friends) {
                result = result.replaceAll("(?i)" + friend, "Protected");
            }
        }

        // Подсветка друзей
        for (String friend : Config.friends) {
            if (text.contains(friend)) {
                return class_2561.method_43470(result).method_27692(class_124.field_1060);
            }
        }
        
        return class_2561.method_43470(result);
    }
}
