package com.swordlab.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;

/** Small helpers shared by the particle-based visual modules. */
final class Fx {
    static final String[] COLORS = {"Purple", "Blue", "Red", "Green", "White", "Gold"};

    private Fx() {}

    static ParticleOptions color(int mode) {
        return switch (mode) {
            case 1 -> ParticleTypes.SOUL_FIRE_FLAME;
            case 2 -> ParticleTypes.CRIMSON_SPORE;
            case 3 -> ParticleTypes.HAPPY_VILLAGER;
            case 4 -> ParticleTypes.END_ROD;
            case 5 -> ParticleTypes.FLAME;
            default -> ParticleTypes.WITCH;
        };
    }

    static void spawn(Minecraft mc, ParticleOptions o, double x, double y, double z, double vx, double vy, double vz) {
        if (mc.level == null) return;
        Particle p = mc.particleEngine.createParticle(o, x, y, z, vx, vy, vz);
        if (p != null) mc.particleEngine.add(p);
    }
}
