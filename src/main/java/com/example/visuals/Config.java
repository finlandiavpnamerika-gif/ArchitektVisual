package com.example.visuals;

import java.util.ArrayList;
import java.util.List;

public class Config {

    // ===== China Hat =====
    public static boolean chinaHat = true;
    public static int chinaHatColor = 0xFF859CFF;

    // ===== Friends & Alts =====
    public static List<String> friends = new ArrayList<>();
    public static List<String> alts = new ArrayList<>();

    // ===== Crosshair / HUD =====
    public static boolean adaptiveCrosshair = true;
    public static int crosshairColor = 0xFFFFFFFF;
    public static boolean hudEnabled = true;
    
    // Восстановленное поле цвета из MenuScreen
    public static int arrayListColor = 0;
    public static final String[] COLOR_NAMES = {"Красный", "Синий", "Зеленый", "Белый"};

    // ===== Trails =====
    public static boolean trails = true;
    public static int trailColor = 0xFF00FFFF;

    // ===== Misc =====
    public static boolean autoRespawn = true;
    public static boolean freelook = true;
    public static boolean macrosEnabled = true;
    public static boolean streamerMode = false;
    public static boolean boardSpoofer = false;
    public static boolean shulkerPreview = true;

    // ===== Weather & Time =====
    public static int timeValue = 0;
    public static boolean customWeather = false;
    public static int weatherType = 0;

    // ===== Новые поля из MenuScreen =====
    public static int predictions = 0;
    public static int itemPhysic = 0;
    public static int sounds = 0;

    // ===== Bind & Swap =====
    public static List<BoundItem> binds = new ArrayList<>();
    public static List<SwapPair> swapPairs = new ArrayList<>();

    // ===== Метод сохранения =====
    public static void save() {
        // Заглушка для компиляции
    }
}
