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
            Map.entry("freelook", new String[]{"freelookToggle", "freelookThird", "freelookSmooth", "freelookSens"}),
            Map.entry("zoom", new String[]{"zoomFov"}),
            Map.entry("fb", new String[]{"gamma"}),
            Map.entry("cross", new String[]{"crosshairColor", "crosshairSize", "crosshairThickness", "crosshairDot"}),
            Map.entry("trails", new String[]{"trailsCustom", "trailsHue", "trailsSat", "trailsVal", "trailsColor", "trailsLength", "trailsHeight"}),
            Map.entry("zones", new String[]{"zonesHeld"}),
            Map.entry("tesp", new String[]{"targetEspShape", "targetEspHue", "targetEspHitHue", "targetEspSize", "targetEspSpeed", "targetEspAlpha"}),
            Map.entry("hv", new String[]{"leftHanded", "svHand", "sv", "svNoSwing"}),
            Map.entry("thud", new String[]{"thScale", "thX", "thY", "thHue", "thDist"}),
            Map.entry("fog", new String[]{"fogOff", "fogStart", "fogEnd", "fogHue", "fogSat", "fogVal"}),
            Map.entry("dmg", new String[]{"dfMine", "dfDecimals", "dfHue", "dfSize", "dfLife"}),
            Map.entry("part", new String[]{"partMode", "partShape", "partHue", "partRainbow", "partCount", "partSize", "partLife", "partGravity", "partAlpha"}),
            Map.entry("sky", new String[]{"skyPreset", "skyAlpha", "skySpeed", "skyNightOnly", "skyStars"}),
            Map.entry("hud", new String[]{"hudHoriz", "hudFps", "hudPing", "hudCoords", "hudFacing", "hudScale", "hudAlpha", "hudColor", "hudX", "hudY"}),
            Map.entry("array", new String[]{"arrayListHoriz", "arrayListColor", "arrayListShadow", "arrayListScale", "arrayListAlpha", "arrayListX", "arrayListY"}),
            Map.entry("armor", new String[]{"armorHudHoriz", "armorHudShowTool", "armorHudShowDurability", "armorHudScale", "armorHudAlpha", "armorHudX", "armorHudY"}),
            Map.entry("potion", new String[]{"potionHoriz", "potionIcons", "potionBars", "potionLevel", "potionScale", "potionAlpha", "potionColor", "potionX", "potionY"}),
            Map.entry("jc", new String[]{"jcStyle", "jcHue", "jcRainbow", "jcSize", "jcWidth", "jcLife", "jcAlpha", "jcLand"}),
            Map.entry("keys", new String[]{"keystrokesHoriz", "keystrokesShowMouse", "keystrokesShowSpace", "keystrokesScale", "keystrokesAlpha", "keystrokesColor", "keystrokesX", "keystrokesY"}),
            Map.entry("time", new String[]{"timeValue"}),
            Map.entry("weather", new String[]{"weatherType"}),
            Map.entry("hat", new String[]{"hatCustom", "hatHue", "hatSat", "hatVal", "chinaHatMode", "hatSize", "hatHeight"}),
            Map.entry("physic", new String[]{"physicLift"}),
            Map.entry("server", new String[]{"serverIp", "serverTps", "serverPing", "serverColor", "serverScale", "serverAlpha", "serverX", "serverY"}),
            Map.entry("clock", new String[]{"clockSeconds", "clockDate", "clockGame", "clockColor", "clockScale", "clockAlpha", "clockX", "clockY"}),
            Map.entry("inv", new String[]{"invHotbar", "invColor", "invScale", "invAlpha", "invX", "invY"}),
            Map.entry("totem", new String[]{"totemHideZero", "totemColor", "totemScale", "totemAlpha", "totemX", "totemY"}),
            Map.entry("crit", new String[]{"critStyle", "critCount"}),
            Map.entry("kill", new String[]{"killFlash", "killSound", "killParticles", "killColor"}),
            Map.entry("atk", new String[]{"atkStyle", "atkColor", "atkSize", "atkAlways", "atkOffset"}),
            Map.entry("stats", new String[]{"statsAcc", "statsKills", "statsStreak", "statsDeaths", "statsKd", "statsPlayersOnly", "statsColor", "statsScale", "statsAlpha", "statsX", "statsY"}),
            Map.entry("dlog", new String[]{"dlDealt", "dlTaken", "dlLines", "dlLife", "dlColor", "dlScale", "dlAlpha", "dlX", "dlY"}),
            Map.entry("hbox", new String[]{"hbPlayers", "hbMobs", "hbOthers", "hbSelf", "hbFill", "hbColorP", "hbColorM", "hbColorO", "hbWidth", "hbRange", "hbAlpha"}),
            Map.entry("bout", new String[]{"boColor", "boRainbow", "boWidth", "boFill", "boFillAlpha", "boHideVanilla"}),
            Map.entry("chunk", new String[]{"cbRadius", "cbHeight", "cbColor", "cbWidth", "cbLevels", "cbAlpha"}),
            Map.entry("iesp", new String[]{"ieRange", "ieValuable", "ieFill", "ieTracers", "ieColor", "ieColorHi", "ieWidth"}),
            Map.entry("ptr", new String[]{"ptColor", "ptRainbow", "ptLength", "ptWidth"}),
            Map.entry("etr", new String[]{"etColor", "etRainbow", "etLength", "etWidth", "etAll"}),
            Map.entry("pcd", new String[]{"pcdHideIdle", "pcdColor", "pcdScale", "pcdAlpha", "pcdX", "pcdY"}),
            Map.entry("wp", new String[]{"wpBeam", "wpLabels", "wpDist", "wpMax", "wpColor", "wpScale", "wpDeath"}),
            Map.entry("wings", new String[]{"wingStyle", "wingColor", "wingRainbow", "wingSize", "wingSpeed", "wingFriends"}),
            Map.entry("aura", new String[]{"auraStyle", "auraColor", "auraRainbow", "auraSize", "auraSpeed", "auraFriends"}),
            Map.entry("cape", new String[]{"capeColor", "capeColor2", "capeRainbow", "capeWave", "capeFriends"}),
            Map.entry("nt", new String[]{"ntRange", "ntScale", "ntBg", "ntHealth", "ntDist", "ntDistScale", "ntHide"}),
            Map.entry("chams", new String[]{"chPlayers", "chMobs", "chFriends", "chSelf"}),
            Map.entry("swing", new String[]{"swingSpeed", "swingSelfOnly"}),
            Map.entry("clouds", new String[]{"cloudHide", "cloudTint", "cloudHue", "cloudSat", "cloudRainbow", "cloudHeightOffset"}),
            Map.entry("tip", new String[]{"tipDur", "tipId", "tipStack"}),
            Map.entry("noov", new String[]{"noHurtShake", "noFireOverlay", "noTotemParticles", "noFireworkParticles"}),
            Map.entry("hitsound", new String[]{"hitSoundType", "hitSoundVol", "hitSoundPitch"}),
            Map.entry("combo", new String[]{"comboColor", "comboTimeout", "comboScale", "comboAlpha", "comboX", "comboY"}),
            Map.entry("wm", new String[]{"watermarkName", "watermarkNick", "watermarkFps", "watermarkTime", "watermarkRainbow", "watermarkColor", "watermarkScale", "watermarkAlpha", "watermarkX", "watermarkY"})
    );

    private static final Map<String, Object> DEFAULTS = new HashMap<>();

    /** Вызвать до Config.load(): запоминает значения по умолчанию. */
    public static void capture() {
        for (Field f : Config.class.getFields()) {
            if (!Modifier.isStatic(f.getModifiers())) continue;
            try { DEFAULTS.put(f.getName(), copy(f.get(null))); } catch (Exception ignored) {}
        }
    }

    /** Сбрасывает все числовые/булевы/массивные настройки Config к значениям по умолчанию (списки не трогает). */
    public static void resetAll() {
        for (Field f : Config.class.getFields()) {
            if (!Modifier.isStatic(f.getModifiers())) continue;
            try {
                Object d = DEFAULTS.get(f.getName());
                if (d == null) continue;
                Object cur = f.get(null);
                if (cur instanceof java.util.Collection) continue;
                if (cur != null && cur.getClass().isArray()) fill(cur, d);
                else if (!Modifier.isFinal(f.getModifiers())) f.set(null, copy(d));
            } catch (Exception ignored) {}
        }
    }

    /** Переключатели модулей (то, что включается в меню). */
    private static final String[] TOGGLES = {
        "sprint", "zoom", "autoRespawn", "thirdPerson", "fullbright", "customCrosshair", "trails", "zones", "targetEsp",
        "jumpCircle", "customItems", "handView", "targetHud", "customFog", "damageFever", "particles", "skyShader",
        "hud", "arrayList", "armorHud", "potionHud", "keystrokes", "autoswap", "itembind", "customTime", "customWeather",
        "chinaHat", "predictions", "itemPhysic", "shulkerPreview", "highlight", "streamerMode",
        "noOverlay", "hitSound", "comboHud", "watermark",
        "serverHud", "clockHud", "invHud", "totemHud",
        "critFx", "killFx", "atkInd", "statsHud", "dlHud",
        "hbox", "bout", "chunkB", "itemEsp", "wp", "ptrail", "etrail", "pcdHud",
        "wingsOn", "auraOn", "capeOn", "ntOn", "chamsOn", "swingAnim", "cloudsOn", "tipOn"
    };

    /** Выключает все модули (настройки внутри модулей не трогает). */
    public static void disableAll() {
        for (String n : TOGGLES) {
            try { Config.class.getField(n).setBoolean(null, false); } catch (Exception ignored) {}
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
