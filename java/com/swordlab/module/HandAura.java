package com.swordlab.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.RandomSource;

/** Particles swirling around your hand. */
public class HandAura extends Module {
    private final Setting color = add(Setting.mode("Color", 0, Fx.COLORS));
    private final Setting amount = add(Setting.number("Amount", 2, 1, 8, 1));
    private final Setting spread = add(Setting.number("Spread", 0.2, 0.05, 0.6, 0.05));

    public HandAura() {
        super("Hand Aura", "Particles around your hand", Category.VISUALS);
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null) return;
        RandomSource r = p.getRandom();
        double yaw = Math.toRadians(p.getYRot());
        double fx = -Math.sin(yaw), fz = Math.cos(yaw), rx = -fz, rz = fx;
        double bx = p.getX() + fx * 0.6 + rx * 0.4, by = p.getEyeY() - 0.55, bz = p.getZ() + fz * 0.6 + rz * 0.4;
        double s = spread.value;
        for (int i = 0; i < amount.asInt(); i++) {
            double a = r.nextDouble() * Math.PI * 2;
            Fx.spawn(mc, Fx.color(color.mode), bx + Math.cos(a) * s, by + (r.nextDouble() - 0.5) * s, bz + Math.sin(a) * s,
                    0, 0.015, 0);
        }
    }
}
