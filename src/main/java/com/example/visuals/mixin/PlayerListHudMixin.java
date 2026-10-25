package com.example.visuals.mixin;

import com.example.visuals.FriendGlowRenderer;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true)
    private Text visuals$highlightTab(Text text) {
        if (text != null) {
            return FriendGlowRenderer.highlightName(text);
        }
        return text;
    }
}
