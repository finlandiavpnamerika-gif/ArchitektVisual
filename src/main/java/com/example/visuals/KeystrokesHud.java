package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.GameOptions;
import org.lwjgl.glfw.GLFW;

/** Keystrokes HUD: WASD, Space, Shift и ЛКМ/ПКМ. */
public final class KeystrokesHud {
    private KeystrokesHud() {}

    private static final int BOX = 16;
    private static final int GAP = 2;

    /** Рисует в (0,0), возвращает {w, h}. */
    public static int[] draw(DrawContext g, MinecraftClient mc, float a) {
        TextRenderer tr = mc.textRenderer;
        GameOptions o = mc.options;
        long window = mc.getWindow().getHandle();

        int accent = Config.COLORS[Config.keystrokesColor];

        boolean w = o.forwardKey.isPressed();
        boolean aa = o.leftKey.isPressed();
        boolean s = o.backKey.isPressed();
        boolean d = o.rightKey.isPressed();
        boolean space = o.jumpKey.isPressed();
        boolean shift = o.sneakKey.isPressed();

        boolean lmb = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rmb = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

        if (Config.keystrokesHoriz) {
            int x = 0;
            String[] lab = {"W", "A", "S", "D"};
            boolean[] st = {w, aa, s, d};
            for (int i = 0; i < 4; i++) { HudStyle.keyBox(g, tr, x, 0, BOX, BOX, lab[i], st[i], accent, a); x += BOX + GAP; }
            if (Config.keystrokesShowSpace) { HudStyle.keyBox(g, tr, x, 0, 38, BOX, "SPACE", space, accent, a); x += 38 + GAP; }
            HudStyle.keyBox(g, tr, x, 0, 34, BOX, "SHIFT", shift, accent, a); x += 34 + GAP;
            if (Config.keystrokesShowMouse) {
                HudStyle.keyBox(g, tr, x, 0, 24, BOX, "LMB", lmb, accent, a); x += 24 + GAP;
                HudStyle.keyBox(g, tr, x, 0, 24, BOX, "RMB", rmb, accent, a); x += 24 + GAP;
            }
            return new int[] {x - GAP, BOX};
        }

        int wideW = BOX * 3 + GAP * 2;

        // WASD
        HudStyle.keyBox(g, tr, BOX + GAP, 0, BOX, BOX, "W", w, accent, a);
        HudStyle.keyBox(g, tr, 0, BOX + GAP, BOX, BOX, "A", aa, accent, a);
        HudStyle.keyBox(g, tr, BOX + GAP, BOX + GAP, BOX, BOX, "S", s, accent, a);
        HudStyle.keyBox(g, tr, (BOX + GAP) * 2, BOX + GAP, BOX, BOX, "D", d, accent, a);

        int rowY = (BOX + GAP) * 2;

        if (Config.keystrokesShowSpace) {
            HudStyle.keyBox(g, tr, 0, rowY, wideW, BOX, "SPACE", space, accent, a);
            rowY += BOX + GAP;
        }

        HudStyle.keyBox(g, tr, 0, rowY, wideW, BOX, "SHIFT", shift, accent, a);
        rowY += BOX + GAP;

        if (Config.keystrokesShowMouse) {
            int half = (wideW - GAP) / 2;
            HudStyle.keyBox(g, tr, 0, rowY, half, BOX, "LMB", lmb, accent, a);
            HudStyle.keyBox(g, tr, half + GAP, rowY, half, BOX, "RMB", rmb, accent, a);
            rowY += BOX + GAP;
        }

        return new int[] {wideW, rowY - GAP};
    }
}
