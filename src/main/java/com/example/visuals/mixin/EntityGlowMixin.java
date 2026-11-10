package com.example.visuals.mixin;

import com.example.visuals.Chams;
import com.example.visuals.Config;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Chams: делает выбранные клиентские сущности "светящимися" (контур сквозь стены). */
@Mixin(Entity.class)
public class EntityGlowMixin {
    @Inject(method = "isGlowing", at = @At("RETURN"), cancellable = true, require = 0)
    private void visuals$chams(CallbackInfoReturnable<Boolean> cir) {
        if (!Config.chamsOn || cir.getReturnValueZ()) return;
        if (Chams.want((Entity) (Object) this)) cir.setReturnValue(true);
    }
}
