package com.example.visuals.mixin;

import com.example.visuals.FriendGlowRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @ModifyVariable(method = "renderLabelIfPresent", at = @At("HEAD"), argsOnly = true)
    private Text visuals$highlightOverhead(Text text) {
        if (text != null) {
            return FriendGlowRenderer.highlightName(text);
        }
        return text;
    }
}
