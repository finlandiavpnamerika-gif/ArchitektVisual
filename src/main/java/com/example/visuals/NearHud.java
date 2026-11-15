package com.example.visuals;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_408;

/**
 * Players Near: окно со списком игроков рядом.
 * В каждой строке: стрелка (куда повернуться, чтобы смотреть на игрока), ник и расстояние в метрах.
 * Стрелка вверх = игрок прямо перед тобой, вправо = справа, вниз = за спиной.
 * Ближайшие сверху. Друзья - зелёным. Окно перетаскивается мышью в открытом чате.
 */
public final class NearHud {
    private NearHud() {}

    private static final int PAD = 5;
    private static final int HEADER = 14;
    private static final int ROW = 14;
    private static final int ARROW_W = 14;   // место под стрелку слева

    private static final class Row {
        final String name;
        final double dist;
        final float angle;     // радианы, поворот стрелки по часовой
        final boolean friend;
        Row(String name, double dist, float angle, boolean friend) {
            this.name = name; this.dist = dist; this.angle = angle; this.friend = friend;
        }
    }

    public static int[] draw(class_332 g, class_310 mc, float a) {
        if (mc.field_1724 == null || mc.field_1687 == null) return null;
        class_327 tr = mc.field_1772;
        int accent = Config.COLORS[Math.max(0, Math.min(Config.nearColor, Config.COLORS.length - 1))];

        double range = Math.max(10, Config.nearRange);
        int max = Math.max(1, Math.min(12, (int) Config.nearMax));
        double px = mc.field_1724.method_23317(), py = mc.field_1724.method_23318(), pz = mc.field_1724.method_23321();
        float viewYaw = mc.field_1724.method_36454();

        List<Row> rows = new ArrayList<>();
        for (class_1657 p : mc.field_1687.method_18456()) {
            if (p == mc.field_1724 || p.method_7325() || p.method_5767()) continue;   // себя, спектаторов и невидимых не показываем
            double dx = p.method_23317() - px, dy = p.method_23318() - py, dz = p.method_23321() - pz;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist > range) continue;

            // куда надо повернуться: yaw на цель минус свой yaw (по часовой = вправо)
            double yawTo = Math.toDegrees(Math.atan2(-dx, dz));
            double rel = yawTo - viewYaw;
            rel = ((rel % 360.0) + 540.0) % 360.0 - 180.0;          // -180..180
            String name = p.method_5477().getString();
            rows.add(new Row(name, dist, (float) Math.toRadians(rel), Config.friends.contains(name)));
        }
        rows.sort((r1, r2) -> Double.compare(r1.dist, r2.dist));

        boolean demo = rows.isEmpty();
        if (demo && !(mc.field_1755 instanceof class_408)) return null;   // пусто - прячем (в чате показываем заглушку, чтобы можно было перетащить)

        int total = rows.size();
        if (rows.size() > max) rows = new ArrayList<>(rows.subList(0, max));

        String title = "Players Near";
        String count = demo ? "0" : String.valueOf(total);
        int w = PAD + tr.method_1727(title) + 10 + tr.method_1727(count) + PAD;
        for (Row r : rows) {
            int rw = PAD + ARROW_W + tr.method_1727(r.name) + 12 + tr.method_1727(distText(r.dist)) + PAD;
            if (rw > w) w = rw;
        }
        if (demo) w = Math.max(w, PAD + tr.method_1727("Никого рядом") + PAD);
        w = Math.max(w, 96);
        int h = HEADER + 3 + (demo ? ROW : rows.size() * ROW) + 3;

        HudStyle.panel(g, 0, 0, w, h, a, accent);
        HudStyle.header(g, 0, 0, w, HEADER, a, accent);
        HudStyle.text(g, tr, title, PAD, 3, Theme.TEXT, a, true);
        HudStyle.textRight(g, tr, count, w - PAD, 3, Theme.TEXT_DIM, a, true);
        HudStyle.divider(g, 1, w - 1, HEADER, accent, a * 0.5f);

        int y = HEADER + 3;
        if (demo) {
            HudStyle.text(g, tr, "Никого рядом", PAD, y + 3, Theme.TEXT_DIM, a, true);
        } else {
            for (Row r : rows) {
                int nameCol = r.friend ? HudStyle.GOOD : Theme.TEXT;
                int distCol = r.dist <= 15 ? HudStyle.BAD : r.dist <= 40 ? HudStyle.MID : Theme.TEXT_DIM;

                arrow(g, PAD + 4, y + ROW / 2, r.angle, HudStyle.withAlpha(0xFF000000 | (accent & 0xFFFFFF), a));
                HudStyle.text(g, tr, r.name, PAD + ARROW_W, y + 3, nameCol, a, true);
                HudStyle.textRight(g, tr, distText(r.dist), w - PAD, y + 3, distCol, a, true);
                y += ROW;
            }
        }
        return new int[] {w, h};
    }

    private static String distText(double d) {
        return (int) Math.round(d) + "м";
    }

    /** Стрелка 7x9 пикселей, смотрит вверх; angle - поворот по часовой (радианы). Центр в (cx, cy). */
    private static void arrow(class_332 g, int cx, int cy, float angle, int color) {
        var mat = g.method_51448();
        mat.pushMatrix();
        mat.translate(cx, cy);
        mat.rotate(angle);
        // острие
        g.method_25294(0, -5, 1, -4, color);
        g.method_25294(-1, -4, 2, -3, color);
        g.method_25294(-2, -3, 3, -2, color);
        g.method_25294(-3, -2, 4, -1, color);
        // хвост
        g.method_25294(-1, -1, 2, 4, color);
        mat.popMatrix();
    }
}
