package dev.zoenetic.outsider.survival.gametest

import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameType

private const val CHEST_SLOTS = 27
private const val LEFT_BUTTON = 0
private const val RIGHT_BUTTON = 1

// The inventory menu's slotsChanged updates the crafting grid, which needs a connected server
// player, so clicks go through a chest menu over the player's inventory instead. Chest slots are
// menu slots 0 until 27; inventory slot n (n >= 9) is menu slot 27 + n - 9.
internal fun GameTestHelper.withChestMenuOpen(test: (Player) -> kotlin.Unit) {
    val player = makeMockPlayer(GameType.SURVIVAL)
    player.containerMenu = ChestMenu.threeRows(0, player.inventory)
    test(player)
}

private fun inventoryMenuSlot(inventorySlot: Int): Int =
    CHEST_SLOTS + inventorySlot - Inventory.SELECTION_SIZE

private fun Player.click(menuSlot: Int, button: Int, input: ContainerInput) {
    containerMenu.clicked(menuSlot, button, input, this)
}

internal fun Player.leftClickInventorySlot(slot: Int) {
    click(inventoryMenuSlot(slot), LEFT_BUTTON, ContainerInput.PICKUP)
}

internal fun Player.rightClickInventorySlot(slot: Int) {
    click(inventoryMenuSlot(slot), RIGHT_BUTTON, ContainerInput.PICKUP)
}

internal fun Player.doubleClickInventorySlot(slot: Int) {
    click(inventoryMenuSlot(slot), LEFT_BUTTON, ContainerInput.PICKUP_ALL)
}

internal fun Player.shiftClickInventorySlot(slot: Int) {
    click(inventoryMenuSlot(slot), LEFT_BUTTON, ContainerInput.QUICK_MOVE)
}

internal fun Player.shiftClickChestSlot(slot: Int) {
    click(slot, LEFT_BUTTON, ContainerInput.QUICK_MOVE)
}

internal fun Player.chestItem(slot: Int): ItemStack = containerMenu.getSlot(slot).item

internal fun Player.setChestItem(slot: Int, stack: ItemStack) {
    containerMenu.getSlot(slot).set(stack)
}

internal val Player.chestContents: List<ItemStack>
    get() = (0 until CHEST_SLOTS).map(::chestItem)

internal val Player.carried: ItemStack get() = containerMenu.carried
