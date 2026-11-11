package com.example.visuals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemCooldownManager;
import net.minecraft.item.ItemStack;

/** HUD с кулдаунами предметов (жемчуг, хорус, щит, серверные кулдауны и т.д.). */
public final class CooldownHud {
    private CooldownHud() {}

    public static boolean enabled = true;
    public static int x = 8;

    private static final int ROW_H = 22;
    private static final int BAR_W = 56;

    private static final class St {
        long startMs;
        float p0;
    }

    private static final class Row {
        ItemStack stack;
        float progress;
        float seconds; // < 0 = пока неизвестно
    }

    private static final Map<Item, St> STATE = new HashMap<>();

    public static void hud(DrawContext ctx) {
        if (!enabled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;
        if (player == null || mc.options.hudHidden) {
            STATE.clear();
            return;
        }

        ItemCooldownManager cm = player.getItemCooldownManager();
        long now = System.currentTimeMillis();
        Set<Item> seen = new HashSet<>();
        List<Row> rows = new ArrayList<>();

        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < 36; i++) stacks.add(player.getInventory().getStack(i));
        stacks.add(player.getOffHandStack());

        for (ItemStack s : stacks) {
            if (s.isEmpty()) continue;
            Item item = s.getItem();
            if (seen.contains(item)) continue;
            float p = cm.getCooldownProgress(s, 0f);
            if (p <= 0f) continue;
            seen.add(item);

            St st = STATE.get(item);
            if (st == null) {
                st = new St();
                st.startMs = now;
                st.p0 = p;
                STATE.put(item, st);
            }

            Row r = new Row();
            r.stack = s;
            r.progress = Math.min(1f, p);
            float drop = st.p0 - p;
            long elapsed = now - st.startMs;
            if (drop > 0.02f && elapsed > 80) {
                float totalMs = elapsed / drop;
                r.seconds = Math.max(0f, p * totalMs / 1000f);
            } else {
                r.seconds = -1f;
            }
            rows.add(r);
        }
        STATE.keySet().retainAll(seen);

        if (rows.isEmpty()) return;

        int total = rows.size() * ROW_H;
        int y = mc.getWindow().getScaledHeight() / 2 - total / 2;

        for (Row r : rows) {
            int w = 20 + 4 + BAR_W + 4 + 30;
            ctx.fill(x - 3, y - 2, x + w, y + ROW_H - 4, 0x90000000);

            ctx.drawItem(r.stack, x, y);

            int bx = x + 24;
            int by = y + 6;
            ctx.fill(bx, by, bx + BAR_W, by + 5, 0xFF2A2A2A);
            int filled = Math.round(BAR_W * r.progress);
            int color = r.progress > 0.5f ? 0xFFE74C3C : (r.progress > 0.2f ? 0xFFF1C40F : 0xFF2ECC71);
            ctx.fill(bx, by, bx + filled, by + 5, color);

            String t = r.seconds >= 0
                    ? String.format(Locale.ROOT, "%.1fс", r.seconds)
                    : String.format(Locale.ROOT, "%d%%", Math.round(r.progress * 100));
            ctx.drawText(mc.textRenderer, t, bx + BAR_W + 4, y + 4, 0xFFFFFFFF, true);

            y += ROW_H;
        }
    }
}
