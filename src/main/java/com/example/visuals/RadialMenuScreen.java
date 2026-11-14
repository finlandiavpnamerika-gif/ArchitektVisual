package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Круговое меню макросов.
 * Зажми клавишу - появится колесо, наведи мышь на сектор и отпусти клавишу: команда выполнится.
 * Отпустил в центре - просто закрылось. Быстрый тап (меньше 0.25 с) оставляет колесо открытым:
 * тогда выбор - кликом, закрытие - Esc или повторным нажатием клавиши.
 *
 * Рисование: кольцо строится один раз в набор горизонтальных полос (с запасом на сглаживание краёв),
 * дальше каждый кадр полосы только перекрашиваются - градиент, свечение наведённого сектора и т.д.
 */
public class RadialMenuScreen extends Screen {
    private static final long TAP_MS = 250;
    private static final double GAP = 3.0;       // зазор между секторами в пикселях
    private static final int BANDS = 6;          // ступеней градиента от центра к краю

    // виды пикселей кольца
    private static final int K_BODY = 0, K_RIM = 1, K_HUB_RIM = 2, K_ARC = 3, K_DECOR = 4;

    private final long openedAt = System.currentTimeMillis();
    private boolean sticky = false;
    private boolean keyWasDown = true;
    private boolean mouseDown, clicked, prevMouseDown = true;

    // кэш кольца: полосы по 4 числа (x, y, длина, упакованный вид), координаты от левого верхнего угла
    private int[] runs = new int[0];
    private int runCount = 0;
    private int ext = 0;
    private int mapN = -1, mapR = -1, mapR0 = -1;

    // плавное наведение: 0..1 на каждый сектор
    private float[] hv = new float[0];
    private long lastFrame = 0;

    public RadialMenuScreen() {
        super(Text.literal("Radial"));
    }

    @Override
    protected void init() {
        super.init();
        // вернулись из редактора (или открыли командой /radial): клавиша не зажата -> режим клика
        long window = MinecraftClient.getInstance().getWindow().getHandle();
        boolean d = Config.radialKey != -1 && Keys.down(window, Config.radialKey);
        if (!d) sticky = true;
        keyWasDown = d;
    }

    // ---------- геометрия ----------
    private int radius() {
        return Math.max(60, Math.min(112, Math.min(width, height) / 2 - 40));
    }

    private int centerY() {
        return height / 2 - 6;
    }

    private static int sectorAt(double dx, double dy, int n) {
        double sa = 2 * Math.PI / n;
        double a = Math.atan2(dx, -dy);           // 0 = вверх, по часовой
        if (a < 0) a += 2 * Math.PI;
        double sh = a + sa / 2;                   // сектор 0 центрирован на верхней точке
        if (sh >= 2 * Math.PI) sh -= 2 * Math.PI;
        return Math.min(n - 1, (int) (sh / sa));
    }

    private static double clamp01(double v) { return v < 0 ? 0 : Math.min(v, 1); }

    private static int pack(int kind, int level, int band, int idx) {
        return (kind << 14) | (level << 12) | (band << 8) | idx;
    }

    private static int level(double cov) {
        if (cov >= 0.8) return 0;
        if (cov >= 0.5) return 1;
        if (cov >= 0.22) return 2;
        return -1;
    }

