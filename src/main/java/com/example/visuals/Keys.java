package com.example.visuals;

import org.lwjgl.glfw.GLFW;

/** Названия клавиш без зависимости от версии Minecraft. */
public final class Keys {
    private Keys() {}

    public static boolean isModifier(int k) {
        return k == GLFW.GLFW_KEY_LEFT_SHIFT || k == GLFW.GLFW_KEY_RIGHT_SHIFT
                || k == GLFW.GLFW_KEY_LEFT_CONTROL || k == GLFW.GLFW_KEY_RIGHT_CONTROL
                || k == GLFW.GLFW_KEY_LEFT_ALT || k == GLFW.GLFW_KEY_RIGHT_ALT
                || k == GLFW.GLFW_KEY_LEFT_SUPER || k == GLFW.GLFW_KEY_RIGHT_SUPER;
    }

    public static boolean down(long window, int key) {
        return key >= GLFW.GLFW_KEY_SPACE && key <= GLFW.GLFW_KEY_LAST
                && GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
    }

    public static String name(int key) {
        if (key < 0) return "-";
        if (key >= GLFW.GLFW_KEY_F1 && key <= GLFW.GLFW_KEY_F25) return "F" + (key - GLFW.GLFW_KEY_F1 + 1);
        if (key >= GLFW.GLFW_KEY_KP_0 && key <= GLFW.GLFW_KEY_KP_9) return "Num" + (key - GLFW.GLFW_KEY_KP_0);
        switch (key) {
            case GLFW.GLFW_KEY_SPACE: return "Space";
            case GLFW.GLFW_KEY_TAB: return "Tab";
            case GLFW.GLFW_KEY_ENTER: return "Enter";
            case GLFW.GLFW_KEY_BACKSPACE: return "Backspace";
            case GLFW.GLFW_KEY_INSERT: return "Insert";
            case GLFW.GLFW_KEY_DELETE: return "Delete";
            case GLFW.GLFW_KEY_HOME: return "Home";
            case GLFW.GLFW_KEY_END: return "End";
            case GLFW.GLFW_KEY_PAGE_UP: return "PgUp";
            case GLFW.GLFW_KEY_PAGE_DOWN: return "PgDn";
            case GLFW.GLFW_KEY_UP: return "Up";
            case GLFW.GLFW_KEY_DOWN: return "Down";
            case GLFW.GLFW_KEY_LEFT: return "Left";
            case GLFW.GLFW_KEY_RIGHT: return "Right";
            case GLFW.GLFW_KEY_CAPS_LOCK: return "Caps";
            case GLFW.GLFW_KEY_LEFT_SHIFT: return "LShift";
            case GLFW.GLFW_KEY_RIGHT_SHIFT: return "RShift";
            case GLFW.GLFW_KEY_LEFT_CONTROL: return "LCtrl";
            case GLFW.GLFW_KEY_RIGHT_CONTROL: return "RCtrl";
            case GLFW.GLFW_KEY_LEFT_ALT: return "LAlt";
            case GLFW.GLFW_KEY_RIGHT_ALT: return "RAlt";
            default: break;
        }
        String n = GLFW.glfwGetKeyName(key, 0);
        if (n != null && !n.isEmpty()) return n.toUpperCase(java.util.Locale.ROOT);
        return "Key" + key;
    }
}
