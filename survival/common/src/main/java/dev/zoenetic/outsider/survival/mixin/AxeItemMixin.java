package dev.zoenetic.outsider.survival.mixin;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.zoenetic.outsider.survival.fuel.firewood.MaybeSplitKt.maybeSplit;

@Mixin(AxeItem.class)
public class AxeItemMixin {
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    public void outsider_survival$evaluateSplitBlockState(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        boolean didSplit = maybeSplit(context);
        if (didSplit) cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
