package dev.zoenetic.outsider.survival.mixin;

import dev.zoenetic.outsider.survival.superstack.SuperStack;
import dev.zoenetic.outsider.survival.superstack.SuperStackItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    
    @Inject(method = "set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;", at = @At("HEAD"), cancellable = true)
    public <T> void outsider_survival$routeSet(DataComponentType<T> type, @Nullable T value, CallbackInfoReturnable<T> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (!(stack.getItem() instanceof SuperStackItem)) return;
        T previous = stack.get(type);
        if (SuperStack.routeSet(stack, type, value)) cir.setReturnValue(previous);
    }

    @Inject(method = "set(Lnet/minecraft/core/component/TypedDataComponent;)Ljava/lang/Object;", at = @At("HEAD"), cancellable = true)
    public <T> void outsider_survival$routeSetTyped(TypedDataComponent<T> value, CallbackInfoReturnable<T> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (!(stack.getItem() instanceof SuperStackItem)) return;
        T previous = stack.get(value.type());
        if (SuperStack.routeSet(stack, value.type(), value.value())) cir.setReturnValue(previous);
    }

    @Inject(method = "remove(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;", at = @At("HEAD"), cancellable = true)
    public <T> void outsider_survival$routeRemove(DataComponentType<? extends T> type, CallbackInfoReturnable<T> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (!(stack.getItem() instanceof SuperStackItem)) return;
        T previous = stack.get(type);
        if (SuperStack.routeSet(stack, type, null)) cir.setReturnValue(previous);
    }
}
