package dev.zoenetic.outsider.survival.superstack

import net.minecraft.core.component.DataComponentType
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

public class SuperStackType(
    public val item: Item,
    public val rules: SuperStackRules = SuperStackRules.NONE,
    public val mirroredComponents: List<DataComponentType<*>> = emptyList(),
) {
    public fun isValid(itemsToAdd: ItemStack): Boolean = itemsToAdd.item === item
}
