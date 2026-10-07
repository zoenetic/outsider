package dev.zoenetic.outsider.survival.fabric.gametest

import dev.zoenetic.outsider.survival.gametest.SuperStackClickTests
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.gametest.framework.GameTestHelper

class SuperStackClickGameTests {

    @GameTest
    fun aSuperStackClickedOntoAnotherMergesIntoIt(helper: GameTestHelper): Unit =
        SuperStackClickTests.aSuperStackClickedOntoAnotherMergesIntoIt(helper)

    @GameTest
    fun aMergeThatDoesNotFitLeavesTheRestOnTheCursor(helper: GameTestHelper): Unit =
        SuperStackClickTests.aMergeThatDoesNotFitLeavesTheRestOnTheCursor(helper)

    @GameTest
    fun mergingTwoLitStacksLeavesOneLit(helper: GameTestHelper): Unit =
        SuperStackClickTests.mergingTwoLitStacksLeavesOneLit(helper)

    @GameTest
    fun rightClickingAnEmptySlotPlacesTheActiveTorch(helper: GameTestHelper): Unit =
        SuperStackClickTests.rightClickingAnEmptySlotPlacesTheActiveTorch(helper)

    @GameTest
    fun rightClickingASuperStackMovesOneTorchOver(helper: GameTestHelper): Unit =
        SuperStackClickTests.rightClickingASuperStackMovesOneTorchOver(helper)

    @GameTest
    fun rightClickingTheLastTorchEmptiesTheCursor(helper: GameTestHelper): Unit =
        SuperStackClickTests.rightClickingTheLastTorchEmptiesTheCursor(helper)

    @GameTest
    fun anotherItemClickedOntoASuperStackSwaps(helper: GameTestHelper): Unit =
        SuperStackClickTests.anotherItemClickedOntoASuperStackSwaps(helper)
}
