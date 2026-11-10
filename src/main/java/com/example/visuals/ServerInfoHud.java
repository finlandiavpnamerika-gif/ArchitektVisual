package com.example.visuals;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.gui.DrawContext;

/** Server Info: IP сервера, TPS и пинг. */
public final class ServerInfoHud {
    private ServerInfoHud() {}

    public static int[] draw(DrawContext g, MinecraftClient mc, float a) {
        List<String> labels = new ArrayList<>();
        List<String> values = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        if (Config.serverIp) {
            String ip;
            ServerInfo si = mc.getCurrentServerEntry();
            if (si != null) ip = si.address;
            else ip = mc.isInSingleplayer() ? "Singleplayer" : "-";
            if (ip == null) ip = "-";
            if (ip.length() > 26) ip = ip.substring(0, 25) + "…";
            labels.add("IP");
            values.add(ip);
            colors.add(Theme.TEXT);
        }
        if (Config.serverTps) {
            double t = Tps.get();
            labels.add("TPS");
            values.add(t < 0 ? "--" : String.format(Locale.ROOT, "%.1f", t));
            colors.add(t < 0 ? Theme.TEXT_DIM : t >= 18 ? HudStyle.GOOD : t >= 12 ? HudStyle.MID : HudStyle.BAD);
        }
        if (Config.serverPing && mc.player != null) {
            PlayerListEntry e = mc.getNetworkHandler() == null
                    ? null : mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            int ping = e == null ? 0 : e.getLatency();
            labels.add("Ping");
            values.add(ping + " ms");
            colors.add(ping <= 80 ? HudStyle.GOOD : ping <= 150 ? HudStyle.MID : HudStyle.BAD);
        }
        if (labels.isEmpty()) return null;
        return HudRows.draw(g, mc.textRenderer, "Server", labels, values, colors, Config.COLORS[Config.serverColor], a);
    }
}
