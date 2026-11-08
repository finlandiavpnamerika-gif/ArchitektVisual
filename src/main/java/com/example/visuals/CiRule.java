package com.example.visuals;

import com.google.gson.JsonObject;

/** Правило CustomItems: какой предмет (id) заменяется какой моделью и с какими настройками. */
public final class CiRule {
    public String item;            // например "minecraft:diamond_sword"
    public boolean enabled = true;
    public int model = 0;          // индекс в CustomItems.MODELS
    public int skin = 0;           // индекс в CustomItems.SKINS
    public double hue = 0.6, sat = 0.8;
    public double scale = 0.75;
    public double x = 0, y = 0, z = 0;
    public double rx = -30, ry = 20, rz = 0;

    public CiRule(String item) { this.item = item; }

    public CiRule(String item, int model) { this.item = item; this.model = model; }

    /** Сбросить положение и размер (модель и предмет остаются). */
    public void resetTransform() {
        scale = 0.75; x = 0; y = 0; z = 0; rx = -30; ry = 20; rz = 0;
    }

    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("item", item);
        o.addProperty("enabled", enabled);
        o.addProperty("model", model);
        o.addProperty("skin", skin);
        o.addProperty("hue", hue);
        o.addProperty("sat", sat);
        o.addProperty("scale", scale);
        o.addProperty("x", x); o.addProperty("y", y); o.addProperty("z", z);
        o.addProperty("rx", rx); o.addProperty("ry", ry); o.addProperty("rz", rz);
        return o;
    }

    private static double d(JsonObject o, String k, double def) { return o.has(k) ? o.get(k).getAsDouble() : def; }

    public static CiRule fromJson(JsonObject o) {
        CiRule r = new CiRule(o.has("item") ? o.get("item").getAsString() : "minecraft:netherite_sword");
        r.enabled = !o.has("enabled") || o.get("enabled").getAsBoolean();
        r.model = (int) d(o, "model", 0);
        r.skin = (int) d(o, "skin", 0);
        r.hue = d(o, "hue", r.hue); r.sat = d(o, "sat", r.sat);
        r.scale = d(o, "scale", r.scale);
        r.x = d(o, "x", 0); r.y = d(o, "y", 0); r.z = d(o, "z", 0);
        r.rx = d(o, "rx", r.rx); r.ry = d(o, "ry", r.ry); r.rz = d(o, "rz", r.rz);
        return r;
    }
}
