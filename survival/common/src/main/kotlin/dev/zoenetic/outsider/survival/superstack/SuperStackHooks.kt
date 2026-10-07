package dev.zoenetic.outsider.survival.superstack

import dev.zoenetic.outsider.survival.mixin.ItemEntityAccessor
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponentType
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack

public object SuperStackHooks {

    @JvmStatic
    public fun countText(itemStack: ItemStack, countText: String?): String? {
        if (countText != null) return countText
        val count = itemStack.asSuperStackOrNull()?.count ?: return null
        return if (count <= 1) null else count.toString()
    }

    @JvmStatic
    public fun mergeIntoSuperStacks(
        menu: AbstractContainerMenu,
        itemStack: ItemStack,
        startSlot: Int,
        endSlot: Int,
        backwards: Boolean,
    ): Boolean {
        val incoming = itemStack.asSuperStackOrNull()
        val baseItem = incoming?.type?.item ?: itemStack.item
        if (incoming == null && !SuperStacks.byBaseItem.containsKey(baseItem)) return false
        val range = if (backwards) (startSlot until endSlot).reversed() else startSlot until endSlot
        val targets = range.asSequence()
            .map { menu.slots[it] }
            .filter { it.mayPlace(itemStack) }
            .filter { it.item.asSuperStackOrNull()?.type?.item == baseItem }
        var merged = false
        for (slot in targets) {
            if (itemStack.isEmpty || incoming?.count == 0) break
            val target = checkNotNull(slot.item.asSuperStackOrNull())
            if (moveInto(target, itemStack, incoming)) {
                slot.setChanged()
                merged = true
            }
        }
        incoming?.discardIfEmpty()
        return merged
    }

    private fun moveInto(target: SuperStack, itemStack: ItemStack, incoming: SuperStack?): Boolean {
        if (incoming == null) return target.insert(itemStack) > 0
        val before = incoming.count
        incoming.mergeInto(target)
        return incoming.count < before
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

    @JvmStatic
    public fun routePickupAll(
        menu: AbstractContainerMenu,
        slotIndex: Int,
        button: Int,
        player: Player,
    ): Boolean {
        if (slotIndex < 0) return false
        val carried = menu.carried
        val superStack = carried.asSuperStackOrNull() ?: return false
        val clicked = menu.slots[slotIndex]
        if (clicked.hasItem() && clicked.mayPickup(player)) return true
        val order = if (button == 0) menu.slots.indices else menu.slots.indices.reversed()
        for (i in order) {
            val slot = menu.slots[i]
            if (!slot.hasItem() || !slot.mayPickup(player) ||
                !menu.canTakeItemForPickAll(carried, slot)
            ) {
                continue
            }
            val item = slot.item
            val source = item.asSuperStackOrNull()?.takeIf { item.`is`(carried.item) }
            when {
                source != null -> {
                    source.mergeInto(superStack)
                    source.discardIfEmpty()
                    slot.setChanged()
                }

                item.`is`(superStack.type.item) -> superStack.takeFrom(slot, player)
            }
        }
        return true
    }

    @JvmStatic
    public fun routeRemoveFromSelected(inventory: Inventory, all: Boolean): ItemStack? {
        if (all) return null
        val superStack = inventory.selectedItem.asSuperStackOrNull() ?: return null
        val removed = superStack.split(1)
        superStack.discardIfEmpty()
        return removed
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

    @JvmStatic
    public fun splitItemEntity(entity: ItemEntity): Boolean {
        val level = entity.level() as? ServerLevel ?: return false
        val superStack = entity.item.asSuperStackOrNull() ?: return false
        val source = entity.accessor
        val motion = entity.deltaMovement
        for (stack in superStack.drain()) {
            val split =
                ItemEntity(level, entity.x, entity.y, entity.z, stack, motion.x, motion.y, motion.z)
            split.setPickUpDelay(source.`outsider_survival$getPickupDelay`())
            split.accessor.`outsider_survival$setAge`(
                source.`outsider_survival$getAge`(),
            )
            entity.owner?.let(split::setThrower)
            val _ = level.addFreshEntity(split)
        }
        entity.discard()
        return true
    }

    private fun mergeIntoExisting(inventory: Inventory, itemStack: ItemStack): Boolean {
        for (slotStack in inventory) {
            val superStack = slotStack.asSuperStackOrNull() ?: continue
            val _ = superStack.insert(itemStack)
            if (itemStack.isEmpty) return true
        }
        return false
    }

    private val ItemEntity.accessor: ItemEntityAccessor get() = this as ItemEntityAccessor
}
