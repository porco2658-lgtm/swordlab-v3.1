package com.swordlab.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;

/** Glyphs and sparks orbiting around you. */
public class FloatingRelics extends Module {
    private final Setting count = add(Setting.number("Relics", 4, 1, 8, 1));
    private final Setting radius = add(Setting.number("Radius", 1.1, 0.6, 2.0, 0.1));
    private final Setting speed = add(Setting.number("Speed", 2.0, 0.5, 6.0, 0.5));
    private final Setting height = add(Setting.number("Height", 1.0, 0.0, 2.2, 0.1));
    private final Setting color = add(Setting.mode("Color", 0, Fx.COLORS));

    public FloatingRelics() {
        super("Floating Relics", "Glyphs orbiting around you", Category.VISUALS);
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null) return;
        int n = count.asInt();
        double t = p.tickCount * 0.05 * speed.value;
        for (int i = 0; i < n; i++) {
            double a = t + i * Math.PI * 2 / n;
            double bob = Math.sin(t * 1.5 + i) * 0.15;
            double x = p.getX() + Math.cos(a) * radius.value, z = p.getZ() + Math.sin(a) * radius.value;
            double y = p.getY() + height.value + bob;
            Fx.spawn(mc, ParticleTypes.ENCHANT, x, y + 0.3, z, 0, -0.05, 0);
            Fx.spawn(mc, Fx.color(color.mode), x, y, z, 0, 0, 0);
        }
    }
}
