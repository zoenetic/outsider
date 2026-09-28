package dev.zoenetic.outsider.survival.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.zoenetic.outsider.survival.campfire.client.LightingCampfireRenderer;
import dev.zoenetic.outsider.survival.vitals.client.ShiverRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At("TAIL"))
    private void outsider_survival$captureShiver(LivingEntity entity, LivingEntityRenderState state,
                                              float partialTicks, CallbackInfo ci) {
        ShiverRenderer.INSTANCE.capture(entity, state);
        LightingCampfireRenderer.INSTANCE.capture(entity, state);
    }

    @ModifyVariable(method = "setupRotations", at = @At("HEAD"), argsOnly = true, name = "bodyRot")
    private float outsider_survival$shiverBodyRotation(float bodyRot,
                                                    @Local(argsOnly = true, name = "state") LivingEntityRenderState state) {
        return ShiverRenderer.INSTANCE.applyToBodyRotation(bodyRot, state);
    }
}
