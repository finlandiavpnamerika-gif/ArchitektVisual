package com.example.visuals.mixin;

import com.example.visuals.Config;
import com.example.visuals.HandView;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import com.example.visuals.CustomItems;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

    private static final String RENDER =
            "renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;F"
            + "Lnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;"
            + "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;I)V";

    private static final String RENDER_ITEM =
            "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;"
            + "Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;I)V";

    // CustomItems: своя модель вместо предмета в руке
    @Inject(method = RENDER_ITEM, at = @At("HEAD"), cancellable = true, require = 0)
    private void visuals$customItem(LivingEntity entity, ItemStack stack, ItemDisplayContext context, MatrixStack matrices,
                                    OrderedRenderCommandQueue queue, int light, CallbackInfo ci) {
        if (CustomItems.renderHeld(stack, context, matrices, queue)) ci.cancel();
    }

    // смещение / поворот / размер предмета в руке
    @Inject(method = RENDER, at = @At("HEAD"), require = 0)
    private void visuals$handView(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand,
                                  float swingProgress, ItemStack stack, float equipProgress, MatrixStack matrices,
                                  OrderedRenderCommandQueue queue, int light, CallbackInfo ci) {
        if (!Config.handView) return;
        HandView.apply(matrices, HandView.side(hand, player));
    }

    // "Без замаха": прогресс удара = 0 (аргументы float: 0 tickProgress, 1 pitch, 2 swingProgress, 3 equipProgress)
    @ModifyVariable(method = RENDER, at = @At("HEAD"), argsOnly = true, ordinal = 2, require = 0)
    private float visuals$noSwing(float swingProgress, AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand) {
        if (!Config.handView) return swingProgress;
        return Config.svNoSwing[HandView.side(hand, player)] ? 0f : swingProgress;
    }
}
