package com.example.visuals;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import org.lwjgl.glfw.GLFW;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

public final class MainMenuUI {

    private MainMenuUI() {}

    public static final int ACCENT = 0xFF7CE38B;

    // ---------- состояние ----------
    private static boolean prevDown;
    private static volatile WorldInfo lastWorld;
    private static volatile boolean worldsLoaded;

    private record WorldInfo(String name, String mode, long lastPlayed) {}

    private enum Act { NONE, WORLDS, MULTI, OPTIONS, MODS, QUIT }

    private static Act hit = Act.NONE;

    // ---------- вызывается из миксина ----------

    public static void onOpen(MinecraftClient mc) {
        prevDown = isDown(mc);
        CompletableFuture.runAsync(MainMenuUI::loadLastWorld);
    }

    public static void render(DrawContext c, MinecraftClient mc, Screen screen, TextRenderer tr, int mx, int my) {
        boolean down = isDown(mc);
        boolean click = down && !prevDown;
        prevDown = down;
        hit = Act.NONE;

        int w = screen.width;
        int h = screen.height;
        LocalDateTime now = LocalDateTime.now();

        topBar(c, tr, w, now);
        int clockBottom = clock(c, w, h, now);
        datePill(c, tr, mc, w, h, now, clockBottom);
        continueCard(c, tr, w, h, mx, my, click);
        dock(c, tr, w, h, mx, my, click);

        switch (hit) {
            case WORLDS -> mc.setScreen(new SelectWorldScreen(screen));
            case MULTI -> mc.setScreen(new MultiplayerScreen(screen));
            case OPTIONS -> mc.setScreen(new OptionsScreen(screen, mc.options));
            case MODS -> openMods(mc, screen);
            case QUIT -> mc.scheduleStop();
            default -> { }
        }
    }

    private static boolean isDown(MinecraftClient mc) {
        return GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
    }

    // ---------- верхняя строка ----------

    private static final String[] DOW_SHORT = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
    private static final String[] DOW_FULL = {"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"};
    private static final String[] MON_SHORT = {"янв", "фев", "мар", "апр", "мая", "июн", "июл", "авг", "сен", "окт", "ноя", "дек"};
    private static final String[] MON_GEN = {"января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря"};

    private static void topBar(DrawContext c, TextRenderer tr, int w, LocalDateTime now) {
        int bh = 15;
        for (int i = 0; i < bh; i++) {
            int a = 0xB0 - i * 0xA0 / bh;
            c.fill(0, i, w, i + 1, (a << 24) | 0x0C1015);
        }
        creeper(c, 6, 3, 1);
        c.drawText(tr, "ArchitektVisuals", 18, 4, 0xFFF2F5F7, false);

        String t = DOW_SHORT[now.getDayOfWeek().getValue() - 1] + " " + now.getDayOfMonth() + " "
                + MON_SHORT[now.getMonthValue() - 1] + "  " + hhmm(now);
        int tw = tr.getWidth(t);
        c.drawText(tr, t, w - tw - 8, 4, 0xFFF2F5F7, false);
        c.drawText(tr, "RU", w - tw - 8 - 14 - tr.getWidth("RU"), 4, 0xFFD6DDE3, false);
    }

    private static String hhmm(LocalDateTime now) {
        return String.format("%02d:%02d", now.getHour(), now.getMinute());
    }

    // ---------- большие часы (7 сегментов) ----------

    private static final int[] SEG = {63, 6, 91, 79, 102, 109, 125, 7, 127, 111};

