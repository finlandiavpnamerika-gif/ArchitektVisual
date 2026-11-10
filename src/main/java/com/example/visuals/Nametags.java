package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

/** Nametags+: свои подписи игроков (имя, здоровье, расстояние) с фоном и масштабом. */
public final class Nametags {
    private Nametags() {}

    public static void hud(DrawContext g) {
        if (!Config.ntOn) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.options.hudHidden || mc.currentScreen != null) return;
        Projection pr = Projection.of(mc);
        if (pr == null) return;
        TextRenderer tr = mc.textRenderer;
        float tp = mc.getRenderTickCounter().getTickProgress(false);
        int drawn = 0;

        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || p.isSpectator() || p.isInvisible() || drawn >= 40) continue;
            double x = MathHelper.lerp((double) tp, p.lastX, p.getX());
            double y = MathHelper.lerp((double) tp, p.lastY, p.getY()) + p.getHeight() + 0.35;
            double z = MathHelper.lerp((double) tp, p.lastZ, p.getZ());
            float[] s = pr.project(x, y, z);
            if (s == null || s[2] > Config.ntRange) continue;
            drawn++;

            String name = p.getName().getString();
            boolean friend = Config.friends.contains(name);
            float hp = p.getHealth() + p.getAbsorptionAmount();
            float ratio = MathHelper.clamp(p.getHealth() / Math.max(1f, p.getMaxHealth()), 0f, 1f);

            StringBuilder sub = new StringBuilder();
            if (Config.ntHealth) sub.append((int) Math.ceil(hp)).append(" HP");
            if (Config.ntDist) sub.append(sub.length() > 0 ? "  " : "").append(Math.round(s[2])).append("m");
            String line2 = sub.toString();

            int w = Math.max(tr.getWidth(name), tr.getWidth(line2)) + 10;
            int h = 12 + (line2.isEmpty() ? 0 : 10) + (Config.ntHealth ? 4 : 0);

            float sc = (float) Config.ntScale;
            if (Config.ntDistScale) sc *= MathHelper.clamp(10f / Math.max(1f, s[2]), 0.6f, 1.6f);

            var m = g.getMatrices();
            m.pushMatrix();
            m.translate(s[0], s[1]);
            m.scale(sc, sc);
            int x0 = -w / 2, y0 = -h;
            if (Config.ntBg) HudStyle.roundFill(g, x0, y0, w, h, 3, 0xA0000000);
            int nameCol = friend ? HudStyle.GOOD : Theme.TEXT;
            HudStyle.text(g, tr, name, -tr.getWidth(name) / 2, y0 + 2, nameCol, 1f, true);
            int yy = y0 + 12;
            if (!line2.isEmpty()) {
                HudStyle.text(g, tr, line2, -tr.getWidth(line2) / 2, yy, Theme.TEXT_DIM, 1f, true);
                yy += 10;
            }
            if (Config.ntHealth) {
                int bw = w - 8;
                g.fill(x0 + 4, yy, x0 + 4 + bw, yy + 2, 0x80000000);
                g.fill(x0 + 4, yy, x0 + 4 + Math.round(bw * ratio), yy + 2, HudStyle.ratioColor(ratio));
            }
            m.popMatrix();
        }
    }
}
