package com.example.visuals;

import java.util.ArrayList;
import java.util.List;

public class Config {

    // ===== China Hat =====
    public static boolean chinaHat = true;

    /**
     * Цвет шляпы в формате ARGB.
     * Значение взято из байт-кода: new ColorSetting("Цвет визуализации шляпы", 133, 156, 255, 255)
     * R=133, G=156, B=255, A=255 → 0xFF859CFF
     */
    public static int chinaHatColor = 0xFF859CFF;

    // ===== Friends =====
    public static List<String> friends = new ArrayList<>();

    // ===== Crosshair / Adaptive Crosshair =====
    public static boolean adaptiveCrosshair = true;
    public static int crosshairColor = 0xFFFFFFFF;

    // ===== Trails =====
    public static boolean trails = true;
    public static int trailColor = 0xFF00FFFF;

    // ===== AutoRespawn =====
    public static boolean autoRespawn = true;

    // ===== Freelook =====
    public static boolean freelook = true;

    // ===== Macros =====
    public static boolean macrosEnabled = true;

    // ===== Streamer Mode =====
    public static boolean streamerMode = false;

    // ===== Board Spoofer =====
    public static boolean boardSpoofer = false;

    // ===== Shulker Preview =====
    public static boolean shulkerPreview = true;

    // ===== HUD =====
    public static boolean hudEnabled = true;

    // ===== Alt Manager =====
    public static List<String> alts = new ArrayList<>();
}
