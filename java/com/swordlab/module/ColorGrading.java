package com.swordlab.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Full-screen color tint and vignette drawn over the game (lite color grading). */
public class ColorGrading extends Module {
    private final Setting preset = add(Setting.mode("Preset", 0, "Violet", "Warm", "Cool", "Sunset", "Noir", "Toxic"));
    private final Setting tint = add(Setting.number("Tint", 18, 0, 60, 1));
    private final Setting vignette = add(Setting.number("Vignette", 40, 0, 100, 5));

    public ColorGrading() {
        super("Color Grading", "Screen tint + vignette presets", Category.VISUALS);
    }

    @Override
    public void onTick(Minecraft mc) {}

    private int rgb() {
        return switch (preset.mode) {
            case 1 -> 0xFF9A3C;
            case 2 -> 0x3C8CFF;
            case 3 -> 0xFF4D7A;
            case 4 -> 0x101010;
            case 5 -> 0x6BFF3C;
            default -> 0x9B4DFF;
        };
    }

    public void render(GuiGraphics g, Minecraft mc) {
        if (!enabled || mc.options.hideGui) return;
        int w = g.guiWidth(), h = g.guiHeight();
        int c = rgb();
        int ta = (int) (tint.value / 100.0 * 255);
        if (ta > 0) g.fill(0, 0, w, h, (ta << 24) | c);
        int steps = 14;
        double vmax = vignette.value / 100.0;
        if (vmax <= 0) return;
        for (int i = 0; i < steps; i++) {
            float t = 1f - (float) i / steps;
            int a = (int) (vmax * t * t * 150);
            if (a <= 0) continue;
            int col = (a << 24) | (preset.mode == 4 ? 0x000000 : (c & 0x3F3F3F));
            int ox = Math.round(w * 0.012f * i), oy = Math.round(h * 0.012f * i);
            g.fill(ox, oy, ox + Math.max(1, w / 90), h - oy, col);
            g.fill(w - ox - Math.max(1, w / 90), oy, w - ox, h - oy, col);
            g.fill(ox, oy, w - ox, oy + Math.max(1, h / 90), col);
            g.fill(ox, h - oy - Math.max(1, h / 90), w - ox, h - oy, col);
        }
    }
}
