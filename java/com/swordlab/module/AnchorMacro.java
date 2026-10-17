package com.swordlab.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

/**
 * Look at a block, press the activate key: places an anchor, charges it with glowstone, then
 * right-clicks it with a totem (or any non-glowstone item) to detonate.
 * Items are found by type, so it doesn't matter which hotbar slot or keybind they are on. If one is
 * missing from the hotbar it can be pulled in from the inventory.
 */
public class AnchorMacro extends Module {
    private final Setting activate = add(Setting.key("Activate key", GLFW.GLFW_KEY_G));
    private final Setting pull = add(Setting.bool("Pull from inventory", true));
    private final Setting delay = add(Setting.number("Step delay (ticks)", 1, 0, 5, 1));

    private enum S { IDLE, PLACE, CHARGE, DETONATE }

    private S state = S.IDLE;
    private int wait;
    private boolean wasDown;
    private BlockHitResult placeHit;
    private BlockPos anchorPos;

    public AnchorMacro() {
        super("Anchor Macro", "Place, charge and detonate (any slot)", Category.CRYSTAL);
    }

    @Override
    public void onDisable() { state = S.IDLE; }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null || mc.gameMode == null) { state = S.IDLE; return; }

        boolean down = mc.screen == null && Keys.down(activate.key);
        boolean pressed = down && !wasDown;
        wasDown = down;

        if (state == S.IDLE) {
            if (pressed) start(mc);
            return;
        }
        if (wait > 0) { wait--; return; }

        switch (state) {
            case PLACE -> {
                if (!use(mc, p, slotFor(mc, p, Items.RESPAWN_ANCHOR), placeHit)) { state = S.IDLE; return; }
                state = S.CHARGE;
                wait = delay.asInt();
            }
            case CHARGE -> {
                if (!mc.level.getBlockState(anchorPos).is(Blocks.RESPAWN_ANCHOR)) { state = S.IDLE; return; }
                if (!use(mc, p, slotFor(mc, p, Items.GLOWSTONE), anchorHit())) { state = S.IDLE; return; }
                state = S.DETONATE;
                wait = delay.asInt();
            }
            case DETONATE -> {
                int slot = Inv.find(p, Items.TOTEM_OF_UNDYING, 0, 8);
                if (slot < 0) slot = nonGlowstoneSlot(p);
                use(mc, p, slot, anchorHit());
                state = S.IDLE;
            }
            default -> state = S.IDLE;
        }
    }

    private void start(Minecraft mc) {
        if (!(mc.hitResult instanceof BlockHitResult bh) || bh.getType() != HitResult.Type.BLOCK) return;
        if (mc.level.getBlockState(bh.getBlockPos()).is(Blocks.RESPAWN_ANCHOR)) {
            anchorPos = bh.getBlockPos();
            state = S.CHARGE;
        } else {
            placeHit = bh;
            anchorPos = bh.getBlockPos().relative(bh.getDirection());
            state = S.PLACE;
        }
        wait = 0;
    }

    private BlockHitResult anchorHit() {
        return new BlockHitResult(Vec3.atCenterOf(anchorPos), Direction.UP, anchorPos, false);
    }

    /** Hotbar slot holding the item; if absent, pulls it from the inventory into a free hotbar slot. */
    private int slotFor(Minecraft mc, LocalPlayer p, Item item) {
        int slot = Inv.find(p, item, 0, 8);
        if (slot >= 0 || !pull.bool || p.containerMenu != p.inventoryMenu) return slot;

        int src = Inv.find(p, item, 9, 35);
        if (src < 0) return -1;
        int target = freeSlot(p);
        Inv.swap(mc, p, src, target);
        return target;
    }

    /** A hotbar slot not holding any of the macro items, falling back to the selected slot. */
    private int freeSlot(LocalPlayer p) {
        for (int i = 0; i < 9; i++) {
            var s = p.getInventory().getItem(i);
            if (!s.is(Items.RESPAWN_ANCHOR) && !s.is(Items.GLOWSTONE) && !s.is(Items.TOTEM_OF_UNDYING)) return i;
        }
        return p.getInventory().getSelectedSlot();
    }

    /** Selected slot if it isn't glowstone, otherwise the first hotbar slot that isn't. */
    private int nonGlowstoneSlot(LocalPlayer p) {
        int cur = p.getInventory().getSelectedSlot();
        if (!p.getInventory().getItem(cur).is(Items.GLOWSTONE)) return cur;
        for (int i = 0; i < 9; i++) if (!p.getInventory().getItem(i).is(Items.GLOWSTONE)) return i;
        return -1;
    }

    private boolean use(Minecraft mc, LocalPlayer p, int slot, BlockHitResult hit) {
        if (slot < 0) return false;
        Inv.select(p, slot);
        mc.gameMode.useItemOn(p, InteractionHand.MAIN_HAND, hit);
        p.swing(InteractionHand.MAIN_HAND);
        return true;
    }
}
