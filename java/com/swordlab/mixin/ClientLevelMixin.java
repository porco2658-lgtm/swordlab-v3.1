package com.swordlab.mixin;

import com.swordlab.module.CustomSky;
import com.swordlab.module.ModuleManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    // Best effort: only takes effect if this build still computes the sky color through this method.
    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void swordlab$sky(Vec3 pos, float partialTick, CallbackInfoReturnable<Integer> cir) {
        CustomSky s = ModuleManager.SKY;
        if (s != null && s.enabled) cir.setReturnValue(s.color());
    }
}
