package com.swordlab.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;

/** Purple particle aura around your body. */
public class Aura extends Module {
    private final Setting density = add(Setting.number("Density", 3, 1, 12, 1));
    private final Setting radius = add(Setting.number("Radius", 0.7, 0.3, 1.5, 0.1));
    private final Setting rise = add(Setting.number("Rise speed", 0.02, 0.0, 0.1, 0.01));

    public Aura() {
        super("Aura", "Purple particles around your body", Category.VISUALS);
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null) return;
        RandomSource r = p.getRandom();
        for (int i = 0; i < density.asInt(); i++) {
            double a = r.nextDouble() * Math.PI * 2;
            double d = radius.value * (0.8 + r.nextDouble() * 0.4);
            double x = p.getX() + Math.cos(a) * d;
            double z = p.getZ() + Math.sin(a) * d;
            double y = p.getY() + r.nextDouble() * p.getBbHeight();
            Particle part = mc.particleEngine.createParticle(ParticleTypes.WITCH, x, y, z, 0, rise.value, 0);
            if (part != null) mc.particleEngine.add(part);
        }
    }
}
