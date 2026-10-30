package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class SplashArt {
    private static final int GREEN = 0x7CE38B;

    private SplashArt() {}

    private static int argb(int alpha, int rgb) {
        int a = Math.max(1, Math.min(255, alpha));
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    private static int lerpRgb(int from, int to, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (int) (((from >> 16) & 255) + (((to >> 16) & 255) - ((from >> 16) & 255)) * t);
        int gr = (int) (((from >> 8) & 255) + (((to >> 8) & 255) - ((from >> 8) & 255)) * t);
        int b = (int) ((from & 255) + ((to & 255) - (from & 255)) * t);
        return (r << 16) | (gr << 8) | b;
    }

    private static float frac(float v) {
        return v - (float) Math.floor(v);
    }

    public static void draw(DrawContext g, MinecraftClient mc) {
        int w = mc.getWindow().getScaledWidth();
        int h = mc.getWindow().getScaledHeight();
        TextRenderer tr = mc.textRenderer;
        long now = System.currentTimeMillis();
        float t = (now % 1000000L) / 1000f;
        int cy = h / 2;

        // ===== Фон =====
        g.fillGradient(0, 0, w, h, 0xFF050806, 0xFF0F1D12);
        g.fillGradient(0, cy - 80, w, cy, argb(1, GREEN), argb(22, GREEN));
        g.fillGradient(0, cy, w, cy + 80, argb(22, GREEN), argb(1, GREEN));

        // ===== Частицы =====
        for (int i = 0; i < 40; i++) {
            float seed = i * 12.9898f;
            float fx = frac((float) Math.sin(seed) * 43758.5453f);
            float fy = frac((float) Math.sin(seed * 1.7f + 3.1f) * 24634.6345f);
            float speed = 0.02f + frac(fx * 7.3f) * 0.05f;
            float y01 = (fy - t * speed) % 1f;
            if (y01 < 0f) y01 += 1f;
            int px = (int) (fx * w + Math.sin(t * 0.5f + i) * 6.0);
            int py = (int) (y01 * h);
            int size = (i % 3 == 0) ? 2 : 1;
            int a = (int) (30 + 70 * (0.5f + 0.5f * Math.sin(t * 1.5f + i)));
            g.fill(px, py, px + size, py + size, argb(a, GREEN));
        }

        // ===== Уголки =====
        int m = 12, len = 16;
        int c = argb(110, GREEN);
        g.fill(m, m, m + len, m + 1, c);
        g.fill(m, m, m + 1, m + len, c);
        g.fill(w - m - len, m, w - m, m + 1, c);
        g.fill(w - m - 1, m, w - m, m + len, c);
        g.fill(m, h - m - 1, m + len, h - m, c);
        g.fill(m, h - m - len, m + 1, h - m, c);
        g.fill(w - m - len, h - m - 1, w - m, h - m, c);
        g.fill(w - m - 1, h - m - len, w - m, h - m, c);

        // ===== Заголовок (буквы переливаются волной) =====
        String title = "ARCHITEKT VISUALS";
        int spacing = 2;
        int totalW = 0;
        for (int i = 0; i < title.length(); i++) {
            totalW += tr.getWidth(String.valueOf(title.charAt(i)));
            if (i < title.length() - 1) totalW += spacing;
        }
        int tx = (w - totalW) / 2;
        int ty = cy - 22;
        g.fill(tx - 24, ty - 7, tx + totalW + 24, ty + 16, argb(12, GREEN));

        int x = tx;
        for (int i = 0; i < title.length(); i++) {
            String ch = String.valueOf(title.charAt(i));
            float s = (float) Math.sin(t * 2.2f - i * 0.45f);
            float wave = 0.5f + 0.5f * s;
            int yOff = Math.round(s * 1.5f);
            g.drawTextWithShadow(tr, ch, x, ty + yOff, argb(255, lerpRgb(0x4FBF62, 0xD6FFDC, wave)));
            x += tr.getWidth(ch) + spacing;
        }

        // Линия под заголовком (плавно затухает к краям)
        int lw = 140;
        int lx = (w - lw) / 2;
        int ly = ty + 18;
        for (int i = 0; i < lw; i++) {
            float k = 1f - Math.abs(i - lw / 2f) / (lw / 2f);
            g.fill(lx + i, ly, lx + i + 1, ly + 1, argb((int) (170 * k), GREEN));
        }

        // ===== Подзаголовок с точками =====
        int dots = (int) ((now / 400) % 4);
        int fullW = tr.getWidth("Loading...");
        g.drawTextWithShadow(tr, "Loading" + ".".repeat(dots), (w - fullW) / 2, cy + 8, argb(255, 0xA8B8AA));

        // ===== Полоса загрузки (бегущая комета) =====
        int barW = 220;
        int barH = 4;
        int bx = (w - barW) / 2;
        int by = cy + 28;
        g.fill(bx - 1, by - 1, bx + barW + 1, by + barH + 1, argb(255, 0x1F3A25));
        g.fill(bx, by, bx + barW, by + barH, 0xFF0B120C);

        float p = (now % 1800L) / 1800f;
        float ease = p * p * (3f - 2f * p);
        int tail = 90;
        int head = (int) (ease * (barW + tail));
        for (int i = 0; i < tail; i += 2) {
            int px = head - i;
            if (px < 0 || px >= barW) continue;
            int a = 255 - (i * 255 / tail);
            g.fill(bx + px, by, bx + Math.min(px + 2, barW), by + barH, argb(a, GREEN));
        }

        // ===== Подсказка =====
        String hint = "Right Shift - меню";
        g.drawTextWithShadow(tr, hint, (w - tr.getWidth(hint)) / 2, h - 28, argb(150, 0x8FA394));

        // ===== Нижняя линия с бегущим бликом =====
        g.fill(0, h - 2, w, h, argb(60, GREEN));
        int hx = (int) (((now % 3000L) / 3000f) * (w + 200)) - 200;
        int left = Math.max(0, hx);
        int right = Math.min(w, hx + 200);
        if (right > left) {
            g.fill(left, h - 2, right, h, argb(255, GREEN));
        }
    }
}
