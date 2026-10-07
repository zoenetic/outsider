package dev.zoenetic.outsider.survival.fabric.gametest

import dev.zoenetic.outsider.survival.gametest.SuperStackCollectTests
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.gametest.framework.GameTestHelper

class SuperStackCollectGameTests {

    @GameTest
    fun doubleClickCollectsTorchesFromTheChestAndInventory(helper: GameTestHelper): Unit =
        SuperStackCollectTests.doubleClickCollectsTorchesFromTheChestAndInventory(helper)

    @GameTest
    fun doubleClickStopsWhenFull(helper: GameTestHelper): Unit =
        SuperStackCollectTests.doubleClickStopsWhenFull(helper)

    @GameTest
    fun shiftClickingPlainTorchesMergesIntoTheInventoryStack(helper: GameTestHelper): Unit =
        SuperStackCollectTests.shiftClickingPlainTorchesMergesIntoTheInventoryStack(helper)

    @GameTest
    fun shiftClickOverflowLandsInAnEmptySlot(helper: GameTestHelper): Unit =
        SuperStackCollectTests.shiftClickOverflowLandsInAnEmptySlot(helper)

    @GameTest
    fun shiftClickingASuperStackMergesIntoTheChestStack(helper: GameTestHelper): Unit =
        SuperStackCollectTests.shiftClickingASuperStackMergesIntoTheChestStack(helper)

    @GameTest
    fun aPartlyFittingSuperStackPutsTheRestInAnEmptyChestSlot(helper: GameTestHelper): Unit =
        SuperStackCollectTests.aPartlyFittingSuperStackPutsTheRestInAnEmptyChestSlot(helper)

    @GameTest
    fun aSuperStackShiftClickedIntoAChestWithoutOneMovesWhole(helper: GameTestHelper): Unit =
        SuperStackCollectTests.aSuperStackShiftClickedIntoAChestWithoutOneMovesWhole(helper)
}
