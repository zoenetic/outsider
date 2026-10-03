package dev.zoenetic.outsider.survival.superstack

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.Item

public object SuperStacks {
    public val byBaseItem: Map<Item, SuperStackItem> by lazy {
        BuiltInRegistries.ITEM
            .filterIsInstance<SuperStackItem>()
            .groupBy { superStack -> superStack.type.item }
            .onEach { (base, superStacks) ->
                check(superStacks.size == 1) {
                    val ids =
                        superStacks.map { superStack -> BuiltInRegistries.ITEM.getKey(superStack) }
                    "Duplicate SuperStack registered for item ${BuiltInRegistries.ITEM.getKey(
                        base,
                    )}: $ids"
                }
            }
            .mapValues { (_, value) -> value.single() }
    }
}
