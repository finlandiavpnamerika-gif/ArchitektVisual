package com.example.visuals;

import com.example.visuals.mixin.HandledScreenAccessor;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.MathHelper;

/**
 * Item Highlight: как ItemBind, но только подсвечивает.
 * Наведи на предмет в инвентаре и нажми клавишу - предмет попадёт в список (или удалится, если уже там).
 * Все слоты с предметом такого же названия подсвечиваются выбранным цветом и прозрачностью.
 */
public final class ItemHighlight {
    private ItemHighlight() {}

    /** Выбранный предмет: название, цвет (оттенок 0..1) и прозрачность (0.1..1). */
    public static final class Entry {
        public final String name;
        public double hue;
        public double alpha;

        public Entry(String name, double hue, double alpha) {
            this.name = name;
            this.hue = hue;
            this.alpha = alpha;
        }
    }

    private static boolean selWasDown = true;

    public static String keyName() {
        return Keys.name(KeyBindingHelper.getBoundKeyOf(VisualsClient.highlightKey).getCode());
    }

    public static void tick(MinecraftClient mc) {
        if (mc.player == null) return;
        long window = mc.getWindow().getHandle();
        boolean down = Keys.down(window, KeyBindingHelper.getBoundKeyOf(VisualsClient.highlightKey).getCode());
        Screen screen = mc.currentScreen;
        // только инвентарь и сундуки: там нет полей ввода, где клавиша мешала бы печатать
        boolean ok = screen instanceof InventoryScreen || screen instanceof GenericContainerScreen;
        if (Config.highlight && ok && down && !selWasDown) toggleHovered(mc, screen);
        selWasDown = down;
    }

    private static void toggleHovered(MinecraftClient mc, Screen screen) {
        Slot slot = ((HandledScreenAccessor) screen).visuals$getFocusedSlot();
        if (slot == null || !slot.hasStack()) return;
        final String name = nameOf(slot.getStack());

        boolean removed = Config.highlights.removeIf(e -> e.name.equalsIgnoreCase(name));
        if (removed) {
            Config.save();
            ItemFeatures.msg(mc, "Highlight: «" + name + "» убран");
            return;
        }
        // каждому новому предмету - свой стартовый цвет
        double hue = (Config.highlights.size() * 0.17 + 0.05) % 1.0;
        Config.highlights.add(new Entry(name, hue, 0.6));
        Config.save();
        ItemFeatures.msg(mc, "Highlight: «" + name + "» подсвечивается");
    }

    /** Вызывается из HandledScreenMixin: рисует подсветку слотов. */
    public static void render(DrawContext g, HandledScreen<?> screen) {
        if (!Config.highlight || Config.highlights.isEmpty()) return;

        HandledScreenAccessor acc = (HandledScreenAccessor) screen;
        int sx = acc.visuals$getX();
        int sy = acc.visuals$getY();

        for (Slot slot : screen.getScreenHandler().slots) {
            if (!slot.hasStack()) continue;
            Entry e = find(nameOf(slot.getStack()));
            if (e == null) continue;

            int rgb = MathHelper.hsvToRgb((float) e.hue, 0.85f, 1.0f) & 0xFFFFFF;
            int a = Math.max(0, Math.min(255, (int) Math.round(255.0 * e.alpha)));
            int fill = ((int) (a * 0.45) << 24) | rgb;
            int edge = (a << 24) | rgb;

            int x = sx + slot.x, y = sy + slot.y;
            g.fill(x, y, x + 16, y + 16, fill);
            g.fill(x, y, x + 16, y + 1, edge);
            g.fill(x, y + 15, x + 16, y + 16, edge);
            g.fill(x, y + 1, x + 1, y + 15, edge);
            g.fill(x + 15, y + 1, x + 16, y + 15, edge);
        }
    }

    private static Entry find(String name) {
        for (Entry e : Config.highlights) if (e.name.equalsIgnoreCase(name)) return e;
        return null;
    }

    private static String nameOf(ItemStack s) {
        return s.getName().getString().trim();
    }
}
