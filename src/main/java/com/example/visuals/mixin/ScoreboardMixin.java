package com.example.visuals.mixin;

import com.example.visuals.Config;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(InGameHud.class)
public class ScoreboardMixin {

    @ModifyVariable(method = "renderScoreboardObjective", at = @At("HEAD"), argsOnly = true)
    private Text visuals$spoofBoard(Text text, ScoreboardObjective objective, DrawContext context, int x, int y) {
        if (!Config.boardSpoofer) return text;

        String original = text.getString();
        
        // Подменяем текст (пример для FunTime)
        if (original.contains("Монеты:")) return Text.literal("§6Монеты: §f100.000.000");
        if (original.contains("Токены:")) return Text.literal("§bТокены: §f50.000");
        if (original.contains("Ранг:")) return Text.literal("§cРанг: §fГерцог");
        
        return text;
    }
}
