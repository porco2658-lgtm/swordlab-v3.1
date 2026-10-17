package com.swordlab.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

/**
 * When you open your inventory, moves a totem into the hotbar slot you pick (number keys 1-9)
 * if that slot doesn't already hold one.
 */
public class AutoInvTotem extends Module {
    private final Setting totemKey = add(Setting.key("Totem slot key", GLFW.GLFW_KEY_9));
    private final Setting delay = add(Setting.number("Delay (ticks)", 1, 0, 10, 1));
    private int ticks;
    private boolean done;

    public AutoInvTotem() {
        super("Auto Inv Totem", "Inventory open: totem to hotbar slot", Category.CRYSTAL);
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null || mc.gameMode == null || !(mc.screen instanceof InventoryScreen)) { ticks = 0; done = false; return; }
        if (done || ticks++ < delay.asInt()) return;
        done = true;

        int slot = Keys.slot(totemKey.key);
        if (slot < 0 || p.getInventory().getItem(slot).is(Items.TOTEM_OF_UNDYING)) return;

        int src = Inv.find(p, Items.TOTEM_OF_UNDYING, 9, 35);
        if (src < 0) {
            for (int i = 0; i < 9 && src < 0; i++) {
                if (i != slot && p.getInventory().getItem(i).is(Items.TOTEM_OF_UNDYING)) src = i;
            }
        }
        if (src >= 0) Inv.swap(mc, p, src, slot);
    }
}
