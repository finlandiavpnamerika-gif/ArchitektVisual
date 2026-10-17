package com.example.visuals.mixin;

import com.example.visuals.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    private static final Identifier BG = Identifier.of("minecraft", "textures/gui/title/background.png");

    @Inject(method = "render", at = @At("HEAD"))
    private void visuals$drawBackground(DrawContext g, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!Config.customMainMenu) return;

        TitleScreen self = (TitleScreen) (Object) this;
        
        // Прямая отрисовка через RenderSystem (работает во всех версиях)
        RenderSystem.setShaderTexture(0, BG);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.enableBlend();
        
        Matrix4f matrix = g.getMatrices().peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        
        buffer.vertex(matrix, 0, self.height, 0).texture(0, 1);
        buffer.vertex(matrix, self.width, self.height, 0).texture(1, 1);
        buffer.vertex(matrix, self.width, 0, 0).texture(1, 0);
        buffer.vertex(matrix, 0, 0, 0).texture(0, 0);
        
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
    }
}
