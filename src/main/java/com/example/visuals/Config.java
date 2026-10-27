package com.example.visuals;

import java.util.ArrayList;
import java.util.List;

public class Config {
    // --- China Hat ---
    public static boolean chinaHat = true;
    public static int chinaHatColor = 0xFF859CFF;

    // --- Остальные настройки (пример) ---
    public static boolean adaptiveCrosshair = true;
    public static boolean trails = true;
    public static boolean autoRespawn = true;
    public static boolean freelook = true;
    public static boolean macrosEnabled = true;
    public static boolean streamerMode = false;
    public static boolean boardSpoofer = false;
    public static boolean shulkerPreview = true;
    public static boolean hudEnabled = true;
    
    public static List<String> friends = new ArrayList<>();
    public static List<String> alts = new ArrayList<>();

    // --- ВОССТАНОВЛЕННЫЕ ПОЛЯ (из-за ошибок сборки) ---
    public static List<BoundItem> binds = new ArrayList<>();
    public static List<SwapPair> swapPairs = new ArrayList<>();

    public static void save() {
        // Заглушка. Если у вас есть класс для сохранения JSON/NBT, вызовите его здесь.
        // System.out.println("Config saved");
    }

    // Если классы BoundItem и SwapPair лежат в другом пакете, добавьте их импорт выше.
    // Если они не существуют, MenuScreen выдаст ошибку на них.
}
