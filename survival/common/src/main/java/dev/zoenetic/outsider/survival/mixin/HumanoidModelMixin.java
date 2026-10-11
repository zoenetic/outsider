package dev.zoenetic.outsider.survival.mixin;

import dev.zoenetic.outsider.survival.animation.client.itemuse.OutsiderItemUseAnimationState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("TAIL"))
    private void outsider_survival$renderThirdPersonItemUseAnimations(HumanoidRenderState state, CallbackInfo ci) {
        HumanoidModel<?> self = (HumanoidModel<?>) (Object) this;
        var outsiderItemUseAnimationState = (OutsiderItemUseAnimationState) state;
        var animation = outsiderItemUseAnimationState.outsider_survival$getItemUseAnimation();
        var ticks = outsiderItemUseAnimationState.outsider_survival$getItemUseTicks();
        if (animation == null) return;
        animation.thirdPerson(self.rightArm, self.leftArm, ticks);
    }
}
