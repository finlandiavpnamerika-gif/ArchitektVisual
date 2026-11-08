package com.example.visuals;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Выбор предмета: из твоего инвентаря (по умолчанию) или из всех предметов игры. */
public class ItemPickScreen extends Screen {
    private static final int SLOT = 20, COLS = 9;

    private final Screen parent;
    private final Consumer<String> onPick;
    private boolean allMode = false;
    private int scrollRows = 0, maxRows = 0;
    private int mx, my;
    private boolean down, clicked, prevDown = true;
    private static List<ItemStack> ALL;

    public ItemPickScreen(Screen parent, Consumer<String> onPick) {
        super(Text.literal("Выбор предмета"));
        this.parent = parent;
        this.onPick = onPick;
    }

    private static List<ItemStack> all() {
        if (ALL == null) {
            ALL = new ArrayList<>();
            for (Item it : Registries.ITEM) if (it != Items.AIR) ALL.add(it.getDefaultStack());
        }
        return ALL;
    }

    private boolean hit(int x, int y, int w, int h) { return mx >= x && mx < x + w && my >= y && my < y + h; }

    private boolean tab(DrawContext g, int x, int y, int w, String label, boolean on) {
        boolean hov = hit(x, y, w, 18);
        HudStyle.roundFill(g, x, y, w, 18, 3, on ? ((0x70 << 24) | (Theme.ACCENT & 0xFFFFFF)) : (hov ? Theme.ROW_HOVER : 0x30000000));
        g.drawTextWithShadow(textRenderer, label, x + (w - textRenderer.getWidth(label)) / 2, y + 5, on ? Theme.TEXT : Theme.TEXT_DIM);
        return clicked && hov;
    }

    @Override
    public void render(DrawContext g, int mouseX, int mouseY, float delta) {
        mx = mouseX; my = mouseY;
        long window = client.getWindow().getHandle();
        down = GLFW.glfwGetMouseButton(window, 0) == GLFW.GLFW_PRESS;
        clicked = down && !prevDown;
        prevDown = down;

        g.fillGradient(0, 0, width, height, 0xB0080810, 0xE0000000);

        int pw = COLS * SLOT + 20;
        int visRows = allMode ? Math.max(3, Math.min(9, (height - 120) / SLOT)) : 4;
        int ph = 62 + visRows * SLOT + 22;
        int px = (width - pw) / 2, py = (height - ph) / 2;

        HudStyle.roundFill(g, px + 1, py + 2, pw, ph, 6, 0x40000000);
        HudStyle.roundFill(g, px, py, pw, ph, 6, Theme.PANEL_BG);
        g.fillGradient(px + 3, py + 1, px + pw - 3, py + 26, (0x50 << 24) | (Theme.ACCENT & 0xFFFFFF), Theme.ACCENT & 0xFFFFFF);
        HudStyle.roundRing(g, px, py, pw, ph, 6, (0x70 << 24) | (Theme.ACCENT & 0xFFFFFF));
        g.drawCenteredTextWithShadow(textRenderer, "Выбери предмет", px + pw / 2, py + 8, Theme.TEXT);

        int half = (pw - 24) / 2;
        if (tab(g, px + 8, py + 22, half, "Инвентарь", !allMode)) { allMode = false; scrollRows = 0; }
        if (tab(g, px + 16 + half, py + 22, half, "Все предметы", allMode)) { allMode = true; scrollRows = 0; }

        int gx = px + 10, gy = py + 46;
        List<ItemStack> list = new ArrayList<>();
        if (allMode) {
            list = all();
        } else if (client.player != null) {
            // как в ванильном инвентаре: сначала основные ряды, потом хотбар
            for (int i = 9; i < 36; i++) list.add(client.player.getInventory().getStack(i));
            for (int i = 0; i < 9; i++) list.add(client.player.getInventory().getStack(i));
        }
        int rows = (list.size() + COLS - 1) / COLS;
        maxRows = Math.max(0, rows - visRows);
        scrollRows = Math.max(0, Math.min(scrollRows, maxRows));

        ItemStack hover = null;
        int start = scrollRows * COLS;
        for (int k = 0; k < visRows * COLS; k++) {
            int idx = start + k;
            if (idx >= list.size()) break;
            ItemStack st = list.get(idx);
            int sx = gx + (k % COLS) * SLOT, sy = gy + (k / COLS) * SLOT;
            boolean hov = hit(sx, sy, SLOT - 1, SLOT - 1);
            HudStyle.roundFill(g, sx, sy, SLOT - 1, SLOT - 1, 2, hov ? ((0x60 << 24) | (Theme.ACCENT & 0xFFFFFF)) : 0x35000000);
            if (st.isEmpty()) continue;
            g.drawItem(st, sx + 1, sy + 1);
            if (hov) {
                hover = st;
                if (clicked) {
                    onPick.accept(Registries.ITEM.getId(st.getItem()).toString());
                    close();
                    return;
                }
            }
        }
        if (!allMode && client.player != null && list.stream().allMatch(ItemStack::isEmpty))
            g.drawCenteredTextWithShadow(textRenderer, "Инвентарь пуст", px + pw / 2, gy + 30, Theme.TEXT_DIM);

        String hint = allMode ? "Колесо мыши - прокрутка · Esc - отмена" : "Кликни предмет из инвентаря · Esc - отмена";
        g.drawCenteredTextWithShadow(textRenderer, hint, px + pw / 2, py + ph - 14, Theme.TEXT_DIM);

        if (hover != null) {
            String nm = hover.getName().getString();
            int tw = textRenderer.getWidth(nm) + 10;
            int tx = Math.min(mx + 10, width - tw - 2), ty = my - 16;
            HudStyle.roundFill(g, tx, ty, tw, 14, 3, 0xE0101018);
            HudStyle.roundRing(g, tx, ty, tw, 14, 3, (0x90 << 24) | (Theme.ACCENT & 0xFFFFFF));
            g.drawTextWithShadow(textRenderer, nm, tx + 5, ty + 3, 0xFFFFFFFF);
        }
    }

    // колесо мыши (без @Override: сигнатура зависит от версии)
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollRows = Math.max(0, Math.min(maxRows, scrollRows - (int) Math.signum(verticalAmount) * 2));
        return true;
    }

    @Override
    public void close() { client.setScreen(parent); }

    @Override
    public boolean shouldPause() { return false; }
}
