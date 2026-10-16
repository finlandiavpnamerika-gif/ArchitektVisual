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

    public static boolean fullbright = false;
    public static double gamma = 10.0;
    public static boolean zoom = true;
    public static double zoomFov = 30.0;
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
    public static boolean sprint = false;
    public static boolean sprintAllDirs = false;
    public static boolean limitDist = false;
    public static double viewDist = 8.0;

    public static boolean autoswap = true;
    public static boolean itembind = true;
    public static final List<SwapPair> swapPairs = new ArrayList<>();
    public static final List<BoundItem> binds = new ArrayList<>();

    public static final int[] COLORS = {-4883201, -11141121, -11141291, -171, -43691};
    public static final String[] COLOR_NAMES = {"Фиолетовый", "Голубой", "Зелёный", "Жёлтый", "Красный"};

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("visualsmod.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void save() {
        try {
            JsonObject o = new JsonObject();
            o.addProperty("fullbright", fullbright);
            o.addProperty("gamma", gamma);
            o.addProperty("zoom", zoom);
            o.addProperty("zoomFov", zoomFov);
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
            o.addProperty("sprint", sprint);
            o.addProperty("sprintAllDirs", sprintAllDirs);
            o.addProperty("limitDist", limitDist);
            o.addProperty("viewDist", viewDist);
            o.addProperty("autoswap", autoswap);
            o.addProperty("itembind", itembind);

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
                fullbright = b(o, "fullbright", fullbright);
                gamma = d(o, "gamma", gamma);
                zoom = b(o, "zoom", zoom);
                zoomFov = d(o, "zoomFov", zoomFov);
                hud = b(o, "hud", hud);
                hudFps = b(o, "hudFps", hudFps);
                hudPing = b(o, "hudPing", hudPing);
                hudCoords = b(o, "hudCoords", hudCoords);
                hudFacing = b(o, "hudFacing", hudFacing);
                hudEffects = b(o, "hudEffects", hudEffects);
                hudScale = d(o, "hudScale", hudScale);
                hudX = (int) d(o, "hudX", hudX);
                hudY = (int) d(o, "hudY", hudY);
                hudColor = Math.max(0, Math.min(COLORS.length - 1, (int) d(o, "hudColor", hudColor)));
                sprint = b(o, "sprint", sprint);
                sprintAllDirs = b(o, "sprintAllDirs", sprintAllDirs);
                limitDist = b(o, "limitDist", limitDist);
                viewDist = Math.max(2.0, Math.min(32.0, d(o, "viewDist", viewDist)));
                autoswap = b(o, "autoswap", autoswap);
                itembind = b(o, "itembind", itembind);

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

    private static boolean b(JsonObject o, String k, boolean def) {
        return o.has(k) ? o.get(k).getAsBoolean() : def;
    }

    private static double d(JsonObject o, String k, double def) {
        return o.has(k) ? o.get(k).getAsDouble() : def;
    }
}
