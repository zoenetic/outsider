package dev.zoenetic.outsider.survival.mixin;

import dev.zoenetic.outsider.survival.animation.client.itemuse.OutsiderItemUseAnimationState;
import dev.zoenetic.outsider.survival.animation.client.player.OutsiderPlayerAnimationState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("TAIL"))
    private void outsider_survival$renderThirdPersonAnimations(HumanoidRenderState state, CallbackInfo ci) {
        HumanoidModel<?> self = (HumanoidModel<?>) (Object) this;
        var playerAnimations = ((OutsiderPlayerAnimationState) state).outsider_survival$getPlayerAnimations();
        for (var entry : playerAnimations.entrySet()) {
            entry.getKey().thirdPerson(entry.getValue(), self, state);
        }
        var outsiderItemUseAnimationState = (OutsiderItemUseAnimationState) state;
        var animation = outsiderItemUseAnimationState.outsider_survival$getItemUseAnimation();
        var ticks = outsiderItemUseAnimationState.outsider_survival$getItemUseTicks();
        if (animation == null) return;
        animation.thirdPerson(self, ticks);
    }
}
