package com.swordlab.module;

import net.minecraft.client.Minecraft;

/** Overrides the FOV option while enabled and restores it when disabled. */
public class CustomFov extends Module {
    private final Setting fov = add(Setting.number("FOV", 100, 30, 110, 1));
    private int saved = -1;

    public CustomFov() {
        super("Custom FOV", "Set your field of view", Category.VISUALS);
    }

    @Override
    public void onEnable() {
        Minecraft mc = Minecraft.getInstance();
        saved = mc.options.fov().get();
    }

    @Override
    public void onTick(Minecraft mc) {
        if (saved < 0) saved = mc.options.fov().get();
        int want = fov.asInt();
        if (mc.options.fov().get() != want) mc.options.fov().set(want);
    }

    @Override
    public void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (saved >= 0) mc.options.fov().set(saved);
        saved = -1;
    }
}
