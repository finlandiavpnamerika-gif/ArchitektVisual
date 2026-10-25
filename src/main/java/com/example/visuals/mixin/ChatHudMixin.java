package com.example.visuals.mixin;

import com.example.visuals.Config;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class ChatHudMixin {

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true)
    private void visuals$highlightChat(Text message, CallbackInfo ci) {
        String text = message.getString();
        
        // Проходимся по списку друзей и ищем их ник в сообщении
        for (String friend : Config.friends) {
            if (text.contains(friend)) {
                // Если нашли друга в сообщении — красим всё сообщение в зелёный
                // (Или можно покрасить только ник, но для простоты красим строку)
                MutableText colored = Text.literal(text).formatted(Formatting.GREEN);
                ((ChatHud) (Object) this).addMessage(colored);
                ci.cancel(); // Отменяем стандартное добавление, чтобы не было дубля
                return;
            }
        }
    }
}
