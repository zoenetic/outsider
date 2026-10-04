package dev.zoenetic.outsider.survival.mixin;

import dev.zoenetic.outsider.survival.superstack.SuperStackHooks;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public class InventoryMixin {

    @Inject(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    public void outsider_survival$addSuperStackedItem(int slot, ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        Inventory inventory = (Inventory) (Object) this;
        if (SuperStackHooks.routeInventoryAdd(inventory, itemStack)) cir.setReturnValue(true);
    }
}
