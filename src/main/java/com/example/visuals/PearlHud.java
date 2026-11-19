package com.example.visuals;

import java.util.Locale;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_408;

/** Pearl Cooldown: оставшееся время перезарядки эндер-жемчуга. */
public final class PearlHud {
    private PearlHud() {}

    public static int[] draw(class_332 g, class_310 mc, float a) {
        if (mc.field_1724 == null) return null;
        float prog = mc.field_1724.method_7357().method_7905(new class_1799(class_1802.field_8634), 0f);
        boolean editing = mc.field_1755 instanceof class_408;
        if (prog <= 0f && Config.pcdHideIdle && !editing) return null;

        class_327 tr = mc.field_1772;
        int accent = Config.COLORS[Config.pcdColor];
        String txt = prog > 0f ? String.format(Locale.ROOT, "%.1fs", prog * 1.0f) : "Ready";
        int w = 26 + Math.max(HudStyle.width(tr, txt), 28) + 6, h = 22;

        HudStyle.panel(g, 0, 0, w, h, a, accent);
        HudStyle.header(g, 0, 0, w, 10, a, accent);
        g.method_51427(new class_1799(class_1802.field_8634), 4, 3);
        HudStyle.text(g, tr, txt, 26, 4, prog > 0f ? HudStyle.MID : HudStyle.GOOD, a, true);
        HudStyle.bar(g, 26, 15, w - 32, 3, prog > 0f ? 1f - prog : 1f, accent, a);
        return new int[] {w, h};
    }
}