    private static int clock(DrawContext c, int w, int h, LocalDateTime now) {
        int H = Math.max(36, Math.min(150, (int) (h * 0.26)));
        int W = (int) (H * 0.52);
        int t = Math.max(2, Math.round(H * 0.05f));
        int gap = Math.max(4, (int) (W * 0.22));
        int total = 4 * W + 4 * gap + t;
        int x0 = (w - total) / 2;
        int y0 = 15 + (int) (h * 0.04);
        int[] d = {now.getHour() / 10, now.getHour() % 10, now.getMinute() / 10, now.getMinute() % 10};

        clockPass(c, x0 + 1, y0 + 2, W, H, t, gap, d, 0x3A000000); // тень
        clockPass(c, x0, y0, W, H, t, gap, d, 0xE8F5F8FA);
        return y0 + H;
    }

    private static void clockPass(DrawContext c, int x0, int y0, int W, int H, int t, int gap, int[] d, int col) {
        int x1 = x0 + W + gap;
        int cx = x1 + W + gap;
        int x2 = cx + t + gap;
        int x3 = x2 + W + gap;
        digit(c, x0, y0, W, H, t, d[0], col);
        digit(c, x1, y0, W, H, t, d[1], col);
        int yA = y0 + H * 3 / 10 - t / 2;
        int yB = y0 + H * 7 / 10 - t / 2;
        c.fill(cx, yA, cx + t, yA + t, col);
        c.fill(cx, yB, cx + t, yB + t, col);
        digit(c, x2, y0, W, H, t, d[2], col);
        digit(c, x3, y0, W, H, t, d[3], col);
    }

    private static void digit(DrawContext c, int x, int y, int W, int H, int t, int n, int col) {
        int m = SEG[n];
        boolean a = (m & 1) != 0, b = (m & 2) != 0, cc = (m & 4) != 0, d = (m & 8) != 0;
        boolean e = (m & 16) != 0, f = (m & 32) != 0, g = (m & 64) != 0;
        int mid = y + (H - t) / 2;
        int upTop = a ? y + t : y;
        int loBot = d ? y + H - t : y + H;
        int upBot = g ? mid : mid + t / 2;
        int loTop = g ? mid + t : mid + t / 2;
        if (a) c.fill(x, y, x + W, y + t, col);
        if (g) c.fill(x, mid, x + W, mid + t, col);
        if (d) c.fill(x, y + H - t, x + W, y + H, col);
        if (f) c.fill(x, upTop, x + t, upBot, col);
        if (b) c.fill(x + W - t, upTop, x + W, upBot, col);
        if (e) c.fill(x, loTop, x + t, loBot, col);
        if (cc) c.fill(x + W - t, loTop, x + W, loBot, col);
    }

    // ---------- плашка с датой ----------

    private static void datePill(DrawContext c, TextRenderer tr, MinecraftClient mc, int w, int h,
                                 LocalDateTime now, int clockBottom) {
        String date = DOW_FULL[now.getDayOfWeek().getValue() - 1] + ", " + now.getDayOfMonth() + " "
                + MON_GEN[now.getMonthValue() - 1];
        int hr = now.getHour();
        String greet = hr < 5 ? "Доброй ночи" : hr < 12 ? "Доброе утро" : hr < 18 ? "Добрый день" : "Добрый вечер";
        String name = mc.getSession().getUsername();
        String text = date + "  \u00B7  " + greet + ", " + name;

        int pw = tr.getWidth(text) + 24;
        int ph = 18;
        int px = (w - pw) / 2;
        int py = clockBottom + Math.max(8, (int) (h * 0.03));
        glass(c, px, py, pw, ph, 9, 0x8A262E36, 0x34FFFFFF);
        c.drawText(tr, text, px + 12, py + 5, 0xFFEEF2F4, false);
    }

    // ---------- карточка "Продолжить" ----------

