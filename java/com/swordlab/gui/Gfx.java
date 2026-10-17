package com.swordlab.gui;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;

/** 2D helpers: anti-aliased rounded rects / borders, color lerp. */
public final class Gfx {
    private static final Map<Integer, float[][]> FILL = new HashMap<>();
    private static final Map<Integer, float[][]> RING = new HashMap<>();

    private Gfx() {}

    public static int lerp(int a, int b, float t) {
        int out = 0;
        for (int s = 24; s >= 0; s -= 8) {
            int ca = (a >>> s) & 255, cb = (b >>> s) & 255;
            out |= ((int) (ca + (cb - ca) * t) & 255) << s;
        }
        return out;
    }

    /** Pixel coverage (0-1) of the top-left corner circle of radius rad, pixel (i, j) inside the r x r corner square. */
    private static float cov(int r, int i, int j, double rad) {
        double dx = r - i - 0.5, dy = r - j - 0.5;
        double d = rad - Math.sqrt(dx * dx + dy * dy) + 0.5;
        return (float) Math.max(0, Math.min(1, d));
    }

    private static float[][] fillTable(int r) {
        return FILL.computeIfAbsent(r, k -> {
            float[][] t = new float[k][k];
            for (int j = 0; j < k; j++) for (int i = 0; i < k; i++) t[j][i] = cov(k, i, j, k);
            return t;
        });
    }

    private static float[][] ringTable(int r) {
        return RING.computeIfAbsent(r, k -> {
            float[][] t = new float[k][k];
            for (int j = 0; j < k; j++)
                for (int i = 0; i < k; i++) t[j][i] = Math.max(0f, cov(k, i, j, k) - cov(k, i, j, k - 1));
            return t;
        });
    }

    private static int withAlpha(int c, float f) {
        int a = (int) (((c >>> 24) & 255) * f);
        return (a << 24) | (c & 0xFFFFFF);
    }

    private static int clampR(int x1, int y1, int x2, int y2, int r) {
        return Math.max(0, Math.min(r, Math.min(x2 - x1, y2 - y1) / 2));
    }

    /** Vertical-gradient rounded rect with anti-aliased corners. */
    public static void rr(GuiGraphics g, int x1, int y1, int x2, int y2, int r, int top, int bot) {
        int h = y2 - y1, w = x2 - x1;
        if (h <= 0 || w <= 0) return;
        r = clampR(x1, y1, x2, y2, r);
        float[][] t = r > 0 ? fillTable(r) : null;
        boolean solid = top == bot;
        if (solid) g.fill(x1, y1 + r, x2, y2 - r, top); // whole middle in one call

        for (int row = 0; row < h; row++) {
            int j = row < r ? row : (row >= h - r ? h - 1 - row : -1);
            if (j < 0) {
                if (!solid) g.fill(x1, y1 + row, x2, y1 + row + 1, lerp(top, bot, h <= 1 ? 0f : (float) row / (h - 1)));
                continue;
            }
            int c = solid ? top : lerp(top, bot, h <= 1 ? 0f : (float) row / (h - 1));
            float[] cv = t[j];
            int k = 0;
            while (k < r && cv[k] < 0.999f) k++;
            g.fill(x1 + k, y1 + row, x2 - k, y1 + row + 1, c);
            for (int i = 0; i < k; i++) {
                if (cv[i] < 0.02f) continue;
                int pc = withAlpha(c, cv[i]);
                g.fill(x1 + i, y1 + row, x1 + i + 1, y1 + row + 1, pc);
                g.fill(x2 - 1 - i, y1 + row, x2 - i, y1 + row + 1, pc);
            }
        }
    }

    public static void rr(GuiGraphics g, int x1, int y1, int x2, int y2, int r, int color) {
        rr(g, x1, y1, x2, y2, r, color, color);
    }

    /** 1px anti-aliased rounded border. */
    public static void border(GuiGraphics g, int x1, int y1, int x2, int y2, int r, int c) {
        if (x2 - x1 <= 0 || y2 - y1 <= 0) return;
        r = clampR(x1, y1, x2, y2, r);
        g.fill(x1 + r, y1, x2 - r, y1 + 1, c);
        g.fill(x1 + r, y2 - 1, x2 - r, y2, c);
        g.fill(x1, y1 + r, x1 + 1, y2 - r, c);
        g.fill(x2 - 1, y1 + r, x2, y2 - r, c);
        if (r == 0) return;
        float[][] t = ringTable(r);
        for (int j = 0; j < r; j++) {
            for (int i = 0; i < r; i++) {
                float v = t[j][i];
                if (v < 0.02f) continue;
                int pc = withAlpha(c, v);
                g.fill(x1 + i, y1 + j, x1 + i + 1, y1 + j + 1, pc);
                g.fill(x2 - 1 - i, y1 + j, x2 - i, y1 + j + 1, pc);
                g.fill(x1 + i, y2 - 1 - j, x1 + i + 1, y2 - j, pc);
                g.fill(x2 - 1 - i, y2 - 1 - j, x2 - i, y2 - j, pc);
            }
        }
    }
}
