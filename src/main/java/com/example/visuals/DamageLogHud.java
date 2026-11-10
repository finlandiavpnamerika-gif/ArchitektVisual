package com.example.visuals;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;

/** Damage Log: последние события урона (нанесённый зелёным, полученный красным). */
public final class DamageLogHud {
    private DamageLogHud() {}

    public static int[] draw(DrawContext g, MinecraftClient mc, float a) {
        long now = System.currentTimeMillis();
        long life = (long) (Config.dlLife * 1000);
        List<String> labels = new ArrayList<>();
        List<String> values = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (int i = Combat.LOG.size() - 1; i >= 0 && labels.size() < Config.dlLines; i--) {
            Combat.LogEntry e = Combat.LOG.get(i);
            if (now - e.at > life) continue;
            if (e.dealt ? !Config.dlDealt : !Config.dlTaken) continue;
            labels.add(e.name);
            values.add(String.format(Locale.ROOT, e.dealt ? "+%.1f" : "-%.1f", e.amount));
            colors.add(e.dealt ? HudStyle.GOOD : HudStyle.BAD);
        }
        if (labels.isEmpty()) {
            if (!(mc.currentScreen instanceof ChatScreen)) return null;
            labels.add("Example");
            values.add("+4.0");
            colors.add(HudStyle.GOOD);
        }
        return HudRows.draw(g, mc.textRenderer, "Damage Log", labels, values, colors, Config.COLORS[Config.dlColor], a);
    }
}