    private static void continueCard(DrawContext c, TextRenderer tr, int w, int h, int mx, int my, boolean click) {
        final int cw = 184, ch = 68;
        int cx = w - cw - 16;
        int cy = h - ch - 58;
        WorldInfo wi = lastWorld;

        glass(c, cx, cy, cw, ch, 8, 0x98161C24, 0x34FFFFFF);
        c.drawText(tr, wi != null ? "ПРОДОЛЖИТЬ" : "НАЧАТЬ", cx + 9, cy + 8, 0xFF9AA4AE, false);
        worldIcon(c, cx + 9, cy + 21);

        String title, sub, play;
        if (wi != null) {
            title = wi.name();
            sub = wi.mode() + " \u00B7 " + ago(wi.lastPlayed());
            play = "Играть";
        } else if (!worldsLoaded) {
            title = "Загрузка...";
            sub = "";
            play = "Играть";
        } else {
            title = "Миров пока нет";
            sub = "Создайте первый мир";
            play = "Создать";
        }

        int tx = cx + 34;
        int max = cw - 34 - 9;
        c.drawText(tr, fit(tr, title, max), tx, cy + 22, 0xFFFFFFFF, false);
        c.drawText(tr, fit(tr, sub, max), tx, cy + 33, 0xFFAAB3BC, false);

        int bw = 62, bh = 16;
        int bx = cx + cw - 9 - bw;
        int by = cy + ch - 9 - bh;
        boolean hp = in(mx, my, bx, by, bw, bh);
        rrect(c, bx, by, bw, bh, 8, hp ? 0xFFFFFFFF : 0xFFE4EAE6);
        for (int i = 0; i < 4; i++) {
            c.fill(bx + 9 + i, by + 4 + i, bx + 10 + i, by + bh - 4 - i, 0xFF10151A);
        }
        c.drawText(tr, play, bx + 18, by + 4, 0xFF10151A, false);
        if (click && hp) hit = Act.WORLDS;

        String all = "Все миры";
        int ax = cx + 9;
        int ay = by + 4;
        boolean ha = in(mx, my, ax - 2, ay - 3, tr.getWidth(all) + 4, 15);
        c.drawText(tr, all, ax, ay, ha ? 0xFFFFFFFF : 0xFFB9C2CB, false);
        if (click && ha) hit = Act.WORLDS;
    }

    private static void worldIcon(DrawContext c, int x, int y) {
        rrect(c, x, y, 18, 18, 3, 0xFF6B4A2B);
        rrect(c, x, y, 18, 8, 3, 0xFF8EC5F0);
        c.fill(x, y + 6, x + 18, y + 10, 0xFF5DA03A);
        c.fill(x, y + 10, x + 18, y + 11, 0xFF477A2B);
        c.fill(x + 3, y + 13, x + 5, y + 14, 0xFF543A20);
        c.fill(x + 9, y + 15, x + 11, y + 16, 0xFF543A20);
        c.fill(x + 13, y + 12, x + 15, y + 13, 0xFF543A20);
    }

    private static String ago(long t) {
        long s = Math.max(0, (System.currentTimeMillis() - t) / 1000);
        if (s < 60) return "только что";
        long m = s / 60;
        if (m < 60) return m + " мин назад";
        long hr = m / 60;
        if (hr < 24) return hr + " ч назад";
        long d = hr / 24;
        if (d < 30) return d + " дн назад";
        return (d / 30) + " мес назад";
    }

    // ---------- нижний док ----------

    private static final int CUBE = 0, SERVER = 1, CLOUD = 2, LOGO = 3, GEAR = 4, LAYERS = 5, INFO = 6, POWER = 7;
    private static final int[] DOCK = {CUBE, SERVER, CLOUD, -1, LOGO, -1, GEAR, LAYERS, -1, INFO, POWER};
    private static final String[] TIPS = {
            "Одиночная игра", "Сетевая игра", "Realms (скоро)", "Главное меню",
            "Настройки", "Моды", "Minecraft 1.21.11", "Выход"
    };

