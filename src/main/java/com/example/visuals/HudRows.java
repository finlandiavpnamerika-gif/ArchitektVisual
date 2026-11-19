package com.example.visuals;

import java.util.List;
import net.minecraft.class_327;
import net.minecraft.class_332;

/** Общая панель "заголовок + строки (подпись слева, значение справа)" для небольших инфо-HUD. */
public final class HudRows {
    private HudRows() {}

    private static final int PAD = 6;
    private static final int LINE = 10;
    private static final int HEADER = 14;
    private static final int GAP = 12;

    /** Рисует в (0,0), возвращает {w, h}. */
    public static int[] draw(class_332 g, class_327 tr, String title, List<String> labels,
                             List<String> values, List<Integer> colors, int accent, float a) {
        int lw = 0, vw = 0;
        for (int i = 0; i < labels.size(); i++) {
            lw = Math.max(lw, HudStyle.width(tr, labels.get(i)));
            vw = Math.max(vw, HudStyle.width(tr, values.get(i)));
        }
        String icon = HudStyle.iconFor(title);
        int w = Math.max(HudStyle.titleWidth(tr, icon, title) + PAD * 2, PAD * 2 + lw + GAP + vw);
        int h = HEADER + labels.size() * LINE + PAD - 1;

        HudStyle.panel(g, 0, 0, w, h, a, accent);
        HudStyle.header(g, 0, 0, w, HEADER - 2, a, accent);
        HudStyle.title(g, tr, icon, title, PAD, 3, a, accent);
        HudStyle.divider(g, PAD, w - PAD, HEADER - 2, 0xFFFFFFFF, a * 0.5f);

        int y = HEADER + 1;
        for (int i = 0; i < labels.size(); i++) {
            HudStyle.text(g, tr, labels.get(i), PAD, y, HudStyle.MUTED, a, true);
            HudStyle.textRight(g, tr, values.get(i), w - PAD, y, colors.get(i), a, true);
            y += LINE;
        }
        return new int[] {w, h};
    }
}
