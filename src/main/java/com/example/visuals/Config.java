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
    
    // Визуал (Часть 2 и 3)
    public static boolean customCrosshair = true;
    public static boolean chinaHat = false;
    public static boolean entityBox = false;
    public static boolean predictions = false;
    public static boolean itemPhysic = false;
    public static boolean shulkerPreview = true;

    // Утилиты (Часть 1)
    public static boolean autoRespawn = true;
    public static boolean thirdPerson = false;
    public static boolean itemScroller = false;
    public static final List<String> macros = new ArrayList<>(); // Формат: "Клавиша:Команда"

    // Разное (Часть 1)
    public static boolean streamerMode = false;
    public static boolean boardSpoofer = false;
    public static boolean sounds = true;

    // Старые настройки (не трогаем)
    public static boolean customTime = false;
    public static int timeValue = 6000;
    public static boolean customWeather = false;
    public static int weatherType = 0;
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
    public static boolean trails = true;
    public static int trailsColor = 2;
    public static boolean sprint = false;
    public static boolean sprintAllDirs = false;
    public static boolean zoom = true;
    public static double zoomFov = 30.0;
    public static boolean fullbright = false;
    public static double gamma = 10.0;
    public static boolean autoReconnect = false;
    public static double autoReconnectDelay = 5.0;
    public static boolean keystrokes = true;
    public static boolean armorHud = true;
    public static final List<String> friends = new ArrayList<>();
    public static final List<SwapPair> swapPairs = new ArrayList<>();
    public static final List<BoundItem> binds = new ArrayList<>();

    public static final int[] COLORS = {-4883201, -11141121, -11141291, -171, -43691};
    public static final String[] COLOR_NAMES = {"Фиолетовый", "Голубой", "Зелёный", "Жёлтый", "Красный"};

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("visualsmod.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void save() {
        try {
            JsonObject o = new JsonObject();
            o.addProperty("customCrosshair", customCrosshair);
            o.addProperty("chinaHat", chinaHat);
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
            o.addProperty("customMainMenu", customMainMenu);
            o.addProperty("trails", trails);

            JsonArray mac = new JsonArray();
            for (String m : macros) mac.add(m);
            o.add("macros", mac);

            Files.writeString(FILE, GSON.toJson(o));
        } catch (Exception ignored) {}
    }

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                JsonObject o = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();
                customCrosshair = b(o, "customCrosshair", customCrosshair);
                chinaHat = b(o, "chinaHat", chinaHat);
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
                customMainMenu = b(o, "customMainMenu", customMainMenu);
                trails = b(o, "trails", trails);

                macros.clear();
                if (o.has("macros")) for (JsonElement el : o.getAsJsonArray("macros")) macros.add(el.getAsString());
            }
        } catch (Exception ignored) {}
    }

    private static boolean b(JsonObject o, String k, boolean def) { return o.has(k) ? o.get(k).getAsBoolean() : def; }
}
