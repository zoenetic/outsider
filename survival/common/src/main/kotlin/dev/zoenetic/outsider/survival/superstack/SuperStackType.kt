package dev.zoenetic.outsider.survival.superstack

import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemInstance
import net.minecraft.world.item.ItemStack

public class SuperStackType(
    public val item: Item,
    public val rule: Comparator<ItemInstance>? = null,
) {
    public fun isValid(itemsToAdd: ItemStack): Boolean =
        itemsToAdd.item === item
}