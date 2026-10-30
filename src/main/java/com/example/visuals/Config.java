package com.example.visuals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Config {
    private Config() {}

    public static boolean customMainMenu = true;

    // --- Визуал ---
    public static boolean customCrosshair = true;
    public static boolean chinaHat = false;
    public static int chinaHatColor = 0xFF859CFF; // Добавлено
    public static int chinaHatMode = 0; // 0-4 = COLORS, 5 = радуга
    public static boolean entityBox = false;
    public static boolean predictions = false;
    public static boolean itemPhysic = false;
    public static boolean shulkerPreview = true;
    public static int crosshairColor = 0;
    public static int crosshairSize = 8;
    public static int crosshairThickness = 2;
    public static boolean crosshairDot = true;

    // --- Утилиты ---
    public static boolean autoRespawn = true;
    public static boolean thirdPerson = false;
    public static boolean itemScroller = false;
    public static final List<String> macros = new ArrayList<>();

    // --- Разное ---
    public static boolean streamerMode = false;
    public static boolean boardSpoofer = false;
    public static boolean sounds = true;

    // --- Время и Погода ---
    public static boolean customTime = false;
    public static int timeValue = 6000;
    public static boolean customWeather = false;
    public static int weatherType = 0; // 0 = Ясно, 1 = Дождь, 2 = Гроза

    // --- HUD ---
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

    // --- ArrayList ---
    public static boolean arrayList = true;
    public static int arrayListColor = 2;
    public static boolean arrayListShadow = true;
    public static int arrayListX = 4;
    public static int arrayListY = 4;

    // --- Keystrokes ---
    public static boolean keystrokes = true;
    public static int keystrokesX = 4;
    public static int keystrokesY = 150;
    public static double keystrokesScale = 1.0;
    public static int keystrokesColor = 2;
    public static boolean keystrokesShowMouse = true; // Добавлено
    public static boolean keystrokesShowSpace = true;

    // --- Armor ---
    public static boolean armorHud = true;
    public static int armorHudX = 4;
    public static int armorHudY = 220;
    public static double armorHudScale = 1.0;
    public static boolean armorHudShowTool = true;
    public static boolean armorHudShowDurability = true;

    // --- Utils (Остальное) ---
    public static boolean sprint = false;
    public static boolean sprintAllDirs = false;
    public static boolean zoom = true;
    public static double zoomFov = 30.0;
    public static boolean fullbright = false;
    public static double gamma = 10.0;
    public static boolean limitDist = false;
    public static double viewDist = 8.0;
    public static boolean autoReconnect = false;
    public static double autoReconnectDelay = 5.0;

    // --- AutoSwap & ItemBind ---
    public static boolean autoswap = true;
    public static boolean itembind = true;
    public static final List<SwapPair> swapPairs = new ArrayList<>();
    public static final List<BoundItem> binds = new ArrayList<>();

    // --- Трейлы ---
    public static boolean trails = true;
    public static int trailsColor = 2;

    // --- Друзья ---
    public static final List<String> friends = new ArrayList<>();

    public static final int[] COLORS = {-4883201, -11141121, -11141291, -171, -43691};
    public static final String[] COLOR_NAMES = {"Фиолетовый", "Голубой", "Зелёный", "Жёлтый", "Красный"};

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("visualsmod.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void save() {
        try {
            JsonObject o = new JsonObject();
            o.addProperty("customMainMenu", customMainMenu);
            o.addProperty("customCrosshair", customCrosshair);
            o.addProperty("chinaHat", chinaHat);
            o.addProperty("chinaHatColor", chinaHatColor);
            o.addProperty("chinaHatMode", chinaHatMode);
            o.addProperty("entityBox", entityBox);
            o.addProperty("predictions", predictions);
            o.addProperty("itemPhysic", itemPhysic);
            o.addProperty("shulkerPreview", shulkerPreview);
            o.addProperty("autoRespawn", autoRespawn);
            o.addProperty("thirdPerson", thirdPerson);
            o.addProperty("itemScroller", itemScroller);
            o.addProperty("streamerMode", streamerMode);
            o.addProperty("boardSpoofer", boardSpoofer);
            o.addProperty("sounds", sounds);
            o.addProperty("trails", trails);
            o.addProperty("trailsColor", trailsColor);

            // HUD
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

            // ArrayList & Keystrokes
            o.addProperty("arrayList", arrayList);
            o.addProperty("arrayListColor", arrayListColor);
            o.addProperty("arrayListShadow", arrayListShadow);
            o.addProperty("arrayListX", arrayListX);
            o.addProperty("arrayListY", arrayListY);
            o.addProperty("keystrokes", keystrokes);
            o.addProperty("keystrokesX", keystrokesX);
            o.addProperty("keystrokesY", keystrokesY);
            o.addProperty("keystrokesScale", keystrokesScale);
            o.addProperty("keystrokesColor", keystrokesColor);
            o.addProperty("keystrokesShowMouse", keystrokesShowMouse);
            o.addProperty("keystrokesShowSpace", keystrokesShowSpace);
            
            // Armor
            o.addProperty("armorHud", armorHud);
            o.addProperty("armorHudX", armorHudX);
            o.addProperty("armorHudY", armorHudY);
            o.addProperty("armorHudScale", armorHudScale);
            o.addProperty("armorHudShowTool", armorHudShowTool);
            o.addProperty("armorHudShowDurability", armorHudShowDurability);

            // Время и погода
            o.addProperty("customTime", customTime);
            o.addProperty("timeValue", timeValue);
            o.addProperty("customWeather", customWeather);
            o.addProperty("weatherType", weatherType);

            // Макросы
            JsonArray mac = new JsonArray();
            for (String m : macros) mac.add(m);
            o.add("macros", mac);

            // Друзья
            JsonArray fArr = new JsonArray();
            for (String f : friends) fArr.add(f);
            o.add("friends", fArr);

            Files.writeString(FILE, GSON.toJson(o));
        } catch (Exception ignored) {}
    }

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                JsonObject o = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();
                customMainMenu = b(o, "customMainMenu", customMainMenu);
                customCrosshair = b(o, "customCrosshair", customCrosshair);
                chinaHat = b(o, "chinaHat", chinaHat);
                chinaHatColor = (int) d(o, "chinaHatColor", chinaHatColor);
                chinaHatMode = (int) d(o, "chinaHatMode", chinaHatMode);
                entityBox = b(o, "entityBox", entityBox);
                predictions = b(o, "predictions", predictions);
                itemPhysic = b(o, "itemPhysic", itemPhysic);
                shulkerPreview = b(o, "shulkerPreview", shulkerPreview);
                autoRespawn = b(o, "autoRespawn", autoRespawn);
                thirdPerson = b(o, "thirdPerson", thirdPerson);
                itemScroller = b(o, "itemScroller", itemScroller);
                streamerMode = b(o, "streamerMode", streamerMode);
                boardSpoofer = b(o, "boardSpoofer", boardSpoofer);
                sounds = b(o, "sounds", sounds);
                trails = b(o, "trails", trails);
                trailsColor = (int) d(o, "trailsColor", trailsColor);

                // HUD
                hud = b(o, "hud", hud);
                hudFps = b(o, "hudFps", hudFps);
                hudPing = b(o, "hudPing", hudPing);
                hudCoords = b(o, "hudCoords", hudCoords);
                hudFacing = b(o, "hudFacing", hudFacing);
                hudEffects = b(o, "hudEffects", hudEffects);
                hudScale = d(o, "hudScale", hudScale);
                hudX = (int) d(o, "hudX", hudX);
                hudY = (int) d(o, "hudY", hudY);
                hudColor = (int) d(o, "hudColor", hudColor);

                // ArrayList & Keystrokes
                arrayList = b(o, "arrayList", arrayList);
                arrayListColor = (int) d(o, "arrayListColor", arrayListColor);
                arrayListShadow = b(o, "arrayListShadow", arrayListShadow);
                arrayListX = (int) d(o, "arrayListX", arrayListX);
                arrayListY = (int) d(o, "arrayListY", arrayListY);
                keystrokes = b(o, "keystrokes", keystrokes);
                keystrokesX = (int) d(o, "keystrokesX", keystrokesX);
                keystrokesY = (int) d(o, "keystrokesY", keystrokesY);
                keystrokesScale = d(o, "keystrokesScale", keystrokesScale);
                keystrokesColor = (int) d(o, "keystrokesColor", keystrokesColor);
                keystrokesShowMouse = b(o, "keystrokesShowMouse", keystrokesShowMouse);
                keystrokesShowSpace = b(o, "keystrokesShowSpace", keystrokesShowSpace);

                // Armor
                armorHud = b(o, "armorHud", armorHud);
                armorHudX = (int) d(o, "armorHudX", armorHudX);
                armorHudY = (int) d(o, "armorHudY", armorHudY);
                armorHudScale = d(o, "armorHudScale", armorHudScale);
                armorHudShowTool = b(o, "armorHudShowTool", armorHudShowTool);
                armorHudShowDurability = b(o, "armorHudShowDurability", armorHudShowDurability);

                // Время и погода
                customTime = b(o, "customTime", customTime);
                timeValue = (int) d(o, "timeValue", timeValue);
                customWeather = b(o, "customWeather", customWeather);
                weatherType = (int) d(o, "weatherType", weatherType);

                macros.clear();
                if (o.has("macros")) for (JsonElement el : o.getAsJsonArray("macros")) macros.add(el.getAsString());

                friends.clear();
                if (o.has("friends")) for (JsonElement el : o.getAsJsonArray("friends")) friends.add(el.getAsString());
            }
        } catch (Exception ignored) {}
    }

    private static boolean b(JsonObject o, String k, boolean def) { return o.has(k) ? o.get(k).getAsBoolean() : def; }
    private static double d(JsonObject o, String k, double def) { return o.has(k) ? o.get(k).getAsDouble() : def; }
}
