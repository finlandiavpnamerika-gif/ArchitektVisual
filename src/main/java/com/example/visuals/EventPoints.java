package com.example.visuals;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4588;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Event Point: когда в чат приходит сообщение про ивент с координатами,
 * в мире появляется метка с названием ивента и расстоянием до него
 * ("Аирдроп / 1131 метров"), как у обычной метки на скрине.
 *
 * Как ловит: ищет в сообщении координаты (x y z / x: .. z: .. / "123 64 -456" / "123 -456")
 * и слово-ключ ивента из таблицы KINDS ниже. Без координат метку поставить нельзя.
 * Таблицу KINDS правь под свой сервер.
 */
public final class EventPoints {
    private EventPoints() {}

    public static final class Pt {
        public String title;
        public double x, y, z;
        public String dim;
        public long expires;
    }

    public static final List<Pt> LIST = new CopyOnWriteArrayList<>();

    private static final int MAX_POINTS = 8;
    private static final int ICON_RGB = 0x3D6BFF;        // синий квадрат с домиком
    private static final double ARRIVE_DIST = 4.0;        // подошёл ближе - метка пропадает

    // ====== ТАБЛИЦА ИВЕНТОВ: правь здесь ======
    // Первое совпадение выигрывает, поэтому конкретные - выше, общее "ивент/событие" - в самом конце.
    // [0] = регулярка (без учёта регистра), [1] = название на метке.
    private static final String[][] KINDS = {
            {"аирдроп|эирдроп|аэродроп|airdrop|air drop", "Аирдроп"},
            {"метеорит|meteor",                           "Метеорит"},
            {"мистическ\\S*\\s+сундук|мистик\\S*\\s+сундук", "Мистический сундук"},
            {"сундук|chest",                              "Сундук"},
            {"босс|boss",                                 "Босс"},
            {"торговец|trader",                           "Торговец"},
            {"портал|portal",                             "Портал"},
            {"дракон|dragon",                             "Дракон"},
            {"вулкан",                                    "Вулкан"},
            {"крепост",                                   "Крепость"},
            {"ивент|событи|event",                        "Событие"},
    };
    private static final Pattern[] KIND_PATTERNS = new Pattern[KINDS.length];
    static {
        for (int i = 0; i < KINDS.length; i++)
            KIND_PATTERNS[i] = Pattern.compile(KINDS[i][0], Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    // название в кавычках: «Падение метеорита» -> используем как название для общего "Событие"
    private static final Pattern QUOTED = Pattern.compile("[«\"“]([^»\"”]{2,28})[»\"”]");

    private static final String NUM = "(-?\\d+(?:\\.\\d+)?)";
    private static final Pattern XYZ = Pattern.compile(
            "x\\s*[:=]?\\s*" + NUM + "[\\s,;|/]*y\\s*[:=]?\\s*" + NUM + "[\\s,;|/]*z\\s*[:=]?\\s*" + NUM,
            Pattern.CASE_INSENSITIVE);
    private static final Pattern XZ = Pattern.compile(
            "x\\s*[:=]?\\s*" + NUM + "[\\s,;|/]*z\\s*[:=]?\\s*" + NUM,
            Pattern.CASE_INSENSITIVE);
    private static final Pattern INT3 = Pattern.compile(
            "(?<![\\d.-])(-?\\d{1,7})(?:\\s*[,;]\\s*|\\s+)(-?\\d{1,4})(?:\\s*[,;]\\s*|\\s+)(-?\\d{1,7})(?![\\d.])");
    private static final Pattern INT2 = Pattern.compile(
            "(?<![\\d.-])(-?\\d{2,7})(?:\\s*[,;]\\s*|\\s+)(-?\\d{2,7})(?![\\d.])");

    // ------------------------------------------------------------------ разбор чата

    /** Вызывается на каждое сообщение чата. */
    public static void onChat(String text) {
        if (!Config.eventPts) return;
        try { parse(text); } catch (Exception ignored) {}
    }

    /**
     * Разбирает сообщение и (если нашёл ивент + координаты) ставит метку.
     * Возвращает метку или null. Используется и командой /eventpoint parse.
     */
    public static Pt parse(String text) {
        if (text == null || text.isEmpty()) return null;
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null || mc.field_1687 == null) return null;

        String title = null;
        for (int i = 0; i < KINDS.length; i++) {
            if (KIND_PATTERNS[i].matcher(text).find()) { title = KINDS[i][1]; break; }
        }
        if (title == null) return null;
        if (title.equals("Событие")) {
            Matcher q = QUOTED.matcher(text);
            if (q.find()) title = q.group(1).trim();
        }

        double[] c = coords(text, mc.field_1724.method_23318());
        if (c == null) return null;

        String dim = Waypoints.dim(mc);
        long expires = System.currentTimeMillis() + (long) (Math.max(1.0, Config.epMinutes) * 60_000L);

        // то же событие (то же название рядом) - просто обновляем, а не плодим копии
        for (Pt p : LIST) {
            if (p.title.equals(title) && p.dim.equals(dim)
                    && Math.abs(p.x - c[0]) <= 16 && Math.abs(p.z - c[2]) <= 16) {
                p.x = c[0]; p.y = c[1]; p.z = c[2]; p.expires = expires;
                return p;
            }
        }
        Pt p = new Pt();
        p.title = title; p.x = c[0]; p.y = c[1]; p.z = c[2]; p.dim = dim; p.expires = expires;
        LIST.add(p);
        while (LIST.size() > MAX_POINTS) LIST.remove(0);
        return p;
    }

    /** Достаёт координаты {x, y, z}. Если Y нет - берёт высоту игрока. null - координат нет. */
    private static double[] coords(String s, double defaultY) {
        Matcher m = XYZ.matcher(s);
        if (m.find()) return new double[] {num(m.group(1)), num(m.group(2)), num(m.group(3))};

        m = XZ.matcher(s);
        if (m.find()) return new double[] {num(m.group(1)), defaultY, num(m.group(2))};

        m = INT3.matcher(s);
        while (m.find()) {
            double y = num(m.group(2));
            if (y >= -64 && y <= 320) return new double[] {num(m.group(1)), y, num(m.group(3))};
        }

        m = INT2.matcher(s);
        if (m.find()) return new double[] {num(m.group(1)), defaultY, num(m.group(2))};
        return null;
    }

    private static double num(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return 0; }
    }

