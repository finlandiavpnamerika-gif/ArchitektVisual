package com.example.visuals;

import net.minecraft.class_310;
import net.minecraft.class_332;

/**
 * Один перетаскиваемый HUD-элемент.
 * draw() рисует в локальных координатах (0,0) и возвращает {ширина, высота};
 * сдвиг и масштаб накладывает HudManager.
 */
public abstract class HudElement {
    public final String name;

    /** Границы на экране после масштаба (в GUI-координатах), обновляются каждый кадр. */
    public int bx, by, bw, bh;
    public boolean visible;

    protected HudElement(String name) { this.name = name; }

    public abstract boolean enabled();
    public abstract int x();
    public abstract int y();
    public abstract void setPos(int x, int y);
    public abstract double scale();
    public abstract double alpha();

    /** @return {w, h} или null, если рисовать нечего */
    public abstract int[] draw(class_332 g, class_310 mc, float alpha);

    public boolean contains(int mx, int my) {
        return mx >= bx && mx < bx + bw && my >= by && my < by + bh;
    }
}
