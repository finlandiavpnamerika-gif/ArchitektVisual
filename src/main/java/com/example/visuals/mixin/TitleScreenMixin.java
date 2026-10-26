package com.example.visuals.mixin;

import com.example.visuals.Config;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void visuals$drawClock(DrawContext g, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!Config.customMainMenu) return;

        TitleScreen self = (TitleScreen) (Object) this;
        int w = self.width;
        int h = self.height;

        // Берем ПОЛНУЮ дату и время (LocalDateTime), а не только время
        LocalDateTime now = LocalDateTime.now();
        
        // Время (часы:минуты)
        String timeString = now.format(DateTimeFormatter.ofPattern("HH:mm"));
        
        // Дата (День недели, число, месяц)
        String dateString = now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", new java.util.Locale("ru")));
        dateString = dateString.substring(0, 1).toUpperCase() + dateString.substring(1); // Первая буква заглавная

        // Масштабируем и рисуем часы (x3)
        Matrix3x2fStack matrices = g.getMatrices();
        matrices.pushMatrix();
        matrices.translate(w / 2.0f, h / 2.0f - 40);
        matrices.scale(3.0f, 3.0f);

        int timeWidth = self.getTextRenderer().getWidth(timeString);
        g.drawTextWithShadow(self.getTextRenderer(), timeString, -timeWidth / 2, 0, 0xFFFFFFFF);
        
        matrices.popMatrix();

        // Рисуем дату под часами (обычный масштаб)
        int dateWidth = self.getTextRenderer().getWidth(dateString);
        g.drawTextWithShadow(self.getTextRenderer(), dateString, (w - dateWidth) / 2, h / 2 + 20, 0xFFAAAAAA);
    }
}
