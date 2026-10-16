package com.example.visuals;

/** Пара предметов для AutoSwap: по клавише меняет предмет в левой руке на один из пары. */
public final class SwapPair {
    public final String a;
    public final String b;
    public int key;
    public boolean wasDown;

    public SwapPair(String a, String b, int key) {
        this.a = a;
        this.b = b;
        this.key = key;
    }

    public boolean has(String name) {
        return a.equalsIgnoreCase(name) || b.equalsIgnoreCase(name);
    }
}
