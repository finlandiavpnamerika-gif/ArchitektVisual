package com.example.visuals;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;

/** Armor + Tool HUD: иконки брони и предмета в руке, цифра и полоска прочности. */
public final class ArmorHud {
    private ArmorHud() {}

    private static final int PAD = 4;
    private static final int ICON = 16;
    private static final int ROW = 18;
    private static final int TOOL_GAP = 4;   // отступ перед предметом в руке
    private static final int MIN_BAR = 26;

    /** Рисует в (0,0), возвращает {w, h}. */
    public static int[] draw(class_332 g, class_310 mc, float a) {
        class_327 tr = mc.field_1772;

        List<class_1799> items = new ArrayList<>();
        items.add(mc.field_1724.method_6118(class_1304.field_6169));   // шлем
        items.add(mc.field_1724.method_6118(class_1304.field_6174));   // нагрудник
        items.add(mc.field_1724.method_6118(class_1304.field_6172));   // поножи
        items.add(mc.field_1724.method_6118(class_1304.field_6166));   // ботинки
        boolean tool = Config.armorHudShowTool;
        if (tool) items.add(mc.field_1724.method_6047());

        boolean showDur = Config.armorHudShowDurability;

        // ширина колонки с цифрой/полоской
        int barW = MIN_BAR;
        if (showDur) {
            for (class_1799 st : items) {
                if (!st.method_7960() && st.method_7963()) {
                    barW = Math.max(barW, tr.method_1727(String.valueOf(st.method_7936() - st.method_7919())));
                }
            }
        }

        int w = PAD * 2 + ICON + (showDur ? 4 + barW : 0);
        int h = PAD * 2 + items.size() * ROW - (ROW - ICON) + (tool ? TOOL_GAP : 0);

        HudStyle.panel(g, 0, 0, w, h, a, Theme.ACCENT);

        for (int i = 0; i < items.size(); i++) {
            class_1799 st = items.get(i);
            int y = PAD + i * ROW + (tool && i == 4 ? TOOL_GAP : 0);

            if (tool && i == 4) {
                HudStyle.divider(g, PAD, w - PAD, y - TOOL_GAP / 2 - 1, 0x30FFFFFF, a);
            }

            if (st.method_7960()) {
                // пустой слот: еле заметная плашка, чтобы раскладка не прыгала
                HudStyle.roundFill(g, PAD, y, ICON, ICON, 2, HudStyle.withAlpha(0x22FFFFFF, a));
                continue;
            }

            g.method_51427(st, PAD, y);   // иконка (прозрачность на неё не действует)

            if (showDur && st.method_7963()) {
                int max = st.method_7936();
                int left = max - st.method_7919();
                float ratio = max > 0 ? (float) left / max : 0f;
                int col = HudStyle.ratioColor(ratio);
                int tx = PAD + ICON + 4;

                HudStyle.text(g, tr, String.valueOf(left), tx, y + 1, col, a, true);
                HudStyle.bar(g, tx, y + 11, barW, 3, ratio, col, a);
            }
        }
        return new int[] {w, h};
    }
}
