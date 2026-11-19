package com.example.visuals;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_327;
import net.minecraft.class_332;

/**
 * Свой шрифт для HUD (Inter): assets/visualsmod/font/hud.json.
 *
 * Нужные вызовы Minecraft (применить шрифт к тексту, измерить ширину, нарисовать Text) в разных версиях
 * называются и принимают аргументы по-разному, поэтому здесь они находятся по структуре через рефлексию
 * один раз при первом использовании. Если что-то не нашлось или сломалось - HUD молча остаётся
 * на обычном шрифте, ничего не падает.
 */
public final class HudFont {
    private HudFont() {}

    private static final String NAMESPACE = "visualsmod", FONT = "hud";

    private static boolean tried, ok;
    private static Object style;          // Style со шрифтом hud
    private static Method setStyle;       // MutableText.setStyle(Style)
    private static Method textWidth;      // TextRenderer.getWidth(Text)
    private static Method drawText;       // DrawContext.drawText(TextRenderer, Text, x, y, color, shadow)

    private static final Map<String, class_2561> CACHE = new LinkedHashMap<>(256, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<String, class_2561> e) { return size() > 600; }
    };

    private static void fail() { ok = false; CACHE.clear(); }

    private static boolean isStyle(Class<?> c) {
        return c.getName().endsWith("class_2583") || c.getSimpleName().equals("Style");
    }

    /** Значение для Style.withFont: Identifier напрямую либо вложенная запись с конструктором (Identifier). */
    private static Object fontArg(Class<?> param, class_2960 id) throws Exception {
        if (param.isAssignableFrom(class_2960.class)) return id;
        for (Class<?> n : param.getDeclaredClasses()) {
            if (!param.isAssignableFrom(n)) continue;
            for (Constructor<?> k : n.getDeclaredConstructors()) {
                if (k.getParameterCount() == 1 && k.getParameterTypes()[0].isAssignableFrom(class_2960.class)) {
                    k.setAccessible(true);
                    return k.newInstance(id);
                }
            }
        }
        return null;
    }

    private static synchronized void init() {
        if (tried) return;
        tried = true;
        try {
            class_2960 id = class_2960.method_60655(NAMESPACE, FONT);
            Object probe = class_2561.method_43470("x");

            // MutableText.setStyle(Style)
            for (Method m : probe.getClass().getMethods()) {
                if (m.getParameterCount() == 1 && isStyle(m.getParameterTypes()[0])
                        && m.getReturnType().isInstance(probe) && !Modifier.isStatic(m.getModifiers())) {
                    setStyle = m;
                    break;
                }
            }
            if (setStyle == null) return;

            // Style.EMPTY и Style.withFont(...)
            Class<?> styleCls = setStyle.getParameterTypes()[0];
            Object empty = null;
            for (java.lang.reflect.Field f : styleCls.getFields()) {
                if (Modifier.isStatic(f.getModifiers()) && f.getType() == styleCls) { empty = f.get(null); break; }
            }
            if (empty == null) return;
            Object st = null;
            for (Method m : styleCls.getMethods()) {
                if (m.getParameterCount() != 1 || m.getReturnType() != styleCls || Modifier.isStatic(m.getModifiers())) continue;
                Object arg = fontArg(m.getParameterTypes()[0], id);
                if (arg != null) { st = m.invoke(empty, arg); break; }
            }
            if (st == null) return;
            style = st;

            // TextRenderer.getWidth(Text)
            for (Method m : class_327.class.getMethods()) {
                if (m.getParameterCount() == 1 && m.getReturnType() == int.class && !Modifier.isStatic(m.getModifiers())
                        && m.getParameterTypes()[0].isAssignableFrom(class_2561.class)
                        && !m.getParameterTypes()[0].isAssignableFrom(String.class)) {
                    textWidth = m;
                    break;
                }
            }
            // DrawContext.drawText(TextRenderer, Text, int, int, int, boolean)
            for (Method m : class_332.class.getMethods()) {
                Class<?>[] p = m.getParameterTypes();
                if (p.length == 6 && p[0] == class_327.class && p[1].isAssignableFrom(class_2561.class)
                        && p[2] == int.class && p[3] == int.class && p[4] == int.class && p[5] == boolean.class) {
                    drawText = m;
                    break;
                }
            }
            if (textWidth == null || drawText == null) return;

            // пробный прогон: если что-то не так - выключаемся здесь, а не в середине кадра
            ok = true;
            if (text("A") == null) ok = false;
        } catch (Throwable t) {
            ok = false;
        }
    }

    public static boolean active() {
        if (!tried) init();
        return ok && Config.hudFont;
    }

    private static class_2561 text(String s) throws Exception {
        class_2561 t = CACHE.get(s);
        if (t == null) {
            Object mt = class_2561.method_43470(s);
            Object styled = setStyle.invoke(mt, style);
            t = (class_2561) (styled != null ? styled : mt);
            CACHE.put(s, t);
        }
        return t;
    }

    /** Ширина строки в нужном шрифте (если шрифт не включён - как раньше). */
    public static int width(class_327 tr, String s) {
        if (active()) {
            try { return (Integer) textWidth.invoke(tr, text(s)); }
            catch (Throwable t) { fail(); }
        }
        return tr.method_1727(s);
    }

    /** Рисует строку своим шрифтом. @return false, если шрифт недоступен (тогда рисуйте обычным способом). */
    public static boolean draw(class_332 g, class_327 tr, String s, int x, int y, int argb, boolean shadow) {
        if (!active()) return false;
        try {
            drawText.invoke(g, tr, text(s), x, y, argb, shadow);
            return true;
        } catch (Throwable t) {
            fail();
            return false;
        }
    }
}
