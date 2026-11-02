package dev.zoenetic.outsider.survival.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.zoenetic.outsider.survival.work.ClampWorkingLookKt.clampWorkingLook;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "turn", at = @At("TAIL"))
    private void outsider_survival$clampWorkingLook(double xo, double yo, CallbackInfo ci) {
        if ((Object) this instanceof Player player) clampWorkingLook(player);
    }
}
