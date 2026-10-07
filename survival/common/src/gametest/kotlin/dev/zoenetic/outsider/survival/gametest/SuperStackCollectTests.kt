package dev.zoenetic.outsider.survival.gametest

import dev.zoenetic.outsider.survival.superstack.asSuperStackOrNull
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.item.ItemStack

private const val FEW = 4
private const val SOME = 5
private const val MANY = 10
private const val PART_BURNT = 7
private const val NEARLY_FULL = 60
private const val BUNDLE_CAPACITY = 64
private const val CLICKED_SLOT = 9
private const val OTHER_SLOT = 10
private const val CHEST_SLOT = 0
private const val TEST_TICKS = 20

private fun ItemStack.torchTotal(): Int = asSuperStackOrNull()?.count ?: count

/** Game-chosen destinations: double-click collect and shift-click merge into super stacks. */
object SuperStackCollectTests {

    fun doubleClickCollectsTorchesFromTheChestAndInventory(helper: GameTestHelper) {
        helper.withChestMenuOpen { player ->
            player.containerMenu.carried = helper.litSuperStackOf(torches(FEW))
            player.inventory.setItem(OTHER_SLOT, helper.litSuperStackOf(torches(SOME)))
            player.setChestItem(CHEST_SLOT, torches(MANY, fuel = PART_BURNT))

            player.doubleClickInventorySlot(CLICKED_SLOT)

            val collected = player.carried
            val total = helper.totalIn(collected)
            helper.ensure(total == FEW + SOME + MANY && collected.litCount() == 1) {
                "expected ${FEW + SOME + MANY} collected with one lit, found ${collected.groups()}"
            }
            val otherEmptied = player.inventory.getItem(OTHER_SLOT).isEmpty
            helper.ensure(otherEmptied && player.chestItem(CHEST_SLOT).isEmpty) {
                "expected the sources emptied, found ${player.inventory.getItem(OTHER_SLOT)} " +
                    "and ${player.chestItem(CHEST_SLOT)}"
            }
        }
        helper.succeed()
    }

    fun doubleClickStopsWhenFull(helper: GameTestHelper) {
        helper.withChestMenuOpen { player ->
            player.containerMenu.carried = helper.superStackOf(torches(NEARLY_FULL))
            player.setChestItem(CHEST_SLOT, torches(MANY))

            player.doubleClickInventorySlot(CLICKED_SLOT)

            val collected = helper.totalIn(player.carried)
            val left = player.chestItem(CHEST_SLOT).count
            val fits = BUNDLE_CAPACITY - NEARLY_FULL
            helper.ensure(collected == BUNDLE_CAPACITY && left == MANY - fits) {
                "expected $BUNDLE_CAPACITY collected and ${MANY - fits} left, " +
                    "found $collected and $left"
            }
        }
        helper.succeed()
    }

    fun shiftClickingPlainTorchesMergesIntoTheInventoryStack(helper: GameTestHelper) {
        helper.withChestMenuOpen { player ->
            player.inventory.setItem(CLICKED_SLOT, helper.superStackOf(torches(MANY)))
            player.setChestItem(CHEST_SLOT, torches(SOME, lit = true))

            player.shiftClickChestSlot(CHEST_SLOT)

            val merged = player.inventory.getItem(CLICKED_SLOT)
            helper.ensure(helper.totalIn(merged) == MANY + SOME && merged.litCount() == 0) {
                "expected ${MANY + SOME} unlit torches, found ${merged.groups()}"
            }
            val occupied = player.inventory.count { !it.isEmpty }
            helper.ensure(player.chestItem(CHEST_SLOT).isEmpty && occupied == 1) {
                "expected everything in the one super stack, " +
                    "inventory ${player.inventory.filter { !it.isEmpty }}"
            }
        }
        helper.succeed()
    }

    fun shiftClickOverflowLandsInAnEmptySlot(helper: GameTestHelper) {
        helper.withChestMenuOpen { player ->
            player.inventory.setItem(CLICKED_SLOT, helper.superStackOf(torches(NEARLY_FULL)))
            player.setChestItem(CHEST_SLOT, torches(MANY))

            player.shiftClickChestSlot(CHEST_SLOT)

            val total = player.inventory.sumOf { it.torchTotal() }
            val full = helper.totalIn(player.inventory.getItem(CLICKED_SLOT))
            helper.ensure(full == BUNDLE_CAPACITY && total == NEARLY_FULL + MANY) {
                "expected the stack filled and the rest elsewhere, found $full of $total"
            }
            helper.ensure(player.chestItem(CHEST_SLOT).isEmpty) {
                "expected the chest slot emptied, found ${player.chestItem(CHEST_SLOT)}"
            }
        }
        helper.succeed()
    }

