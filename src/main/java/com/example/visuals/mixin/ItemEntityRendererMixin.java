package com.example.visuals.mixin;

import com.example.visuals.ItemPhysic;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.render.entity.state.ItemStackEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public class ItemEntityRendererMixin {

    private static final String ENTITY_RENDER =
            "render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;"
            + "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V";
    private static final String STACK_RENDER =
            "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;I"
            + "Lnet/minecraft/client/render/entity/state/ItemStackEntityRenderState;Lnet/minecraft/util/math/random/Random;"
            + "Lnet/minecraft/util/math/Box;)V";

    // открываем "окно" на время отрисовки предмета: из состояния берём время и смещение для покачивания
    @Inject(method = ENTITY_RENDER, at = @At("HEAD"), require = 0)
    private void visuals$begin(ItemEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue,
                               CameraRenderState camera, CallbackInfo ci) {
        ItemPhysic.begin(state.age, state.uniqueOffset);
    }

    @Inject(method = ENTITY_RENDER, at = @At("RETURN"), require = 0)
    private void visuals$end(ItemEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue,
                             CameraRenderState camera, CallbackInfo ci) {
        ItemPhysic.end();
    }

    // сюда матрица приходит уже с подъёмом, покачиванием и поворотом - корректируем
    @Inject(method = STACK_RENDER, at = @At("HEAD"), require = 0)
    private static void visuals$physic(MatrixStack matrices, OrderedRenderCommandQueue queue, int light,
                                       ItemStackEntityRenderState state, Random random, Box box, CallbackInfo ci) {
        ItemPhysic.apply(matrices, box);
    }
}
