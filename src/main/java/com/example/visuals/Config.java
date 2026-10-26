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
    
    // Crosshair
    public static boolean customCrosshair = true;
    public static int crosshairColor = 0; // 0-4 из COLORS
    public static int crosshairSize = 8;
    public static int crosshairThickness = 2;
    public static boolean crosshairDot = true;

    public static boolean customTime = false;
    public static int timeValue = 6000;
    public static boolean customWeather = false;
    public static int weatherType = 0;

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
            o.addProperty("customCrosshair", customCrosshair);
            o.addProperty("crosshairColor", crosshairColor);
            o.addProperty("crosshairSize", crosshairSize);
            o.addProperty("crosshairThickness", crosshairThickness);
            o.addProperty("crosshairDot", crosshairDot);
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
            
            Files.writeString(FILE, GSON.toJson(o));
        } catch (Exception ignored) {}
    }

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                JsonObject o = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();
                customMainMenu = b(o, "customMainMenu", customMainMenu);
                customCrosshair = b(o, "customCrosshair", customCrosshair);
                crosshairColor = clampColor((int) d(o, "crosshairColor", crosshairColor));
                crosshairSize = (int) d(o, "crosshairSize", crosshairSize);
                crosshairThickness = (int) d(o, "crosshairThickness", crosshairThickness);
                crosshairDot = b(o, "crosshairDot", crosshairDot);
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
            }
        } catch (Exception ignored) {}
    }

    private static int clampColor(int v) { return Math.max(0, Math.min(COLORS.length - 1, v)); }
    private static boolean b(JsonObject o, String k, boolean def) { return o.has(k) ? o.get(k).getAsBoolean() : def; }
    private static double d(JsonObject o, String k, double def) { return o.has(k) ? o.get(k).getAsDouble() : def; }
}
