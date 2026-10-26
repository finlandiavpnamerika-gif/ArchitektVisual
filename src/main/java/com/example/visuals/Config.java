package com.example.visuals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Config {
    private Config() {}

    public static boolean customMainMenu = true;
    
    // Время и Погода
    public static boolean customTime = false;
    public static int timeValue = 6000;
    public static boolean customWeather = false;
    public static int weatherType = 0;

    // Кастомное небо
    public static boolean customSky = false;
    public static int skyColor1 = 0x80003366;
    public static int skyColor2 = 0x808B5CF6;
    public static boolean skyLines = true;
    public static double skySpeed = 1.0;

    public static boolean sprint = false;
    public static boolean sprintAllDirs = false;
    public static boolean zoom = true;
    public static double zoomFov = 30.0;
    public static boolean fullbright = false;
    public static double gamma = 10.0;
    
    public static boolean hud = true;
    public static boolean hudFps = true;
    public static boolean hudPing = true;
    public static boolean hudCoords = true;
    public static boolean hudFacing = true;
    public static boolean hudEffects = true;
    public static double hudScale = 1.0;
    public static int hudX = 4;
    public static int hudY = 4;
    public static int hudColor = 2;

    public static boolean arrayList = true;
    public static int arrayListColor = 2;
    public static boolean arrayListShadow = true;
    public static int arrayListX = 4;
    public static int arrayListY = 4;

    public static boolean limitDist = false;
    public static double viewDist = 8.0;

    public static boolean autoswap = true;
    public static boolean itembind = true;
    public static final List<SwapPair> swapPairs = new ArrayList<>();
    public static final List<BoundItem> binds = new ArrayList<>();
    
    public static final List<String> alts = new ArrayList<>();
    public static final List<String> friends = new ArrayList<>();
    
    // Свечение друзей (Outline)
    public static boolean friendGlow = true; 
    
    public static boolean trails = true;
    public static int trailsColor = 2;
    public static double trailsLength = 1.0;

    public static boolean keystrokes = true;
    public static int keystrokesX = 4;
    public static int keystrokesY = 150;
    public static double keystrokesScale = 1.0;
    public static int keystrokesColor = 2;
    public static boolean keystrokesShowMouse = true;
    public static boolean keystrokesShowSpace = true;

    public static boolean armorHud = true;
    public static int armorHudX = 4;
    public static int armorHudY = 220;
    public static double armorHudScale = 1.0;
    public static boolean armorHudShowTool = true;
    public static boolean armorHudShowDurability = true;

    public static boolean autoReconnect = false;
    public static double autoReconnectDelay = 5.0;
    public static int themeColor = 2;
    public static boolean showIcons = true;
    public static boolean showCheckmarks = true;

    public static final int[] COLORS = {-4883201, -11141121, -11141291, -171, -43691};
    public static final String[] COLOR_NAMES = {"Фиолетовый", "Голубой", "Зелёный", "Жёлтый", "Красный"};

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("visualsmod.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void save() {
        try {
            JsonObject o = new JsonObject();
            o.addProperty("customMainMenu", customMainMenu);
            o.addProperty("customTime", customTime);
            o.addProperty("timeValue", timeValue);
            o.addProperty("customWeather", customWeather);
            o.addProperty("weatherType", weatherType);
            o.addProperty("customSky", customSky);
            o.addProperty("skyLines", skyLines);
            o.addProperty("skySpeed", skySpeed);
            o.addProperty("friendGlow", friendGlow);
            o.addProperty("trails", trails);
            o.addProperty("trailsColor", trailsColor);
            
            // Сохранение остальных настроек
            o.addProperty("sprint", sprint);
            o.addProperty("sprintAllDirs", sprintAllDirs);
            o.addProperty("zoom", zoom);
            o.addProperty("zoomFov", zoomFov);
            o.addProperty("fullbright", fullbright);
            o.addProperty("gamma", gamma);
            o.addProperty("hud", hud);
            o.addProperty("hudFps", hudFps);
            o.addProperty("hudPing", hudPing);
            o.addProperty("hudCoords", hudCoords);
            o.addProperty("hudFacing", hudFacing);
            o.addProperty("hudEffects", hudEffects);
            o.addProperty("hudScale", hudScale);
            o.addProperty("hudX", hudX);
            o.addProperty("hudY", hudY);
            o.addProperty("hudColor", hudColor);
            o.addProperty("arrayList", arrayList);
            o.addProperty("arrayListColor", arrayListColor);
            o.addProperty("arrayListShadow", arrayListShadow);
            o.addProperty("arrayListX", arrayListX);
            o.addProperty("arrayListY", arrayListY);
            o.addProperty("limitDist", limitDist);
            o.addProperty("viewDist", viewDist);
            o.addProperty("autoswap", autoswap);
            o.addProperty("itembind", itembind);
            o.addProperty("keystrokes", keystrokes);
            o.addProperty("keystrokesX", keystrokesX);
            o.addProperty("keystrokesY", keystrokesY);
            o.addProperty("keystrokesScale", keystrokesScale);
            o.addProperty("keystrokesColor", keystrokesColor);
            o.addProperty("keystrokesShowMouse", keystrokesShowMouse);
            o.addProperty("keystrokesShowSpace", keystrokesShowSpace);
            o.addProperty("armorHud", armorHud);
            o.addProperty("armorHudX", armorHudX);
            o.addProperty("armorHudY", armorHudY);
            o.addProperty("armorHudScale", armorHudScale);
            o.addProperty("armorHudShowTool", armorHudShowTool);
            o.addProperty("armorHudShowDurability", armorHudShowDurability);
            o.addProperty("autoReconnect", autoReconnect);
            o.addProperty("autoReconnectDelay", autoReconnectDelay);
            o.addProperty("themeColor", themeColor);
            o.addProperty("showIcons", showIcons);
            o.addProperty("showCheckmarks", showCheckmarks);

            JsonArray sp = new JsonArray();
            for (SwapPair p : swapPairs) {
                JsonObject e = new JsonObject();
                e.addProperty("a", p.a);
                e.addProperty("b", p.b);
                e.addProperty("key", p.key);
                sp.add(e);
            }
            o.add("swapPairs", sp);

            JsonArray bl = new JsonArray();
            for (BoundItem b : binds) {
                JsonObject e = new JsonObject();
                e.addProperty("name", b.name);
                e.addProperty("key", b.key);
                bl.add(e);
            }
            o.add("binds", bl);

            JsonArray altsArray = new JsonArray();
            for (String alt : alts) altsArray.add(alt);
            o.add("alts", altsArray);

            JsonArray friendsArray = new JsonArray();
            for (String f : friends) friendsArray.add(f);
            o.add("friends", friendsArray);

            Files.writeString(FILE, GSON.toJson(o));
        } catch (Exception ignored) {}
    }

    public static void load() {
        boolean hadLists = false;
        try {
            if (Files.exists(FILE)) {
                JsonObject o = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();
                customMainMenu = b(o, "customMainMenu", customMainMenu);
                customTime = b(o, "customTime", customTime);
                timeValue = (int) d(o, "timeValue", timeValue);
                customWeather = b(o, "customWeather", customWeather);
                weatherType = (int) d(o, "weatherType", weatherType);
                customSky = b(o, "customSky", customSky);
                skyLines = b(o, "skyLines", skyLines);
                skySpeed = d(o, "skySpeed", skySpeed);
                friendGlow = b(o, "friendGlow", friendGlow);
                trails = b(o, "trails", trails);
                trailsColor = clampColor((int) d(o, "trailsColor", trailsColor));

                sprint = b(o, "sprint", sprint);
                sprintAllDirs = b(o, "sprintAllDirs", sprintAllDirs);
                zoom = b(o, "zoom", zoom);
                zoomFov = d(o, "zoomFov", zoomFov);
                fullbright = b(o, "fullbright", fullbright);
                gamma = d(o, "gamma", gamma);
                hud = b(o, "hud", hud);
                hudFps = b(o, "hudFps", hudFps);
                hudPing = b(o, "hudPing", hudPing);
                hudCoords = b(o, "hudCoords", hudCoords);
                hudFacing = b(o, "hudFacing", hudFacing);
                hudEffects = b(o, "hudEffects", hudEffects);
                hudScale = d(o, "hudScale", hudScale);
                hudX = (int) d(o, "hudX", hudX);
                hudY = (int) d(o, "hudY", hudY);
                hudColor = clampColor((int) d(o, "hudColor", hudColor));
                arrayList = b(o, "arrayList", arrayList);
                arrayListColor = clampColor((int) d(o, "arrayListColor", arrayListColor));
                arrayListShadow = b(o, "arrayListShadow", arrayListShadow);
                arrayListX = (int) d(o, "arrayListX", arrayListX);
                arrayListY = (int) d(o, "arrayListY", arrayListY);
                limitDist = b(o, "limitDist", limitDist);
                viewDist = Math.max(2.0, Math.min(32.0, d(o, "viewDist", viewDist)));
                autoswap = b(o, "autoswap", autoswap);
                itembind = b(o, "itembind", itembind);
                keystrokes = b(o, "keystrokes", keystrokes);
                keystrokesX = (int) d(o, "keystrokesX", keystrokesX);
                keystrokesY = (int) d(o, "keystrokesY", keystrokesY);
                keystrokesScale = d(o, "keystrokesScale", keystrokesScale);
                keystrokesColor = clampColor((int) d(o, "keystrokesColor", keystrokesColor));
                keystrokesShowMouse = b(o, "keystrokesShowMouse", keystrokesShowMouse);
                keystrokesShowSpace = b(o, "keystrokesShowSpace", keystrokesShowSpace);
                armorHud = b(o, "armorHud", armorHud);
                armorHudX = (int) d(o, "armorHudX", armorHudX);
                armorHudY = (int) d(o, "armorHudY", armorHudY);
                armorHudScale = d(o, "armorHudScale", armorHudScale);
                armorHudShowTool = b(o, "armorHudShowTool", armorHudShowTool);
                armorHudShowDurability = b(o, "armorHudShowDurability", armorHudShowDurability);
                autoReconnect = b(o, "autoReconnect", autoReconnect);
                autoReconnectDelay = d(o, "autoReconnectDelay", autoReconnectDelay);
                themeColor = clampColor((int) d(o, "themeColor", themeColor));
                showIcons = b(o, "showIcons", showIcons);
                showCheckmarks = b(o, "showCheckmarks", showCheckmarks);

                swapPairs.clear();
                binds.clear();
                alts.clear();
                friends.clear();
                
                if (o.has("swapPairs")) {
                    hadLists = true;
                    for (JsonElement el : o.getAsJsonArray("swapPairs")) {
                        JsonObject e = el.getAsJsonObject();
                        swapPairs.add(new SwapPair(e.get("a").getAsString(), e.get("b").getAsString(),
                                e.has("key") ? e.get("key").getAsInt() : -1));
                    }
                }
                if (o.has("binds")) {
                    hadLists = true;
                    for (JsonElement el : o.getAsJsonArray("binds")) {
                        JsonObject e = el.getAsJsonObject();
                        binds.add(new BoundItem(e.get("name").getAsString(),
                                e.has("key") ? e.get("key").getAsInt() : -1));
                    }
                }
                if (o.has("alts")) for (JsonElement el : o.getAsJsonArray("alts")) alts.add(el.getAsString());
                if (o.has("friends")) for (JsonElement el : o.getAsJsonArray("friends")) friends.add(el.getAsString());
            }
        } catch (Exception ignored) {}
    }

    private static int clampColor(int v) { return Math.max(0, Math.min(COLORS.length - 1, v)); }
    private static boolean b(JsonObject o, String k, boolean def) { return o.has(k) ? o.get(k).getAsBoolean() : def; }
    private static double d(JsonObject o, String k, double def) { return o.has(k) ? o.get(k).getAsDouble() : def; }
}
