package com.example.visuals;

import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_408;

/** Combo Counter: счётчик попаданий подряд с полоской оставшегося времени. */
public final class ComboHud {
    private ComboHud() {}

    private static final int W = 64;
    private static final int H = 30;

    /** Рисует в (0,0), возвращает {w, h}. Пока комбо нет - ничего (кроме режима правки в чате). */
    public static int[] draw(class_332 g, class_310 mc, float a) {
        boolean editing = mc.field_1755 instanceof class_408;
        int combo = Combat.combo;
        if (combo <= 0 && !editing) return null;

        class_327 tr = mc.field_1772;
        int accent = Config.COLORS[Config.comboColor];
        int shown = combo > 0 ? combo : 5;

        // "всплеск" при новом попадании
        long since = System.currentTimeMillis() - Combat.lastHitMs;
        float pop = combo > 0 ? Math.max(0f, 1f - since / 180f) : 0f;

        HudStyle.panel(g, 0, 0, W, H, a, accent);
        HudStyle.header(g, 0, 0, W, 10, a, accent);
        HudStyle.text(g, tr, "COMBO", (W - HudStyle.width(tr, "COMBO")) / 2, 2, Theme.TEXT_DIM, a, true);

        String num = "x" + shown;
        int col = HudStyle.mix(0xFFFFFFFF, accent, pop);
        float sc = 1.0f + 0.45f * pop;
        var m = g.method_51448();
        float cxm = W / 2f, cym = 17f;
        m.pushMatrix();
        m.translate(cxm, cym);
        m.scale(sc, sc);
        HudStyle.text(g, tr, num, -HudStyle.width(tr, num) / 2, -4, col, a, true);
        m.popMatrix();

        float left = combo > 0
                ? 1f - (since / (float) Math.max(1.0, Config.comboTimeout * 1000.0))
                : 1f;
        HudStyle.bar(g, 4, H - 5, W - 8, 2, left, accent, a);
        return new int[] {W, H};
    }
}
