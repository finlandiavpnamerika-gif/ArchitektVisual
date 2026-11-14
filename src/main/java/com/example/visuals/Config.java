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
    public static int menuTheme = 1;   // палитра меню (Theme.NAMES)
    public static int menuStyle = 0;   // 0 = Classic (4 колонки), 1 = Sidebar

    // --- Визуал ---
    public static boolean customCrosshair = true;
    public static boolean chinaHat = false;
    public static int chinaHatColor = 0xFF859CFF; // Добавлено
    public static int chinaHatMode = 0; // 0-4 = COLORS, 5 = радуга
    public static double hatSize = 1.0;     // множитель радиуса шляпы
    public static double hatHeight = 1.0;   // множитель высоты конуса
    public static boolean hatCustom = false; // своя палитра вместо списка цветов
    public static double hatHue = 0.75, hatSat = 0.8, hatVal = 1.0;
    public static boolean halo = false;       // Angel Halo
    public static int haloMode = 0;           // 0-3 = HaloRenderer.COLOR_NAMES, 4 = радуга
    public static double haloSize = 1.0;      // множитель радиуса нимба
    public static double haloHeight = 1.0;    // множитель высоты над головой
    public static boolean haloBob = true;     // плавное парение вверх-вниз
    public static boolean haloCustom = false; // своя палитра вместо списка цветов
    public static double haloHue = 0.12, haloSat = 0.7, haloVal = 1.0;
    public static boolean horns = false;       // Рога
    public static int hornsMode = 0;           // 0-3 = HornsRenderer.COLOR_NAMES, 4 = радуга
    public static double hornsSize = 1.0;      // множитель размера рогов
    public static boolean hornsGlow = true;    // раскалённые кончики
    public static boolean hornsCustom = false; // своя палитра вместо списка цветов
    public static double hornsHue = 0.0, hornsSat = 0.9, hornsVal = 0.7;
    public static boolean entityBox = false;
    public static boolean predictions = false;
    public static boolean itemPhysic = false;
    public static boolean shulkerPreview = true;
    public static int crosshairColor = 0;
    public static int crosshairSize = 8;
    public static int crosshairThickness = 2;
    public static boolean crosshairDot = true;

    // --- Утилиты ---
    public static boolean autoRespawn = true;
    public static boolean thirdPerson = false;
    public static boolean freelookToggle = false;      // false = зажатие, true = переключатель
    public static boolean freelookThird = true;        // переключать на третье лицо
    public static boolean freelookSmooth = true;       // плавный возврат камеры
    public static double freelookSens = 1.0;           // чувствительность
    public static boolean itemScroller = false;
    public static final List<Macro> macros = new ArrayList<>();
    public static int radialKey = 71;                  // GLFW_KEY_G; -1 = колесо выключено
    public static final List<RadialItem> radialItems = new ArrayList<>();

    // --- Разное ---
    public static boolean streamerMode = false;
    public static boolean boardSpoofer = false;
    public static boolean sounds = true;

    // --- Время и Погода ---
    public static boolean customTime = false;
    public static int timeValue = 6000;
    public static boolean customWeather = false;
    public static int weatherType = 0; // 0 = Ясно, 1 = Дождь, 2 = Гроза

    // --- HUD ---
    public static boolean hud = true;
    public static boolean hudFps = true;
    public static boolean hudPing = true;
    public static boolean hudCoords = true;
    public static boolean hudFacing = true;
    public static boolean hudEffects = true;
    public static double hudScale = 1.0;
    public static double hudAlpha = 1.0;            // прозрачность 0.1..1.0
    public static int hudX = 4;
    public static int hudY = 4;
    public static int hudColor = 2;

    // --- ArrayList ---
    public static boolean arrayList = true;
    public static int arrayListColor = 2;
    public static boolean arrayListShadow = true;
    public static double arrayListScale = 1.0;
    public static double arrayListAlpha = 1.0;
    public static int arrayListX = 4;
    public static int arrayListY = 4;

    // --- Keystrokes ---
    public static boolean keystrokes = true;
    public static int keystrokesX = 4;
    public static int keystrokesY = 150;
    public static double keystrokesScale = 1.0;
    public static double keystrokesAlpha = 1.0;
    public static int keystrokesColor = 2;
    public static boolean keystrokesShowMouse = true; // Добавлено
    public static boolean keystrokesShowSpace = true;

    // --- Armor ---
    public static boolean armorHud = true;
    public static int armorHudX = 4;
    public static int armorHudY = 220;
    public static double armorHudScale = 1.0;
    public static double armorHudAlpha = 1.0;
    public static boolean armorHudShowTool = true;
    public static boolean armorHudShowDurability = true;
    public static boolean armorHudColorIcons = true;
    public static boolean armorTint = true;

    // NoOverlay
    public static boolean noOverlay = false;
    public static boolean noHurtShake = true;
    public static boolean noFireOverlay = true;
    public static boolean noTotemParticles = true;
    public static boolean noFireworkParticles = true;

    // Hit Sound
    public static boolean hitSound = false;
    public static int hitSoundType = 0;
    public static double hitSoundVol = 1.0;
    public static double hitSoundPitch = 1.0;

    // Combo Counter
    public static boolean comboHud = false;
    public static int comboX = 190;
    public static int comboY = 140;
    public static double comboScale = 1.0;
    public static double comboAlpha = 1.0;
    public static int comboColor = 0;
    public static double comboTimeout = 3.0;

    // Watermark
    public static boolean watermark = false;
    public static int watermarkX = 4;
    public static int watermarkY = 30;
    public static double watermarkScale = 1.0;
    public static double watermarkAlpha = 1.0;
    public static int watermarkColor = 0;
    public static int watermarkName = 0;
    public static boolean watermarkRainbow = false;
    public static boolean watermarkNick = false;
    public static boolean watermarkFps = true;
    public static boolean watermarkTime = false;

    // Server Info
    public static boolean serverHud = false;
    public static int serverX = 4;
    public static int serverY = 70;
    public static double serverScale = 1.0;
    public static double serverAlpha = 1.0;
    public static int serverColor = 1;
    public static boolean serverIp = true;
    public static boolean serverTps = true;
    public static boolean serverPing = true;

    // Time & Date
    public static boolean clockHud = false;
    public static int clockX = 4;
    public static int clockY = 120;
    public static double clockScale = 1.0;
    public static double clockAlpha = 1.0;
    public static int clockColor = 0;
    public static boolean clockSeconds = true;
    public static boolean clockDate = true;
    public static boolean clockGame = false;

    // Inventory HUD
    public static boolean invHud = false;
    public static int invX = 4;
    public static int invY = 170;
    public static double invScale = 1.0;
    public static double invAlpha = 1.0;
    public static int invColor = 0;
    public static boolean invHotbar = false;

    // Totem Counter
    public static boolean totemHud = false;
    public static int totemX = 190;
    public static int totemY = 170;
    public static double totemScale = 1.0;
    public static double totemAlpha = 1.0;
    public static int totemColor = 4;
    public static boolean totemHideZero = false;

    // Cooldown HUD
    public static boolean cdHud = true;
    public static int cdX = 8;
    public static int cdY = 120;
    public static double cdAlpha = 1.0;
    public static int cdColor = 0;          // 0 = авто (по прогрессу), 1..5 = COLORS
    public static boolean cdTime = true;

    // Crit Effects
    public static boolean critFx = false;
    public static int critStyle = 0;
    public static double critCount = 20;

    // Kill Effect
    public static boolean killFx = false;
    public static boolean killFlash = true;
    public static boolean killSound = true;
    public static int killParticles = 1;
    public static int killColor = 0;

    // Attack Indicator
    public static boolean atkInd = false;
    public static int atkStyle = 0;
    public static int atkColor = 2;
    public static double atkSize = 4;
    public static boolean atkAlways = false;
    public static int atkOffset = 14;

    // Combat Stats
    public static boolean statsHud = false;
    public static int statsX = 4;
    public static int statsY = 210;
    public static double statsScale = 1.0;
    public static double statsAlpha = 1.0;
    public static int statsColor = 0;
    public static boolean statsAcc = true;
    public static boolean statsKills = true;
    public static boolean statsStreak = true;
    public static boolean statsDeaths = true;
    public static boolean statsKd = true;
    public static boolean statsPlayersOnly = true;

    // Damage Log
    public static boolean dlHud = false;
    public static int dlX = 300;
    public static int dlY = 70;
    public static double dlScale = 1.0;
    public static double dlAlpha = 1.0;
    public static int dlColor = 4;
    public static boolean dlDealt = true;
    public static boolean dlTaken = true;
    public static int dlLines = 5;
    public static double dlLife = 8.0;

    // Hitboxes
    public static boolean hbox = false;
    public static boolean hbPlayers = true;
    public static boolean hbMobs = true;
    public static boolean hbOthers = false;
    public static boolean hbSelf = false;
    public static boolean hbFill = false;
    public static double hbWidth = 0.02;
    public static double hbRange = 48;
    public static double hbAlpha = 0.9;
    public static int hbColorP = 0;
    public static int hbColorM = 4;
    public static int hbColorO = 3;

    // Block Outline
    public static boolean bout = false;
    public static int boColor = 1;
    public static boolean boRainbow = false;
    public static double boWidth = 0.02;
    public static boolean boFill = false;
    public static double boFillAlpha = 0.25;
    public static boolean boHideVanilla = true;

    // Chunk Borders
    public static boolean chunkB = false;
    public static int cbRadius = 1;
    public static double cbHeight = 32;
    public static int cbColor = 3;
    public static double cbWidth = 0.03;
    public static boolean cbLevels = true;
    public static double cbAlpha = 0.9;

    // Item ESP
    public static boolean itemEsp = false;
    public static double ieRange = 48;
    public static boolean ieValuable = false;
    public static boolean ieFill = true;
    public static boolean ieTracers = false;
    public static int ieColor = 1;
    public static int ieColorHi = 3;
    public static double ieWidth = 0.02;

    // Waypoints
    public static boolean wp = false;
    public static boolean wpBeam = true;
    public static boolean wpLabels = true;
    public static boolean wpDist = true;
    public static double wpMax = 2000;
    public static int wpColor = 0;
    public static double wpScale = 1.0;
    public static boolean wpDeath = true;

    // Pearl Trail
    public static boolean ptrail = false;
    public static int ptColor = 0;
    public static boolean ptRainbow = false;
    public static int ptLength = 40;
    public static double ptWidth = 0.06;

    // Elytra Trail
    public static boolean etrail = false;
    public static int etColor = 1;
    public static boolean etRainbow = false;
    public static int etLength = 40;
    public static double etWidth = 0.12;
    public static boolean etAll = false;

    // Pearl Cooldown
    public static boolean pcdHud = false;
    public static int pcdX = 190;
    public static int pcdY = 200;
    public static double pcdScale = 1.0;
    public static double pcdAlpha = 1.0;
    public static int pcdColor = 1;
    public static boolean pcdHideIdle = true;

    // Wings
    public static boolean wingsOn = false;
    public static int wingStyle = 0;
    public static int wingColor = 1;
    public static boolean wingRainbow = false;
    public static double wingSize = 1.0;
    public static double wingSpeed = 1.0;
    public static boolean wingFriends = false;

    // Aura
    public static boolean auraOn = false;
    public static int auraStyle = 0;
    public static int auraColor = 0;
    public static boolean auraRainbow = false;
    public static double auraSize = 1.0;
    public static double auraSpeed = 1.0;
    public static boolean auraFriends = false;

    // Animated Cape
    public static boolean capeOn = false;
    public static int capeColor = 0;
    public static int capeColor2 = 1;
    public static boolean capeRainbow = false;
    public static double capeWave = 0.6;
    public static boolean capeFriends = false;

    // Nametags+
    public static boolean ntOn = false;
    public static double ntRange = 64;
    public static double ntScale = 1.0;
    public static boolean ntBg = true;
    public static boolean ntHealth = true;
    public static boolean ntDist = true;
    public static boolean ntDistScale = false;
    public static boolean ntHide = true;

    // Chams
    public static boolean chamsOn = false;
    public static boolean chPlayers = true;
    public static boolean chMobs = false;
    public static boolean chFriends = true;
    public static boolean chSelf = false;

    // Swing Animation
    public static boolean swingAnim = false;
    public static double swingSpeed = 1.0;
    public static boolean swingSelfOnly = true;

    // Custom Clouds
    public static boolean cloudsOn = false;
    public static boolean cloudHide = false;
    public static boolean cloudTint = true;
    public static double cloudHue = 0.6;
    public static double cloudSat = 0.5;
    public static boolean cloudRainbow = false;
    public static double cloudHeightOffset = 0;

    // Tooltips Plus
    public static boolean tipOn = false;
    public static boolean tipDur = true;
    public static boolean tipId = true;
    public static boolean tipStack = false;

    // Potion HUD
    public static boolean potionHud = true;
    public static int potionX = 130;
    public static int potionY = 4;
    public static double potionScale = 1.0;
    public static double potionAlpha = 1.0;
    public static int potionColor = 2;
    public static boolean potionIcons = true;
    public static boolean potionBars = true;
    public static boolean potionLevel = true;
    public static boolean potionHoriz = false;
    public static boolean hudHoriz = false;
    public static boolean arrayListHoriz = false;
    public static boolean armorHudHoriz = false;
    public static boolean keystrokesHoriz = false;

    // JumpCircle
    public static boolean jumpCircle = true;
    public static int jcStyle = 0;
    public static double jcSize = 1.6;
    public static double jcLife = 0.9;
    public static double jcWidth = 0.35;
    public static double jcAlpha = 0.85;
    public static double jcHue = 0.75;
    public static boolean jcRainbow = false;
    public static boolean jcLand = false;

    // --- Utils (Остальное) ---
    public static boolean sprint = false;
    public static boolean sprintAllDirs = false;
    public static boolean zoom = true;
    public static double zoomFov = 30.0;
    public static boolean fullbright = false;
    public static double gamma = 10.0;
    public static boolean limitDist = false;
    public static double viewDist = 8.0;
    public static boolean autoReconnect = false;
    public static double autoReconnectDelay = 5.0;

    // --- AutoSwap & ItemBind ---
    public static boolean autoswap = true;
    public static boolean itembind = true;
    public static final List<SwapPair> swapPairs = new ArrayList<>();
    public static final List<BoundItem> binds = new ArrayList<>();

    // --- Трейлы ---
    public static boolean trails = true;
    public static int trailsColor = 2;
    public static int trailsLength = 30;          // длина хвоста в тиках (20 тиков = 1 сек)
    public static double trailsHeight = 0.8;      // высота "занавески" в блоках
    public static boolean trailsCustom = false;   // своя палитра вместо списка цветов
    public static double trailsHue = 0.75, trailsSat = 0.8, trailsVal = 1.0;

    // --- FunTime Helper (радиусы зон) ---
    public static boolean zones = true;
    public static boolean zonesHeld = true;   // показывать радиус у игроков с предметом в руке

    // --- Target ESP ---
    public static boolean targetEsp = true;
    public static int targetEspShape = 0;          // 0 Chevrons, 1 Ring, 2 Diamond, 3 Orbit
    public static double targetEspSize = 1.0;
    public static double targetEspSpeed = 1.0;
    public static double targetEspAlpha = 0.9;
    public static double targetEspHue = 0.33;      // обычный цвет (зелёный)
    public static double targetEspHitHue = 0.0;    // цвет при ударе (красный)

    // --- Sky Shader ---
    public static boolean skyShader = false;
    public static int skyPreset = 0;               // 0 Aurora, 1 Galaxy, 2 Purple Night
    public static double skyAlpha = 1.0;
    public static double skySpeed = 1.0;
    public static boolean skyNightOnly = false;
    public static boolean skyStars = true;

    // --- CustomItems (своя модель в руке) ---
    public static boolean customItems = true;
    public static int ciModel = 0;                 // какая модель
    public static int ciSkin = 0;                  // текстура (перекраска)
    public static double ciHue = 0.6;
    public static double ciSat = 0.8;
    public static int ciTarget = 0;                // какой предмет заменяем
    public static final List<CiRule> ciRules = new ArrayList<>();   // правила: предмет -> модель
    private static boolean ciRulesSaved = false;
    public static double ciScale = 0.75;           // длина модели, блоки
    public static double ciX = 0, ciY = 0, ciZ = 0; // смещение, см
    public static double ciRX = -30, ciRY = 20, ciRZ = 0;   // поворот, градусы

    // --- Hand View (вид предмета в руке) ---
    public static boolean handView = true;
    public static boolean leftHanded = false;     // главная рука слева
    public static int svHand = 0;                  // какая рука сейчас редактируется в меню
    /** [параметр][рука]: параметры SCALE, X, Y, Z, RX, RY, RZ (см. HandView); рука 0 = правая, 1 = левая */
    public static final double[][] sv = {
            {1.0, 1.0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}
    };
    public static final boolean[] svNoSwing = new boolean[2];

    // --- TargetHUD ---
    public static boolean targetHud = true;
    public static double thScale = 1.0;
    public static double thX = 50;                 // положение центра, % ширины экрана
    public static double thY = 62;                 // положение верха, % высоты экрана
    public static double thHue = 0.75;
    public static boolean thDist = true;

    // --- CustomFog ---
    public static boolean customFog = false;
    public static boolean fogOff = false;          // совсем без тумана
    public static double fogStart = 16;
    public static double fogEnd = 96;
    public static double fogHue = 0.6;
    public static double fogSat = 0.3;
    public static double fogVal = 0.85;
    public static double physicLift = 0;           // подстройка высоты Item Physic, см

    // --- DamageFever ---
    public static boolean damageFever = true;
    public static boolean dfMine = true;           // только урон от моих ударов
    public static boolean dfDecimals = true;       // 4.5 вместо 5
    public static double dfSize = 1.0;
    public static double dfLife = 1.2;
    public static double dfHue = 0.0;              // цвет числа (красный)

    // --- Particles ---
    public static boolean particles = true;
    public static int partMode = 0;                // 0 Удар, 1 Аура, 2 Оба
    public static int partShape = 2;               // 0 Квадрат, 1 Ромб, 2 Звезда, 3 Точка
    public static double partCount = 20;
    public static double partSize = 0.12;
    public static double partLife = 1.0;
    public static double partGravity = 4.0;
    public static double partAlpha = 0.9;
    public static double partHue = 0.8;
    public static boolean partRainbow = false;

    // --- Item Highlight ---
    public static boolean highlight = true;
    public static final List<ItemHighlight.Entry> highlights = new ArrayList<>();

    // --- Друзья ---
    public static final List<String> friends = new ArrayList<>();

    public static final int[] COLORS = {-4883201, -11141121, -11141291, -171, -43691};
    public static final String[] COLOR_NAMES = {"Фиолетовый", "Голубой", "Зелёный", "Жёлтый", "Красный"};

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("visualsmod.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void save() { saveTo(FILE); }

    /** Сохраняет все настройки в указанный файл (для профилей). */
    public static void saveTo(Path file) {
        try {
            JsonObject o = new JsonObject();
            o.addProperty("customMainMenu", customMainMenu);
            o.addProperty("menuStyle", menuStyle);
            o.addProperty("menuTheme", menuTheme);
            o.addProperty("customCrosshair", customCrosshair);
            o.addProperty("chinaHat", chinaHat);
            o.addProperty("chinaHatColor", chinaHatColor);
            o.addProperty("chinaHatMode", chinaHatMode);
            o.addProperty("hatSize", hatSize);
            o.addProperty("hatHeight", hatHeight);
            o.addProperty("hatCustom", hatCustom);
            o.addProperty("hatHue", hatHue);
            o.addProperty("hatSat", hatSat);
            o.addProperty("hatVal", hatVal);
            o.addProperty("halo", halo);
            o.addProperty("haloMode", haloMode);
            o.addProperty("haloSize", haloSize);
            o.addProperty("haloHeight", haloHeight);
            o.addProperty("haloBob", haloBob);
            o.addProperty("haloCustom", haloCustom);
            o.addProperty("haloHue", haloHue);
            o.addProperty("haloSat", haloSat);
            o.addProperty("haloVal", haloVal);
            o.addProperty("horns", horns);
            o.addProperty("hornsMode", hornsMode);
            o.addProperty("hornsSize", hornsSize);
            o.addProperty("hornsGlow", hornsGlow);
            o.addProperty("hornsCustom", hornsCustom);
            o.addProperty("hornsHue", hornsHue);
            o.addProperty("hornsSat", hornsSat);
            o.addProperty("hornsVal", hornsVal);
            o.addProperty("entityBox", entityBox);
            o.addProperty("predictions", predictions);
            o.addProperty("itemPhysic", itemPhysic);
            o.addProperty("shulkerPreview", shulkerPreview);
            o.addProperty("autoRespawn", autoRespawn);
            o.addProperty("thirdPerson", thirdPerson);
            o.addProperty("freelookToggle", freelookToggle);
            o.addProperty("freelookThird", freelookThird);
            o.addProperty("freelookSmooth", freelookSmooth);
            o.addProperty("freelookSens", freelookSens);
            o.addProperty("itemScroller", itemScroller);
            o.addProperty("streamerMode", streamerMode);
            o.addProperty("boardSpoofer", boardSpoofer);
            o.addProperty("sounds", sounds);
            o.addProperty("trails", trails);
            o.addProperty("trailsColor", trailsColor);
            o.addProperty("trailsLength", trailsLength);
            o.addProperty("trailsHeight", trailsHeight);
            o.addProperty("trailsCustom", trailsCustom);
            o.addProperty("trailsHue", trailsHue);
            o.addProperty("trailsSat", trailsSat);
            o.addProperty("trailsVal", trailsVal);
            o.addProperty("zones", zones);
            o.addProperty("zonesHeld", zonesHeld);
            o.addProperty("targetEsp", targetEsp);
            o.addProperty("targetEspShape", targetEspShape);
            o.addProperty("targetEspSize", targetEspSize);
            o.addProperty("targetEspSpeed", targetEspSpeed);
            o.addProperty("targetEspAlpha", targetEspAlpha);
            o.addProperty("targetEspHue", targetEspHue);
            o.addProperty("targetEspHitHue", targetEspHitHue);
            o.addProperty("skyShader", skyShader);
            o.addProperty("skyPreset", skyPreset);
            o.addProperty("skyAlpha", skyAlpha);
            o.addProperty("skySpeed", skySpeed);
            o.addProperty("skyNightOnly", skyNightOnly);
            o.addProperty("skyStars", skyStars);
            o.addProperty("particles", particles);
            o.addProperty("damageFever", damageFever);
            o.addProperty("customFog", customFog);
            o.addProperty("targetHud", targetHud);
            o.addProperty("handView", handView);
            o.addProperty("customItems", customItems);
            o.addProperty("ciModel", ciModel);
            o.addProperty("ciSkin", ciSkin);
            o.addProperty("ciHue", ciHue);
            o.addProperty("ciSat", ciSat);
            o.addProperty("ciTarget", ciTarget);
            o.addProperty("ciScale", ciScale);
            o.addProperty("ciX", ciX);
            o.addProperty("ciY", ciY);
            o.addProperty("ciZ", ciZ);
            o.addProperty("ciRX", ciRX);
            o.addProperty("ciRY", ciRY);
            o.addProperty("ciRZ", ciRZ);
            o.addProperty("leftHanded", leftHanded);
            for (int k = 0; k < sv.length; k++) for (int h = 0; h < 2; h++) o.addProperty("sv" + k + "_" + h, sv[k][h]);
            for (int h = 0; h < 2; h++) o.addProperty("svNoSwing" + h, svNoSwing[h]);
            o.addProperty("thScale", thScale);
            o.addProperty("thX", thX);
            o.addProperty("thY", thY);
            o.addProperty("thHue", thHue);
            o.addProperty("thDist", thDist);
            o.addProperty("fogOff", fogOff);
            o.addProperty("fogStart", fogStart);
            o.addProperty("fogEnd", fogEnd);
            o.addProperty("fogHue", fogHue);
            o.addProperty("fogSat", fogSat);
            o.addProperty("fogVal", fogVal);
            o.addProperty("physicLift", physicLift);
            o.addProperty("dfMine", dfMine);
            o.addProperty("dfDecimals", dfDecimals);
            o.addProperty("dfSize", dfSize);
            o.addProperty("dfLife", dfLife);
            o.addProperty("dfHue", dfHue);
            o.addProperty("partMode", partMode);
            o.addProperty("partShape", partShape);
            o.addProperty("partCount", partCount);
            o.addProperty("partSize", partSize);
            o.addProperty("partLife", partLife);
            o.addProperty("partGravity", partGravity);
            o.addProperty("partAlpha", partAlpha);
            o.addProperty("partHue", partHue);
            o.addProperty("partRainbow", partRainbow);

            // HUD
            o.addProperty("hud", hud);
            o.addProperty("hudFps", hudFps);
            o.addProperty("hudPing", hudPing);
            o.addProperty("hudCoords", hudCoords);
            o.addProperty("hudFacing", hudFacing);
            o.addProperty("hudEffects", hudEffects);
            o.addProperty("hudScale", hudScale);
            o.addProperty("hudAlpha", hudAlpha);
            o.addProperty("hudX", hudX);
            o.addProperty("hudY", hudY);
            o.addProperty("hudColor", hudColor);

            // ArrayList & Keystrokes
            o.addProperty("arrayList", arrayList);
            o.addProperty("arrayListColor", arrayListColor);
            o.addProperty("arrayListShadow", arrayListShadow);
            o.addProperty("arrayListScale", arrayListScale);
            o.addProperty("arrayListAlpha", arrayListAlpha);
            o.addProperty("arrayListX", arrayListX);
            o.addProperty("arrayListY", arrayListY);
            o.addProperty("keystrokes", keystrokes);
            o.addProperty("keystrokesX", keystrokesX);
            o.addProperty("keystrokesY", keystrokesY);
            o.addProperty("keystrokesScale", keystrokesScale);
            o.addProperty("keystrokesAlpha", keystrokesAlpha);
            o.addProperty("keystrokesColor", keystrokesColor);
            o.addProperty("keystrokesShowMouse", keystrokesShowMouse);
            o.addProperty("keystrokesShowSpace", keystrokesShowSpace);
            
            // Armor
            o.addProperty("armorHud", armorHud);
            o.addProperty("armorHudX", armorHudX);
            o.addProperty("armorHudY", armorHudY);
            o.addProperty("armorHudScale", armorHudScale);
            o.addProperty("armorHudAlpha", armorHudAlpha);
            o.addProperty("armorHudShowTool", armorHudShowTool);
            o.addProperty("armorHudShowDurability", armorHudShowDurability);
            o.addProperty("armorHudColorIcons", armorHudColorIcons);
            o.addProperty("armorTint", armorTint);
            o.addProperty("noOverlay", noOverlay);
            o.addProperty("noHurtShake", noHurtShake);
            o.addProperty("noFireOverlay", noFireOverlay);
            o.addProperty("noTotemParticles", noTotemParticles);
            o.addProperty("noFireworkParticles", noFireworkParticles);
            o.addProperty("hitSound", hitSound);
            o.addProperty("hitSoundType", hitSoundType);
            o.addProperty("hitSoundVol", hitSoundVol);
            o.addProperty("hitSoundPitch", hitSoundPitch);
            o.addProperty("comboHud", comboHud);
            o.addProperty("comboX", comboX);
            o.addProperty("comboY", comboY);
            o.addProperty("comboScale", comboScale);
            o.addProperty("comboAlpha", comboAlpha);
            o.addProperty("comboColor", comboColor);
            o.addProperty("comboTimeout", comboTimeout);
            o.addProperty("watermark", watermark);
            o.addProperty("watermarkX", watermarkX);
            o.addProperty("watermarkY", watermarkY);
            o.addProperty("watermarkScale", watermarkScale);
            o.addProperty("watermarkAlpha", watermarkAlpha);
            o.addProperty("watermarkColor", watermarkColor);
            o.addProperty("watermarkName", watermarkName);
            o.addProperty("watermarkRainbow", watermarkRainbow);
            o.addProperty("watermarkNick", watermarkNick);
            o.addProperty("watermarkFps", watermarkFps);
            o.addProperty("watermarkTime", watermarkTime);
            o.addProperty("serverHud", serverHud);
            o.addProperty("serverX", serverX);
            o.addProperty("serverY", serverY);
            o.addProperty("serverScale", serverScale);
            o.addProperty("serverAlpha", serverAlpha);
            o.addProperty("serverColor", serverColor);
            o.addProperty("serverIp", serverIp);
            o.addProperty("serverTps", serverTps);
            o.addProperty("serverPing", serverPing);
            o.addProperty("clockHud", clockHud);
            o.addProperty("clockX", clockX);
            o.addProperty("clockY", clockY);
            o.addProperty("clockScale", clockScale);
            o.addProperty("clockAlpha", clockAlpha);
            o.addProperty("clockColor", clockColor);
            o.addProperty("clockSeconds", clockSeconds);
            o.addProperty("clockDate", clockDate);
            o.addProperty("clockGame", clockGame);
            o.addProperty("invHud", invHud);
            o.addProperty("invX", invX);
            o.addProperty("invY", invY);
            o.addProperty("invScale", invScale);
            o.addProperty("invAlpha", invAlpha);
            o.addProperty("invColor", invColor);
            o.addProperty("invHotbar", invHotbar);
            o.addProperty("totemHud", totemHud);
            o.addProperty("totemX", totemX);
            o.addProperty("totemY", totemY);
            o.addProperty("totemScale", totemScale);
            o.addProperty("totemAlpha", totemAlpha);
            o.addProperty("totemColor", totemColor);
            o.addProperty("totemHideZero", totemHideZero);
            o.addProperty("cdHud", cdHud);
            o.addProperty("cdX", cdX);
            o.addProperty("cdY", cdY);
            o.addProperty("cdAlpha", cdAlpha);
            o.addProperty("cdColor", cdColor);
            o.addProperty("cdTime", cdTime);
            o.addProperty("critFx", critFx);
            o.addProperty("critStyle", critStyle);
            o.addProperty("critCount", critCount);
            o.addProperty("killFx", killFx);
            o.addProperty("killFlash", killFlash);
            o.addProperty("killSound", killSound);
            o.addProperty("killParticles", killParticles);
            o.addProperty("killColor", killColor);
            o.addProperty("atkInd", atkInd);
            o.addProperty("atkStyle", atkStyle);
            o.addProperty("atkColor", atkColor);
            o.addProperty("atkSize", atkSize);
            o.addProperty("atkAlways", atkAlways);
            o.addProperty("atkOffset", atkOffset);
            o.addProperty("statsHud", statsHud);
            o.addProperty("statsX", statsX);
            o.addProperty("statsY", statsY);
            o.addProperty("statsScale", statsScale);
            o.addProperty("statsAlpha", statsAlpha);
            o.addProperty("statsColor", statsColor);
            o.addProperty("statsAcc", statsAcc);
            o.addProperty("statsKills", statsKills);
            o.addProperty("statsStreak", statsStreak);
            o.addProperty("statsDeaths", statsDeaths);
            o.addProperty("statsKd", statsKd);
            o.addProperty("statsPlayersOnly", statsPlayersOnly);
            o.addProperty("dlHud", dlHud);
            o.addProperty("dlX", dlX);
            o.addProperty("dlY", dlY);
            o.addProperty("dlScale", dlScale);
            o.addProperty("dlAlpha", dlAlpha);
            o.addProperty("dlColor", dlColor);
            o.addProperty("dlDealt", dlDealt);
            o.addProperty("dlTaken", dlTaken);
            o.addProperty("dlLines", dlLines);
            o.addProperty("dlLife", dlLife);
            o.addProperty("hbox", hbox);
            o.addProperty("hbPlayers", hbPlayers);
            o.addProperty("hbMobs", hbMobs);
            o.addProperty("hbOthers", hbOthers);
            o.addProperty("hbSelf", hbSelf);
            o.addProperty("hbFill", hbFill);
            o.addProperty("hbWidth", hbWidth);
            o.addProperty("hbRange", hbRange);
            o.addProperty("hbAlpha", hbAlpha);
            o.addProperty("hbColorP", hbColorP);
            o.addProperty("hbColorM", hbColorM);
            o.addProperty("hbColorO", hbColorO);
            o.addProperty("bout", bout);
            o.addProperty("boColor", boColor);
            o.addProperty("boRainbow", boRainbow);
            o.addProperty("boWidth", boWidth);
            o.addProperty("boFill", boFill);
            o.addProperty("boFillAlpha", boFillAlpha);
            o.addProperty("boHideVanilla", boHideVanilla);
            o.addProperty("chunkB", chunkB);
            o.addProperty("cbRadius", cbRadius);
            o.addProperty("cbHeight", cbHeight);
            o.addProperty("cbColor", cbColor);
            o.addProperty("cbWidth", cbWidth);
            o.addProperty("cbLevels", cbLevels);
            o.addProperty("cbAlpha", cbAlpha);
            o.addProperty("itemEsp", itemEsp);
            o.addProperty("ieRange", ieRange);
            o.addProperty("ieValuable", ieValuable);
            o.addProperty("ieFill", ieFill);
            o.addProperty("ieTracers", ieTracers);
            o.addProperty("ieColor", ieColor);
            o.addProperty("ieColorHi", ieColorHi);
            o.addProperty("ieWidth", ieWidth);
            o.addProperty("wp", wp);
            o.addProperty("wpBeam", wpBeam);
            o.addProperty("wpLabels", wpLabels);
            o.addProperty("wpDist", wpDist);
            o.addProperty("wpMax", wpMax);
            o.addProperty("wpColor", wpColor);
            o.addProperty("wpScale", wpScale);
            o.addProperty("wpDeath", wpDeath);
            o.addProperty("ptrail", ptrail);
            o.addProperty("ptColor", ptColor);
            o.addProperty("ptRainbow", ptRainbow);
            o.addProperty("ptLength", ptLength);
            o.addProperty("ptWidth", ptWidth);
            o.addProperty("etrail", etrail);
            o.addProperty("etColor", etColor);
            o.addProperty("etRainbow", etRainbow);
            o.addProperty("etLength", etLength);
            o.addProperty("etWidth", etWidth);
            o.addProperty("etAll", etAll);
            o.addProperty("pcdHud", pcdHud);
            o.addProperty("pcdX", pcdX);
            o.addProperty("pcdY", pcdY);
            o.addProperty("pcdScale", pcdScale);
            o.addProperty("pcdAlpha", pcdAlpha);
            o.addProperty("pcdColor", pcdColor);
            o.addProperty("pcdHideIdle", pcdHideIdle);
            o.addProperty("wingsOn", wingsOn);
            o.addProperty("wingStyle", wingStyle);
            o.addProperty("wingColor", wingColor);
            o.addProperty("wingRainbow", wingRainbow);
            o.addProperty("wingSize", wingSize);
            o.addProperty("wingSpeed", wingSpeed);
            o.addProperty("wingFriends", wingFriends);
            o.addProperty("auraOn", auraOn);
            o.addProperty("auraStyle", auraStyle);
            o.addProperty("auraColor", auraColor);
            o.addProperty("auraRainbow", auraRainbow);
            o.addProperty("auraSize", auraSize);
            o.addProperty("auraSpeed", auraSpeed);
            o.addProperty("auraFriends", auraFriends);
            o.addProperty("capeOn", capeOn);
            o.addProperty("capeColor", capeColor);
            o.addProperty("capeColor2", capeColor2);
            o.addProperty("capeRainbow", capeRainbow);
            o.addProperty("capeWave", capeWave);
            o.addProperty("capeFriends", capeFriends);
            o.addProperty("ntOn", ntOn);
            o.addProperty("ntRange", ntRange);
            o.addProperty("ntScale", ntScale);
            o.addProperty("ntBg", ntBg);
            o.addProperty("ntHealth", ntHealth);
            o.addProperty("ntDist", ntDist);
            o.addProperty("ntDistScale", ntDistScale);
            o.addProperty("ntHide", ntHide);
            o.addProperty("chamsOn", chamsOn);
            o.addProperty("chPlayers", chPlayers);
            o.addProperty("chMobs", chMobs);
            o.addProperty("chFriends", chFriends);
            o.addProperty("chSelf", chSelf);
            o.addProperty("swingAnim", swingAnim);
            o.addProperty("swingSpeed", swingSpeed);
            o.addProperty("swingSelfOnly", swingSelfOnly);
            o.addProperty("cloudsOn", cloudsOn);
            o.addProperty("cloudHide", cloudHide);
            o.addProperty("cloudTint", cloudTint);
            o.addProperty("cloudHue", cloudHue);
            o.addProperty("cloudSat", cloudSat);
            o.addProperty("cloudRainbow", cloudRainbow);
            o.addProperty("cloudHeightOffset", cloudHeightOffset);
            o.addProperty("tipOn", tipOn);
            o.addProperty("tipDur", tipDur);
            o.addProperty("tipId", tipId);
            o.addProperty("tipStack", tipStack);
            o.addProperty("potionHud", potionHud);
            o.addProperty("potionX", potionX);
            o.addProperty("potionY", potionY);
            o.addProperty("potionScale", potionScale);
            o.addProperty("potionAlpha", potionAlpha);
            o.addProperty("potionColor", potionColor);
            o.addProperty("potionIcons", potionIcons);
            o.addProperty("potionBars", potionBars);
            o.addProperty("potionLevel", potionLevel);
            o.addProperty("potionHoriz", potionHoriz);
            o.addProperty("hudHoriz", hudHoriz);
            o.addProperty("arrayListHoriz", arrayListHoriz);
            o.addProperty("armorHudHoriz", armorHudHoriz);
            o.addProperty("keystrokesHoriz", keystrokesHoriz);
            o.addProperty("jumpCircle", jumpCircle);
            o.addProperty("jcStyle", jcStyle);
            o.addProperty("jcSize", jcSize);
            o.addProperty("jcLife", jcLife);
            o.addProperty("jcWidth", jcWidth);
            o.addProperty("jcAlpha", jcAlpha);
            o.addProperty("jcHue", jcHue);
            o.addProperty("jcRainbow", jcRainbow);
            o.addProperty("jcLand", jcLand);

            // Время и погода
            o.addProperty("customTime", customTime);
            o.addProperty("timeValue", timeValue);
            o.addProperty("customWeather", customWeather);
            o.addProperty("weatherType", weatherType);

            // CustomItems: правила
            JsonArray cir = new JsonArray();
            for (CiRule r : ciRules) cir.add(r.toJson());
            o.add("ciRules", cir);

            // Макросы
            JsonArray mac = new JsonArray();
            for (Macro m : macros) {
                JsonObject jo = new JsonObject();
                jo.addProperty("key", m.key);
                jo.addProperty("text", m.text);
                jo.addProperty("enabled", m.enabled);
                mac.add(jo);
            }
            o.add("macroList", mac);

            // Круговое меню макросов
            o.addProperty("radialKey", radialKey);
            JsonArray rad = new JsonArray();
            for (RadialItem r : radialItems) {
                JsonObject jo = new JsonObject();
                jo.addProperty("label", r.label);
                jo.addProperty("text", r.text);
                rad.add(jo);
            }
            o.add("radialList", rad);

            // Item Highlight
            o.addProperty("highlight", highlight);
            JsonArray hl = new JsonArray();
            for (ItemHighlight.Entry e : highlights) {
                JsonObject jo = new JsonObject();
                jo.addProperty("name", e.name);
                jo.addProperty("hue", e.hue);
                jo.addProperty("alpha", e.alpha);
                hl.add(jo);
            }
            o.add("highlights", hl);

            // Друзья
            JsonArray fArr = new JsonArray();
            for (String f : friends) fArr.add(f);
            o.add("friends", fArr);

            if (file.getParent() != null) Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(o));
        } catch (Exception ignored) {}
    }

    /** true, если файла настроек раньше не было (первый запуск мода) */
    public static boolean freshInstall = false;

    public static void load() { freshInstall = !Files.exists(FILE); loadFrom(FILE); }

    /** Перечитать основной файл настроек (без изменения freshInstall). */
    public static void reload() { loadFrom(FILE); }

    /** Загружает настройки из указанного файла (для профилей). */
    public static void loadFrom(Path file) {
        if (ciRules.isEmpty() && !ciRulesSaved) ciRules.add(new CiRule("minecraft:netherite_sword", 0));
        try {
            if (Files.exists(file)) {
                JsonObject o = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
                customMainMenu = b(o, "customMainMenu", customMainMenu);
                menuStyle = (int) d(o, "menuStyle", menuStyle);
                menuTheme = (int) d(o, "menuTheme", menuTheme);
                customCrosshair = b(o, "customCrosshair", customCrosshair);
                chinaHat = b(o, "chinaHat", chinaHat);
                chinaHatColor = (int) d(o, "chinaHatColor", chinaHatColor);
                chinaHatMode = (int) d(o, "chinaHatMode", chinaHatMode);
                hatSize = d(o, "hatSize", hatSize);
                hatHeight = d(o, "hatHeight", hatHeight);
                hatCustom = b(o, "hatCustom", hatCustom);
                hatHue = d(o, "hatHue", hatHue);
                hatSat = d(o, "hatSat", hatSat);
                hatVal = d(o, "hatVal", hatVal);
                halo = b(o, "halo", halo);
                haloMode = (int) d(o, "haloMode", haloMode);
                haloSize = d(o, "haloSize", haloSize);
                haloHeight = d(o, "haloHeight", haloHeight);
                haloBob = b(o, "haloBob", haloBob);
                haloCustom = b(o, "haloCustom", haloCustom);
                haloHue = d(o, "haloHue", haloHue);
                haloSat = d(o, "haloSat", haloSat);
                haloVal = d(o, "haloVal", haloVal);
                horns = b(o, "horns", horns);
                hornsMode = (int) d(o, "hornsMode", hornsMode);
                hornsSize = d(o, "hornsSize", hornsSize);
                hornsGlow = b(o, "hornsGlow", hornsGlow);
                hornsCustom = b(o, "hornsCustom", hornsCustom);
                hornsHue = d(o, "hornsHue", hornsHue);
                hornsSat = d(o, "hornsSat", hornsSat);
                hornsVal = d(o, "hornsVal", hornsVal);
                entityBox = b(o, "entityBox", entityBox);
                predictions = b(o, "predictions", predictions);
                itemPhysic = b(o, "itemPhysic", itemPhysic);
                shulkerPreview = b(o, "shulkerPreview", shulkerPreview);
                autoRespawn = b(o, "autoRespawn", autoRespawn);
                thirdPerson = b(o, "thirdPerson", thirdPerson);
                freelookToggle = b(o, "freelookToggle", freelookToggle);
                freelookThird = b(o, "freelookThird", freelookThird);
                freelookSmooth = b(o, "freelookSmooth", freelookSmooth);
                freelookSens = d(o, "freelookSens", freelookSens);
                itemScroller = b(o, "itemScroller", itemScroller);
                streamerMode = b(o, "streamerMode", streamerMode);
                boardSpoofer = b(o, "boardSpoofer", boardSpoofer);
                sounds = b(o, "sounds", sounds);
                trails = b(o, "trails", trails);
                trailsColor = (int) d(o, "trailsColor", trailsColor);
                trailsLength = (int) d(o, "trailsLength", trailsLength);
                trailsHeight = d(o, "trailsHeight", trailsHeight);
                trailsCustom = b(o, "trailsCustom", trailsCustom);
                trailsHue = d(o, "trailsHue", trailsHue);
                trailsSat = d(o, "trailsSat", trailsSat);
                trailsVal = d(o, "trailsVal", trailsVal);
                zones = b(o, "zones", zones);
                zonesHeld = b(o, "zonesHeld", zonesHeld);
                targetEsp = b(o, "targetEsp", targetEsp);
                targetEspShape = (int) d(o, "targetEspShape", targetEspShape);
                targetEspSize = d(o, "targetEspSize", targetEspSize);
                targetEspSpeed = d(o, "targetEspSpeed", targetEspSpeed);
                targetEspAlpha = d(o, "targetEspAlpha", targetEspAlpha);
                targetEspHue = d(o, "targetEspHue", targetEspHue);
                targetEspHitHue = d(o, "targetEspHitHue", targetEspHitHue);
                skyShader = b(o, "skyShader", skyShader);
                skyPreset = (int) d(o, "skyPreset", skyPreset);
                skyAlpha = d(o, "skyAlpha", skyAlpha);
                skySpeed = d(o, "skySpeed", skySpeed);
                skyNightOnly = b(o, "skyNightOnly", skyNightOnly);
                skyStars = b(o, "skyStars", skyStars);
                particles = b(o, "particles", particles);
                damageFever = b(o, "damageFever", damageFever);
                customFog = b(o, "customFog", customFog);
                targetHud = b(o, "targetHud", targetHud);
                handView = b(o, "handView", handView);
                customItems = b(o, "customItems", customItems);
                ciModel = (int) d(o, "ciModel", ciModel);
                ciSkin = (int) d(o, "ciSkin", ciSkin);
                ciHue = d(o, "ciHue", ciHue);
                ciSat = d(o, "ciSat", ciSat);
                ciTarget = (int) d(o, "ciTarget", ciTarget);
                ciScale = d(o, "ciScale", ciScale);
                ciX = d(o, "ciX", ciX);
                ciY = d(o, "ciY", ciY);
                ciZ = d(o, "ciZ", ciZ);
                ciRX = d(o, "ciRX", ciRX);
                ciRY = d(o, "ciRY", ciRY);
                ciRZ = d(o, "ciRZ", ciRZ);
                leftHanded = b(o, "leftHanded", leftHanded);
                for (int k = 0; k < sv.length; k++) for (int h = 0; h < 2; h++) sv[k][h] = d(o, "sv" + k + "_" + h, sv[k][h]);
                for (int h = 0; h < 2; h++) svNoSwing[h] = b(o, "svNoSwing" + h, svNoSwing[h]);
                thScale = d(o, "thScale", thScale);
                thX = d(o, "thX", thX);
                thY = d(o, "thY", thY);
                thHue = d(o, "thHue", thHue);
                thDist = b(o, "thDist", thDist);
                fogOff = b(o, "fogOff", fogOff);
                fogStart = d(o, "fogStart", fogStart);
                fogEnd = d(o, "fogEnd", fogEnd);
                fogHue = d(o, "fogHue", fogHue);
                fogSat = d(o, "fogSat", fogSat);
                fogVal = d(o, "fogVal", fogVal);
                physicLift = d(o, "physicLift", physicLift);
                dfMine = b(o, "dfMine", dfMine);
                dfDecimals = b(o, "dfDecimals", dfDecimals);
                dfSize = d(o, "dfSize", dfSize);
                dfLife = d(o, "dfLife", dfLife);
                dfHue = d(o, "dfHue", dfHue);
                partMode = (int) d(o, "partMode", partMode);
                partShape = (int) d(o, "partShape", partShape);
                partCount = d(o, "partCount", partCount);
                partSize = d(o, "partSize", partSize);
                partLife = d(o, "partLife", partLife);
                partGravity = d(o, "partGravity", partGravity);
                partAlpha = d(o, "partAlpha", partAlpha);
                partHue = d(o, "partHue", partHue);
                partRainbow = b(o, "partRainbow", partRainbow);

                // HUD
                hud = b(o, "hud", hud);
                hudFps = b(o, "hudFps", hudFps);
                hudPing = b(o, "hudPing", hudPing);
                hudCoords = b(o, "hudCoords", hudCoords);
                hudFacing = b(o, "hudFacing", hudFacing);
                hudEffects = b(o, "hudEffects", hudEffects);
                hudScale = d(o, "hudScale", hudScale);
                hudAlpha = d(o, "hudAlpha", hudAlpha);
                hudX = (int) d(o, "hudX", hudX);
                hudY = (int) d(o, "hudY", hudY);
                hudColor = (int) d(o, "hudColor", hudColor);

                // ArrayList & Keystrokes
                arrayList = b(o, "arrayList", arrayList);
                arrayListColor = (int) d(o, "arrayListColor", arrayListColor);
                arrayListShadow = b(o, "arrayListShadow", arrayListShadow);
                arrayListScale = d(o, "arrayListScale", arrayListScale);
                arrayListAlpha = d(o, "arrayListAlpha", arrayListAlpha);
                arrayListX = (int) d(o, "arrayListX", arrayListX);
                arrayListY = (int) d(o, "arrayListY", arrayListY);
                keystrokes = b(o, "keystrokes", keystrokes);
                keystrokesX = (int) d(o, "keystrokesX", keystrokesX);
                keystrokesY = (int) d(o, "keystrokesY", keystrokesY);
                keystrokesScale = d(o, "keystrokesScale", keystrokesScale);
                keystrokesAlpha = d(o, "keystrokesAlpha", keystrokesAlpha);
                keystrokesColor = (int) d(o, "keystrokesColor", keystrokesColor);
                keystrokesShowMouse = b(o, "keystrokesShowMouse", keystrokesShowMouse);
                keystrokesShowSpace = b(o, "keystrokesShowSpace", keystrokesShowSpace);

                // Armor
                armorHud = b(o, "armorHud", armorHud);
                armorHudX = (int) d(o, "armorHudX", armorHudX);
                armorHudY = (int) d(o, "armorHudY", armorHudY);
                armorHudScale = d(o, "armorHudScale", armorHudScale);
                armorHudAlpha = d(o, "armorHudAlpha", armorHudAlpha);
                armorHudShowTool = b(o, "armorHudShowTool", armorHudShowTool);
                armorHudShowDurability = b(o, "armorHudShowDurability", armorHudShowDurability);
                armorHudColorIcons = b(o, "armorHudColorIcons", armorHudColorIcons);
                armorTint = b(o, "armorTint", armorTint);
                noOverlay = b(o, "noOverlay", noOverlay);
                noHurtShake = b(o, "noHurtShake", noHurtShake);
                noFireOverlay = b(o, "noFireOverlay", noFireOverlay);
                noTotemParticles = b(o, "noTotemParticles", noTotemParticles);
                noFireworkParticles = b(o, "noFireworkParticles", noFireworkParticles);
                hitSound = b(o, "hitSound", hitSound);
                hitSoundType = (int) d(o, "hitSoundType", hitSoundType);
                hitSoundVol = d(o, "hitSoundVol", hitSoundVol);
                hitSoundPitch = d(o, "hitSoundPitch", hitSoundPitch);
                comboHud = b(o, "comboHud", comboHud);
                comboX = (int) d(o, "comboX", comboX);
                comboY = (int) d(o, "comboY", comboY);
                comboScale = d(o, "comboScale", comboScale);
                comboAlpha = d(o, "comboAlpha", comboAlpha);
                comboColor = (int) d(o, "comboColor", comboColor);
                comboTimeout = d(o, "comboTimeout", comboTimeout);
                watermark = b(o, "watermark", watermark);
                watermarkX = (int) d(o, "watermarkX", watermarkX);
                watermarkY = (int) d(o, "watermarkY", watermarkY);
                watermarkScale = d(o, "watermarkScale", watermarkScale);
                watermarkAlpha = d(o, "watermarkAlpha", watermarkAlpha);
                watermarkColor = (int) d(o, "watermarkColor", watermarkColor);
                watermarkName = (int) d(o, "watermarkName", watermarkName);
                watermarkRainbow = b(o, "watermarkRainbow", watermarkRainbow);
                watermarkNick = b(o, "watermarkNick", watermarkNick);
                watermarkFps = b(o, "watermarkFps", watermarkFps);
                watermarkTime = b(o, "watermarkTime", watermarkTime);
                serverHud = b(o, "serverHud", serverHud);
                serverX = (int) d(o, "serverX", serverX);
                serverY = (int) d(o, "serverY", serverY);
                serverScale = d(o, "serverScale", serverScale);
                serverAlpha = d(o, "serverAlpha", serverAlpha);
                serverColor = (int) d(o, "serverColor", serverColor);
                serverIp = b(o, "serverIp", serverIp);
                serverTps = b(o, "serverTps", serverTps);
                serverPing = b(o, "serverPing", serverPing);
                clockHud = b(o, "clockHud", clockHud);
                clockX = (int) d(o, "clockX", clockX);
                clockY = (int) d(o, "clockY", clockY);
                clockScale = d(o, "clockScale", clockScale);
                clockAlpha = d(o, "clockAlpha", clockAlpha);
                clockColor = (int) d(o, "clockColor", clockColor);
                clockSeconds = b(o, "clockSeconds", clockSeconds);
                clockDate = b(o, "clockDate", clockDate);
                clockGame = b(o, "clockGame", clockGame);
                invHud = b(o, "invHud", invHud);
                invX = (int) d(o, "invX", invX);
                invY = (int) d(o, "invY", invY);
                invScale = d(o, "invScale", invScale);
                invAlpha = d(o, "invAlpha", invAlpha);
                invColor = (int) d(o, "invColor", invColor);
                invHotbar = b(o, "invHotbar", invHotbar);
                totemHud = b(o, "totemHud", totemHud);
                totemX = (int) d(o, "totemX", totemX);
                totemY = (int) d(o, "totemY", totemY);
                totemScale = d(o, "totemScale", totemScale);
                totemAlpha = d(o, "totemAlpha", totemAlpha);
                totemColor = (int) d(o, "totemColor", totemColor);
                totemHideZero = b(o, "totemHideZero", totemHideZero);
                cdHud = b(o, "cdHud", cdHud);
                cdX = (int) d(o, "cdX", cdX);
                cdY = (int) d(o, "cdY", cdY);
                cdAlpha = d(o, "cdAlpha", cdAlpha);
                cdColor = (int) d(o, "cdColor", cdColor);
                cdTime = b(o, "cdTime", cdTime);
                critFx = b(o, "critFx", critFx);
                critStyle = (int) d(o, "critStyle", critStyle);
                critCount = d(o, "critCount", critCount);
                killFx = b(o, "killFx", killFx);
                killFlash = b(o, "killFlash", killFlash);
                killSound = b(o, "killSound", killSound);
                killParticles = (int) d(o, "killParticles", killParticles);
                killColor = (int) d(o, "killColor", killColor);
                atkInd = b(o, "atkInd", atkInd);
                atkStyle = (int) d(o, "atkStyle", atkStyle);
                atkColor = (int) d(o, "atkColor", atkColor);
                atkSize = d(o, "atkSize", atkSize);
                atkAlways = b(o, "atkAlways", atkAlways);
                atkOffset = (int) d(o, "atkOffset", atkOffset);
                statsHud = b(o, "statsHud", statsHud);
                statsX = (int) d(o, "statsX", statsX);
                statsY = (int) d(o, "statsY", statsY);
                statsScale = d(o, "statsScale", statsScale);
                statsAlpha = d(o, "statsAlpha", statsAlpha);
                statsColor = (int) d(o, "statsColor", statsColor);
                statsAcc = b(o, "statsAcc", statsAcc);
                statsKills = b(o, "statsKills", statsKills);
                statsStreak = b(o, "statsStreak", statsStreak);
                statsDeaths = b(o, "statsDeaths", statsDeaths);
                statsKd = b(o, "statsKd", statsKd);
                statsPlayersOnly = b(o, "statsPlayersOnly", statsPlayersOnly);
                dlHud = b(o, "dlHud", dlHud);
                dlX = (int) d(o, "dlX", dlX);
                dlY = (int) d(o, "dlY", dlY);
                dlScale = d(o, "dlScale", dlScale);
                dlAlpha = d(o, "dlAlpha", dlAlpha);
                dlColor = (int) d(o, "dlColor", dlColor);
                dlDealt = b(o, "dlDealt", dlDealt);
                dlTaken = b(o, "dlTaken", dlTaken);
                dlLines = (int) d(o, "dlLines", dlLines);
                dlLife = d(o, "dlLife", dlLife);
                hbox = b(o, "hbox", hbox);
                hbPlayers = b(o, "hbPlayers", hbPlayers);
                hbMobs = b(o, "hbMobs", hbMobs);
                hbOthers = b(o, "hbOthers", hbOthers);
                hbSelf = b(o, "hbSelf", hbSelf);
                hbFill = b(o, "hbFill", hbFill);
                hbWidth = d(o, "hbWidth", hbWidth);
                hbRange = d(o, "hbRange", hbRange);
                hbAlpha = d(o, "hbAlpha", hbAlpha);
                hbColorP = (int) d(o, "hbColorP", hbColorP);
                hbColorM = (int) d(o, "hbColorM", hbColorM);
                hbColorO = (int) d(o, "hbColorO", hbColorO);
                bout = b(o, "bout", bout);
                boColor = (int) d(o, "boColor", boColor);
                boRainbow = b(o, "boRainbow", boRainbow);
                boWidth = d(o, "boWidth", boWidth);
                boFill = b(o, "boFill", boFill);
                boFillAlpha = d(o, "boFillAlpha", boFillAlpha);
                boHideVanilla = b(o, "boHideVanilla", boHideVanilla);
                chunkB = b(o, "chunkB", chunkB);
                cbRadius = (int) d(o, "cbRadius", cbRadius);
                cbHeight = d(o, "cbHeight", cbHeight);
                cbColor = (int) d(o, "cbColor", cbColor);
                cbWidth = d(o, "cbWidth", cbWidth);
                cbLevels = b(o, "cbLevels", cbLevels);
                cbAlpha = d(o, "cbAlpha", cbAlpha);
                itemEsp = b(o, "itemEsp", itemEsp);
                ieRange = d(o, "ieRange", ieRange);
                ieValuable = b(o, "ieValuable", ieValuable);
                ieFill = b(o, "ieFill", ieFill);
                ieTracers = b(o, "ieTracers", ieTracers);
                ieColor = (int) d(o, "ieColor", ieColor);
                ieColorHi = (int) d(o, "ieColorHi", ieColorHi);
                ieWidth = d(o, "ieWidth", ieWidth);
                wp = b(o, "wp", wp);
                wpBeam = b(o, "wpBeam", wpBeam);
                wpLabels = b(o, "wpLabels", wpLabels);
                wpDist = b(o, "wpDist", wpDist);
                wpMax = d(o, "wpMax", wpMax);
                wpColor = (int) d(o, "wpColor", wpColor);
                wpScale = d(o, "wpScale", wpScale);
                wpDeath = b(o, "wpDeath", wpDeath);
                ptrail = b(o, "ptrail", ptrail);
                ptColor = (int) d(o, "ptColor", ptColor);
                ptRainbow = b(o, "ptRainbow", ptRainbow);
                ptLength = (int) d(o, "ptLength", ptLength);
                ptWidth = d(o, "ptWidth", ptWidth);
                etrail = b(o, "etrail", etrail);
                etColor = (int) d(o, "etColor", etColor);
                etRainbow = b(o, "etRainbow", etRainbow);
                etLength = (int) d(o, "etLength", etLength);
                etWidth = d(o, "etWidth", etWidth);
                etAll = b(o, "etAll", etAll);
                pcdHud = b(o, "pcdHud", pcdHud);
                pcdX = (int) d(o, "pcdX", pcdX);
                pcdY = (int) d(o, "pcdY", pcdY);
                pcdScale = d(o, "pcdScale", pcdScale);
                pcdAlpha = d(o, "pcdAlpha", pcdAlpha);
                pcdColor = (int) d(o, "pcdColor", pcdColor);
                pcdHideIdle = b(o, "pcdHideIdle", pcdHideIdle);
                wingsOn = b(o, "wingsOn", wingsOn);
                wingStyle = (int) d(o, "wingStyle", wingStyle);
                wingColor = (int) d(o, "wingColor", wingColor);
                wingRainbow = b(o, "wingRainbow", wingRainbow);
                wingSize = d(o, "wingSize", wingSize);
                wingSpeed = d(o, "wingSpeed", wingSpeed);
                wingFriends = b(o, "wingFriends", wingFriends);
                auraOn = b(o, "auraOn", auraOn);
                auraStyle = (int) d(o, "auraStyle", auraStyle);
                auraColor = (int) d(o, "auraColor", auraColor);
                auraRainbow = b(o, "auraRainbow", auraRainbow);
                auraSize = d(o, "auraSize", auraSize);
                auraSpeed = d(o, "auraSpeed", auraSpeed);
                auraFriends = b(o, "auraFriends", auraFriends);
                capeOn = b(o, "capeOn", capeOn);
                capeColor = (int) d(o, "capeColor", capeColor);
                capeColor2 = (int) d(o, "capeColor2", capeColor2);
                capeRainbow = b(o, "capeRainbow", capeRainbow);
                capeWave = d(o, "capeWave", capeWave);
                capeFriends = b(o, "capeFriends", capeFriends);
                ntOn = b(o, "ntOn", ntOn);
                ntRange = d(o, "ntRange", ntRange);
                ntScale = d(o, "ntScale", ntScale);
                ntBg = b(o, "ntBg", ntBg);
                ntHealth = b(o, "ntHealth", ntHealth);
                ntDist = b(o, "ntDist", ntDist);
                ntDistScale = b(o, "ntDistScale", ntDistScale);
                ntHide = b(o, "ntHide", ntHide);
                chamsOn = b(o, "chamsOn", chamsOn);
                chPlayers = b(o, "chPlayers", chPlayers);
                chMobs = b(o, "chMobs", chMobs);
                chFriends = b(o, "chFriends", chFriends);
                chSelf = b(o, "chSelf", chSelf);
                swingAnim = b(o, "swingAnim", swingAnim);
                swingSpeed = d(o, "swingSpeed", swingSpeed);
                swingSelfOnly = b(o, "swingSelfOnly", swingSelfOnly);
                cloudsOn = b(o, "cloudsOn", cloudsOn);
                cloudHide = b(o, "cloudHide", cloudHide);
                cloudTint = b(o, "cloudTint", cloudTint);
                cloudHue = d(o, "cloudHue", cloudHue);
                cloudSat = d(o, "cloudSat", cloudSat);
                cloudRainbow = b(o, "cloudRainbow", cloudRainbow);
                cloudHeightOffset = d(o, "cloudHeightOffset", cloudHeightOffset);
                tipOn = b(o, "tipOn", tipOn);
                tipDur = b(o, "tipDur", tipDur);
                tipId = b(o, "tipId", tipId);
                tipStack = b(o, "tipStack", tipStack);
                potionHud = b(o, "potionHud", potionHud);
                potionX = (int) d(o, "potionX", potionX);
                potionY = (int) d(o, "potionY", potionY);
                potionScale = d(o, "potionScale", potionScale);
                potionAlpha = d(o, "potionAlpha", potionAlpha);
                potionColor = (int) d(o, "potionColor", potionColor);
                potionIcons = b(o, "potionIcons", potionIcons);
                potionBars = b(o, "potionBars", potionBars);
                potionLevel = b(o, "potionLevel", potionLevel);
                potionHoriz = b(o, "potionHoriz", potionHoriz);
                hudHoriz = b(o, "hudHoriz", hudHoriz);
                arrayListHoriz = b(o, "arrayListHoriz", arrayListHoriz);
                armorHudHoriz = b(o, "armorHudHoriz", armorHudHoriz);
                keystrokesHoriz = b(o, "keystrokesHoriz", keystrokesHoriz);
                jumpCircle = b(o, "jumpCircle", jumpCircle);
                jcStyle = (int) d(o, "jcStyle", jcStyle);
                jcSize = d(o, "jcSize", jcSize);
                jcLife = d(o, "jcLife", jcLife);
                jcWidth = d(o, "jcWidth", jcWidth);
                jcAlpha = d(o, "jcAlpha", jcAlpha);
                jcHue = d(o, "jcHue", jcHue);
                jcRainbow = b(o, "jcRainbow", jcRainbow);
                jcLand = b(o, "jcLand", jcLand);

                // Время и погода
                customTime = b(o, "customTime", customTime);
                timeValue = (int) d(o, "timeValue", timeValue);
                customWeather = b(o, "customWeather", customWeather);
                weatherType = (int) d(o, "weatherType", weatherType);

                ciRules.clear();
                if (o.has("ciRules")) {
                    ciRulesSaved = true;
                    for (JsonElement el : o.getAsJsonArray("ciRules")) {
                        try { ciRules.add(CiRule.fromJson(el.getAsJsonObject())); } catch (Exception ignored) {}
                    }
                } else {
                    // старый формат: одна модель для незеритового меча
                    CiRule r = new CiRule("minecraft:netherite_sword", ciModel);
                    r.skin = ciSkin; r.hue = ciHue; r.sat = ciSat;
                    r.scale = ciScale; r.x = ciX; r.y = ciY; r.z = ciZ; r.rx = ciRX; r.ry = ciRY; r.rz = ciRZ;
                    ciRules.add(r);
                }

                macros.clear();
                if (o.has("macroList")) {
                    for (JsonElement el : o.getAsJsonArray("macroList")) {
                        JsonObject jo = el.getAsJsonObject();
                        Macro m = new Macro(jo.get("key").getAsInt(), jo.get("text").getAsString());
                        if (jo.has("enabled")) m.enabled = jo.get("enabled").getAsBoolean();
                        macros.add(m);
                    }
                } else if (o.has("macros")) {
                    // старый формат "клавиша:команда" -> переносим
                    for (JsonElement el : o.getAsJsonArray("macros")) {
                        try {
                            String[] parts = el.getAsString().split(":", 2);
                            if (parts.length == 2) {
                                String cmd = parts[1].trim();
                                if (!cmd.startsWith("/")) cmd = "/" + cmd;
                                macros.add(new Macro(Integer.parseInt(parts[0].trim()), cmd));
                            }
                        } catch (Exception ignored) {}
                    }
                }

                if (o.has("radialKey")) radialKey = o.get("radialKey").getAsInt();
                if (o.has("radialList")) {
                    radialItems.clear();
                    for (JsonElement el : o.getAsJsonArray("radialList")) {
                        JsonObject jo = el.getAsJsonObject();
                        if (!jo.has("text")) continue;
                        radialItems.add(new RadialItem(
                                jo.has("label") ? jo.get("label").getAsString() : "",
                                jo.get("text").getAsString()));
                    }
                }

                highlight = b(o, "highlight", highlight);
                highlights.clear();
                if (o.has("highlights")) {
                    for (JsonElement el : o.getAsJsonArray("highlights")) {
                        JsonObject jo = el.getAsJsonObject();
                        highlights.add(new ItemHighlight.Entry(
                                jo.get("name").getAsString(),
                                jo.has("hue") ? jo.get("hue").getAsDouble() : 0.1,
                                jo.has("alpha") ? jo.get("alpha").getAsDouble() : 0.6));
                    }
                }

                friends.clear();
                if (o.has("friends")) for (JsonElement el : o.getAsJsonArray("friends")) friends.add(el.getAsString());
            }
        } catch (Exception ignored) {}
    }

    private static boolean b(JsonObject o, String k, boolean def) { return o.has(k) ? o.get(k).getAsBoolean() : def; }
    private static double d(JsonObject o, String k, double def) { return o.has(k) ? o.get(k).getAsDouble() : def; }
}
