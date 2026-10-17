package com.swordlab.module;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Outlines the block you are looking at with particles along its edges. */
public class BlockOverlay extends Module {
    private final Setting color = add(Setting.mode("Color", 0, Fx.COLORS));
    private final Setting dots = add(Setting.number("Dots per edge", 3, 1, 6, 1));

    public BlockOverlay() {
        super("Block Overlay", "Particle outline on the targeted block", Category.VISUALS);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.level == null || !(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return;
        BlockPos b = hit.getBlockPos();
        int n = dots.asInt();
        var o = Fx.color(color.mode);
        for (int e = 0; e < 3; e++) {
            for (int c = 0; c < 4; c++) {
                double u = c & 1, v = (c >> 1) & 1;
                for (int i = 0; i <= n; i++) {
                    double t = (double) i / n;
                    double x = e == 0 ? t : u, y = e == 1 ? t : (e == 0 ? v : u), z = e == 2 ? t : v;
                    Fx.spawn(mc, o, b.getX() + x, b.getY() + y, b.getZ() + z, 0, 0, 0);
                }
            }
        }
    }
}
