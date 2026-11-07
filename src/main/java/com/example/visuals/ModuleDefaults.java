package com.example.visuals;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/** Значения настроек по умолчанию и сброс настроек модуля (кнопка "Сбросить настройки" в меню). */
public final class ModuleDefaults {
    private ModuleDefaults() {}

    /** какие поля Config относятся к настройкам каждого модуля (без самого переключателя модуля) */
    private static final Map<String, String[]> FIELDS = Map.ofEntries(
            Map.entry("sprint", new String[]{"sprintAllDirs"}),
            Map.entry("zoom", new String[]{"zoomFov"}),
            Map.entry("fb", new String[]{"gamma"}),
            Map.entry("cross", new String[]{"crosshairColor", "crosshairSize", "crosshairThickness", "crosshairDot"}),
            Map.entry("trails", new String[]{"trailsCustom", "trailsHue", "trailsSat", "trailsVal", "trailsColor", "trailsLength", "trailsHeight"}),
            Map.entry("zones", new String[]{"zonesHeld"}),
            Map.entry("tesp", new String[]{"targetEspShape", "targetEspHue", "targetEspHitHue", "targetEspSize", "targetEspSpeed", "targetEspAlpha"}),
            Map.entry("ci", new String[]{"ciScale", "ciX", "ciY", "ciZ", "ciRX", "ciRY", "ciRZ"}),
            Map.entry("hv", new String[]{"leftHanded", "svHand", "sv", "svNoSwing"}),
            Map.entry("thud", new String[]{"thScale", "thX", "thY", "thHue", "thDist"}),
            Map.entry("fog", new String[]{"fogOff", "fogStart", "fogEnd", "fogHue", "fogSat", "fogVal"}),
            Map.entry("dmg", new String[]{"dfMine", "dfDecimals", "dfHue", "dfSize", "dfLife"}),
            Map.entry("part", new String[]{"partMode", "partShape", "partHue", "partRainbow", "partCount", "partSize", "partLife", "partGravity", "partAlpha"}),
            Map.entry("sky", new String[]{"skyPreset", "skyAlpha", "skySpeed", "skyNightOnly", "skyStars"}),
            Map.entry("hud", new String[]{"hudFps", "hudPing", "hudCoords", "hudFacing", "hudEffects", "hudScale", "hudAlpha", "hudColor", "hudX", "hudY"}),
            Map.entry("array", new String[]{"arrayListColor", "arrayListShadow", "arrayListScale", "arrayListAlpha", "arrayListX", "arrayListY"}),
            Map.entry("armor", new String[]{"armorHudShowTool", "armorHudShowDurability", "armorHudScale", "armorHudAlpha", "armorHudX", "armorHudY"}),
            Map.entry("keys", new String[]{"keystrokesShowMouse", "keystrokesShowSpace", "keystrokesScale", "keystrokesAlpha", "keystrokesColor", "keystrokesX", "keystrokesY"}),
            Map.entry("time", new String[]{"timeValue"}),
            Map.entry("weather", new String[]{"weatherType"}),
            Map.entry("hat", new String[]{"hatCustom", "hatHue", "hatSat", "hatVal", "chinaHatMode", "hatSize", "hatHeight"}),
            Map.entry("physic", new String[]{"physicLift"})
    );

    private static final Map<String, Object> DEFAULTS = new HashMap<>();

    /** Вызвать до Config.load(): запоминает значения по умолчанию. */
    public static void capture() {
        for (Field f : Config.class.getFields()) {
            if (!Modifier.isStatic(f.getModifiers())) continue;
            try { DEFAULTS.put(f.getName(), copy(f.get(null))); } catch (Exception ignored) {}
        }
    }

    public static boolean has(String id) { return FIELDS.containsKey(id); }

    public static void reset(String id) {
        String[] names = FIELDS.get(id);
        if (names == null) return;
        for (String n : names) {
            try {
                Field f = Config.class.getField(n);
                Object d = DEFAULTS.get(n);
                if (d == null) continue;
                Object cur = f.get(null);
                if (cur != null && cur.getClass().isArray()) fill(cur, d);       // массивы: меняем содержимое
                else if (!Modifier.isFinal(f.getModifiers())) f.set(null, copy(d));
            } catch (Exception ignored) {}
        }
    }

    private static Object copy(Object v) {
        if (v == null || !v.getClass().isArray()) return v;
        int len = Array.getLength(v);
        Object out = Array.newInstance(v.getClass().getComponentType(), len);
        for (int i = 0; i < len; i++) Array.set(out, i, copy(Array.get(v, i)));
        return out;
    }

    private static void fill(Object target, Object src) {
        int len = Math.min(Array.getLength(target), Array.getLength(src));
        for (int i = 0; i < len; i++) {
            Object t = Array.get(target, i), s = Array.get(src, i);
            if (t != null && t.getClass().isArray()) fill(t, s); else Array.set(target, i, s);
        }
    }
}
