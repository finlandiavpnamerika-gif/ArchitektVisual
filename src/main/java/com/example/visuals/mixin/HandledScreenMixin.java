package com.example.visuals.mixin;

import com.example.visuals.ItemHighlight;
import com.example.visuals.ItemScroller;
import com.example.visuals.ShulkerPreview;
import net.minecraft.class_332;
import net.minecraft.class_465;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_465.class)
public class HandledScreenMixin {

    // Отрисовка превью шалкера
    @Inject(method = "render", at = @At("TAIL"))
    private void visuals$renderShulker(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ItemHighlight.render(context, (class_465<?>) (Object) this);
        ItemScroller.render((class_465<?>) (Object) this);
        ShulkerPreview.render(context, (class_465<?>) (Object) this, mouseX, mouseY);
    }
}
