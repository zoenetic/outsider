package dev.zoenetic.outsider.survival.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemEntity.class)
public interface ItemEntityAccessor {
    @Accessor("pickupDelay")
    int outsider_survival$getPickupDelay();

    @Accessor("age")
    int outsider_survival$getAge();

    @Accessor("age")
    void outsider_survival$setAge(int age);
}
