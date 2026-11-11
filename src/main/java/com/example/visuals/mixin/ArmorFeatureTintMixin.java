package com.example.visuals.mixin;

import com.example.visuals.ArmorTint;
import net.minecraft.class_11659;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_4587;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Запоминает предмет брони, который сейчас рисуется (ArmorFeatureRenderer.renderArmor). */
@Mixin(targets = "net.minecraft.client.render.entity.feature.ArmorFeatureRenderer")
public class ArmorFeatureTintMixin {
    @Inject(method = "renderArmor", at = @At("HEAD"))
    private void visuals$begin(class_4587 matrices, class_11659 queue, class_1799 stack, class_1304 slot,
                               int light, @Coerce Object state, CallbackInfo ci) {
        ArmorTint.begin(stack);
    }

    @Inject(method = "renderArmor", at = @At("RETURN"))
    private void visuals$end(class_4587 matrices, class_11659 queue, class_1799 stack, class_1304 slot,
                             int light, @Coerce Object state, CallbackInfo ci) {
        ArmorTint.end();
    }
}