    private static void dock(DrawContext c, TextRenderer tr, int w, int h, int mx, int my, boolean click) {
        final int BTN = 24, GAP = 4, SEP = 9, PAD = 6;

        int total = PAD * 2;
        for (int i = 0; i < DOCK.length; i++) {
            if (DOCK[i] < 0) {
                total += SEP;
            } else {
                total += BTN;
                if (i + 1 < DOCK.length && DOCK[i + 1] >= 0) total += GAP;
            }
        }
        int dh = BTN + PAD * 2;
        int dx = (w - total) / 2;
        int dy = h - dh - 10;
        glass(c, dx, dy, total, dh, 11, 0xA0141A22, 0x38FFFFFF);

        int x = dx + PAD;
        int y = dy + PAD;
        int hovered = -1;
        int hoverX = 0;
        for (int i = 0; i < DOCK.length; i++) {
            int id = DOCK[i];
            if (id < 0) {
                c.fill(x + SEP / 2, y + 4, x + SEP / 2 + 1, y + BTN - 4, 0x30FFFFFF);
                x += SEP;
                continue;
            }
            boolean hov = in(mx, my, x, y, BTN, BTN);
            if (id == LOGO) glass(c, x, y, BTN, BTN, 7, 0xFF080B0E, 0x40FFFFFF);
            else rrect(c, x, y, BTN, BTN, 7, hov ? 0x5CFFFFFF : 0x2AFFFFFF);

            drawIcon(c, id, x, y, BTN, hov ? 0xFFFFFFFF : 0xFFD6DDE3);

            if (hov) {
                hovered = id;
                hoverX = x;
                if (click) hit = actFor(id);
            }
            x += BTN;
            if (i + 1 < DOCK.length && DOCK[i + 1] >= 0) x += GAP;
        }

        if (hovered >= 0) {
            String tip = TIPS[hovered];
            int tw = tr.getWidth(tip) + 12;
            int th = 16;
            int tx = Math.max(4, Math.min(w - tw - 4, hoverX + BTN / 2 - tw / 2));
            int ty = dy - th - 5;
            rrect(c, tx, ty, tw, th, 6, 0xE0101418);
            c.drawText(tr, tip, tx + 6, ty + 4, 0xFFFFFFFF, false);
        }
    }

    private static Act actFor(int id) {
        return switch (id) {
            case CUBE -> Act.WORLDS;
            case SERVER -> Act.MULTI;
            case GEAR -> Act.OPTIONS;
            case LAYERS -> Act.MODS;
            case POWER -> Act.QUIT;
            default -> Act.NONE;
        };
    }

    private static void openMods(MinecraftClient mc, Screen parent) {
        try {
            Class<?> k = Class.forName("com.terraformersmc.modmenu.gui.ModsScreen");
            mc.setScreen((Screen) k.getConstructor(Screen.class).newInstance(parent));
        } catch (Throwable ignored) {
        }
    }

    // ---------- иконки ----------

    private static final String[] ICON_CUBE = {
            ".....#.....",
            "...##.##...",
            ".##.....##.",
            "#.........#",
            "###.....###",
            "#..##.##..#",
            "#....#....#",
            "#....#....#",
            ".##..#..##.",
            "...#####...",
            ".....#....."
    };
    private static final String[] ICON_SERVER = {
            "###########",
            "#.........#",
            "#.#.#.....#",
            "#.........#",
            "###########",
            "...........",
            "###########",
            "#.........#",
            "#.#.#.....#",
            "#.........#",
            "###########"
    };
    private static final String[] ICON_CLOUD = {
            "...........",
            "...........",
            "....###....",
            "...#...#...",
            ".###....##.",
            "#.........#",
            "#.........#",
            ".#########.",
            "...........",
            "...........",
            "..........."
    };
    private static final String[] ICON_LAYERS = {
            ".....#.....",
            "...##.##...",
            ".##.....##.",
            "...##.##...",
            ".....#.....",
            ".#.......#.",
            "..##...##..",
            "....###....",
            ".#.......#.",
            "..##...##..",
            "....###...."
    };
    private static final String[] CREEPER = {
            "........",
            ".##..##.",
            ".##..##.",
            "...##...",
            "..####..",
            "..####..",
            "..#..#..",
            "........"
    };

