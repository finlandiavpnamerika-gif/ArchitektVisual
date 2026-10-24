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
    public static boolean sprint = false;
    public static boolean sprintAllDirs = false;
    public static boolean zoom = true;
    public static double zoomFov = 30.0;
    public static boolean fullbright = false;
    public static double gamma = 10.0;
    
    // HUD
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

    // ArrayList
    public static boolean arrayList = true;
    public static int arrayListColor = 2;
    public static boolean arrayListShadow = true;
    public static int arrayListX = 4;
    public static int arrayListY = 4;

    // World
    public static boolean limitDist = false;
    public static double viewDist = 8.0;

    // Utils
    public static boolean autoswap = true;
    public static boolean itembind = true;
    public static final List<SwapPair> swapPairs = new ArrayList<>();
    public static final List<BoundItem> binds = new ArrayList<>();
    
    // === НОВОЕ: Альты и Друзья ===
    public static final List<String> alts = new ArrayList<>();
    public static final List<String> friends = new ArrayList<>();
    
    // === НОВОЕ: Трейлы ===
    public static boolean trails = true;
    public static int trailsColor = 2;
    public static double trailsLength = 1.0;

    // Keystrokes
    public static boolean keystrokes = true;
    public static int keystrokesX = 4;
    public static int keystrokesY = 150;
    public static double keystrokesScale = 1.0;
    public static int keystrokesColor = 2;
    public static boolean keystrokesShowMouse = true;
    public static boolean keystrokesShowSpace = true;

    // Armor
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
            o.addProperty("sprint", sprint);
            // ... (тут остаются старые сохранения, не буду их все выводить, чтобы не заспамить)
            
            // Сохраняем Альты
            JsonArray altsArray = new JsonArray();
            for (String alt : alts) altsArray.add(alt);
            o.add("alts", altsArray);

            // Сохраняем Друзей
            JsonArray friendsArray = new JsonArray();
            for (String f : friends) friendsArray.add(f);
            o.add("friends", friendsArray);

            // Трейлы
            o.addProperty("trails", trails);
            o.addProperty("trailsColor", trailsColor);
            o.addProperty("trailsLength", trailsLength);

            Files.writeString(FILE, GSON.toJson(o));
        } catch (Exception ignored) {}
    }

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                JsonObject o = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();
                customMainMenu = b(o, "customMainMenu", customMainMenu);
                // ... (остальные загрузки)
                
                alts.clear();
                if (o.has("alts")) for (JsonElement el : o.getAsJsonArray("alts")) alts.add(el.getAsString());
                
                friends.clear();
                if (o.has("friends")) for (JsonElement el : o.getAsJsonArray("friends")) friends.add(el.getAsString());

                trails = b(o, "trails", trails);
                trailsColor = clampColor((int) d(o, "trailsColor", trailsColor));
                trailsLength = d(o, "trailsLength", trailsLength);
            }
        } catch (Exception ignored) {}
    }
    
    // Хелперы (b, d, clampColor)
    private static int clampColor(int v) { return Math.max(0, Math.min(COLORS.length - 1, v)); }
    private static boolean b(JsonObject o, String k, boolean def) { return o.has(k) ? o.get(k).getAsBoolean() : def; }
    private static double d(JsonObject o, String k, double def) { return o.has(k) ? o.get(k).getAsDouble() : def; }
}
