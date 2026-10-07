package dev.zoenetic.outsider.survival.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.zoenetic.outsider.survival.superstack.SuperStackHooks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {

    @WrapMethod(method = "moveItemStackTo")
    private boolean outsider_survival$mergeIntoSuperStacks(ItemStack itemStack, int startSlot, int endSlot, boolean backwards, Operation<Boolean> original) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        boolean merged = SuperStackHooks.mergeIntoSuperStacks(menu, itemStack, startSlot, endSlot, backwards);
        if (itemStack.isEmpty()) return merged;
        return original.call(itemStack, startSlot, endSlot, backwards) || merged;
    }

    @Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
    private void outsider_survival$pickupAllIntoSuperStack(int slotIndex, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo ci) {
        if (containerInput != ContainerInput.PICKUP_ALL) return;
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        if (SuperStackHooks.routePickupAll(menu, slotIndex, buttonNum, player)) ci.cancel();
    }
}
