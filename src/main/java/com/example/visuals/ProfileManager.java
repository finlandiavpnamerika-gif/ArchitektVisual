package com.example.visuals;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;

/** Профили настроек: config/visualsmod_profiles/<имя>.json. В профиль входит всё, включая положения и стили HUD. */
public final class ProfileManager {
    private ProfileManager() {}

    public record Info(String name, long modified) {}

    /** имя профиля, который загружали/сохраняли последним (для пометки в списке) */
    public static String current = "";

    /** Встроенный профиль: все модули выключены. */
    public static final String EMPTY = "Пустой";

    private static Path dir() { return FabricLoader.getInstance().getConfigDir().resolve("visualsmod_profiles"); }

    private static Path file(String name) { return dir().resolve(name + ".json"); }

    /** Оставляет в имени только буквы, цифры, пробел, _ и -. */
    public static String clean(String s) {
        if (s == null) return "";
        String r = s.trim().replaceAll("[^\\p{L}\\p{N} _\\-]", "").replaceAll("^[_ ]+", "").trim();
        return r.length() > 32 ? r.substring(0, 32).trim() : r;
    }

    public static boolean exists(String name) { return !name.isEmpty() && Files.exists(file(name)); }

    public static List<Info> list() {
        List<Info> out = new ArrayList<>();
        try {
            if (!Files.isDirectory(dir())) return out;
            try (Stream<Path> st = Files.list(dir())) {
                st.filter(p -> p.getFileName().toString().endsWith(".json") && !p.getFileName().toString().startsWith("_")).forEach(p -> {
                    String n = p.getFileName().toString();
                    n = n.substring(0, n.length() - 5);
                    long t = 0;
                    try { t = Files.getLastModifiedTime(p).toMillis(); } catch (IOException ignored) {}
                    out.add(new Info(n, t));
                });
            }
        } catch (Exception ignored) {}
        out.sort(Comparator.comparingLong(Info::modified).reversed());
        return out;
    }

    /** Сохраняет текущие настройки как профиль (перезаписывает, если имя уже есть). */
    public static boolean save(String name) {
        name = clean(name);
        if (name.isEmpty()) return false;
        try {
            Config.save();
            Config.saveTo(file(name));
            current = name;
            return Files.exists(file(name));
        } catch (Exception e) { return false; }
    }

    /** Загружает профиль. Друзья и макросы личные - они не меняются. */
    public static boolean load(String name) {
        if (!exists(name)) return false;
        try {
            List<String> friends = new ArrayList<>(Config.friends);
            List<Macro> macros = new ArrayList<>(Config.macros);
            List<RadialItem> radial = new ArrayList<>(Config.radialItems);
            int radialKey = Config.radialKey;
            ModuleDefaults.resetAll();
            Config.loadFrom(file(name));
            Config.friends.clear(); Config.friends.addAll(friends);
            Config.macros.clear(); Config.macros.addAll(macros);
            Config.radialItems.clear(); Config.radialItems.addAll(radial);
            Config.radialKey = radialKey;
            applyAfterLoad();
            current = name;
            return true;
        } catch (Exception e) { return false; }
    }

    /** Всё по умолчанию (друзья и макросы остаются). */
    public static void resetAll() {
        ModuleDefaults.resetAll();
        CustomItems_resetRules();
        applyAfterLoad();
        current = "";
    }

    private static void CustomItems_resetRules() {
        Config.ciRules.clear();
        Config.ciRules.add(new CiRule("minecraft:netherite_sword", 0));
    }

    private static void applyAfterLoad() {
        Theme.THEME_ID = Config.menuTheme;
        Theme.apply();
        Config.save();
    }

    public static boolean delete(String name) {
        try {
            boolean ok = Files.deleteIfExists(file(name));
            if (name.equals(current)) current = "";
            return ok;
        } catch (Exception e) { return false; }
    }

    /** Содержимое профиля одной строкой JSON (для буфера обмена). */
    public static String export(String name) {
        try {
            JsonObject o = JsonParser.parseString(Files.readString(file(name))).getAsJsonObject();
            return new Gson().toJson(o);
        } catch (Exception e) { return null; }
    }

    /** Создаёт профиль из JSON-текста. Возвращает имя или null, если текст не подошёл. */
    public static String importJson(String text, String baseName) {
        try {
            if (text == null) return null;
            JsonObject o = JsonParser.parseString(text.trim()).getAsJsonObject();
            if (o.size() < 3) return null;
            String base = clean(baseName);
            if (base.isEmpty()) base = "Импорт";
            String name = base;
            for (int i = 2; exists(name); i++) name = base + " " + i;
            Files.createDirectories(dir());
            Files.writeString(file(name), new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(o));
            return name;
        } catch (Exception e) { return null; }
    }

    // ---------- авто-конфиг ----------
    private static Path autoFile() { return dir().resolve("_auto.json"); }

    private static JsonObject readAuto() {
        try {
            if (Files.exists(autoFile())) return JsonParser.parseString(Files.readString(autoFile())).getAsJsonObject();
        } catch (Exception ignored) {}
        return new JsonObject();
    }

    private static void writeAuto(JsonObject o) {
        try {
            Files.createDirectories(dir());
            Files.writeString(autoFile(), new Gson().toJson(o));
        } catch (Exception ignored) {}
    }

    /** имя авто-конфига или "" */
    public static String autoName() {
        JsonObject o = readAuto();
        return o.has("auto") ? o.get("auto").getAsString() : "";
    }

    public static void setAuto(String name) {
        JsonObject o = readAuto();
        o.addProperty("auto", name == null ? "" : name);
        writeAuto(o);
    }

    /** загружать авто-конфиг при каждом заходе в мир (а не только при запуске игры) */
    public static boolean autoOnJoin() {
        JsonObject o = readAuto();
        return o.has("onJoin") && o.get("onJoin").getAsBoolean();
    }

    public static void setAutoOnJoin(boolean v) {
        JsonObject o = readAuto();
        o.addProperty("onJoin", v);
        writeAuto(o);
    }

    public static void applyAuto() {
        String n = autoName();
        if (!n.isEmpty() && exists(n)) load(n);
    }

    /** Создаёт встроенный профиль "Пустой": все модули выключены, остальное по умолчанию. */
    private static void ensureEmpty() {
        if (exists(EMPTY)) return;
        try {
            Config.save();
            ModuleDefaults.resetAll();
            ModuleDefaults.disableAll();
            Config.saveTo(file(EMPTY));
        } catch (Exception ignored) {
        } finally {
            ModuleDefaults.resetAll();     // вернуть текущие настройки из основного файла
            Config.reload();
        }
    }

    /** Вызывается один раз при запуске игры после загрузки настроек. */
    public static void startup() {
        ensureEmpty();
        if (Config.freshInstall && autoName().isEmpty()) setAuto(EMPTY);   // первый запуск: всё выключено
        applyAuto();
    }
}
