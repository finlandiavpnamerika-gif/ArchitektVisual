package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.GameOptions;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

/** Keystrokes HUD: показывает WASD, Space, ЛКМ/ПКМ и Shift в стиле референса. */
public final class KeystrokesHud {
    private KeystrokesHud() {}

    private static final int BOX = 16;
    private static final int GAP = 2;

    public static void render(DrawContext g, MinecraftClient mc) {
        if (!Config.keystrokes) return;
        if (mc.player == null || mc.options.hudHidden) return;

        TextRenderer tr = mc.textRenderer;
        GameOptions o = mc.options;
        long window = mc.getWindow().getHandle();

        int accent = Config.COLORS[Config.keystrokesColor];

        boolean w = o.forwardKey.isPressed();
        boolean a = o.leftKey.isPressed();
        boolean s = o.backKey.isPressed();
        boolean d = o.rightKey.isPressed();
        boolean space = o.jumpKey.isPressed();
        boolean shift = o.sneakKey.isPressed();

        boolean lmb = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rmb = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

        Matrix3x2fStack m = g.getMatrices();
        m.pushMatrix();
        m.translate((float) Config.keystrokesX, (float) Config.keystrokesY);
        m.scale((float) Config.keystrokesScale, (float) Config.keystrokesScale);

        int x0 = 0;
        int y0 = 0;

        // WASD
        HudUtils.keyBox(g, tr, x0 + BOX + GAP, y0, BOX, "W", w, accent);
        HudUtils.keyBox(g, tr, x0, y0 + BOX + GAP, BOX, "A", a, accent);
        HudUtils.keyBox(g, tr, x0 + BOX + GAP, y0 + BOX + GAP, BOX, "S", s, accent);
        HudUtils.keyBox(g, tr, x0 + (BOX + GAP) * 2, y0 + BOX + GAP, BOX, "D", d, accent);

        int rowY = y0 + (BOX + GAP) * 2;
        int wideW = BOX * 3 + GAP * 2;

        // Space
        if (Config.keystrokesShowSpace) {
            HudUtils.keyBox(g, tr, x0, rowY, wideW, "", space, accent);
            rowY += BOX + GAP;
        }

        // Shift
        HudUtils.keyBox(g, tr, x0, rowY, wideW, "", shift, accent);
        rowY += BOX + GAP;

        // LMB / RMB
        if (Config.keystrokesShowMouse) {
            int half = (wideW - GAP) / 2;
            HudUtils.keyBox(g, tr, x0, rowY, half, "LMB", lmb, accent);
            HudUtils.keyBox(g, tr, x0 + half + GAP, rowY, half, "RMB", rmb, accent);
        }

        m.popMatrix();
    }
}
