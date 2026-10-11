package dev.zoenetic.outsider.survival.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.zoenetic.outsider.survival.animation.client.itemuse.OutsiderItemUseAnimation;
import dev.zoenetic.outsider.survival.animation.client.itemuse.OutsiderItemUseAnimations;
import dev.zoenetic.outsider.survival.animation.client.player.OutsiderPlayerAnimations;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.world.InteractionHand.MAIN_HAND;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Inject(method = "submitHandsWithItems", at = @At("HEAD"))
    private void outsider_survival$renderFirstPersonPlayerAnimations(
            float frameInterp, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
            LocalPlayer player, int lightCoords, CallbackInfo ci
    ) {
        float ticks = player.tickCount + frameInterp;
        for (var entry : OutsiderPlayerAnimations.INSTANCE.of(player).entrySet()) {
            entry.getKey().firstPerson(entry.getValue(), poseStack, ticks);
        }
    }

    @Inject(
            method = "submitArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
                    shift = At.Shift.AFTER
            )
    )
    private void outsider_survival$renderFirstPersonItemUseAnimations(
            AbstractClientPlayer player, float frameInterp, float xRot,
            InteractionHand hand, float attack, ItemStack itemStack,
            float inverseArmHeight, PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector, int lightCoords,
            CallbackInfo ci
    ) {
        if (hand != player.getUsedItemHand()) return;
        OutsiderItemUseAnimation animation = OutsiderItemUseAnimations.INSTANCE.of(player);
        if (animation != null) {
            float ticks = player.getTicksUsingItem() + frameInterp;
            HumanoidArm arm = hand == MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
            animation.firstPerson(poseStack, arm, inverseArmHeight, ticks);
        }
    }
}
