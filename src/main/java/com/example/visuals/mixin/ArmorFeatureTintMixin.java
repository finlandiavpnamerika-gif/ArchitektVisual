package com.example.visuals.mixin;

import com.example.visuals.ArmorTint;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Запоминает предмет брони, который сейчас рисуется (ArmorFeatureRenderer.renderArmor). */
@Mixin(targets = "net.minecraft.client.render.entity.feature.ArmorFeatureRenderer")
public class ArmorFeatureTintMixin {
    @Inject(method = "renderArmor", at = @At("HEAD"))
    private void visuals$begin(MatrixStack matrices, OrderedRenderCommandQueue queue, ItemStack stack, EquipmentSlot slot,
                               int light, @Coerce Object state, CallbackInfo ci) {
        ArmorTint.begin(stack);
    }

    @Inject(method = "renderArmor", at = @At("RETURN"))
    private void visuals$end(MatrixStack matrices, OrderedRenderCommandQueue queue, ItemStack stack, EquipmentSlot slot,
                             int light, @Coerce Object state, CallbackInfo ci) {
        ArmorTint.end();
    }
}
