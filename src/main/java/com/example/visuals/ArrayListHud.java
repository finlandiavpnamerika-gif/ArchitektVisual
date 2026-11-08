package com.example.visuals;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/** ArrayList: включённые модули плашками с градиентом цвета. Вертикально или горизонтально. */
public final class ArrayListHud {
    private ArrayListHud() {}

    private static final int H = 12;       // высота плашки
    private static final int GAP = 2;      // зазор между плашками

    /** Рисует в (0,0), возвращает {w, h} или null, если включённых модулей нет. */
    public static int[] draw(DrawContext g, MinecraftClient mc, float a) {
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
        if (Config.potionHud) mods.add("PotionHUD");
        if (Config.customFog) mods.add("CustomFog");
        if (Config.particles) mods.add("Particles");
        if (Config.skyShader) mods.add("SkyShader");
        if (Config.damageFever) mods.add("DamageViewer");
        if (Config.targetHud) mods.add("TargetHUD");
        if (Config.handView) mods.add("HandView");
        if (Config.customItems) mods.add("CustomItems");
        if (mods.isEmpty()) return null;

        mods.sort((p, q) -> tr.getWidth(q) - tr.getWidth(p));   // длинные первыми
        int n = mods.size();

        if (Config.arrayListHoriz) {
            int x = 0;
            for (int i = 0; i < n; i++) {
                String s = mods.get(i);
                int col = color(accent, i, n);
                int w = tr.getWidth(s) + 10;
                HudStyle.pill(g, x, 0, w, H, a, col);
                HudStyle.text(g, tr, s, x + 6, 2, col, a, Config.arrayListShadow);
                x += w + GAP;
            }
            return new int[] {x - GAP, H};
        }

        int maxW = 0;
        for (String s : mods) maxW = Math.max(maxW, tr.getWidth(s) + 10);
        for (int i = 0; i < n; i++) {
            String s = mods.get(i);
            int col = color(accent, i, n);
            int w = tr.getWidth(s) + 10;
            int y = i * (H + GAP);
            HudStyle.pill(g, maxW - w, y, w, H, a, col);          // выровнено по правому краю
            HudStyle.text(g, tr, s, maxW - w + 6, y + 2, col, a, Config.arrayListShadow);
        }
        return new int[] {maxW, n * (H + GAP) - GAP};
    }

    /** Градиент от акцента к светлому оттенку. */
    private static int color(int accent, int i, int n) {
        float t = n <= 1 ? 0f : (float) i / (n - 1);
        return HudStyle.mix(0xFF000000 | accent, 0xFFFFFFFF, t * 0.6f) | 0xFF000000;
    }
}
