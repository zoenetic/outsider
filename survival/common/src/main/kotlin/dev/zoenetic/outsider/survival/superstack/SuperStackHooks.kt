package dev.zoenetic.outsider.survival.superstack

import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponentType
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack

public object SuperStackHooks {
    @JvmStatic
    public fun routeInventoryAdd(inventory: Inventory, stack: ItemStack): Boolean {
        if (!SuperStacks.byBaseItem.containsKey(stack.item)) return false
        for (slotStack in inventory) {
            val _ = slotStack.asSuperStackOrNull()?.insert(stack)
            if (slotStack.isEmpty) break
        }
        return stack.isEmpty
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
