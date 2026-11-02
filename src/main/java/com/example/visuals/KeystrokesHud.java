package com.example.visuals;

import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_327;
import net.minecraft.class_332;
import org.lwjgl.glfw.GLFW;

/** Keystrokes HUD: WASD, Space, Shift и ЛКМ/ПКМ. */
public final class KeystrokesHud {
    private KeystrokesHud() {}

    private static final int BOX = 16;
    private static final int GAP = 2;

    /** Рисует в (0,0), возвращает {w, h}. */
    public static int[] draw(class_332 g, class_310 mc, float a) {
        class_327 tr = mc.field_1772;
        class_315 o = mc.field_1690;
        long window = mc.method_22683().method_4490();

        int accent = Config.COLORS[Config.keystrokesColor];

        boolean w = o.field_1894.method_1434();
        boolean aa = o.field_1913.method_1434();
        boolean s = o.field_1881.method_1434();
        boolean d = o.field_1849.method_1434();
        boolean space = o.field_1903.method_1434();
        boolean shift = o.field_1832.method_1434();

        boolean lmb = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rmb = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

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
