package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

/** ArrayList — список включённых модулей, компактно в углу. */
public final class ArrayListHud {
    private ArrayListHud() {}

    public static void render(DrawContext g, MinecraftClient mc) {
        if (!Config.arrayList) return;
        if (mc.player == null || mc.options.hudHidden) return;

        TextRenderer tr = mc.textRenderer;
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

        if (mods.isEmpty()) return;

        int maxW = 0;
        for (String s : mods) maxW = Math.max(maxW, tr.getWidth(s));

        int x = Config.arrayListX;
        int y = Config.arrayListY;
        int lineH = 11;
        int pad = 3;

        // Фон + полоса слева
        g.fill(x - pad, y - pad, x + maxW + pad, y + mods.size() * lineH + pad, Theme.HUD_BG);
        g.fill(x - pad, y - pad, x - pad + 2, y + mods.size() * lineH + pad, accent);

        for (int i = 0; i < mods.size(); i++) {
            String s = mods.get(i);
            if (Config.arrayListShadow) {
                g.drawTextWithShadow(tr, s, x, y + i * lineH, Theme.TEXT);
            } else {
                g.drawText(tr, s, x, y + i * lineH, Theme.TEXT, false);
            }
        }
    }
}
