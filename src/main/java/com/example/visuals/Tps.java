package com.example.visuals;

/** Оценка TPS сервера по интервалу между пакетами обновления времени (раз в 20 тиков). */
public final class Tps {
    private Tps() {}

    private static final double[] S = new double[5];
    private static int n = 0, idx = 0;
    private static long last = 0;

    /** Вызывается миксином при каждом пакете времени мира. */
    public static void onPacket() {
        long now = System.currentTimeMillis();
        if (last != 0) {
            long dt = now - last;
            if (dt > 0 && dt < 10000) {
                S[idx] = Math.min(20.0, 20000.0 / dt);
                idx = (idx + 1) % S.length;
                if (n < S.length) n++;
            }
        }
        last = now;
    }

    public static void reset() { n = 0; idx = 0; last = 0; }

    /** Среднее TPS или -1, пока данных нет. */
    public static double get() {
        if (n == 0) return -1;
        double s = 0;
        for (int i = 0; i < n; i++) s += S[i];
        return s / n;
    }
}
