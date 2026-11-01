package dev.zoenetic.outsider.survival.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.zoenetic.outsider.survival.fire.client.HearthBoardRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(
            method = "submitFeatures",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;submitEntities(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void outsider_survival$submitHearthBoards(
            LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector, boolean renderOutline, CallbackInfo ci, @Local PoseStack poseStack
    ) {
        HearthBoardRenderer.INSTANCE.submit(levelRenderState.cameraRenderState.pos, poseStack, submitNodeCollector);
    }
}

