package dev.zoenetic.outsider.survival.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.zoenetic.outsider.survival.animation.client.itemuse.OutsiderItemUseAnimation;
import dev.zoenetic.outsider.survival.animation.client.itemuse.OutsiderItemUseAnimationState;
import dev.zoenetic.outsider.survival.animation.client.itemuse.OutsiderItemUseAnimations;
import dev.zoenetic.outsider.survival.animation.client.player.OutsiderPlayerAnimation;
import dev.zoenetic.outsider.survival.animation.client.player.OutsiderPlayerAnimationState;
import dev.zoenetic.outsider.survival.animation.client.player.OutsiderPlayerAnimations;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At("TAIL"))
    private void outsider_survival$dispatchAnimations(LivingEntity entity, LivingEntityRenderState state,
                                                      float partialTicks, CallbackInfo ci) {

        @NotNull Map<@NotNull OutsiderPlayerAnimation, @NotNull Float> playerAnimations = OutsiderPlayerAnimations.INSTANCE.of(entity);
        ((OutsiderPlayerAnimationState) state).outsider_survival$setPlayerAnimations(playerAnimations);

        OutsiderItemUseAnimation itemUseAnimation = OutsiderItemUseAnimations.INSTANCE.of(entity);
        float itemUseTicks = entity.getTicksUsingItem() + partialTicks;
        ((OutsiderItemUseAnimationState) state).outsider_survival$setItemUseAnimation(itemUseAnimation, itemUseTicks);
    }

    @ModifyVariable(method = "setupRotations", at = @At("HEAD"), argsOnly = true, name = "bodyRot")
    private float outsider_survival$setBodyRotations(float bodyRot,
                                                     @Local(argsOnly = true, name = "state") LivingEntityRenderState state) {
        var outsiderPlayerAnimationState = (OutsiderPlayerAnimationState) state;
        var playerAnimations = outsiderPlayerAnimationState.outsider_survival$getPlayerAnimations();
        for (var entry : playerAnimations.entrySet()) {
            bodyRot += entry.getKey().bodyRotation(entry.getValue(), state);
        }
        return bodyRot;
    }
}
