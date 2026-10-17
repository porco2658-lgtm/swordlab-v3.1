package com.swordlab.module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Shows the sword in your hand as a CS-style knife with a skin. Purely visual: it only sets the item-model
 * component on your local copy of the stack, and puts it back whenever you open a screen or switch away.
 */
public class CsKnives extends Module {
    public static final String[] KNIVES = {"Bayonet", "Karambit", "Huntsman", "Butterfly", "Balisong"};
    public static final String[] SKINS = {"Fade", "Doppler", "Slaughter", "Case Hardened", "Night", "Steel"};
    private static final String NS = "swordlab";

    private final Setting knife = add(Setting.mode("Knife", 1, KNIVES));
    private final Setting skin = add(Setting.mode("Skin", 0, SKINS));
    private final Setting inspect = add(Setting.mode("Inspect anim", 0, "Flip", "Spin", "Twirl", "Off"));
    private final Setting inspectKey = add(Setting.key("Inspect key", org.lwjgl.glfw.GLFW.GLFW_KEY_V));
    private final Setting draw = add(Setting.bool("Draw animation", true));
    private final Setting idle = add(Setting.bool("Idle sway", true));
    private long inspectStart = -100000L, drawStart = -100000L;
    private int lastSlot = -1;
    private boolean keyWas;

    public CsKnives() {
        super("CS Knives", "Held sword looks like a CS knife skin", Category.VISUALS);
    }

    private Identifier current() {
        String k = KNIVES[knife.mode].toLowerCase();
        String s = SKINS[skin.mode].toLowerCase().replace(' ', '_');
        return Identifier.fromNamespaceAndPath(NS, "knife_" + k + "_" + s);
    }

    @Override
    public void onTick(Minecraft mc) {
        sync(mc, true);
        if (mc.player == null) return;
        int slot = mc.player.getInventory().getSelectedSlot();
        if (slot != lastSlot) { lastSlot = slot; drawStart = System.currentTimeMillis(); }
        boolean down = mc.screen == null && Keys.down(inspectKey.key);
        if (down && !keyWas && inspect.mode != 3) inspectStart = System.currentTimeMillis();
        keyWas = down;
    }

    public boolean holdingKnife() {
        Minecraft mc = Minecraft.getInstance();
        return enabled && mc.player != null && mc.player.getMainHandItem().is(ItemTags.SWORDS);
    }

    /** Called from the item-in-hand mixin; every animation returns to identity when finished. */
    public void applyAnim(PoseStack ps) {
        long now = System.currentTimeMillis();
        if (idle.bool) {
            float t = now / 1000f;
            ps.translate(0.012f * Mth.sin(t * 1.6f), 0.014f * Mth.sin(t * 2.1f), 0f);
            ps.mulPose(Axis.ZP.rotationDegrees(1.6f * Mth.sin(t * 1.3f)));
        }
        if (draw.bool) {
            float d = (now - drawStart) / 380f;
            if (d >= 0 && d < 1) {
                float e = (1 - d) * (1 - d);
                ps.translate(0f, -0.4f * e, 0f);
                ps.mulPose(Axis.XP.rotationDegrees(-60f * e));
            }
        }
        float i = (now - inspectStart) / 1400f;
        if (i >= 0 && i < 1) {
            float e = i * i * (3 - 2 * i);
            ps.translate(0f, 0.12f * Mth.sin(i * (float) Math.PI), -0.05f * Mth.sin(i * (float) Math.PI));
            switch (inspect.mode) {
                case 0 -> ps.mulPose(Axis.XP.rotationDegrees(-360f * e));
                case 1 -> ps.mulPose(Axis.YP.rotationDegrees(720f * e));
                case 2 -> { ps.mulPose(Axis.ZP.rotationDegrees(360f * e)); ps.mulPose(Axis.XP.rotationDegrees(-180f * e)); }
                default -> {}
            }
        }
    }

    @Override
    public void onDisable() { sync(Minecraft.getInstance(), false); }

    private void sync(Minecraft mc, boolean active) {
        if (mc.player == null) return;
        Inventory inv = mc.player.getInventory();
        Identifier want = current();
        boolean show = active && mc.screen == null;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty()) continue;
            Identifier model = s.get(DataComponents.ITEM_MODEL);
            boolean ours = model != null && NS.equals(model.getNamespace());
            boolean held = i == inv.getSelectedSlot() || i == 40;
            if (show && held && s.is(ItemTags.SWORDS)) {
                if (!want.equals(model)) s.set(DataComponents.ITEM_MODEL, want);
            } else if (ours) {
                Identifier def = s.getItem().components().get(DataComponents.ITEM_MODEL);
                if (def != null) s.set(DataComponents.ITEM_MODEL, def);
            }
        }
    }
}
