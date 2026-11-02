package com.example.visuals;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

/**
 * Рисует все HUD-элементы (Info, ArrayList, Armor, Keystrokes) и даёт перетаскивать их мышью,
 * пока открыт чат. Размер и прозрачность настраиваются в меню (Right Shift).
 */
public final class HudManager {
    private HudManager() {}

    private static final List<HudElement> ELEMENTS = new ArrayList<>();

    static {
        // порядок = порядок отрисовки (последний - сверху, его и хватаем первым)
        ELEMENTS.add(new HudElement("Keystrokes") {
            public boolean enabled() { return Config.keystrokes; }
            public int x() { return Config.keystrokesX; }
            public int y() { return Config.keystrokesY; }
            public void setPos(int x, int y) { Config.keystrokesX = x; Config.keystrokesY = y; }
            public double scale() { return Config.keystrokesScale; }
            public double alpha() { return Config.keystrokesAlpha; }
            public int[] draw(class_332 g, class_310 mc, float a) { return KeystrokesHud.draw(g, mc, a); }
        });
        ELEMENTS.add(new HudElement("ArrayList") {
            public boolean enabled() { return Config.arrayList; }
            public int x() { return Config.arrayListX; }
            public int y() { return Config.arrayListY; }
            public void setPos(int x, int y) { Config.arrayListX = x; Config.arrayListY = y; }
            public double scale() { return Config.arrayListScale; }
            public double alpha() { return Config.arrayListAlpha; }
            public int[] draw(class_332 g, class_310 mc, float a) { return ArrayListHud.draw(g, mc, a); }
        });
        ELEMENTS.add(new HudElement("Armor") {
            public boolean enabled() { return Config.armorHud; }
            public int x() { return Config.armorHudX; }
            public int y() { return Config.armorHudY; }
            public void setPos(int x, int y) { Config.armorHudX = x; Config.armorHudY = y; }
            public double scale() { return Config.armorHudScale; }
            public double alpha() { return Config.armorHudAlpha; }
            public int[] draw(class_332 g, class_310 mc, float a) { return ArmorHud.draw(g, mc, a); }
        });
        ELEMENTS.add(new HudElement("Info") {
            public boolean enabled() { return Config.hud; }
            public int x() { return Config.hudX; }
            public int y() { return Config.hudY; }
            public void setPos(int x, int y) { Config.hudX = x; Config.hudY = y; }
            public double scale() { return Config.hudScale; }
            public double alpha() { return Config.hudAlpha; }
            public int[] draw(class_332 g, class_310 mc, float a) { return InfoHud.draw(g, mc, a); }
        });
    }

    // --- состояние перетаскивания ---
    private static HudElement dragging = null;
    private static int dragOffX, dragOffY;
    private static boolean prevDown = false;
    private static boolean editActive = false;
    private static boolean moved = false;

    /** Вызывается каждый кадр из Features.hud. */
    public static void render(class_332 g, class_310 mc) {
        if (mc.field_1724 == null || mc.field_1690.field_1842) {
            stopEdit();
            return;
        }

        Matrix3x2fStack m = g.method_51448();
        for (HudElement e : ELEMENTS) {
            e.visible = false;
            if (!e.enabled()) continue;

            float s = (float) Math.max(0.25, Math.min(4.0, e.scale()));
            float a = HudStyle.clampAlpha(e.alpha());

            m.pushMatrix();
            m.translate((float) e.x(), (float) e.y());
            m.scale(s, s);
            int[] size = e.draw(g, mc, a);
            m.popMatrix();

            if (size != null) {
                e.bx = e.x();
                e.by = e.y();
                e.bw = Math.max(1, Math.round(size[0] * s));
                e.bh = Math.max(1, Math.round(size[1] * s));
                e.visible = true;
            }
        }

        if (isChatOpen(mc)) edit(g, mc);
        else stopEdit();
    }

    /** Открыт ли чат (по имени класса: net.minecraft.class_408 / ChatScreen). */
    private static boolean isChatOpen(class_310 mc) {
        if (mc.field_1755 == null) return false;
        String n = mc.field_1755.getClass().getName();
        return n.endsWith("class_408") || n.endsWith("ChatScreen");
    }

    private static void stopEdit() {
        if (dragging != null && moved) Config.save();
        dragging = null;
        moved = false;
        editActive = false;
    }

    private static void edit(class_332 g, class_310 mc) {
        long window = mc.method_22683().method_4490();
        int sw = mc.method_22683().method_4486();
        int sh = mc.method_22683().method_4502();

        double[] cx = new double[1];
        double[] cy = new double[1];
        int[] ww = new int[1];
        int[] wh = new int[1];
        GLFW.glfwGetCursorPos(window, cx, cy);
        GLFW.glfwGetWindowSize(window, ww, wh);
        if (ww[0] <= 0 || wh[0] <= 0) return;

        int mx = (int) (cx[0] * sw / ww[0]);
        int my = (int) (cy[0] * sh / wh[0]);
        boolean down = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        if (!editActive) {            // первый кадр с чатом: зажатая кнопка не считается нажатием
            editActive = true;
            prevDown = down;
        }
        boolean pressed = down && !prevDown;
        prevDown = down;

        HudElement top = null;
        for (int i = ELEMENTS.size() - 1; i >= 0; i--) {
            HudElement e = ELEMENTS.get(i);
            if (e.visible && e.contains(mx, my)) { top = e; break; }
        }

        if (pressed && top != null) {
            dragging = top;
            dragOffX = mx - top.bx;
            dragOffY = my - top.by;
            moved = false;
        }
        if (!down && dragging != null) {
            if (moved) Config.save();
            dragging = null;
            moved = false;
        }
        if (dragging != null) {
            int nx = Math.max(0, Math.min(mx - dragOffX, Math.max(0, sw - dragging.bw)));
            int ny = Math.max(0, Math.min(my - dragOffY, Math.max(0, sh - dragging.bh)));
            if (nx != dragging.x() || ny != dragging.y()) {
                dragging.setPos(nx, ny);
                dragging.bx = nx;
                dragging.by = ny;
                moved = true;
            }
        }

        // --- оверлей редактирования ---
        class_327 tr = mc.field_1772;
        HudElement hover = dragging != null ? dragging : top;
        for (HudElement e : ELEMENTS) {
            if (!e.visible) continue;
            boolean hot = e == hover;
            int p = 2;
            if (hot) g.method_25294(e.bx - p, e.by - p, e.bx + e.bw + p, e.by + e.bh + p, 0x18FFFFFF);
            HudStyle.rectRing(g, e.bx - p, e.by - p, e.bw + p * 2, e.bh + p * 2,
                    HudStyle.withAlpha(Theme.ACCENT, hot ? 1f : 0.45f));
            int ty = e.by - p - 10;
            if (ty < 1) ty = e.by + e.bh + p + 2;
            g.method_25303(tr, e.name, e.bx, ty, hot ? Theme.TEXT : Theme.TEXT_DIM);
        }
        g.method_25300(tr, "Перетаскивай HUD мышью", sw / 2, 4, Theme.TEXT_DIM);
    }
}
