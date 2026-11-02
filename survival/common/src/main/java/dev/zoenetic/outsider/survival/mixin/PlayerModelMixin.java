package dev.zoenetic.outsider.survival.mixin;

import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.zoenetic.outsider.survival.animation.client.player.SplitLegsKt.splitLegs;
import static dev.zoenetic.outsider.survival.animation.client.player.SplitLegsKt.syncShinPants;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {
    @Inject(method = "createMesh", at = @At("RETURN"))
    private static void outsider_survival$splitLegs(CubeDeformation scale, boolean slim, CallbackInfoReturnable<MeshDefinition> cir) {
        splitLegs(cir.getReturnValue().getRoot(), scale);
    }

    @Inject(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V",
            at = @At("TAIL")
    )
    private void outsider_survival$syncShinPants(AvatarRenderState state, CallbackInfo ci) {
        syncShinPants((PlayerModel) (Object) this);
    }
}
