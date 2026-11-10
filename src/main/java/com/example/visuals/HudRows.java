package com.example.visuals;

import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/** Общая панель "заголовок + строки (подпись слева, значение справа)" для небольших инфо-HUD. */
public final class HudRows {
    private HudRows() {}

    private static final int PAD = 6;
    private static final int LINE = 10;
    private static final int HEADER = 14;
    private static final int GAP = 12;

    /** Рисует в (0,0), возвращает {w, h}. */
    public static int[] draw(DrawContext g, TextRenderer tr, String title, List<String> labels,
                             List<String> values, List<Integer> colors, int accent, float a) {
        int lw = 0, vw = 0;
        for (int i = 0; i < labels.size(); i++) {
            lw = Math.max(lw, tr.getWidth(labels.get(i)));
            vw = Math.max(vw, tr.getWidth(values.get(i)));
        }
        int w = Math.max(tr.getWidth(title) + PAD * 2, PAD * 2 + lw + GAP + vw);
        int h = HEADER + labels.size() * LINE + PAD - 1;

        HudStyle.panel(g, 0, 0, w, h, a, accent);
        HudStyle.header(g, 0, 0, w, HEADER - 2, a, accent);
        HudStyle.text(g, tr, title, PAD, 3, accent, a, true);
        HudStyle.divider(g, PAD, w - PAD, HEADER - 2, accent, a * 0.5f);

        int y = HEADER + 1;
        for (int i = 0; i < labels.size(); i++) {
            HudStyle.text(g, tr, labels.get(i), PAD, y, Theme.TEXT_DIM, a, true);
            HudStyle.textRight(g, tr, values.get(i), w - PAD, y, colors.get(i), a, true);
            y += LINE;
        }
        return new int[] {w, h};
    }
}
