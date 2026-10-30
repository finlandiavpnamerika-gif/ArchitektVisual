package com.example.visuals.mixin;

import com.example.visuals.ShulkerPreview;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {

    // Рисуем фон ПЕРЕД тем, как игра отрисует слоты
    @Inject(method = "render", at = @At("HEAD"))
    private void visuals$drawCustomBackground(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        HandledScreenAccessor accessor = (HandledScreenAccessor) (Object) this;
        int x = accessor.visuals$getX();
        int y = accessor.visuals$getY();

        // Путь: assets/visualsmod/textures/gui/inventory_bg.png, размер 176x166
        Identifier bgTexture = Identifier.of("visualsmod", "textures/gui/inventory_bg.png");
        context.drawTexture(RenderPipelines.GUI_TEXTURED, bgTexture, x, y, 0, 0, 176, 166, 176, 166);
    }

    // Отрисовка превью шалкера
    @Inject(method = "render", at = @At("TAIL"))
    private void visuals$renderShulker(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ShulkerPreview.render(context, (HandledScreen<?>) (Object) this, mouseX, mouseY);
    }
}
