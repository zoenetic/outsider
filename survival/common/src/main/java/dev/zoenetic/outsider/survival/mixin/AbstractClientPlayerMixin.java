package dev.zoenetic.outsider.survival.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.zoenetic.outsider.survival.work.WorkMode;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {
    @ModifyReturnValue(method = "getFieldOfViewModifier", at = @At("TAIL"))
    private float outsider_survival$setFieldOfViewWhileWorking(float original, boolean firstPerson, float effectScale) {
        AbstractClientPlayer player = ((AbstractClientPlayer) (Object) this);
        if (!WorkMode.isEngagedFor(player)) return original;
        return original * Mth.lerp(effectScale, 1.0F, 0.75F);
    }
}
