package com.example.visuals.mixin;

import com.example.visuals.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Свой стиль кнопок во всех меню. */
@Mixin(ButtonWidget.class)
public class ButtonWidgetMixin {

    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    private void visuals$renderButton(DrawContext g, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!Config.customMainMenu) return;

        ButtonWidget self = (ButtonWidget) (Object) this;
        int x = self.getX();
        int y = self.getY();
        int w = self.getWidth();
        int h = self.getHeight();
        boolean hover = self.isHovered();
        boolean active = self.active;

        int bg = active ? (hover ? 0xFF1A3A20 : 0xE0101010) : 0xE0202020;

        // Тень
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xC0000000);
        // Фон
        g.fill(x, y, x + w, y + h, bg);
        // Зелёная рамка
        g.fill(x, y, x + w, y + 1, 0xFF7CE38B);
        g.fill(x, y + h - 1, x + w, y + h, 0xFF7CE38B);
        g.fill(x, y, x + 1, y + h, 0xFF7CE38B);
        g.fill(x + w - 1, y, x + w, y + h, 0xFF7CE38B);

        // Hover-свечение
        if (hover && active) {
            g.fill(x + 2, y + 2, x + w - 2, y + 3, 0x807CE38B);
        }

        // Текст
        MinecraftClient mc = MinecraftClient.getInstance();
        String label = self.getMessage().getString();
        int color = active ? 0xFFFFFFFF : 0xFF888888;
        int tw = mc.textRenderer.getWidth(label);
        g.drawTextWithShadow(mc.textRenderer, label,
                x + (w - tw) / 2,
                y + (h - 8) / 2,
                color);

        ci.cancel();
    }
}
