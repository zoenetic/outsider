package dev.zoenetic.outsider.survival.mixin;

import dev.zoenetic.outsider.survival.campfire.client.LightingCampfireRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("TAIL"))
    private void outsider_survival$armsLightingCampfire(HumanoidRenderState state, CallbackInfo ci) {
        HumanoidModel<?> self = (HumanoidModel<?>) (Object) this;
        LightingCampfireRenderer.INSTANCE.applyToModel(self.rightArm, self.leftArm, state);
    }
}
