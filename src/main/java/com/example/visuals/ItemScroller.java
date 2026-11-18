package com.example.visuals;

import com.example.visuals.mixin.HandledScreenAccessor;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_465;
import org.lwjgl.glfw.GLFW;

/**
 * Item Scroller: быстрое перекладывание предметов в инвентаре и контейнерах.
 *
 * Shift + зажатая ЛКМ и проводишь по слотам - стаки под курсором переносятся (как Shift-клик, но «мазком»).
 * Колесо мыши над предметом:
 *   вниз            - переложить 1 предмет в соседний инвентарь (контейнер <-> игрок, основной <-> хотбар);
 *   вверх           - подтянуть 1 такой же предмет из соседнего инвентаря в этот слот;
 *   Shift + вниз    - переложить весь стак;
 *   Ctrl + вниз     - переложить все такие же предметы из этого инвентаря.
 *
 * Всё делается обычными кликами по слотам (как руками), с задержкой между действиями из настроек.
 */
public final class ItemScroller {
    private ItemScroller() {}

    private static final ArrayDeque<Integer> queue = new ArrayDeque<>();   // очередь слотов на быстрый перенос
    private static final Set<Integer> visited = new HashSet<>();           // слоты, которых уже коснулась текущая «протяжка»
    private static boolean dragging = false;
    private static boolean prevLmb = true;
    private static long lastClick = 0;
    private static class_1703 queueHandler = null;

    // ================= общее =================

    private static boolean usable(class_310 mc, class_465<?> screen) {
        if (!Config.itemScroller) return false;
        if (mc.field_1724 == null || mc.field_1761 == null) return false;
        // только экраны, у которых обработчик - текущий у игрока (не креатив-инвентарь)
        return screen.method_17577() == mc.field_1724.field_7512;
    }

    private static void click(class_310 mc, class_1703 h, int slotId, int button, class_1713 type) {
        mc.field_1761.method_2906(h.field_7763, slotId, button, type, mc.field_1724);
    }

