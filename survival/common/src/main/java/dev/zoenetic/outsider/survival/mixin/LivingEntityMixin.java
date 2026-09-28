package dev.zoenetic.outsider.survival.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @ModifyReturnValue(method = "canFreeze", at = @At("RETURN"))
    private boolean outsider_survival$playersDontFreeze(boolean original) {
        if ((Object) this instanceof Player) {
            return false;
        }
        return original;
    }
}
