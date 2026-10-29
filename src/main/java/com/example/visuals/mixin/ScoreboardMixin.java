package com.example.visuals.mixin;

import com.example.visuals.Config;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Scoreboard.class)
public class ScoreboardMixin {

    @ModifyVariable(method = "getObjectiveForSlot", at = @At("HEAD"), argsOnly = true)
    private Text visuals$spoofBoard(Text original) {
        if (Config.boardSpoofer && original != null) {
            // Логика скрытия/подмены
        }
        return original;
    }
}