    private void buildMap(int n, int R, int r0) {
        int Re = R + 10;
        int size = 2 * Re;
        double sa = 2 * Math.PI / n;
        int[] out = new int[1024];
        int cnt = 0;
        int[] row = new int[size];
        double bodyLo = r0 + 2, bodyHi = R, rimFrom = R - 3;
        for (int j = 0; j < size; j++) {
            double dy = j - Re + 0.5;
            for (int i = 0; i < size; i++) {
                double dx = i - Re + 0.5;
                double d = Math.sqrt(dx * dx + dy * dy);
                int v = -1;
                if (d >= r0 - 2.5 && d <= r0 + 0.5) {
                    double cov = clamp01(Math.min(d - (r0 - 2), r0 - d) + 0.5);
                    int lv = level(cov);
                    if (lv >= 0) v = pack(K_HUB_RIM, lv, 0, 0);
                } else if (d >= bodyLo - 0.5 && d <= bodyHi + 0.5) {
                    double cov = clamp01(Math.min(d - bodyLo, bodyHi - d) + 0.5);
                    double a = Math.atan2(dx, -dy);
                    if (a < 0) a += 2 * Math.PI;
                    double sh = a + sa / 2;
                    if (sh >= 2 * Math.PI) sh -= 2 * Math.PI;
                    int idx = Math.min(n - 1, (int) (sh / sa));
                    if (n > 1) {
                        double loc = sh - idx * sa;
                        double edge = Math.min(loc, sa - loc) * d;      // расстояние до границы сектора, px
                        cov = Math.min(cov, clamp01(edge - GAP / 2 + 0.5));
                    }
                    int lv = level(cov);
                    if (lv >= 0) {
                        if (d >= rimFrom) v = pack(K_RIM, lv, 0, idx);
                        else {
                            int band = (int) ((d - bodyLo) / (rimFrom - bodyLo) * BANDS);
                            v = pack(K_BODY, lv, Math.max(0, Math.min(BANDS - 1, band)), idx);
                        }
                    }
                } else if (d >= R + 2.5 && d <= R + 6) {
                    // внешняя дуга-акцент наведённого сектора
                    double cov = clamp01(Math.min(d - (R + 3), R + 5.5 - d) + 0.5);
                    double a = Math.atan2(dx, -dy);
                    if (a < 0) a += 2 * Math.PI;
                    double sh = a + sa / 2;
                    if (sh >= 2 * Math.PI) sh -= 2 * Math.PI;
                    int idx = Math.min(n - 1, (int) (sh / sa));
                    if (n > 1) {
                        double loc = sh - idx * sa;
                        double edge = Math.min(loc, sa - loc) * d;
                        cov = Math.min(cov, clamp01(edge - GAP / 2 - 1 + 0.5));
                    }
                    int lv = level(cov);
                    if (lv >= 0) v = pack(K_ARC, lv, 0, idx);
                } else if (d >= R + 8.5 && d <= R + 10) {
                    double cov = clamp01(Math.min(d - (R + 9), R + 9.5 - d) + 0.5);
                    int lv = level(cov);
                    if (lv >= 0) v = pack(K_DECOR, lv, 0, 0);
                }
                row[i] = v;
            }
            // склеиваем одинаковые пиксели подряд в полосы
            int i = 0;
            while (i < size) {
                int v = row[i];
                if (v < 0) { i++; continue; }
                int k = i + 1;
                while (k < size && row[k] == v) k++;
                if (cnt + 4 > out.length) out = java.util.Arrays.copyOf(out, out.length * 2);
                out[cnt++] = i; out[cnt++] = j; out[cnt++] = k - i; out[cnt++] = v;
                i = k;
            }
        }
        runs = out;
        runCount = cnt / 4;
        ext = Re;
        mapN = n; mapR = R; mapR0 = r0;
    }

    // ---------- цвет ----------
    private static int mix(int a, int b, double t) {
        t = clamp01(t);
        int aa = (int) (((a >>> 24) & 255) + (((b >>> 24) & 255) - ((a >>> 24) & 255)) * t);
        int ar = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
        int ag = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
        int ab = (int) ((a & 255) + ((b & 255) - (a & 255)) * t);
        return (aa << 24) | (ar << 16) | (ag << 8) | ab;
    }

