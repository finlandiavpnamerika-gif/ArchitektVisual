package com.example.visuals.mixin;

import com.example.visuals.FriendGlowRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    // Мы перехватываем метод получения текста над головой
    // В 1.21.11 он называется getDisplayName или getName
    @Inject(method = "getName", at = @At("RETURN"), cancellable = true)
    private void visuals$highlightOverhead(Entity entity, CallbackInfoReturnable<Text> cir) {
        Text original = cir.getReturnValue();
        if (original != null) {
            Text highlighted = FriendGlowRenderer.highlightName(original);
            if (highlighted != original) {
                cir.setReturnValue(highlighted);
            }
        }
    }
}
