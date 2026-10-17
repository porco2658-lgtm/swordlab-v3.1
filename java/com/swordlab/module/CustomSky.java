package com.swordlab.module;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

/** Best effort: overrides the sky color through a mixin that may not match every 1.21.x build. */
public class CustomSky extends Module {
    private final Setting red = add(Setting.number("Red", 150, 0, 255, 5));
    private final Setting green = add(Setting.number("Green", 60, 0, 255, 5));
    private final Setting blue = add(Setting.number("Blue", 255, 0, 255, 5));
    private final Setting rainbow = add(Setting.bool("Rainbow", false));

    public CustomSky() {
        super("Custom Sky", "Recolors the sky (experimental)", Category.VISUALS);
    }

    @Override
    public void onTick(Minecraft mc) {}

    public int color() {
        if (rainbow.bool) {
            return 0xFF000000 | Mth.hsvToRgb((System.currentTimeMillis() % 10000L) / 10000f, 0.6f, 1f);
        }
        return 0xFF000000 | (red.asInt() << 16) | (green.asInt() << 8) | blue.asInt();
    }
}
