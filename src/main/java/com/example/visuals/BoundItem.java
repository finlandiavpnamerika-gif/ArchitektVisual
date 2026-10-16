package com.example.visuals;

/** Предмет для ItemBind: по клавише берётся в руку, используется и возвращается обратно. */
public final class BoundItem {
    public final String name;
    public int key;
    public boolean wasDown;

    public BoundItem(String name, int key) {
        this.name = name;
        this.key = key;
    }
}
