package com.swordlab.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Items;

/** Keeps a totem in the offhand, pulling one from anywhere in the inventory. */
public class AutoTotem extends Module {
    private final Setting delay = add(Setting.number("Delay (ticks)", 1, 0, 10, 1));
    private final Setting health = add(Setting.number("Only below HP", 20, 1, 20, 1));
    private int cooldown;

    public AutoTotem() {
        super("Auto Totem", "Refills the offhand with a totem", Category.CRYSTAL);
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null || mc.gameMode == null || p.containerMenu != p.inventoryMenu) return;
        if (p.getOffhandItem().is(Items.TOTEM_OF_UNDYING) || p.getHealth() > health.value) { cooldown = 0; return; }
        if (cooldown++ < delay.asInt()) return;

        int src = Inv.find(p, Items.TOTEM_OF_UNDYING, 0, 35);
        if (src >= 0) Inv.swap(mc, p, src, 40);
        cooldown = 0;
    }
}
