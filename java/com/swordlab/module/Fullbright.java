package com.swordlab.module;

import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Client-side night vision so everything is fully lit. */
public class Fullbright extends Module {
    public Fullbright() {
        super("Fullbright", "Everything is fully lit (client-side night vision)", Category.VISUALS);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null) return;
        MobEffectInstance cur = mc.player.getEffect(MobEffects.NIGHT_VISION);
        if (cur == null || cur.getDuration() < 300)
            mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1000, 0, false, false, false));
    }

    @Override
    public void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) mc.player.removeEffect(MobEffects.NIGHT_VISION);
    }
}
