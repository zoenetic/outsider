package dev.zoenetic.outsider.survival.fabric.gametest

import dev.zoenetic.outsider.survival.gametest.SuperStackTests
import dev.zoenetic.outsider.survival.gametest.TorchLootTests
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.gametest.framework.GameTestHelper

class SuperStackGameTests {

    @GameTest
    fun aBrokenTorchDropsWithItsFuelAndFlame(helper: GameTestHelper): Unit =
        TorchLootTests.aBrokenTorchDropsWithItsFuelAndFlame(helper)

    @GameTest
    fun aBrokenWallTorchDropsWithItsFuel(helper: GameTestHelper): Unit =
        TorchLootTests.aBrokenWallTorchDropsWithItsFuel(helper)

    @GameTest
    fun aLitTorchEnteringASuperStackIsSnuffed(helper: GameTestHelper): Unit =
        SuperStackTests.aLitTorchEnteringASuperStackIsSnuffed(helper)

    @GameTest
    fun lightingASuperStackLightsExactlyOneTorch(helper: GameTestHelper): Unit =
        SuperStackTests.lightingASuperStackLightsExactlyOneTorch(helper)

    @GameTest
    fun pickupMergesIntoAnExistingSuperStack(helper: GameTestHelper): Unit =
        SuperStackTests.pickupMergesIntoAnExistingSuperStack(helper)

    @GameTest
    fun pickupWithNoSuperStackWrapsTheTorches(helper: GameTestHelper): Unit =
        SuperStackTests.pickupWithNoSuperStackWrapsTheTorches(helper)

    @GameTest
    fun pickupIntoAFullInventoryLeavesTheTorches(helper: GameTestHelper): Unit =
        SuperStackTests.pickupIntoAFullInventoryLeavesTheTorches(helper)

    @GameTest
    fun aPlainTorchInTheInventoryIsWrappedInPlace(helper: GameTestHelper): Unit =
        SuperStackTests.aPlainTorchInTheInventoryIsWrappedInPlace(helper)

    @GameTest
    fun placingTheLastTorchEmptiesTheHand(helper: GameTestHelper): Unit =
        SuperStackTests.placingTheLastTorchEmptiesTheHand(helper)
}
