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

    // ===== Восстановленные поля из MenuScreen =====
    // Если MenuScreen ожидает конкретный тип (класс BoundItem/SwapPair), 
    // убедись, что он импортирован. Иначе замени Object на нужный класс.
    public static List<Object> binds = new ArrayList<>(); 
    public static List<Object> swapPairs = new ArrayList<>();
    
    public static int timeValue = 0;
    public static boolean customWeather = false;
    public static String weatherType = "Ясно";

    // ===== Метод сохранения =====
    public static void save() {
        // Заглушка, чтобы компилятор не ругался.
        // Если у тебя есть реальный код сохранения в JSON/NBT — вставь его здесь.
    }
}
