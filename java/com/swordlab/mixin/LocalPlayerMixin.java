package com.swordlab.mixin;

import com.swordlab.module.AimState;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Unique private float swordlab$yaw, swordlab$pitch;
    @Unique private boolean swordlab$swapped;

    // Swap the rotation just while the movement packet is built, then restore the camera.
    @Inject(method = "sendPosition", at = @At("HEAD"))
    private void swordlab$pre(CallbackInfo ci) {
        if (!AimState.active) return;
        LocalPlayer self = (LocalPlayer) (Object) this;
        swordlab$yaw = self.getYRot();
        swordlab$pitch = self.getXRot();
        swordlab$swapped = true;
        self.setYRot(AimState.yaw);
        self.setXRot(AimState.pitch);
    }

    @Inject(method = "sendPosition", at = @At("RETURN"))
    private void swordlab$post(CallbackInfo ci) {
        if (!swordlab$swapped) return;
        LocalPlayer self = (LocalPlayer) (Object) this;
        self.setYRot(swordlab$yaw);
        self.setXRot(swordlab$pitch);
        swordlab$swapped = false;
    }
}
