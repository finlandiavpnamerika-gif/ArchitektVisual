package com.example.visuals.mixin;

import com.example.visuals.ShulkerPreview;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {
    
    // Заменяем ванильный фон инвентаря на кастомный
    @Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true)
    private void visuals$drawCustomBackground(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        // Получаем координаты окна инвентаря из нашего Accessor
        HandledScreenAccessor accessor = (HandledScreenAccessor) (Object) this;
        int x = accessor.visuals$getX();
        int y = accessor.visuals$getY();

        // Рисуем нашу картинку (путь: assets/visualsmod/textures/gui/inventory_bg.png)
        // Размер 176x166 — стандартный размер окна инвентаря
        Identifier bgTexture = Identifier.of("visualsmod", "textures/gui/inventory_bg.png");
        context.drawTexture(bgTexture, x, y, 0, 0, 176, 166, 176, 166);
        
        // Отменяем ванильный фон
        ci.cancel();
    }

    // Отрисовка превью шалкера (переведено на DrawContext)
    @Inject(method = "render", at = @At("TAIL"))
    private void visuals$renderShulker(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ShulkerPreview.render(context, (HandledScreen<?>) (Object) this, mouseX, mouseY);
    }
}
