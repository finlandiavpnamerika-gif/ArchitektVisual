package com.example.visuals;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** Combat Stats: точность попаданий, киллы, серия, смерти и K/D. */
public final class StatsHud {
    private StatsHud() {}

    public static int[] draw(DrawContext g, MinecraftClient mc, float a) {
        List<String> labels = new ArrayList<>();
        List<String> values = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        if (Config.statsAcc) {
            int pct = Combat.attempts == 0 ? 0 : Math.round(100f * Combat.hits / Combat.attempts);
            labels.add("Accuracy");
            values.add(pct + "% (" + Combat.hits + "/" + Combat.attempts + ")");
            colors.add(Combat.attempts == 0 ? Theme.TEXT_DIM : pct >= 70 ? HudStyle.GOOD : pct >= 40 ? HudStyle.MID : HudStyle.BAD);
        }
        if (Config.statsKills) {
            labels.add("Kills");
            values.add(String.valueOf(Combat.kills));
            colors.add(Theme.TEXT);
        }
        if (Config.statsStreak) {
            labels.add("Streak");
            values.add(Combat.streak + " (best " + Combat.bestStreak + ")");
            colors.add(Combat.streak >= 3 ? HudStyle.GOOD : Theme.TEXT);
        }
        if (Config.statsDeaths) {
            labels.add("Deaths");
            values.add(String.valueOf(Combat.deaths));
            colors.add(Theme.TEXT);
        }
        if (Config.statsKd) {
            double kd = Combat.deaths == 0 ? Combat.kills : (double) Combat.kills / Combat.deaths;
            labels.add("K/D");
            values.add(String.format(Locale.ROOT, "%.2f", kd));
            colors.add(kd >= 1.0 ? HudStyle.GOOD : HudStyle.BAD);
        }
        if (labels.isEmpty()) return null;
        return HudRows.draw(g, mc.textRenderer, "Combat Stats", labels, values, colors, Config.COLORS[Config.statsColor], a);
    }
}
