package dev.zoenetic.outsider.survival.animation.client.itemuse

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item

public object OutsiderItemUseAnimations {

    private val all: MutableMap<Item, OutsiderItemUseAnimation> = mutableMapOf()

    public fun of(entity: LivingEntity): OutsiderItemUseAnimation? =
        if (entity.isUsingItem) all[entity.useItem.item] else null

    public fun register(item: Item, animation: OutsiderItemUseAnimation) {
        all[item] = animation
    }
}
