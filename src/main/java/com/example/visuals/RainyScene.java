package com.example.visuals;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public final class RainyScene {
    private static final Identifier BG = Identifier.of("visualsmod", "textures/gui/menu_bg.png");
    private static final int TEX_W = 1600;
    private static final int TEX_H = 900;

    // Редкая далёкая вспышка молнии. Не нравится - поставьте false
    private static final boolean LIGHTNING = true;

    private RainyScene() {}

    private static int argb(int alpha, int rgb) {
        int a = Math.max(1, Math.min(255, alpha));
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    private static float rnd(int n) {
        int x = n * 0x9E3779B1;
        x ^= x >>> 15;
        x *= 0x85EBCA6B;
        x ^= x >>> 13;
        return (x & 0xFFFFFF) / (float) 0x1000000;
    }

    // Полная сцена для главного меню
    public static void drawScene(DrawContext g, int w, int h) {
        float t = (System.currentTimeMillis() % 3600000L) / 1000f;
        drawBackground(g, w, h, t);
        g.fillGradient(0, 0, w, h, argb(60, 0x0A1018), argb(150, 0x04070A));
        drawFog(g, w, h, t);
        rain(g, w, h, t, 0, 90, 5, 8, 240f, 320f, 60, 0xAFC3D1);
        rain(g, w, h, t, 5000, 50, 9, 14, 520f, 700f, 100, 0xD2E0EA);
        if (LIGHTNING) lightning(g, w, h, t);
        vignette(g, w, h);
    }

    // Только дождь (для мод-меню), k - сила от 0 до 1
    public static void drawRain(DrawContext g, int w, int h, float k) {
        float t = (System.currentTimeMillis() % 3600000L) / 1000f;
        rain(g, w, h, t, 0, (int) (70 * k), 5, 8, 240f, 320f, (int) (60 * k), 0xAFC3D1);
        rain(g, w, h, t, 5000, (int) (40 * k), 9, 14, 520f, 700f, (int) (100 * k), 0xD2E0EA);
    }

    private static void drawBackground(DrawContext g, int w, int h, float t) {
        // Картинка с запасом по краям: камера медленно плывёт и «дышит»
        float zoom = 1.12f;
        float sa = (float) w / (float) h;
        float ta = (float) TEX_W / (float) TEX_H;
        float rw, rh;
        if (sa > ta) {
            rw = TEX_W / zoom;
            rh = rw / sa;
        } else {
            rh = TEX_H / zoom;
            rw = rh * sa;
        }
        float freeX = TEX_W - rw;
        float freeY = TEX_H - rh;
        float px = 0.5f + 0.42f * (float) Math.sin(t * 0.045f);
        float py = 0.5f + 0.35f * (float) Math.sin(t * 0.031f + 1.3f);
        g.drawTexture(RenderPipelines.GUI_TEXTURED, BG, 0, 0,
                freeX * px, freeY * py, w, h,
                Math.round(rw), Math.round(rh), TEX_W, TEX_H);
    }

    private static void drawFog(DrawContext g, int w, int h, float t) {
        for (int i = 0; i < 6; i++) {
            float speed = 5f + rnd(i * 31 + 7) * 9f;
            int bandW = (int) (w * (0.6f + rnd(i * 17 + 3) * 0.5f));
            float travel = w + bandW;
            int x0 = (int) ((t * speed + rnd(i * 5 + 1) * travel) % travel) - bandW;
            int yc = (int) (h * (0.30f + rnd(i * 13 + 9) * 0.60f));
            int maxA = 12 + (int) (rnd(i * 3 + 2) * 14);
            for (int k = -5; k <= 5; k++) {
                float f = 1f - Math.abs(k) / 6f;
                int sw = (int) (bandW * (0.5f + 0.5f * f));
                int sx = x0 + (bandW - sw) / 2;
                int sy = yc + k * 6;
                int e = Math.max(2, sw / 5);
                int a = (int) (maxA * f);
                g.fill(sx, sy, sx + e, sy + 6, argb(a / 3, 0xB8C4CC));
                g.fill(sx + e, sy, sx + sw - e, sy + 6, argb(a, 0xB8C4CC));
                g.fill(sx + sw - e, sy, sx + sw, sy + 6, argb(a / 3, 0xB8C4CC));
            }
        }
    }

    private static void rain(DrawContext g, int w, int h, float t, int seed, int count,
                             int minLen, int maxLen, float minSpeed, float maxSpeed,
                             int alpha, int rgb) {
        int wrapW = w + 60;
        for (int i = 0; i < count; i++) {
            int s = seed + i * 13;
            float speed = minSpeed + rnd(s + 1) * (maxSpeed - minSpeed);
            int len = minLen + (int) (rnd(s + 2) * (maxLen - minLen));
            float travel = h + len;
            float yf = (t * speed + rnd(s + 3) * travel) % travel;
            int y = (int) yf - len;
            int baseX = (int) (rnd(s + 4) * wrapW);
            int x = baseX - (int) (yf * 0.14f);
            x = ((x % wrapW) + wrapW) % wrapW - 30;
            int seg = Math.max(2, len / 3);
            int a = (int) (alpha * (0.6f + 0.4f * rnd(s + 5)));
            int c = argb(a, rgb);
            for (int j = 0; j < 3; j++) {
                g.fill(x - j, y + j * seg, x - j + 1, y + (j + 1) * seg, c);
            }
        }
    }

    private static void lightning(DrawContext g, int w, int h, float t) {
        float c = t % 19f;
        float f = 0f;
        if (c > 16f && c < 16.14f) f = 1f - (c - 16f) / 0.14f;
        else if (c > 16.28f && c < 16.5f) f = 0.7f * (1f - (c - 16.28f) / 0.22f);
        if (f > 0f) g.fill(0, 0, w, h, argb((int) (34 * f), 0xD6E4FF));
    }

    private static void vignette(DrawContext g, int w, int h) {
        g.fillGradient(0, 0, w, h / 3, argb(150, 0x000000), argb(1, 0x000000));
        g.fillGradient(0, h * 2 / 3, w, h, argb(1, 0x000000), argb(170, 0x000000));
        for (int i = 0; i < 16; i++) {
            float f = 1f - i / 16f;
            int a = (int) (120 * f * f);
            g.fill(i * 4, 0, i * 4 + 4, h, argb(a, 0x000000));
            g.fill(w - i * 4 - 4, 0, w - i * 4, h, argb(a, 0x000000));
        }
    }
}