    private static void drawIcon(DrawContext c, int id, int bx, int by, int btn, int col) {
        int ix = bx + (btn - 11) / 2;
        int iy = by + (btn - 11) / 2;
        switch (id) {
            case CUBE -> bitmap(c, ICON_CUBE, ix, iy, 1, col);
            case SERVER -> bitmap(c, ICON_SERVER, ix, iy, 1, col);
            case CLOUD -> bitmap(c, ICON_CLOUD, ix, iy, 1, col);
            case LAYERS -> bitmap(c, ICON_LAYERS, ix, iy, 1, col);
            case LOGO -> creeper(c, bx + (btn - 16) / 2, by + (btn - 16) / 2, 2);
            case GEAR -> gear(c, ix, iy, col);
            case INFO -> info(c, ix, iy, col);
            case POWER -> power(c, ix, iy, col);
            default -> { }
        }
    }

    private static void bitmap(DrawContext c, String[] rows, int x, int y, int u, int color) {
        for (int r = 0; r < rows.length; r++) {
            String s = rows[r];
            int start = -1;
            for (int i = 0; i <= s.length(); i++) {
                boolean on = i < s.length() && s.charAt(i) == '#';
                if (on && start < 0) start = i;
                if (!on && start >= 0) {
                    c.fill(x + start * u, y + r * u, x + i * u, y + (r + 1) * u, color);
                    start = -1;
                }
            }
        }
    }

    private static void creeper(DrawContext c, int x, int y, int u) {
        c.fill(x, y, x + 8 * u, y + 8 * u, ACCENT);
        bitmap(c, CREEPER, x, y, u, 0xFF0B1A10);
    }

    private static void px(DrawContext c, int x, int y, int col) {
        c.fill(x, y, x + 1, y + 1, col);
    }

    private static void gear(DrawContext c, int x, int y, int col) {
        for (int dy = -5; dy <= 5; dy++) {
            for (int dx = -5; dx <= 5; dx++) {
                double r = Math.sqrt(dx * dx + dy * dy);
                double outer = Math.cos(Math.atan2(dy, dx) * 8) > 0.2 ? 5.4 : 4.0;
                if (r >= 2.2 && r <= outer) px(c, x + 5 + dx, y + 5 + dy, col);
            }
        }
    }

    private static void power(DrawContext c, int x, int y, int col) {
        for (int dy = -5; dy <= 5; dy++) {
            for (int dx = -5; dx <= 5; dx++) {
                double r = Math.sqrt(dx * dx + dy * dy);
                boolean ring = r >= 3.7 && r <= 5.0 && !(dy < 0 && Math.abs(dx) <= 1);
                boolean line = dx == 0 && dy >= -5 && dy <= -1;
                if (ring || line) px(c, x + 5 + dx, y + 5 + dy, col);
            }
        }
    }

    private static void info(DrawContext c, int x, int y, int col) {
        for (int dy = -5; dy <= 5; dy++) {
            for (int dx = -5; dx <= 5; dx++) {
                double r = Math.sqrt(dx * dx + dy * dy);
                boolean ring = r >= 4.0 && r <= 5.2;
                boolean dot = dx == 0 && dy == -2;
                boolean stem = dx == 0 && dy >= 0 && dy <= 3;
                if (ring || dot || stem) px(c, x + 5 + dx, y + 5 + dy, col);
            }
        }
    }

    // ---------- примитивы ----------