    fun shiftClickingASuperStackMergesIntoTheChestStack(helper: GameTestHelper) {
        helper.withChestMenuOpen { player ->
            player.setChestItem(CHEST_SLOT, helper.superStackOf(torches(MANY)))
            player.inventory.setItem(CLICKED_SLOT, helper.superStackOf(torches(SOME)))

            player.shiftClickInventorySlot(CLICKED_SLOT)

            val merged = helper.totalIn(player.chestItem(CHEST_SLOT))
            helper.ensure(merged == MANY + SOME && player.inventory.getItem(CLICKED_SLOT).isEmpty) {
                "expected ${MANY + SOME} in the chest stack and the slot emptied, found $merged"
            }
        }
        helper.succeed()
    }

    fun aPartlyFittingSuperStackPutsTheRestInAnEmptyChestSlot(helper: GameTestHelper) {
        helper.withChestMenuOpen { player ->
            player.setChestItem(CHEST_SLOT, helper.superStackOf(torches(NEARLY_FULL)))
            player.inventory.setItem(CLICKED_SLOT, helper.superStackOf(torches(MANY)))

            player.shiftClickInventorySlot(CLICKED_SLOT)

            val full = helper.totalIn(player.chestItem(CHEST_SLOT))
            val inChest = player.chestContents.sumOf { it.torchTotal() }
            helper.ensure(full == BUNDLE_CAPACITY && inChest == NEARLY_FULL + MANY) {
                "expected the chest stack filled and the rest in the chest, found $full of $inChest"
            }
            helper.ensure(player.inventory.getItem(CLICKED_SLOT).isEmpty) {
                "expected the inventory slot emptied, " +
                    "found ${player.inventory.getItem(CLICKED_SLOT)}"
            }
        }
        helper.succeed()
    }

    fun aSuperStackShiftClickedIntoAChestWithoutOneMovesWhole(helper: GameTestHelper) {
        helper.withChestMenuOpen { player ->
            player.inventory.setItem(CLICKED_SLOT, helper.superStackOf(torches(SOME)))

            player.shiftClickInventorySlot(CLICKED_SLOT)

            val moved = player.chestItem(CHEST_SLOT)
            helper.ensure(moved.asSuperStackOrNull()?.count == SOME) {
                "expected the super stack of $SOME in the chest, found $moved"
            }
        }
        helper.succeed()
    }

    val ALL: List<SurvivalTest> = listOf(
        SurvivalTest(
            "double_click_collects_torches_from_the_chest_and_inventory",
            TEST_TICKS,
            ::doubleClickCollectsTorchesFromTheChestAndInventory,
        ),
        SurvivalTest("double_click_stops_when_full", TEST_TICKS, ::doubleClickStopsWhenFull),
        SurvivalTest(
            "shift_clicking_plain_torches_merges_into_the_inventory_stack",
            TEST_TICKS,
            ::shiftClickingPlainTorchesMergesIntoTheInventoryStack,
        ),
        SurvivalTest(
            "shift_click_overflow_lands_in_an_empty_slot",
            TEST_TICKS,
            ::shiftClickOverflowLandsInAnEmptySlot,
        ),
        SurvivalTest(
            "shift_clicking_a_super_stack_merges_into_the_chest_stack",
            TEST_TICKS,
            ::shiftClickingASuperStackMergesIntoTheChestStack,
        ),
        SurvivalTest(
            "a_partly_fitting_super_stack_puts_the_rest_in_an_empty_chest_slot",
            TEST_TICKS,
            ::aPartlyFittingSuperStackPutsTheRestInAnEmptyChestSlot,
        ),
        SurvivalTest(
            "a_super_stack_shift_clicked_into_a_chest_without_one_moves_whole",
            TEST_TICKS,
            ::aSuperStackShiftClickedIntoAChestWithoutOneMovesWhole,
        ),
    )
}
