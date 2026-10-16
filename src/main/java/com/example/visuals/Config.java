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

    // ================== UTILITIES ==================
    public static boolean sprint = false;
    public static boolean sprintAllDirs = false;
    public static boolean zoom = true;
    public static double zoomFov = 30.0;

    // ================== VISUAL ==================
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
    public static int hudColor = 0;

    public static boolean arrayList = true;
    public static int arrayListColor = 0;
    public static boolean arrayListShadow = true;
    public static int arrayListX = 4;
    public static int arrayListY = 4;

    // ================== WORLD ==================
    public static boolean limitDist = false;
    public static double viewDist = 8.0;

    // ================== CLIENT ==================
    public static boolean autoswap = true;
    public static boolean itembind = true;
    public static final List<SwapPair> swapPairs = new ArrayList<>();
    public static final List<BoundItem> binds = new ArrayList<>();

    public static boolean keystrokes = true;
    public static int keystrokesX = 4;
    public static int keystrokesY = 150;
    public static double keystrokesScale = 1.0;
    public static int keystrokesColor = 0;
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

    // ================== СТИЛЬ ==================
    public static int themeColor = 0;          // индекс из COLORS
    public static boolean showIcons = true;    // показывать иконки категорий
    public static boolean showCheckmarks = true; // показывать ✔ у включённых

    // ================== ПАЛИТРА ==================
    public static final int[] COLORS = {
            -4883201,    // фиолетовый (по умолчанию)
            -11141121,   // голубой
            -11141291,   // зелёный
            -171,        // жёлтый
            -43691       // красный
    };
    public static final String[] COLOR_NAMES = {
            "Фиолетовый", "Голубой", "Зелёный", "Жёлтый", "Красный"
    };

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("visualsmod.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void save() {
        try {
            JsonObject o = new JsonObject();

            // Utilities
            o.addProperty("sprint", sprint);
            o.addProperty("sprintAllDirs", sprintAllDirs);
            o.addProperty("zoom", zoom);
            o.addProperty("zoomFov", zoomFov);

            // Visual
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

            // World
            o.addProperty("limitDist", limitDist);
            o.addProperty("viewDist", viewDist);

            // Client
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

            // Стиль
            o.addProperty("themeColor", themeColor);
            o.addProperty("showIcons", showIcons);
            o.addProperty("showCheckmarks", showCheckmarks);

            // Списки
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

            Files.writeString(FILE, GSON.toJson(o));
        } catch (Exception ignored) {
        }
    }

    public static void load() {
        boolean hadLists = false;
        try {
            if (Files.exists(FILE)) {
                JsonObject o = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();

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
            }
        } catch (Exception ignored) {
        }
        if (!hadLists) {
            importLegacy();
        }
    }

    private static void importLegacy() {
        boolean changed = false;
        try {
            Path dir = FabricLoader.getInstance().getConfigDir();
            Path as = dir.resolve("autoswap.txt");
            if (Files.exists(as)) {
                for (String line : Files.readAllLines(as, StandardCharsets.UTF_8)) {
                    String[] p = line.split("\t");
                    if (p.length == 3) {
                        swapPairs.add(new SwapPair(p[1], p[2], Integer.parseInt(p[0])));
                        changed = true;
                    }
                }
            }
            Path ib = dir.resolve("itembind.txt");
            if (Files.exists(ib)) {
                for (String line : Files.readAllLines(ib, StandardCharsets.UTF_8)) {
                    String[] p = line.split("\t");
                    if (p.length == 2) {
                        binds.add(new BoundItem(p[1], Integer.parseInt(p[0])));
                        changed = true;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        if (changed) save();
    }

    private static int clampColor(int v) {
        return Math.max(0, Math.min(COLORS.length - 1, v));
    }

    private static boolean b(JsonObject o, String k, boolean def) {
        return o.has(k) ? o.get(k).getAsBoolean() : def;
    }

    private static double d(JsonObject o, String k, double def) {
        return o.has(k) ? o.get(k).getAsDouble() : def;
    }
}
