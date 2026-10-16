package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.GameOptions;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

/** Keystrokes HUD: показывает WASD, Space, ЛКМ/ПКМ и Shift. */
public final class KeystrokesHud {
    private KeystrokesHud() {}

    private static final int BOX_SIZE = 16;
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
        drawBox(g, tr, x0 + BOX_SIZE + GAP, y0, BOX_SIZE, "W", w, accent);
        drawBox(g, tr, x0, y0 + BOX_SIZE + GAP, BOX_SIZE, "A", a, accent);
        drawBox(g, tr, x0 + BOX_SIZE + GAP, y0 + BOX_SIZE + GAP, BOX_SIZE, "S", s, accent);
        drawBox(g, tr, x0 + (BOX_SIZE + GAP) * 2, y0 + BOX_SIZE + GAP, BOX_SIZE, "D", d, accent);

        int rowY = y0 + (BOX_SIZE + GAP) * 2;

        // Space
        if (Config.keystrokesShowSpace) {
            int spaceW = BOX_SIZE * 3 + GAP * 2;
            drawBox(g, tr, x0, rowY, spaceW, "", space, accent);
            rowY += BOX_SIZE + GAP;
        }

        // Shift
        int shiftW = BOX_SIZE * 3 + GAP * 2;
        drawBox(g, tr, x0, rowY, shiftW, "", shift, accent);
        rowY += BOX_SIZE + GAP;

        // LMB / RMB
        if (Config.keystrokesShowMouse) {
            int half = (BOX_SIZE * 3 + GAP * 2 - GAP) / 2;
            drawBox(g, tr, x0, rowY, half, "LMB", lmb, accent);
            drawBox(g, tr, x0 + half + GAP, rowY, half, "RMB", rmb, accent);
        }

        m.popMatrix();
    }

    private static void drawBox(DrawContext g, TextRenderer tr, int x, int y, int w, String label, boolean pressed, int accent) {
        int bg = pressed ? accent : 0x90000000;
        int border = pressed ? 0xFFFFFFFF : 0x60FFFFFF;

        g.fill(x, y, x + w, y + BOX_SIZE, border);
        g.fill(x + 1, y + 1, x + w - 1, y + BOX_SIZE - 1, bg);

        if (!label.isEmpty()) {
            int color = pressed ? 0xFF000000 : 0xFFFFFFFF;
            int tw = tr.getWidth(label);
            g.drawTextWithShadow(tr, label, x + (w - tw) / 2, y + (BOX_SIZE - 8) / 2, color);
        }
    }
}
