package com.example.visuals.mixin;

import com.example.visuals.Config;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {

    // Item Physic: поворот предмета постоянный (случайный у каждого), а не крутится со временем.
    @Inject(method = "getRotation", at = @At("HEAD"), cancellable = true, require = 0)
    private static void visuals$staticYaw(float ageInTicks, float uniqueOffset, CallbackInfoReturnable<Float> cir) {
        if (Config.itemPhysic) cir.setReturnValue(uniqueOffset);
    }
}
