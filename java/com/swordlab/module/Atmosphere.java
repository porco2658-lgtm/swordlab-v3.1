package com.swordlab.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;

/** Ambient sky/air effects around you: drifting stars, aurora ribbons, or fireflies (particle based). */
public class Atmosphere extends Module {
    private final Setting style = add(Setting.mode("Style", 0, "Stars", "Aurora", "Fireflies", "Snow dust"));
    private final Setting density = add(Setting.number("Density", 4, 1, 16, 1));
    private final Setting color = add(Setting.mode("Color", 0, Fx.COLORS));
    private final Setting radius = add(Setting.number("Radius", 18, 6, 32, 1));

    public Atmosphere() {
        super("Atmosphere", "Stars, aurora or fireflies around you", Category.VISUALS);
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null) return;
        RandomSource r = p.getRandom();
        double R = radius.value;
        for (int i = 0; i < density.asInt(); i++) {
            double a = r.nextDouble() * Math.PI * 2, d = Math.sqrt(r.nextDouble()) * R;
            double x = p.getX() + Math.cos(a) * d, z = p.getZ() + Math.sin(a) * d;
            switch (style.mode) {
                case 0 -> Fx.spawn(mc, ParticleTypes.END_ROD, x, p.getY() + 14 + r.nextDouble() * 12, z,
                        (r.nextDouble() - 0.5) * 0.01, -0.003, (r.nextDouble() - 0.5) * 0.01);
                case 1 -> {
                    double base = p.getY() + 16 + r.nextDouble() * 4;
                    double wave = Math.sin((p.tickCount + x * 2) * 0.05) * 3;
                    Fx.spawn(mc, Fx.color(color.mode), x, base + wave, z, 0, 0.02 + r.nextDouble() * 0.03, 0);
                }
                case 2 -> Fx.spawn(mc, Fx.color(color.mode), x, p.getY() + r.nextDouble() * 4, z,
                        (r.nextDouble() - 0.5) * 0.04, (r.nextDouble() - 0.5) * 0.02, (r.nextDouble() - 0.5) * 0.04);
                default -> Fx.spawn(mc, ParticleTypes.SNOWFLAKE, x, p.getY() + 3 + r.nextDouble() * 8, z,
                        (r.nextDouble() - 0.5) * 0.02, -0.04, (r.nextDouble() - 0.5) * 0.02);
            }
        }
    }
}
