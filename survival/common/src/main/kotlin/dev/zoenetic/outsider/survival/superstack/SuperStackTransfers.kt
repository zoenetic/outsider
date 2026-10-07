package dev.zoenetic.outsider.survival.superstack

import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

internal fun SuperStack.mergeInto(target: SuperStack) {
    drain()
        .onEach { val _ = target.insert(it) }
        .filterNot { it.isEmpty }
        .forEach { val _ = reinsert(it) }
}

internal fun SuperStack.placeOneInto(target: SuperStack) {
    val one = split(1)
    val _ = target.insert(one)
    if (!one.isEmpty) {
        val _ = reinsert(one)
    }
}

internal fun SuperStack.placeOneInto(slot: Slot) {
    val leftover = slot.safeInsert(split(1))
    if (!leftover.isEmpty) {
        val _ = reinsert(leftover)
    }
}

internal fun SuperStack.takeFrom(slot: Slot, player: Player) {
    val other = slot.item
    val room = insertableCount(other)
    val taken = if (room > 0) slot.safeTake(other.count, room, player) else ItemStack.EMPTY
    val _ = insert(taken)
    if (!taken.isEmpty) {
        val leftover = slot.safeInsert(taken)
        if (!leftover.isEmpty) player.inventory.placeItemBackInInventory(leftover)
    }
}
