package com.example.visuals;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;

/** Armor + Tool HUD: слоты с иконкой, прочностью и полоской. Вертикально или горизонтально. */
public final class ArmorHud {
    private ArmorHud() {}

    private static final int PAD = 4;
    private static final int ICON = 16;
    private static final int SLOT = 22;      // плитка слота (иконка + поля)
    private static final int GAP = 2;

    private static int slotColor(class_1799 st) {
        if (st.method_7960() || !st.method_7963()) return 0xFF8890A0;
        float ratio = (float) (st.method_7936() - st.method_7919()) / Math.max(1, st.method_7936());
        return HudStyle.durabilityColor(ratio);
    }

    /** Иконка предмета; при включённой опции поверх кладётся плашка цвета прочности. */
    private static void drawIcon(class_332 g, class_1799 st, int x, int y, float a) {
        g.method_51427(st, x, y);
        if (Config.armorHudColorIcons && st.method_7963()) {
            g.method_25294(x, y, x + ICON, y + ICON, HudStyle.withAlpha(slotColor(st), a * 0.35f));
        }
    }

    /** Рисует в (0,0), возвращает {w, h}. */
    public static int[] draw(class_332 g, class_310 mc, float a) {
        class_327 tr = mc.field_1772;

        List<class_1799> items = new ArrayList<>();
        items.add(mc.field_1724.method_6118(class_1304.field_6169));
        items.add(mc.field_1724.method_6118(class_1304.field_6174));
        items.add(mc.field_1724.method_6118(class_1304.field_6172));
        items.add(mc.field_1724.method_6118(class_1304.field_6166));
        boolean tool = Config.armorHudShowTool;
        if (tool) items.add(mc.field_1724.method_6047());
        boolean showDur = Config.armorHudShowDurability;
        int n = items.size();
        int accent = Theme.ACCENT;

        if (Config.armorHudHoriz) {
            // плитки в ряд: иконка сверху, цифра и полоска снизу
            int tileW = SLOT + (showDur ? 8 : 0);
            int tileH = SLOT + (showDur ? 13 : 0);
            int w = n * tileW + (n - 1) * GAP + PAD * 2 - 2;
            int h = tileH + PAD * 2 - 2;
            HudStyle.panel(g, 0, 0, w, h, a, accent);
            for (int i = 0; i < n; i++) {
                class_1799 st = items.get(i);
                int x = PAD - 1 + i * (tileW + GAP), y = PAD - 1;
                int col = slotColor(st);
                HudStyle.roundFill(g, x, y, tileW, tileH, 2, HudStyle.withAlpha(0x38000000, a));
                if (tool && i == 4) HudStyle.roundRing(g, x, y, tileW, tileH, 2, HudStyle.withAlpha(accent, a * 0.6f));
                if (st.method_7960()) {
                    HudStyle.roundFill(g, x + (tileW - ICON) / 2, y + 3, ICON, ICON, 2, HudStyle.withAlpha(0x22FFFFFF, a));
                    continue;
                }
                drawIcon(g, st, x + (tileW - ICON) / 2, y + 3, a);
                if (showDur && st.method_7963()) {
                    int left = st.method_7936() - st.method_7919();
                    float ratio = (float) left / Math.max(1, st.method_7936());
                    String t = String.valueOf(left);
                    HudStyle.text(g, tr, t, x + (tileW - tr.method_1727(t)) / 2, y + SLOT - 1, col, a, true);
                    HudStyle.bar(g, x + 3, y + tileH - 5, tileW - 6, 3, ratio, col, a);
                }
            }
            return new int[] {w, h};
        }

        // вертикально: строка на слот, справа цифра и полоска
        int barW = 26;
        if (showDur) for (class_1799 st : items)
            if (!st.method_7960() && st.method_7963())
                barW = Math.max(barW, tr.method_1727(String.valueOf(st.method_7936() - st.method_7919())));
        int rowW = ICON + 6 + (showDur ? barW + 4 : 0);
        int rowH = 20;
        int w = rowW + PAD * 2;
        int h = n * rowH + (n - 1) * 1 + PAD * 2 - 2 + (tool ? 3 : 0);
        HudStyle.panel(g, 0, 0, w, h, a, accent);
        for (int i = 0; i < n; i++) {
            class_1799 st = items.get(i);
            int x = PAD, y = PAD - 1 + i * (rowH + 1) + (tool && i == 4 ? 3 : 0);
            if (tool && i == 4) HudStyle.divider(g, PAD, w - PAD, y - 2, 0x40FFFFFF, a);
            int col = slotColor(st);
            HudStyle.roundFill(g, x, y, rowW, rowH, 2, HudStyle.withAlpha(0x38000000, a));
            if (st.method_7960()) {
                HudStyle.roundFill(g, x + 3, y + 2, ICON, ICON, 2, HudStyle.withAlpha(0x22FFFFFF, a));
                continue;
            }
            drawIcon(g, st, x + 3, y + 2, a);
            if (showDur && st.method_7963()) {
                int left = st.method_7936() - st.method_7919();
                float ratio = (float) left / Math.max(1, st.method_7936());
                int tx = x + ICON + 7;
                HudStyle.text(g, tr, String.valueOf(left), tx, y + 3, col, a, true);
                HudStyle.bar(g, tx, y + 13, barW, 3, ratio, col, a);
            }
        }
        return new int[] {w, h};
    }
                                                       }