    /** Тестовая метка в 300 блоках перед игроком (команда /eventpoint test). */
    public static void addTest(class_310 mc) {
        if (mc.field_1724 == null) return;
        double yaw = Math.toRadians(mc.field_1724.method_36454());
        Pt p = new Pt();
        p.title = "Тест ивент";
        p.x = mc.field_1724.method_23317() - Math.sin(yaw) * 300;
        p.y = mc.field_1724.method_23318();
        p.z = mc.field_1724.method_23321() + Math.cos(yaw) * 300;
        p.dim = Waypoints.dim(mc);
        p.expires = System.currentTimeMillis() + (long) (Math.max(1.0, Config.epMinutes) * 60_000L);
        LIST.add(p);
        while (LIST.size() > MAX_POINTS) LIST.remove(0);
    }

    public static void clear() { LIST.clear(); }

    // ------------------------------------------------------------------ тик

    /** Убирает просроченные метки и те, до которых уже дошёл. */
    public static void tick(class_310 mc) {
        if (LIST.isEmpty()) return;
        if (mc.field_1724 == null || mc.field_1687 == null) { LIST.clear(); return; }
        long now = System.currentTimeMillis();
        String dim = Waypoints.dim(mc);
        double px = mc.field_1724.method_23317(), pz = mc.field_1724.method_23321();
        LIST.removeIf(p -> {
            if (now > p.expires) return true;
            if (!p.dim.equals(dim)) return false;
            double dx = p.x - px, dz = p.z - pz;
            return dx * dx + dz * dz < ARRIVE_DIST * ARRIVE_DIST;
        });
    }

    // ------------------------------------------------------------------ луч в мире

    public static void renderBeams(class_4588 q, Matrix4f m, class_243 cam, class_310 mc) {
        if (LIST.isEmpty()) return;
        String dim = Waypoints.dim(mc);
        for (Pt p : LIST) {
            if (!p.dim.equals(dim)) continue;
            double dx = p.x - cam.field_1352, dy = p.y - cam.field_1351, dz = p.z - cam.field_1350;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            float near = (float) Math.min(1.0, dist / 6.0);      // рядом луч гаснет
            WorldDraw.ribbon(q, m, cam.field_1352, cam.field_1351, cam.field_1350,
                    p.x, p.y - 8, p.z, p.x, p.y + 72, p.z,
                    0.14f, WorldDraw.argb(0.85f * near, ICON_RGB), WorldDraw.argb(0f, ICON_RGB));
        }
    }

    // ------------------------------------------------------------------ метка на экране