    private static int argb(int rgb, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0xFFFFFF);
    }

    private static int scaleAlpha(int c, double m) {
        int a = (int) (((c >>> 24) & 255) * m);
        return (a << 24) | (c & 0xFFFFFF);
    }

    private boolean hit(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    // ---------- выбор ----------
    private void choose(int idx) {
        List<RadialItem> items = Config.radialItems;
        if (idx < 0 || idx >= items.size()) return;
        String text = items.get(idx).text;
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.setScreen(null);
        Macros.run(mc, text);
    }

    // ---------- отрисовка ----------
    @Override
    public void render(DrawContext g, int mouseX, int mouseY, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        long window = mc.getWindow().getHandle();
        long now = System.currentTimeMillis();

        mouseDown = GLFW.glfwGetMouseButton(window, 0) == GLFW.GLFW_PRESS;
        clicked = mouseDown && !prevMouseDown;
        prevMouseDown = mouseDown;

        List<RadialItem> items = Config.radialItems;
        int n = items.size();
        int nn = Math.max(1, n);
        int R = radius();
        int r0 = Math.max(26, (int) (R * 0.36));
        int cx = width / 2, cy = centerY();

        double p = Math.min(1.0, (now - openedAt) / 220.0);
        double ease = 1 - (1 - p) * (1 - p) * (1 - p);
        double fade = Math.min(1.0, p * 1.6);

        // кнопка "Редактировать" внизу
        int bw = 150, bh = 20;
        int bx = (width - bw) / 2, by = height - bh - 8;
        boolean overBtn = hit(mouseX, mouseY, bx, by, bw, bh);

        // наведённый сектор
        int hovered = -1;
        double dx = mouseX - cx, dy = mouseY - cy;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (n > 0 && dist >= r0 && !overBtn) hovered = sectorAt(dx, dy, n);

        // ---- клавиша: отпускание / повторное нажатие ----
        boolean keyDown = Config.radialKey != -1 && Keys.down(window, Config.radialKey);
        if (!keyDown && keyWasDown) {
            keyWasDown = false;
            if (!sticky) {
                if (now - openedAt < TAP_MS) {
                    sticky = true;                       // быстрый тап: колесо остаётся
                } else if (hovered >= 0) {
                    choose(hovered);
                    return;
                } else if (overBtn) {
                    sticky = true;                       // отпустил над кнопкой: остаёмся, чтобы нажать
                } else {
                    close();
                    return;
                }
            }
        } else if (keyDown && !keyWasDown) {
            keyWasDown = true;
            if (sticky) { close(); return; }             // повторное нажатие закрывает
        }

        // ---- клик ----
        if (clicked) {
            if (overBtn) {
                mc.setScreen(new RadialEditScreen(this));
                return;
            }
            if (hovered >= 0) {
                choose(hovered);
                return;
            }
        }

        // ---- плавное наведение ----
        if (hv.length != nn) hv = new float[nn];
        float k = lastFrame == 0 ? 1f : (float) Math.min(1.0, (now - lastFrame) / 80.0);
        lastFrame = now;
        float maxHv = 0;
        for (int i = 0; i < nn; i++) {
            float target = i == hovered ? 1f : 0f;
            hv[i] += (target - hv[i]) * k;
            maxHv = Math.max(maxHv, hv[i]);
        }

        // ---- фон ----
        g.fillGradient(0, 0, width, height,
                argb(0x000000, (int) (0x58 * fade)), argb(0x000000, (int) (0xA0 * fade)));

        // ---- мягкая тень под колесом ----
        for (int s = 0; s < 3; s++) {
            int sr = (int) ((R + 20 - s * 6) * (0.6 + 0.4 * ease));
            int sc = argb(0x000000, (int) (0x1A * fade));
            for (int y = -sr; y < sr; y++) {
                double yy = y + 0.5;
                int half = (int) Math.round(Math.sqrt(Math.max(0, sr * sr - yy * yy)));
                g.fill(cx - half, cy + y, cx + half, cy + y + 1, sc);
            }
        }

        // ---- кольцо ----
        if (mapN != nn || mapR != R || mapR0 != r0 || runCount == 0) buildMap(nn, R, r0);

        int accent = Theme.ACCENT;
        int base = Theme.BG & 0xFFFFFF;
        // цвета: [сектор][ступень градиента]
        int[][] body = new int[nn][BANDS];
        int[] rim = new int[nn];
        int[] arc = new int[nn];
        for (int i = 0; i < nn; i++) {
            double h = n == 0 ? 0 : hv[i];
            for (int b = 0; b < BANDS; b++) {
                double t = b / (double) (BANDS - 1);
                int normal = argb(mix(argb(base, 255), argb(accent, 255), 0.04 + 0.12 * t) & 0xFFFFFF, 0xB8 + (int) (14 * t));
                int hot = argb(mix(argb(accent, 255), argb(0x000000, 255), 0.36 - 0.18 * t) & 0xFFFFFF, 0xA8 + (int) (34 * t));
                body[i][b] = scaleAlpha(mix(normal, hot, h), n == 0 ? 0.6 : 1.0);
            }
            rim[i] = mix(argb(accent, 0x58), argb(accent, 0xFF), h);
            arc[i] = argb(accent, (int) (0xD8 * h));
        }
        int hubRim = argb(accent, (int) (0x60 + 0x70 * maxHv));
        int decor = argb(accent, 0x34);
        double[] lvMul = {1.0, 0.62, 0.30};

        int ox = cx - ext, oy = cy - ext;
        double reveal = ext * ease;
        for (int r = 0; r < runCount; r++) {
            int x = runs[r * 4], y = runs[r * 4 + 1], len = runs[r * 4 + 2], v = runs[r * 4 + 3];
            if (p < 1.0) {
                double mxr = x + len / 2.0 - ext, myr = y - ext + 0.5;
                if (Math.sqrt(mxr * mxr + myr * myr) > reveal) continue;
            }
            int kind = (v >> 14) & 7, lv = (v >> 12) & 3, band = (v >> 8) & 15, idx = v & 0xFF;
            int c;
            switch (kind) {
                case K_BODY -> c = body[Math.min(idx, nn - 1)][band];
                case K_RIM -> c = rim[Math.min(idx, nn - 1)];
                case K_HUB_RIM -> c = hubRim;
                case K_ARC -> c = arc[Math.min(idx, nn - 1)];
                default -> c = decor;
            }
            c = scaleAlpha(c, lvMul[lv] * fade);
            if ((c >>> 24) < 3) continue;
            g.fill(ox + x, oy + y, ox + x + len, oy + y + 1, c);
        }

        // ---- центр (хаб) ----
        int hr = r0 - 3;
        int cHubA = argb(Theme.BG & 0xFFFFFF, (int) (0xE4 * fade));
        for (int y = -hr; y < hr; y++) {
            double yy = y + 0.5;
            int half = (int) Math.round(Math.sqrt(Math.max(0, hr * hr - yy * yy)));
            g.fill(cx - half, cy + y, cx + half, cy + y + 1, cHubA);
        }
        int hubW = hr * 2 - 8;
        int tTxt = argb(Theme.TEXT, (int) Math.max(0x10, 255 * fade));
        int tDim = argb(Theme.TEXT_DIM, (int) Math.max(0x10, 255 * fade));
        if (hovered >= 0) {
            RadialItem it = items.get(hovered);
            if (it.label.isEmpty()) {
                g.drawCenteredTextWithShadow(textRenderer, textRenderer.trimToWidth(it.text, hubW), cx, cy - 4, tTxt);
            } else {
                g.drawCenteredTextWithShadow(textRenderer, textRenderer.trimToWidth(it.label, hubW), cx, cy - 10, tTxt);
                g.drawCenteredTextWithShadow(textRenderer, textRenderer.trimToWidth(it.text, hubW), cx, cy + 2, tDim);
            }
        } else if (n == 0) {
            g.drawCenteredTextWithShadow(textRenderer, "Пусто", cx, cy - 4, tDim);
        } else {
            g.drawCenteredTextWithShadow(textRenderer, "Выбери", cx, cy - 4, tDim);
        }

        // точка-указатель на краю хаба: куда смотрит курсор
        if (n > 0 && dist > 1 && !overBtn) {
            int px2 = cx + (int) Math.round(dx / dist * (hr - 3));
            int py2 = cy + (int) Math.round(dy / dist * (hr - 3));
            g.fill(px2 - 1, py2 - 1, px2 + 2, py2 + 2, argb(accent, (int) (0xFF * fade)));
        }

        // ---- подписи секторов ----
        if (n > 0) {
            double sa = 2 * Math.PI / n;
            double rm = (R + r0) / 2.0;
            int maxW = (int) Math.max(34, Math.min(86, 2 * rm * Math.sin(Math.min(sa / 2, Math.PI / 2)) - 8));
            for (int i = 0; i < n; i++) {
                double a = i * sa;
                double rr = rm + 3 * hv[i];                       // наведённый сектор чуть выдвигает подпись
                int lx = cx + (int) Math.round(Math.sin(a) * rr);
                int ly = cy - (int) Math.round(Math.cos(a) * rr);
                String t = textRenderer.trimToWidth(items.get(i).title(), maxW);
                int col = mix(tDim, tTxt, hv[i]);
                g.drawCenteredTextWithShadow(textRenderer, t, lx, ly - 4, col);
            }
        } else {
            g.drawCenteredTextWithShadow(textRenderer, "Добавь команды кнопкой снизу", cx, cy - R - 18, tDim);
        }

        // ---- подсказка сверху ----
        String key = Keys.name(Config.radialKey);
        String hint = sticky
                ? "Клик по сектору - выполнить · Esc / [" + key + "] - закрыть"
                : "Отпусти [" + key + "] на секторе - выполнить · в центре - отмена";
        g.drawCenteredTextWithShadow(textRenderer, hint, cx, 10, tDim);

        // ---- кнопка "Редактировать" ----
        g.fill(bx + 1, by + 2, bx + bw + 1, by + bh + 2, argb(0x000000, (int) (0x40 * fade)));
        g.fill(bx + 1, by, bx + bw - 1, by + bh, overBtn ? argb(accent, 0x60) : argb(Theme.BG & 0xFFFFFF, (int) (0xC0 * fade)));
        g.fill(bx, by + 1, bx + 1, by + bh - 1, overBtn ? argb(accent, 0x60) : argb(Theme.BG & 0xFFFFFF, (int) (0xC0 * fade)));
        g.fill(bx + bw - 1, by + 1, bx + bw, by + bh - 1, overBtn ? argb(accent, 0x60) : argb(Theme.BG & 0xFFFFFF, (int) (0xC0 * fade)));
        g.fill(bx + 1, by, bx + bw - 1, by + 1, argb(accent, (int) (0xFF * fade)));
        g.drawCenteredTextWithShadow(textRenderer, "✎ Редактировать колесо", cx, by + 6, tTxt);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(null);
    }

    @Override
    public boolean shouldPause() { return false; }
}
