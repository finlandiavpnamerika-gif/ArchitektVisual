package com.example.visuals;

import com.example.visuals.mixin.HandledScreenAccessor;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.class_1268;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_239;
import net.minecraft.class_2561;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3965;
import net.minecraft.class_437;
import net.minecraft.class_490;
import org.lwjgl.glfw.GLFW;

public final class ItemFeatures {
    private ItemFeatures() {}

    private static final long SWAP_COOLDOWN_MS = 50;
    private static final long RETURN_DELAY_MS = 50;

    private static String firstItem = null;
    private static long lastSwap = 0;

    private static int returnSlotId = -1;
    private static int returnHotbar = -1;
    private static long returnAt = 0;

    private static boolean swapSelWasDown = true;
    private static boolean bindSelWasDown = true;

    private static int codeOf(class_304 kb) {
        return KeyBindingHelper.getBoundKeyOf(kb).method_1444();
    }

    public static String swapKeyName() {
        return Keys.name(codeOf(VisualsClient.swapSelectKey));
    }

    public static String bindKeyName() {
        return Keys.name(codeOf(VisualsClient.bindSelectKey));
    }

    public static String validateKey(int key, Object self) {
        if (Keys.isModifier(key)) return "Shift/Ctrl/Alt нельзя, нажми другую клавишу";
        if (key == codeOf(VisualsClient.swapSelectKey) || key == codeOf(VisualsClient.bindSelectKey)
                || key == codeOf(VisualsClient.menuKey) || key == codeOf(VisualsClient.zoomKey)) {
            return "Эта клавиша занята модом";
        }
        if (key == Config.radialKey) return "Клавиша занята круговым меню";
        for (SwapPair p : Config.swapPairs) {
            if (p != self && p.key == key) return "Клавиша уже занята другой парой";
        }
        for (BoundItem b : Config.binds) {
            if (b != self && b.key == key) return "Клавиша уже занята ItemBind";
        }
        return null;
    }

    public static String setKey(SwapPair p, int key) {
        String err = validateKey(key, p);
        if (err != null) return err;
        p.key = key;
        p.wasDown = true;
        Config.save();
        return null;
    }

    public static String setKey(BoundItem b, int key) {
        String err = validateKey(key, b);
        if (err != null) return err;
        b.key = key;
        b.wasDown = true;
        Config.save();
        return null;
    }

    public static void tick(class_310 mc) {
        if (mc.field_1724 == null || mc.field_1761 == null) return;
        long now = System.currentTimeMillis();
        long window = mc.method_22683().method_4490();
        class_437 screen = mc.field_1755;
        boolean inInventory = screen instanceof class_490;
        boolean active = screen == null || inInventory;

        if (returnSlotId != -1 && now >= returnAt
                && mc.field_1724.field_7512 == mc.field_1724.field_7498) {
            mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763,
                    returnSlotId, returnHotbar, class_1713.field_7791, mc.field_1724);
            returnSlotId = -1;
        }

        boolean swapSel = Keys.down(window, codeOf(VisualsClient.swapSelectKey));
        boolean bindSel = Keys.down(window, codeOf(VisualsClient.bindSelectKey));
        if (inInventory) {
            if (Config.autoswap && swapSel && !swapSelWasDown) {
                boolean shift = Keys.down(window, GLFW.GLFW_KEY_LEFT_SHIFT) || Keys.down(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
                if (shift) removeHoveredPair(mc, screen);
                else selectHoveredForSwap(mc, screen);
            }
            if (Config.itembind && bindSel && !bindSelWasDown) {
                toggleHoveredBind(mc, screen);
            }
        }
        swapSelWasDown = swapSel;
        bindSelWasDown = bindSel;

        for (SwapPair p : Config.swapPairs) {
            boolean d = p.key != -1 && Keys.down(window, p.key);
            if (Config.autoswap && active && d && !p.wasDown) swapPair(mc, p, now);
            p.wasDown = d;
        }

        for (BoundItem b : Config.binds) {
            boolean d = b.key != -1 && Keys.down(window, b.key);
            if (Config.itembind && active && d && !b.wasDown && returnSlotId == -1) useBound(mc, b, now);
            b.wasDown = d;
        }
    }

