package com.example.visuals;

import com.example.visuals.mixin.HandledScreenAccessor;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
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

    private static int codeOf(KeyBinding kb) {
        return KeyBindingHelper.getBoundKeyOf(kb).getCode();
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

    public static void tick(MinecraftClient mc) {
        if (mc.player == null || mc.interactionManager == null) return;
        long now = System.currentTimeMillis();
        long window = mc.getWindow().getHandle();
        Screen screen = mc.currentScreen;
        boolean inInventory = screen instanceof InventoryScreen;
        boolean active = screen == null || inInventory;

        if (returnSlotId != -1 && now >= returnAt
                && mc.player.currentScreenHandler == mc.player.playerScreenHandler) {
            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                    returnSlotId, returnHotbar, SlotActionType.SWAP, mc.player);
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

    private static void swapPair(MinecraftClient mc, SwapPair p, long now) {
        if (now - lastSwap < SWAP_COOLDOWN_MS) return;
        ItemStack off = mc.player.getOffHandStack();
        String offName = off.isEmpty() ? "" : nameOf(off);
        String[] targets;
        if (offName.equalsIgnoreCase(p.a)) targets = new String[]{p.b};
        else if (offName.equalsIgnoreCase(p.b)) targets = new String[]{p.a};
        else targets = new String[]{p.a, p.b};

        for (String t : targets) {
            int slotId = findSlotId(mc, t);
            if (slotId == -1) continue;
            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, slotId, 40,
                    SlotActionType.SWAP, mc.player);
            lastSwap = now;
            return;
        }
        msg(mc, "AutoSwap: предметы пары не найдены");
    }

    private static void selectHoveredForSwap(MinecraftClient mc, Screen screen) {
        Slot slot = hovered(screen);
        if (slot == null || !slot.hasStack()) return;
        String name = nameOf(slot.getStack());
        if (firstItem == null) {
            firstItem = name;
            msg(mc, "AutoSwap: выбран «" + name + "», выбери второй предмет");
        } else if (firstItem.equalsIgnoreCase(name)) {
            firstItem = null;
            msg(mc, "AutoSwap: отменено");
        } else {
            final SwapPair pair = new SwapPair(firstItem, name, -1);
            firstItem = null;
            mc.setScreen(new BindScreen(screen,
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

    private static void removeHoveredPair(MinecraftClient mc, Screen screen) {
        Slot slot = hovered(screen);
        if (slot == null || !slot.hasStack()) return;
        String name = nameOf(slot.getStack());
        boolean removed = Config.swapPairs.removeIf(p -> p.has(name));
        if (removed) {
            Config.save();
            msg(mc, "AutoSwap: удалены пары с «" + name + "»");
        }
    }

    private static void toggleHoveredBind(MinecraftClient mc, Screen screen) {
        Slot slot = hovered(screen);
        if (slot == null || !slot.hasStack()) return;
        final String name = nameOf(slot.getStack());
        boolean removed = Config.binds.removeIf(b -> b.name.equalsIgnoreCase(name));
        if (removed) {
            Config.save();
            msg(mc, "ItemBind: бинд «" + name + "» удалён");
            return;
        }
        final BoundItem bind = new BoundItem(name, -1);
        mc.setScreen(new BindScreen(screen, "ItemBind: " + name, key -> {
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

    private static void useBound(MinecraftClient mc, BoundItem b, long now) {
        int idx = findInventoryIndex(mc, b.name);
        if (idx == -1) {
            msg(mc, "ItemBind: «" + b.name + "» не найден");
            return;
        }
        int selected = mc.player.getInventory().getSelectedSlot();
        if (idx != selected) {
            int slotId = idx < 9 ? 36 + idx : idx;
            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, slotId, selected,
                    SlotActionType.SWAP, mc.player);
            returnSlotId = slotId;
            returnHotbar = selected;
            returnAt = now + RETURN_DELAY_MS;
        }
        useMainHand(mc);
    }

    private static void useMainHand(MinecraftClient mc) {
        HitResult hit = mc.crosshairTarget;
        if (hit instanceof BlockHitResult bhr && bhr.getType() == HitResult.Type.BLOCK) {
            if (mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr).isAccepted()) {
                mc.player.swingHand(Hand.MAIN_HAND);
                return;
            }
        }
        if (mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND).isAccepted()) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private static Slot hovered(Screen screen) {
        return ((HandledScreenAccessor) screen).visuals$getFocusedSlot();
    }

    private static int findInventoryIndex(MinecraftClient mc, String name) {
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (!s.isEmpty() && nameOf(s).equalsIgnoreCase(name)) return i;
        }
        return -1;
    }

    private static int findSlotId(MinecraftClient mc, String name) {
        int i = findInventoryIndex(mc, name);
        if (i == -1) return -1;
        return i < 9 ? 36 + i : i;
    }

    private static String nameOf(ItemStack s) {
        return s.getName().getString().trim();
    }

    static void msg(MinecraftClient mc, String s) {
        if (mc.player != null) mc.player.sendMessage(Text.literal(s), true);
    }

    public static void drawPanel(DrawContext g, MinecraftClient mc) {
        if (mc.player == null) return;
        boolean swapOn = Config.autoswap;
        boolean bindOn = Config.itembind;
        if (!swapOn && !bindOn) return;
        TextRenderer tr = mc.textRenderer;

        ItemStack off = mc.player.getOffHandStack();
        String offName = off.isEmpty() ? "" : nameOf(off);

        java.util.List<String> lines = new java.util.ArrayList<>();
        java.util.List<Integer> colors = new java.util.ArrayList<>();
        int w = 0;

        if (swapOn) {
            lines.add("AutoSwap");
            colors.add(-171);
            w = Math.max(w, tr.getWidth("AutoSwap"));
            for (SwapPair p : Config.swapPairs) {
                String l = "[" + Keys.name(p.key) + "] " + p.a + " <-> " + p.b;
                lines.add(l);
                boolean match = p.a.equalsIgnoreCase(offName) || p.b.equalsIgnoreCase(offName);
                colors.add(match ? -11141291 : -1);
                w = Math.max(w, tr.getWidth(l));
            }
            String hint = firstItem != null ? "Выбрано: " + firstItem + " -> выбери второй"
                    : (Config.swapPairs.isEmpty() ? "Наведи на предмет и нажми [" + swapKeyName() + "]" : null);
            if (hint != null) {
                lines.add(hint);
                colors.add(-5592406);
                w = Math.max(w, tr.getWidth(hint));
            }
        }
        if (bindOn) {
            lines.add("ItemBind");
            colors.add(-171);
            w = Math.max(w, tr.getWidth("ItemBind"));
            for (BoundItem b : Config.binds) {
                String l = "[" + Keys.name(b.key) + "] " + b.name;
                lines.add(l);
                colors.add(-1);
                w = Math.max(w, tr.getWidth(l));
            }
            if (Config.binds.isEmpty()) {
                String hint = "Наведи на предмет и нажми [" + bindKeyName() + "]";
                lines.add(hint);
                colors.add(-5592406);
                w = Math.max(w, tr.getWidth(hint));
            }
        }

        int x = 6;
        int y = 6;
        g.fill(x - 3, y - 3, x + w + 3, y + lines.size() * 11 + 1, 0x90000000);
        for (int i = 0; i < lines.size(); i++) {
            g.drawTextWithShadow(tr, lines.get(i), x, y, colors.get(i));
            y += 11;
        }
    }
}
