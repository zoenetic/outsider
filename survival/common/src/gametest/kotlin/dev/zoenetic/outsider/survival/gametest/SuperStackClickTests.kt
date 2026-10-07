package dev.zoenetic.outsider.survival.gametest

import dev.zoenetic.outsider.survival.superstack.asSuperStackOrNull
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

private const val FEW = 4
private const val SOME = 5
private const val MANY = 10
private const val PART_BURNT = 7
private const val NEARLY_FULL = 60
private const val BUNDLE_CAPACITY = 64
private const val CLICKED_SLOT = 9
private const val TEST_TICKS = 20

/** Clicking with a super stack on the cursor: merging, placing one, swapping. */
object SuperStackClickTests {

    fun aSuperStackClickedOntoAnotherMergesIntoIt(helper: GameTestHelper) {
        helper.withChestMenuOpen { player ->
            player.inventory.setItem(CLICKED_SLOT, helper.superStackOf(torches(MANY)))
            player.containerMenu.carried = helper.superStackOf(torches(SOME))

            player.leftClickInventorySlot(CLICKED_SLOT)

            val total = helper.totalIn(player.inventory.getItem(CLICKED_SLOT))
            helper.ensure(player.carried.isEmpty && total == MANY + SOME) {
                "expected all ${MANY + SOME} in the slot, found $total, carrying ${player.carried}"
            }
        }
        helper.succeed()
    }

    fun aMergeThatDoesNotFitLeavesTheRestOnTheCursor(helper: GameTestHelper) {
        helper.withChestMenuOpen { player ->
            player.inventory.setItem(CLICKED_SLOT, helper.superStackOf(torches(NEARLY_FULL)))
            player.containerMenu.carried = helper.superStackOf(torches(MANY))

            player.leftClickInventorySlot(CLICKED_SLOT)

            val inSlot = helper.totalIn(player.inventory.getItem(CLICKED_SLOT))
            val onCursor = helper.totalIn(player.carried)
            val fits = BUNDLE_CAPACITY - NEARLY_FULL
            helper.ensure(inSlot == BUNDLE_CAPACITY && onCursor == MANY - fits) {
                "expected $BUNDLE_CAPACITY in the slot and ${MANY - fits} on the cursor, " +
                    "found $inSlot and $onCursor"
            }
        }
        helper.succeed()
    }

    fun mergingTwoLitStacksLeavesOneLit(helper: GameTestHelper) {
        helper.withChestMenuOpen { player ->
            player.inventory.setItem(CLICKED_SLOT, helper.litSuperStackOf(torches(SOME)))
            player.containerMenu.carried = helper.litSuperStackOf(torches(FEW))

            player.leftClickInventorySlot(CLICKED_SLOT)

            val merged = player.inventory.getItem(CLICKED_SLOT)
            helper.ensure(helper.totalIn(merged) == SOME + FEW && merged.litCount() == 1) {
                "expected ${SOME + FEW} torches with one lit, found ${merged.groups()}"
            }
        }
        helper.succeed()
    }

    fun rightClickingAnEmptySlotPlacesTheActiveTorch(helper: GameTestHelper) {
        helper.withChestMenuOpen { player ->
            player.containerMenu.carried =
                helper.mixedSuperStackOf(full = FEW, burnt = 1, burntFuel = PART_BURNT)

            player.rightClickInventorySlot(CLICKED_SLOT)

            val placed = player.inventory.getItem(CLICKED_SLOT)
            helper.ensure(placed.count == 1 && placed.fuelLevel() == PART_BURNT) {
                "expected the one part-burnt torch placed, found $placed"
            }
            helper.ensure(helper.totalIn(player.carried) == FEW) {
                "expected $FEW left on the cursor, found ${player.carried.groups()}"
            }
        }
        helper.succeed()
    }

    fun rightClickingASuperStackMovesOneTorchOver(helper: GameTestHelper) {
        helper.withChestMenuOpen { player ->
            player.inventory.setItem(CLICKED_SLOT, helper.superStackOf(torches(SOME)))
            player.containerMenu.carried =
                helper.mixedSuperStackOf(full = FEW, burnt = 1, burntFuel = PART_BURNT)

            player.rightClickInventorySlot(CLICKED_SLOT)

            val target = player.inventory.getItem(CLICKED_SLOT)
            val gainedTheBurntOne = target.countWithFuel(PART_BURNT) == 1
            helper.ensure(helper.totalIn(target) == SOME + 1 && gainedTheBurntOne) {
                "expected the part-burnt torch added to $SOME, found ${target.groups()}"
            }
            helper.ensure(helper.totalIn(player.carried) == FEW) {
                "expected $FEW left on the cursor, found ${player.carried.groups()}"
            }
        }
        helper.succeed()
    }

    fun rightClickingTheLastTorchEmptiesTheCursor(helper: GameTestHelper) {
        helper.withChestMenuOpen { player ->
            player.containerMenu.carried = helper.superStackOf(torches(1))

            player.rightClickInventorySlot(CLICKED_SLOT)

            val placed = player.inventory.getItem(CLICKED_SLOT)
            helper.ensure(player.carried.isEmpty && placed.count == 1) {
                "expected the last torch placed and an empty cursor, " +
                    "found $placed, carrying ${player.carried}"
            }
        }
        helper.succeed()
    }

    fun anotherItemClickedOntoASuperStackSwaps(helper: GameTestHelper) {
        helper.withChestMenuOpen { player ->
            player.inventory.setItem(CLICKED_SLOT, helper.superStackOf(torches(SOME)))
            player.containerMenu.carried = ItemStack(Items.STICK, FEW)

            player.leftClickInventorySlot(CLICKED_SLOT)

            val inSlot = player.inventory.getItem(CLICKED_SLOT)
            helper.ensure(inSlot.`is`(Items.STICK) && player.carried.asSuperStackOrNull() != null) {
                "expected sticks and super stack swapped, found $inSlot, carrying ${player.carried}"
            }
        }
        helper.succeed()
    }

    val ALL: List<SurvivalTest> = listOf(
        SurvivalTest(
            "a_super_stack_clicked_onto_another_merges_into_it",
            TEST_TICKS,
            ::aSuperStackClickedOntoAnotherMergesIntoIt,
        ),
        SurvivalTest(
            "a_merge_that_does_not_fit_leaves_the_rest_on_the_cursor",
            TEST_TICKS,
            ::aMergeThatDoesNotFitLeavesTheRestOnTheCursor,
        ),
        SurvivalTest(
            "merging_two_lit_stacks_leaves_one_lit",
            TEST_TICKS,
            ::mergingTwoLitStacksLeavesOneLit,
        ),
        SurvivalTest(
            "right_clicking_an_empty_slot_places_the_active_torch",
            TEST_TICKS,
            ::rightClickingAnEmptySlotPlacesTheActiveTorch,
        ),
        SurvivalTest(
            "right_clicking_a_super_stack_moves_one_torch_over",
            TEST_TICKS,
            ::rightClickingASuperStackMovesOneTorchOver,
        ),
        SurvivalTest(
            "right_clicking_the_last_torch_empties_the_cursor",
            TEST_TICKS,
            ::rightClickingTheLastTorchEmptiesTheCursor,
        ),
        SurvivalTest(
            "another_item_clicked_onto_a_super_stack_swaps",
            TEST_TICKS,
            ::anotherItemClickedOntoASuperStackSwaps,
        ),
    )
}