    private static void swapPair(class_310 mc, SwapPair p, long now) {
        if (now - lastSwap < SWAP_COOLDOWN_MS) return;
        class_1799 off = mc.field_1724.method_6079();
        String offName = off.method_7960() ? "" : nameOf(off);
        String[] targets;
        if (offName.equalsIgnoreCase(p.a)) targets = new String[]{p.b};
        else if (offName.equalsIgnoreCase(p.b)) targets = new String[]{p.a};
        else targets = new String[]{p.a, p.b};

        for (String t : targets) {
            int slotId = findSlotId(mc, t);
            if (slotId == -1) continue;
            mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, slotId, 40,
                    class_1713.field_7791, mc.field_1724);
            lastSwap = now;
            return;
        }
        msg(mc, "AutoSwap: предметы пары не найдены");
    }

    private static void selectHoveredForSwap(class_310 mc, class_437 screen) {
        class_1735 slot = hovered(screen);
        if (slot == null || !slot.method_7681()) return;
        String name = nameOf(slot.method_7677());
        if (firstItem == null) {
            firstItem = name;
            msg(mc, "AutoSwap: выбран «" + name + "», выбери второй предмет");
        } else if (firstItem.equalsIgnoreCase(name)) {
            firstItem = null;
            msg(mc, "AutoSwap: отменено");
        } else {
            final SwapPair pair = new SwapPair(firstItem, name, -1);
            firstItem = null;
            mc.method_1507(new BindScreen(screen,
                    "AutoSwap: " + pair.a + " <-> " + pair.b,
                    key -> {
                        String err = validateKey(key, pair);
                        if (err != null) return err;
                        pair.key = key;
                        pair.wasDown = true;
                        Config.swapPairs.add(pair);
                        Config.save();
                        msg(mc, "AutoSwap: [" + Keys.name(key) + "] " + pair.a + " <-> " + pair.b);
                        return null;
                    }));
        }
    }

    private static void removeHoveredPair(class_310 mc, class_437 screen) {
        class_1735 slot = hovered(screen);
        if (slot == null || !slot.method_7681()) return;
        String name = nameOf(slot.method_7677());
        boolean removed = Config.swapPairs.removeIf(p -> p.has(name));
        if (removed) {
            Config.save();
            msg(mc, "AutoSwap: удалены пары с «" + name + "»");
        }
    }

    private static void toggleHoveredBind(class_310 mc, class_437 screen) {
        class_1735 slot = hovered(screen);
        if (slot == null || !slot.method_7681()) return;
        final String name = nameOf(slot.method_7677());
        boolean removed = Config.binds.removeIf(b -> b.name.equalsIgnoreCase(name));
        if (removed) {
            Config.save();
            msg(mc, "ItemBind: бинд «" + name + "» удалён");
            return;
        }
        final BoundItem bind = new BoundItem(name, -1);
        mc.method_1507(new BindScreen(screen, "ItemBind: " + name, key -> {
            String err = validateKey(key, bind);
            if (err != null) return err;
            bind.key = key;
            bind.wasDown = true;
            Config.binds.add(bind);
            Config.save();
            msg(mc, "ItemBind: [" + Keys.name(key) + "] -> " + name);
            return null;
        }));
    }

    private static void useBound(class_310 mc, BoundItem b, long now) {
        int idx = findInventoryIndex(mc, b.name);
        if (idx == -1) {
            msg(mc, "ItemBind: «" + b.name + "» не найден");
            return;
        }
        int selected = mc.field_1724.method_31548().method_67532();
        if (idx != selected) {
            int slotId = idx < 9 ? 36 + idx : idx;
            mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, slotId, selected,
                    class_1713.field_7791, mc.field_1724);
            returnSlotId = slotId;
            returnHotbar = selected;
            returnAt = now + RETURN_DELAY_MS;
        }
        useMainHand(mc);
    }

    private static void useMainHand(class_310 mc) {
        class_239 hit = mc.field_1765;
        if (hit instanceof class_3965 bhr && bhr.method_17783() == class_239.class_240.field_1332) {
            if (mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, bhr).method_23665()) {
                mc.field_1724.method_6104(class_1268.field_5808);
                return;
            }
        }
        if (mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808).method_23665()) {
            mc.field_1724.method_6104(class_1268.field_5808);
        }
    }

    private static class_1735 hovered(class_437 screen) {
        return ((HandledScreenAccessor) screen).visuals$getFocusedSlot();
    }

    private static int findInventoryIndex(class_310 mc, String name) {
        for (int i = 0; i < 36; i++) {
            class_1799 s = mc.field_1724.method_31548().method_5438(i);
            if (!s.method_7960() && nameOf(s).equalsIgnoreCase(name)) return i;
        }
        return -1;
    }

    private static int findSlotId(class_310 mc, String name) {
        int i = findInventoryIndex(mc, name);
        if (i == -1) return -1;
        return i < 9 ? 36 + i : i;
    }

    private static String nameOf(class_1799 s) {
        return s.method_7964().getString().trim();
    }

    static void msg(class_310 mc, String s) {
        if (mc.field_1724 != null) mc.field_1724.method_7353(class_2561.method_43470(s), true);
    }

    public static void drawPanel(class_332 g, class_310 mc) {
        if (mc.field_1724 == null) return;
        boolean swapOn = Config.autoswap;
        boolean bindOn = Config.itembind;
        if (!swapOn && !bindOn) return;
        class_327 tr = mc.field_1772;

        class_1799 off = mc.field_1724.method_6079();
        String offName = off.method_7960() ? "" : nameOf(off);

        java.util.List<String> lines = new java.util.ArrayList<>();
        java.util.List<Integer> colors = new java.util.ArrayList<>();
        int w = 0;

        if (swapOn) {
            lines.add("AutoSwap");
            colors.add(-171);
            w = Math.max(w, tr.method_1727("AutoSwap"));
            for (SwapPair p : Config.swapPairs) {
                String l = "[" + Keys.name(p.key) + "] " + p.a + " <-> " + p.b;
                lines.add(l);
                boolean match = p.a.equalsIgnoreCase(offName) || p.b.equalsIgnoreCase(offName);
                colors.add(match ? -11141291 : -1);
                w = Math.max(w, tr.method_1727(l));
            }
            String hint = firstItem != null ? "Выбрано: " + firstItem + " -> выбери второй"
                    : (Config.swapPairs.isEmpty() ? "Наведи на предмет и нажми [" + swapKeyName() + "]" : null);
            if (hint != null) {
                lines.add(hint);
                colors.add(-5592406);
                w = Math.max(w, tr.method_1727(hint));
            }
        }
        if (bindOn) {
            lines.add("ItemBind");
            colors.add(-171);
            w = Math.max(w, tr.method_1727("ItemBind"));
            for (BoundItem b : Config.binds) {
                String l = "[" + Keys.name(b.key) + "] " + b.name;
                lines.add(l);
                colors.add(-1);
                w = Math.max(w, tr.method_1727(l));
            }
            if (Config.binds.isEmpty()) {
                String hint = "Наведи на предмет и нажми [" + bindKeyName() + "]";
                lines.add(hint);
                colors.add(-5592406);
                w = Math.max(w, tr.method_1727(hint));
            }
        }

        int x = 6;
        int y = 6;
        g.method_25294(x - 3, y - 3, x + w + 3, y + lines.size() * 11 + 1, 0x90000000);
        for (int i = 0; i < lines.size(); i++) {
            g.method_25303(tr, lines.get(i), x, y, colors.get(i));
            y += 11;
        }
    }
}
