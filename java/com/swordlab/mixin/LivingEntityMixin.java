package com.swordlab.mixin;

import com.swordlab.module.ModuleManager;
import com.swordlab.module.SwingAnimation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "getCurrentSwingDuration", at = @At("RETURN"), cancellable = true)
    private void swordlab$duration(CallbackInfoReturnable<Integer> cir) {
        SwingAnimation s = ModuleManager.SWING;
        if (s != null && s.enabled && (Object) this instanceof LocalPlayer) cir.setReturnValue(s.duration());
    }
}