    private static boolean shiftDown(long window) {
        return Keys.down(window, GLFW.GLFW_KEY_LEFT_SHIFT) || Keys.down(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    private static boolean ctrlDown(long window) {
        return Keys.down(window, GLFW.GLFW_KEY_LEFT_CONTROL) || Keys.down(window, GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    private static int focusedId(class_465<?> screen, class_1703 h) {
        class_1735 slot = ((HandledScreenAccessor) screen).visuals$getFocusedSlot();
        return slot == null ? -1 : h.field_7761.indexOf(slot);
    }

    /**
     * Зона слота. В инвентаре игрока: 0 = основной, 1 = хотбар (остальное не трогаем).
     * В контейнере: 0 = сам контейнер, 1 = инвентарь игрока (последние 36 слотов). Иначе -1.
     */
    private static int zone(class_310 mc, class_1703 h, int id) {
        int n = h.field_7761.size();
        if (h == mc.field_1724.field_7498) {
            if (id >= 36 && id <= 44) return 1;
            if (id >= 9 && id <= 35) return 0;
            return -1;
        }
        int playerStart = n - 36;
        if (playerStart < 0) return -1;
        return id >= playerStart ? 1 : 0;
    }

    private static void reset() {
        queue.clear();
        visited.clear();
        dragging = false;
        queueHandler = null;
    }

    // ================= Shift + протяжка (каждый кадр, из HandledScreenMixin) =================

    public static void render(class_465<?> screen) {
        class_310 mc = class_310.method_1551();
        if (!usable(mc, screen)) { reset(); return; }

        class_1703 h = screen.method_17577();
        if (queueHandler != h) { reset(); queueHandler = h; }

        long window = mc.method_22683().method_4490();
        boolean lmb = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean shift = shiftDown(window);
        boolean edge = lmb && !prevLmb;
        prevLmb = lmb;

        if (Config.scrollerDrag) {
            int id = focusedId(screen, h);
            if (edge && shift && id >= 0 && h.method_34255().method_7960()) {
                // нажатие: ванильный Shift-клик уже обработал этот слот, начинаем протяжку с него
                dragging = true;
                visited.clear();
                visited.add(id);
            } else if (dragging && (!lmb || !shift)) {
                dragging = false;
                visited.clear();
            }
            if (dragging && id >= 0 && visited.add(id) && h.field_7761.get(id).method_7681()) {
                queue.add(id);
            }
        } else {
            dragging = false;
        }

        // обработка очереди: не чаще, чем раз в «задержку»
        long now = System.currentTimeMillis();
        int delay = (int) Config.scrollerDelay;
        int budget = delay <= 0 ? 6 : 1;
        while (!queue.isEmpty() && budget-- > 0 && now - lastClick >= delay) {
            int id = queue.poll();
            if (id < 0 || id >= h.field_7761.size()) continue;
            if (!h.field_7761.get(id).method_7681()) continue;
            click(mc, h, id, 0, class_1713.field_7794);   // QUICK_MOVE
            lastClick = now;
        }
    }

    // ================= колесо мыши =================

    /** @return true, если прокрутка обработана (тогда экран её не получит). */
    public static boolean onScroll(class_465<?> screen, double vertical) {
        if (vertical == 0 || !Config.scrollerWheel) return false;
        class_310 mc = class_310.method_1551();
        if (!usable(mc, screen)) return false;

        class_1703 h = screen.method_17577();
        int id = focusedId(screen, h);
        if (id < 0) return false;
        int z = zone(mc, h, id);
        if (z < 0) return false;
        if (!h.method_34255().method_7960()) return false;            // в руке (на курсоре) что-то есть

        class_1735 slot = h.field_7761.get(id);
        class_1799 st = slot.method_7677();
        if (st.method_7960()) return false;

        long now = System.currentTimeMillis();
        if (now - lastClick < (long) Config.scrollerDelay) return true;   // слишком часто: глотаем прокрутку

        long window = mc.method_22683().method_4490();
        boolean send = (vertical < 0) != Config.scrollerInvert;

        if (send) {
            if (shiftDown(window)) {
                click(mc, h, id, 0, class_1713.field_7794);               // весь стак
            } else if (ctrlDown(window)) {
                List<class_1735> slots = h.field_7761;                    // все такие же предметы этого инвентаря - через очередь
                for (int i = 0; i < slots.size(); i++) {
                    if (zone(mc, h, i) != z) continue;
                    class_1799 o = slots.get(i).method_7677();
                    if (!o.method_7960() && class_1799.method_31577(o, st)) queue.add(i);
                }
            } else {
                if (!slot.method_7680(st)) return true;                   // служебные слоты (результат крафта) не трогаем
                int target = findTarget(mc, h, st, 1 - z, id);
                if (target >= 0) transferOne(mc, h, id, target);
            }
        } else {
            // подтянуть один такой же предмет в этот слот
            if (!slot.method_7680(st) || st.method_7947() >= st.method_7914()) return true;
            int src = findSource(mc, h, st, 1 - z);
            if (src >= 0) transferOne(mc, h, src, id);
        }
        lastClick = now;
        return true;
    }

    /** Один предмет из srcId в dstId: взять стак, правым кликом положить 1, остаток вернуть. */
    private static void transferOne(class_310 mc, class_1703 h, int srcId, int dstId) {
        click(mc, h, srcId, 0, class_1713.field_7790);   // PICKUP: взяли стак
        click(mc, h, dstId, 1, class_1713.field_7790);   // правый клик: положили 1 шт.
        click(mc, h, srcId, 0, class_1713.field_7790);   // вернули остаток
    }

    /** Куда положить один предмет: сначала недобранные такие же стаки, потом пустой слот. */
    private static int findTarget(class_310 mc, class_1703 h, class_1799 st, int zoneTo, int exclude) {
        List<class_1735> slots = h.field_7761;
        int empty = -1;
        for (int i = 0; i < slots.size(); i++) {
            if (i == exclude || zone(mc, h, i) != zoneTo) continue;
            class_1735 s = slots.get(i);
            if (!s.method_7680(st)) continue;
            class_1799 cur = s.method_7677();
            if (cur.method_7960()) {
                if (empty == -1) empty = i;
            } else if (class_1799.method_31577(cur, st) && cur.method_7947() < cur.method_7914()) {
                return i;
            }
        }
        return empty;
    }

    /** Откуда взять один такой же предмет: слот соседней зоны с тем же предметом. */
    private static int findSource(class_310 mc, class_1703 h, class_1799 st, int zoneFrom) {
        List<class_1735> slots = h.field_7761;
        for (int i = 0; i < slots.size(); i++) {
            if (zone(mc, h, i) != zoneFrom) continue;
            class_1735 s = slots.get(i);
            class_1799 cur = s.method_7677();
            if (!cur.method_7960() && class_1799.method_31577(cur, st) && s.method_7674(mc.field_1724)) return i;
        }
        return -1;
    }
}
