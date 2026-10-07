package dev.zoenetic.outsider.survival.mixin;

import dev.zoenetic.outsider.survival.superstack.SuperStackHooks;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void outsider_survival$splitSuperStack(CallbackInfo ci) {
        if (SuperStackHooks.splitItemEntity((ItemEntity) (Object) this)) ci.cancel();
    }
}
