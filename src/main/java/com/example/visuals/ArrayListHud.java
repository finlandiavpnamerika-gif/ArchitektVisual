package com.example.visuals;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;

/** ArrayList — список включённых модулей, самые длинные названия сверху. */
public final class ArrayListHud {
    private ArrayListHud() {}

    private static final int LINE = 11;
    private static final int PAD = 4;

    /** Рисует в (0,0), возвращает {w, h} или null, если включённых модулей нет. */
    public static int[] draw(class_332 g, class_310 mc, float a) {
        class_327 tr = mc.field_1772;
        int accent = Config.COLORS[Config.arrayListColor];

        List<String> mods = new ArrayList<>();
        if (Config.fullbright) mods.add("Fullbright");
        if (Config.hud) mods.add("HUD");
        if (Config.arrayList) mods.add("ArrayList");
        if (Config.limitDist) mods.add("LimitDist");
        if (Config.sprint) mods.add("AutoSprint");
        if (Config.zoom) mods.add("Zoom");
        if (Config.autoReconnect) mods.add("AutoReconnect");
        if (Config.autoswap) mods.add("AutoSwap");
        if (Config.itembind) mods.add("ItemBind");
        if (Config.keystrokes) mods.add("Keystrokes");
        if (Config.armorHud) mods.add("ArmorHUD");

        if (mods.isEmpty()) return null;

        mods.sort((p, q) -> tr.method_1727(q) - tr.method_1727(p));   // длинные сверху

        int maxW = 0;
        for (String s : mods) maxW = Math.max(maxW, tr.method_1727(s));

        int barX = PAD;                       // акцентная полоска слева
        int textX = PAD + 2 + 4;
        int w = textX + maxW + PAD + 1;
        int h = PAD * 2 + mods.size() * LINE - 2;

        HudStyle.panel(g, 0, 0, w, h, a, accent);
        HudStyle.roundFill(g, barX, PAD, 2, h - PAD * 2, 1, HudStyle.withAlpha(accent, a));

        for (int i = 0; i < mods.size(); i++) {
            HudStyle.text(g, tr, mods.get(i), textX, PAD + i * LINE, Theme.TEXT, a, Config.arrayListShadow);
        }
        return new int[] {w, h};
    }
}
