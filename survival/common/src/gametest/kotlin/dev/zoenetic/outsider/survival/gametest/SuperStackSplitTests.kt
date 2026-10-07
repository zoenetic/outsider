package dev.zoenetic.outsider.survival.gametest

import net.minecraft.gametest.framework.GameTestHelper

private const val FEW = 4
private const val SOME = 5
private const val MANY = 10
private const val ODD = 11
private const val PART_BURNT = 7
private const val CLICKED_SLOT = 9
private const val TEST_TICKS = 20

/** Right-clicking a super stack with an empty cursor takes half, in active-variant order. */
object SuperStackSplitTests {

    fun takingHalfOfAnEvenStack(helper: GameTestHelper) {
        helper.withChestMenuOpen { player ->
            player.inventory.setItem(CLICKED_SLOT, helper.superStackOf(torches(MANY)))

            player.rightClickInventorySlot(CLICKED_SLOT)

            val onCursor = helper.totalIn(player.carried)
            val inSlot = helper.totalIn(player.inventory.getItem(CLICKED_SLOT))
            helper.ensure(onCursor == SOME && inSlot == SOME) {
                "expected $SOME and $SOME, found $onCursor on the cursor and $inSlot in the slot"
            }
        }
        helper.succeed()
    }

    fun takingHalfOfAnOddStackRoundsUp(helper: GameTestHelper) {
        helper.withChestMenuOpen { player ->
            player.inventory.setItem(CLICKED_SLOT, helper.superStackOf(torches(ODD)))

            player.rightClickInventorySlot(CLICKED_SLOT)

            val onCursor = helper.totalIn(player.carried)
            val inSlot = helper.totalIn(player.inventory.getItem(CLICKED_SLOT))
            helper.ensure(onCursor == ODD - SOME && inSlot == SOME) {
                "expected ${ODD - SOME} on the cursor and $SOME left, found $onCursor and $inSlot"
            }
        }
        helper.succeed()
    }

    fun takingHalfTakesTheLitTorch(helper: GameTestHelper) {
        helper.withChestMenuOpen { player ->
            player.inventory.setItem(CLICKED_SLOT, helper.litSuperStackOf(torches(MANY)))

            player.rightClickInventorySlot(CLICKED_SLOT)

            val left = player.inventory.getItem(CLICKED_SLOT)
            val split = helper.totalIn(player.carried) == SOME && helper.totalIn(left) == SOME
            helper.ensure(split && player.carried.litCount() == 1 && left.litCount() == 0) {
                "expected the lit torch on the cursor, found cursor ${player.carried.groups()}, " +
                    "slot ${left.groups()}"
            }
        }
        helper.succeed()
    }

    fun takingHalfOfASingleTorchEmptiesTheSlot(helper: GameTestHelper) {
        helper.withChestMenuOpen { player ->
            player.inventory.setItem(CLICKED_SLOT, helper.superStackOf(torches(1)))

            player.rightClickInventorySlot(CLICKED_SLOT)

            val left = player.inventory.getItem(CLICKED_SLOT)
            helper.ensure(helper.totalIn(player.carried) == 1 && left.isEmpty) {
                "expected the torch on the cursor and an empty slot, found $left"
            }
        }
        helper.succeed()
    }

    fun takingHalfTakesTheLeastFuelFirst(helper: GameTestHelper) {
        helper.withChestMenuOpen { player ->
            player.inventory.setItem(
                CLICKED_SLOT,
                helper.mixedSuperStackOf(full = FEW, burnt = FEW, burntFuel = PART_BURNT),
            )

            player.rightClickInventorySlot(CLICKED_SLOT)

            val onCursor = player.carried
            val allBurnt = onCursor.countWithFuel(PART_BURNT) == FEW
            helper.ensure(helper.totalIn(onCursor) == FEW && allBurnt) {
                "expected the $FEW part-burnt torches on the cursor, found ${onCursor.groups()}"
            }
        }
        helper.succeed()
    }

    val ALL: List<SurvivalTest> = listOf(
        SurvivalTest("taking_half_of_an_even_stack", TEST_TICKS, ::takingHalfOfAnEvenStack),
        SurvivalTest(
            "taking_half_of_an_odd_stack_rounds_up",
            TEST_TICKS,
            ::takingHalfOfAnOddStackRoundsUp,
        ),
        SurvivalTest("taking_half_takes_the_lit_torch", TEST_TICKS, ::takingHalfTakesTheLitTorch),
        SurvivalTest(
            "taking_half_of_a_single_torch_empties_the_slot",
            TEST_TICKS,
            ::takingHalfOfASingleTorchEmptiesTheSlot,
        ),
        SurvivalTest(
            "taking_half_takes_the_least_fuel_first",
            TEST_TICKS,
            ::takingHalfTakesTheLeastFuelFirst,
        ),
    )
}
