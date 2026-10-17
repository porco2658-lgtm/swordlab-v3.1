package com.swordlab.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;

/**
 * While the hold key is down, holding crystals: looking at obsidian/bedrock places a crystal,
 * looking at a crystal breaks it. The two alternate on their own as the crosshair target changes.
 */
public class AutoCrystal extends Module {
    private final Setting holdKey = add(Setting.key("Hold key", GLFW.GLFW_KEY_B));
    private final Setting placeDelay = add(Setting.number("Place delay (ticks)", 1, 0, 10, 1));
    private final Setting breakDelay = add(Setting.number("Break delay (ticks)", 0, 0, 10, 1));
    private int cooldown;

    public AutoCrystal() {
        super("Auto Crystal", "Hold key: place + break crystals", Category.CRYSTAL);
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null || mc.gameMode == null || mc.screen != null) return;
        if (!Keys.down(holdKey.key) || !p.getMainHandItem().is(Items.END_CRYSTAL)) { cooldown = 0; return; }
        if (cooldown > 0) { cooldown--; return; }

        HitResult h = mc.hitResult;
        if (h instanceof EntityHitResult eh && eh.getEntity() instanceof EndCrystal crystal) {
            mc.gameMode.attack(p, crystal);
            p.swing(InteractionHand.MAIN_HAND);
            cooldown = breakDelay.asInt();
        } else if (h instanceof BlockHitResult bh && bh.getType() == HitResult.Type.BLOCK) {
            BlockState s = mc.level.getBlockState(bh.getBlockPos());
            if (s.is(Blocks.OBSIDIAN) || s.is(Blocks.BEDROCK)) {
                mc.gameMode.useItemOn(p, InteractionHand.MAIN_HAND, bh);
                p.swing(InteractionHand.MAIN_HAND);
                cooldown = placeDelay.asInt();
            }
        }
    }
}
