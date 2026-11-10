package com.example.visuals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Waypoints: точки в мире с именем. В мире рисуется луч-столб, на экране - метка с названием и расстоянием.
 * Хранятся отдельно от конфига (config/visualsmod_waypoints.json), поэтому не зависят от профилей.
 */
public final class Waypoints {
    private Waypoints() {}

    public static final class Wp {
        public String name;
        public int x, y, z;
        public String dim;
        public Wp(String name, int x, int y, int z, String dim) {
            this.name = name; this.x = x; this.y = y; this.z = z; this.dim = dim;
        }
    }

    public static final List<Wp> LIST = new ArrayList<>();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("visualsmod_waypoints.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final String DEATH_NAME = "Смерть";

    private static double lx, ly, lz;
    private static boolean haveLast = false;
    private static boolean wasDead = false;

    public static void load() {
        LIST.clear();
        try {
            if (!Files.exists(FILE)) return;
            JsonArray a = JsonParser.parseString(Files.readString(FILE)).getAsJsonArray();
            for (JsonElement el : a) {
                JsonObject o = el.getAsJsonObject();
                LIST.add(new Wp(o.get("name").getAsString(), o.get("x").getAsInt(), o.get("y").getAsInt(),
                        o.get("z").getAsInt(), o.has("dim") ? o.get("dim").getAsString() : ""));
            }
        } catch (Exception ignored) {}
    }

    public static void save() {
        try {
            JsonArray a = new JsonArray();
            for (Wp w : LIST) {
                JsonObject o = new JsonObject();
                o.addProperty("name", w.name);
                o.addProperty("x", w.x);
                o.addProperty("y", w.y);
                o.addProperty("z", w.z);
                o.addProperty("dim", w.dim);
                a.add(o);
            }
            Files.writeString(FILE, GSON.toJson(a));
        } catch (Exception ignored) {}
    }

    public static String dim(MinecraftClient mc) {
        return mc.world == null ? "" : mc.world.getRegistryKey().getValue().toString();
    }

    /** Добавляет точку в текущей позиции игрока. Без имени - "WP n". */
    public static void addHere(MinecraftClient mc, String name) {
        if (mc.player == null) return;
        if (name == null || name.isBlank()) name = "WP " + (LIST.size() + 1);
        LIST.add(new Wp(name.trim(), (int) Math.floor(mc.player.getX()), (int) Math.floor(mc.player.getY()),
                (int) Math.floor(mc.player.getZ()), dim(mc)));
        save();
    }

    public static boolean remove(String name) {
        boolean r = LIST.removeIf(w -> w.name.equalsIgnoreCase(name));
        if (r) save();
        return r;
    }

    public static void clear() {
        LIST.clear();
        save();
    }

    /** Каждый тик: запоминает позицию и ставит точку смерти. */
    public static void tick(MinecraftClient mc) {
        if (mc.player == null) { haveLast = false; wasDead = false; return; }
        boolean dead = mc.currentScreen instanceof DeathScreen;
        if (dead && !wasDead && Config.wpDeath && haveLast) {
            LIST.removeIf(w -> w.name.equals(DEATH_NAME));
            LIST.add(new Wp(DEATH_NAME, (int) Math.floor(lx), (int) Math.floor(ly), (int) Math.floor(lz), dim(mc)));
            save();
        }
        if (!dead && mc.player.isAlive()) {
            lx = mc.player.getX(); ly = mc.player.getY(); lz = mc.player.getZ();
            haveLast = true;
        }
        wasDead = dead;
    }

    private static int color() {
        return Config.COLORS[Math.max(0, Math.min(Config.wpColor, Config.COLORS.length - 1))] & 0xFFFFFF;
    }

    /** Лучи-столбы в мире. Вызывается из WorldEsp внутри общего рендера. */
    public static void renderBeams(VertexConsumer q, Matrix4f m, Vec3d cam, MinecraftClient mc) {
        if (LIST.isEmpty()) return;
        String dim = dim(mc);
        int rgb = color();
        for (Wp w : LIST) {
            if (!dim.equals(w.dim)) continue;
            double dx = w.x + 0.5 - cam.x, dy = w.y - cam.y, dz = w.z + 0.5 - cam.z;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (Config.wpMax > 0 && dist > Config.wpMax) continue;
            float near = (float) Math.min(1.0, dist / 6.0);      // рядом луч гаснет
            WorldDraw.ribbon(q, m, cam.x, cam.y, cam.z,
                    w.x + 0.5, w.y, w.z + 0.5, w.x + 0.5, w.y + 64, w.z + 0.5,
                    0.12f, WorldDraw.argb(0.85f * near, rgb), WorldDraw.argb(0f, rgb));
        }
    }

    /** Метки на экране. */
    public static void hud(DrawContext g) {
        if (!Config.wp || !Config.wpLabels || LIST.isEmpty()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.options.hudHidden || mc.currentScreen != null) return;
        Camera cam = mc.gameRenderer.getCamera();
        if (cam == null || !cam.isReady()) return;

        Vec3d cp = cam.getCameraPos();
        int sw = mc.getWindow().getScaledWidth(), sh = mc.getWindow().getScaledHeight();
        float fov = (float) Math.toRadians(mc.options.getFov().getValue());
        float f = (sh / 2f) / (float) Math.tan(fov / 2f);
        Quaternionf inv = cam.getRotation().conjugate(new Quaternionf());
        TextRenderer tr = mc.textRenderer;
        String dim = dim(mc);
        int accent = Config.COLORS[Math.max(0, Math.min(Config.wpColor, Config.COLORS.length - 1))];
        float sc = (float) Math.max(0.5, Math.min(2.0, Config.wpScale));

        for (Wp w : LIST) {
            if (!dim.equals(w.dim)) continue;
            double dx = w.x + 0.5 - cp.x, dy = w.y + 1.0 - cp.y, dz = w.z + 0.5 - cp.z;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (Config.wpMax > 0 && dist > Config.wpMax) continue;

            Vector3f v = new Vector3f((float) dx, (float) dy, (float) dz);
            inv.transform(v);
            if (v.z >= -0.05f) continue;                         // позади камеры
            float sx = sw / 2f + v.x / -v.z * f;
            float sy = sh / 2f - v.y / -v.z * f;
            sx = MathHelper.clamp(sx, 20f, sw - 20f);
            sy = MathHelper.clamp(sy, 16f, sh - 24f);

            String label = Config.wpDist ? w.name + "  " + (int) Math.round(dist) + "m" : w.name;
            int tw = tr.getWidth(label);
            var mat = g.getMatrices();
            mat.pushMatrix();
            mat.translate(sx, sy);
            mat.scale(sc, sc);
            HudStyle.roundFill(g, -tw / 2 - 4, -12, tw + 8, 12, 3, 0xA0000000);
            HudStyle.text(g, tr, label, -tw / 2, -10, Theme.TEXT, 1f, true);
            g.fill(-2, 2, 3, 7, accent | 0xFF000000);            // маркер
            mat.popMatrix();
        }
    }
}
