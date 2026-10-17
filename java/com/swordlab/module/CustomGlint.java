package com.swordlab.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

/** Colored sparkles around the item in your hand while it has an enchantment glint (particle based). */
public class CustomGlint extends Module {
    private final Setting color = add(Setting.mode("Color", 0, Fx.COLORS));
    private final Setting amount = add(Setting.number("Amount", 2, 1, 8, 1));
    private final Setting always = add(Setting.bool("Even without glint", false));

    public CustomGlint() {
        super("Custom Glint", "Colored sparkles on your enchanted item", Category.VISUALS);
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null) return;
        ItemStack s = p.getMainHandItem();
        if (s.isEmpty() || (!always.bool && !s.hasFoil())) return;
        RandomSource r = p.getRandom();
        double yaw = Math.toRadians(p.getYRot());
        double fx = -Math.sin(yaw), fz = Math.cos(yaw), rx = -fz, rz = fx;
        double bx = p.getX() + fx * 0.55 + rx * 0.35, by = p.getEyeY() - 0.45, bz = p.getZ() + fz * 0.55 + rz * 0.35;
        for (int i = 0; i < amount.asInt(); i++)
            Fx.spawn(mc, Fx.color(color.mode), bx + (r.nextDouble() - 0.5) * 0.35, by + (r.nextDouble() - 0.5) * 0.35,
                    bz + (r.nextDouble() - 0.5) * 0.35, 0, 0.01, 0);
    }
}