    /** "1 метр", "2 метра", "5 метров", "11 метров", "21 метр" */
    private static String meters(int d) {
        int m100 = d % 100, m10 = d % 10;
        String w;
        if (m100 >= 11 && m100 <= 14) w = "метров";
        else if (m10 == 1) w = "метр";
        else if (m10 >= 2 && m10 <= 4) w = "метра";
        else w = "метров";
        return d + " " + w;
    }

    public static void hud(class_332 g) {
        if (!Config.eventPts || LIST.isEmpty()) return;
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null || mc.field_1687 == null || mc.field_1690.field_1842 || mc.field_1755 != null) return;
        class_4184 cam = mc.field_1773.method_19418();
        if (cam == null || !cam.method_19332()) return;

        class_243 cp = cam.method_71156();
        int sw = mc.method_22683().method_4486(), sh = mc.method_22683().method_4502();
        float fov = (float) Math.toRadians(mc.field_1690.method_41808().method_41753());
        float f = (sh / 2f) / (float) Math.tan(fov / 2f);
        Quaternionf inv = cam.method_23767().conjugate(new Quaternionf());
        class_327 tr = mc.field_1772;
        String dim = Waypoints.dim(mc);
        float sc = (float) Math.max(0.5, Math.min(2.0, Config.epScale));

        for (Pt p : LIST) {
            if (!p.dim.equals(dim)) continue;
            double dx = p.x - cp.field_1352, dy = p.y + 1.5 - cp.field_1351, dz = p.z - cp.field_1350;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

            Vector3f v = new Vector3f((float) dx, (float) dy, (float) dz);
            inv.transform(v);

            String line1 = p.title;
            String line2 = meters((int) Math.round(dist));
            int tw = Math.max(tr.method_1727(line1), tr.method_1727(line2));
            int w = 4 + 18 + 6 + tw + 6;          // отступ + иконка + зазор + текст + отступ
            float halfW = w / 2f * sc;

            float sx, sy;
            if (v.z >= -0.05f) {
                // метка позади камеры: прижимаем к краю экрана в ту сторону, куда надо повернуться
                sx = v.x < 0 ? halfW + 4 : sw - halfW - 4;
                sy = sh / 2f;
            } else {
                sx = sw / 2f + v.x / -v.z * f;
                sy = sh / 2f - v.y / -v.z * f;
                sx = class_3532.method_15363(sx, halfW + 4, sw - halfW - 4);
                sy = class_3532.method_15363(sy, 32f * sc, sh - 12f);
            }

            var mat = g.method_51448();
            mat.pushMatrix();
            mat.translate(sx, sy);
            mat.scale(sc, sc);

            int x0 = -w / 2;
            // плашка (28 вверх от острия) + хвостик-указатель
            HudStyle.roundFill(g, x0, -28, w, 24, 5, 0xC0101216);
            g.method_25294(-3, -4, 4, -3, 0xC0101216);
            g.method_25294(-2, -3, 3, -2, 0xC0101216);
            g.method_25294(-1, -2, 2, -1, 0xC0101216);
            g.method_25294(0, -1, 1, 0, 0xC0101216);

            // иконка: синий квадрат с домиком
            int ix = x0 + 4, iy = -25;
            HudStyle.roundFill(g, ix, iy, 18, 18, 4, 0xFF000000 | ICON_RGB);
            house(g, ix + 4, iy + 5);

            // текст
            HudStyle.text(g, tr, line1, ix + 18 + 6, -25, Theme.TEXT, 1f, true);
            HudStyle.text(g, tr, line2, ix + 18 + 6, -15, 0xFF9AA3B2, 1f, true);
            mat.popMatrix();
        }
    }

    /** Домик 9x8 пикселей белым. (ox, oy) - левый верхний угол. */
    private static void house(class_332 g, int ox, int oy) {
        int W = 0xFFFFFFFF;
        // крыша: ряды шириной 1,3,5,7,9
        g.method_25294(ox + 4, oy,     ox + 5, oy + 1, W);
        g.method_25294(ox + 3, oy + 1, ox + 6, oy + 2, W);
        g.method_25294(ox + 2, oy + 2, ox + 7, oy + 3, W);
        g.method_25294(ox + 1, oy + 3, ox + 8, oy + 4, W);
        g.method_25294(ox,     oy + 4, ox + 9, oy + 5, W);
        // стены
        g.method_25294(ox + 1, oy + 5, ox + 8, oy + 8, W);
        // дверь
        g.method_25294(ox + 3, oy + 5, ox + 6, oy + 8, 0xFF000000 | ICON_RGB);
    }
}
