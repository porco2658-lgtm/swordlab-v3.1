package com.swordlab.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swordlab.module.ModuleManager;
import com.swordlab.module.SwingAnimation;
import com.swordlab.module.Viewmodel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Inject(method = "applyItemArmAttackTransform", at = @At("HEAD"), cancellable = true)
    private void swordlab$swing(PoseStack poseStack, HumanoidArm arm, float swingProgress, CallbackInfo ci) {
        SwingAnimation s = ModuleManager.SWING;
        if (s != null && s.enabled && s.customStyle()) {
            s.apply(poseStack, arm, swingProgress);
            ci.cancel();
        }
    }

    @Inject(method = "applyItemArmTransform", at = @At("RETURN"))
    private void swordlab$view(PoseStack poseStack, HumanoidArm arm, float equipProgress, CallbackInfo ci) {
        Viewmodel v = ModuleManager.VIEWMODEL;
        if (v != null && v.enabled) v.apply(poseStack);
        com.swordlab.module.CsKnives k = ModuleManager.KNIVES;
        if (k != null && k.holdingKnife()) k.applyAnim(poseStack);
    }
}
