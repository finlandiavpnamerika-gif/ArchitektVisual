package com.example.visuals;

import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_408;

/** Totem Counter: сколько тотемов бессмертия в инвентаре и второй руке. */
public final class TotemHud {
    private TotemHud() {}

    public static int count(class_310 mc) {
        if (mc.field_1724 == null) return 0;
        int n = 0;
        for (int i = 0; i < 36; i++) {
            class_1799 s = mc.field_1724.method_31548().method_5438(i);
            if (s.method_31574(class_1802.field_8288)) n += s.method_7947();
        }
        class_1799 off = mc.field_1724.method_6079();
        if (off.method_31574(class_1802.field_8288)) n += off.method_7947();
        return n;
    }

    public static int[] draw(class_332 g, class_310 mc, float a) {
        int n = count(mc);
        if (n == 0 && Config.totemHideZero && !(mc.field_1755 instanceof class_408)) return null;
        class_327 tr = mc.field_1772;
        int accent = Config.COLORS[Config.totemColor];
        String txt = "x" + n;
        int w = 26 + HudStyle.width(tr, txt) + 6, h = 22;

        HudStyle.panel(g, 0, 0, w, h, a, accent);
        HudStyle.header(g, 0, 0, w, 10, a, accent);
        g.method_51427(new class_1799(class_1802.field_8288), 4, 3);
        int col = n == 0 ? HudStyle.BAD : n <= 1 ? HudStyle.MID : Theme.TEXT;
        HudStyle.text(g, tr, txt, 26, 7, col, a, true);
        return new int[] {w, h};
    }
}
