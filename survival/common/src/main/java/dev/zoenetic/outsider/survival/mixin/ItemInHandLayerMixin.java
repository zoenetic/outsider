package dev.zoenetic.outsider.survival.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.zoenetic.outsider.survival.animation.client.itemuse.OutsiderItemUseAnimation;
import dev.zoenetic.outsider.survival.animation.client.itemuse.OutsiderItemUseAnimationState;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin {

    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void outsider_survival$hideOtherHandWhileTwoHanded(
            ArmedEntityRenderState state, ItemStackRenderState item, ItemStack itemStack,
            HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
            int lightCoords, CallbackInfo ci
    ) {
        OutsiderItemUseAnimation animation = ((OutsiderItemUseAnimationState) state).outsider_survival$getItemUseAnimation();
        if (animation == null || !animation.getTwoHanded()) return;
        if (!(state instanceof HumanoidRenderState humanoid)) return;
        HumanoidArm usedArm = humanoid.useItemHand == InteractionHand.MAIN_HAND ? humanoid.mainArm : humanoid.mainArm.getOpposite();
        if (arm != usedArm) ci.cancel();
    }

    @WrapOperation(
            method = "submitArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/ArmedModel;translateToHand(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;)V"
            )
    )
    private void outsider_survival$placeTwoHandedItem(
            ArmedModel<?> model, EntityRenderState state, HumanoidArm arm, PoseStack poseStack, Operation<Void> original
    ) {
        var animationState = (OutsiderItemUseAnimationState) state;
        OutsiderItemUseAnimation animation = animationState.outsider_survival$getItemUseAnimation();
        if (animation != null && animation.getTwoHanded() && model instanceof HumanoidModel<?> humanoid) {
            animation.thirdPersonItem(poseStack, humanoid, arm, animationState.outsider_survival$getItemUseTicks());
        } else {
            original.call(model, state, arm, poseStack);
        }
    }

}
