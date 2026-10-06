package dev.zoenetic.outsider.survival.superstack

import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponentType
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

public object SuperStackHooks {

    @JvmStatic
    public fun countText(itemStack: ItemStack, countText: String?): String? {
        if (countText != null) return countText
        val count = itemStack.asSuperStackOrNull()?.count ?: return null
        return if (count <= 1) null else count.toString()
    }

    @JvmStatic
    public fun routeInventoryAdd(inventory: Inventory, itemStack: ItemStack): Boolean {
        if (!SuperStacks.byBaseItem.containsKey(itemStack.item)) return false
        val original = itemStack.count
        if (!mergeIntoExisting(inventory, itemStack) && inventory.freeSlot != -1) {
            itemStack.moveIntoSuperStack()?.let(inventory::add)
        }
        return itemStack.count < original
    }

    @JvmStatic
    public fun routeInventoryTick(itemStack: ItemStack, owner: Entity) {
        val inventory = (owner as? Player)?.inventory ?: return
        if (!SuperStacks.byBaseItem.containsKey(itemStack.item)) return
        val index = inventory.indexOfFirst { it === itemStack }
        if (index == -1) return
        val superStack = itemStack.moveIntoSuperStack() ?: return
        inventory.setItem(index, superStack)
    }

    private fun mergeIntoExisting(inventory: Inventory, itemStack: ItemStack): Boolean {
        for (slotStack in inventory) {
            val superStack = slotStack.asSuperStackOrNull() ?: continue
            val _ = superStack.insert(itemStack)
            if (itemStack.isEmpty) return true
        }
        return false
    }

    @JvmStatic
    public fun <T : Any> routeSet(
        stack: ItemStack,
        type: DataComponentType<T>,
        value: T?,
    ): Boolean {
        val superStack = stack.asSuperStackOrNull() ?: return false
        if (!superStack.type.mirroredComponents.contains(type)) return false
        val patch = if (value == null) {
            DataComponentPatch.builder().remove(type).build()
        } else {
            DataComponentPatch.builder().set(type, value).build()
        }
        val _ = superStack.patchActiveStack(patch)
        return true
    }
}
