package com.example.visuals.mixin;

import com.example.visuals.ArmorTint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Подменяет цвет слоя брони (EquipmentRenderer.getDyeColor), пока рисуется окрашиваемый предмет. */
@Mixin(targets = "net.minecraft.client.render.entity.equipment.EquipmentRenderer")
public class EquipmentRendererTintMixin {
    @Inject(method = "getDyeColor", at = @At("RETURN"), cancellable = true)
    private static void visuals$tint(CallbackInfoReturnable<Integer> cir) {
        if (ArmorTint.current != -1) cir.setReturnValue(ArmorTint.current);
    }
}