    private static boolean in(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static void rrect(DrawContext c, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) return;
        r = Math.min(r, Math.min(w, h) / 2);
        if (r <= 0) {
            c.fill(x, y, x + w, y + h, color);
            return;
        }
        c.fill(x, y + r, x + w, y + h - r, color);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = (int) Math.round(r - Math.sqrt(r * r - dy * dy));
            c.fill(x + inset, y + i, x + w - inset, y + i + 1, color);
            c.fill(x + inset, y + h - 1 - i, x + w - inset, y + h - i, color);
        }
    }

    private static void glass(DrawContext c, int x, int y, int w, int h, int r, int fill, int border) {
        rrect(c, x, y, w, h, r, border);
        rrect(c, x + 1, y + 1, w - 2, h - 2, Math.max(0, r - 1), fill);
    }

    private static String fit(TextRenderer tr, String s, int max) {
        if (tr.getWidth(s) <= max) return s;
        while (s.length() > 1 && tr.getWidth(s + "..") > max) s = s.substring(0, s.length() - 1);
        return s + "..";
    }

    // ---------- последний мир (читаем saves/*/level.dat) ----------

    private static void loadLastWorld() {
        WorldInfo result = null;
        try {
            Path saves = FabricLoader.getInstance().getGameDir().resolve("saves");
            if (Files.isDirectory(saves)) {
                Path bestDir = null;
                long bestTime = Long.MIN_VALUE;
                try (Stream<Path> dirs = Files.list(saves)) {
                    for (Path dir : (Iterable<Path>) dirs::iterator) {
                        Path dat = dir.resolve("level.dat");
                        if (!Files.isRegularFile(dat)) continue;
                        long t = Files.getLastModifiedTime(dat).toMillis();
                        if (t > bestTime) {
                            bestTime = t;
                            bestDir = dir;
                        }
                    }
                }
                if (bestDir != null) result = readWorld(bestDir, bestTime);
            }
        } catch (Exception ignored) {
        }
        lastWorld = result;
        worldsLoaded = true;
    }

    private static WorldInfo readWorld(Path dir, long time) {
        String name = dir.getFileName().toString();
        String mode = "Выживание";
        try (InputStream in = new GZIPInputStream(Files.newInputStream(dir.resolve("level.dat")))) {
            byte[] d = in.readAllBytes();
            String n = nbtString(d, "LevelName");
            if (n != null && !n.isBlank()) name = n;
            int gt = nbtInt(d, "GameType", 0);
            boolean hardcore = nbtByte(d, "hardcore", 0) == 1;
            mode = hardcore ? "Хардкор" : switch (gt) {
                case 1 -> "Творческий";
                case 2 -> "Приключение";
                case 3 -> "Наблюдатель";
                default -> "Выживание";
            };
        } catch (Exception ignored) {
        }
        return new WorldInfo(name, mode, time);
    }

    private static int nbtFind(byte[] d, int type, String name) {
        byte[] n = name.getBytes(StandardCharsets.UTF_8);
        outer:
        for (int i = 0; i + 3 + n.length <= d.length; i++) {
            if (d[i] != type || d[i + 1] != 0 || (d[i + 2] & 0xFF) != n.length) continue;
            for (int j = 0; j < n.length; j++) {
                if (d[i + 3 + j] != n[j]) continue outer;
            }
            return i + 3 + n.length;
        }
        return -1;
    }

    private static String nbtString(byte[] d, String name) {
        int p = nbtFind(d, 8, name);
        if (p < 0 || p + 2 > d.length) return null;
        int len = ((d[p] & 0xFF) << 8) | (d[p + 1] & 0xFF);
        if (p + 2 + len > d.length) return null;
        return new String(d, p + 2, len, StandardCharsets.UTF_8);
    }

    private static int nbtInt(byte[] d, String name, int def) {
        int p = nbtFind(d, 3, name);
        if (p < 0 || p + 4 > d.length) return def;
        return ((d[p] & 0xFF) << 24) | ((d[p + 1] & 0xFF) << 16) | ((d[p + 2] & 0xFF) << 8) | (d[p + 3] & 0xFF);
    }

    private static int nbtByte(byte[] d, String name, int def) {
        int p = nbtFind(d, 1, name);
        if (p < 0 || p >= d.length) return def;
        return d[p];
    }
}
