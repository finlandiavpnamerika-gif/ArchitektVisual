package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.function.IntFunction;

public class BindScreen extends Screen {
    private final Screen parent;
    private final String subject;
    private final IntFunction<String> acceptor;
    private final boolean[] prev = new boolean[GLFW.GLFW_KEY_LAST + 1];
    private boolean primed = false;
    private String error = null;

    public BindScreen(Screen parent, String subject, IntFunction<String> acceptor) {
        super(Text.literal("Bind"));
        this.parent = parent;
        this.subject = subject;
        this.acceptor = acceptor;
    }

    @Override
    public void render(DrawContext g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, width, height, 0xB0000000);
        int cy = height / 2 - 20;
        g.drawCenteredTextWithShadow(textRenderer, subject, width / 2, cy, -171);
        g.drawCenteredTextWithShadow(textRenderer, "Нажми клавишу для бинда (Esc - отмена)", width / 2, cy + 14, -1);
        if (error != null) {
            g.drawCenteredTextWithShadow(textRenderer, error, width / 2, cy + 30, -43691);
        }

        long window = MinecraftClient.getInstance().getWindow().getHandle();
        for (int key = GLFW.GLFW_KEY_SPACE; key <= GLFW.GLFW_KEY_LAST; key++) {
            if (key == GLFW.GLFW_KEY_ESCAPE) continue;
            boolean d = GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
            if (primed && d && !prev[key]) {
                String err = acceptor.apply(key);
                if (err == null) {
                    MinecraftClient.getInstance().setScreen(parent);
                    return;
                }
                error = err;
            }
            prev[key] = d;
        }
        primed = true;
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
