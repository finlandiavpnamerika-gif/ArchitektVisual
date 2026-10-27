package com.example.visuals.mixin;

import com.example.visuals.Config;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerListHud.class)
public class ScoreboardMixin {

    // В 1.21.11 метод render принимает DrawContext, а не MatrixStack
    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true)
    private ScoreboardObjective visuals$spoofBoard(ScoreboardObjective original) {
        if (Config.boardSpoofer && original != null) {
            // Здесь твоя логика подмены имени (если нужно)
            // Например, можно вернуть null, чтобы скрыть скорборд
            // return null; 
        }
        return original;
    }
}
