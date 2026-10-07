package dev.zoenetic.outsider.survival.fabric.gametest

import dev.zoenetic.outsider.survival.gametest.SuperStackDropTests
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.gametest.framework.GameTestHelper

class SuperStackDropGameTests {

    @GameTest
    fun droppingOneTakesTheActiveTorch(helper: GameTestHelper): Unit =
        SuperStackDropTests.droppingOneTakesTheActiveTorch(helper)

    @GameTest
    fun droppingOneFromALitStackDropsTheLitTorch(helper: GameTestHelper): Unit =
        SuperStackDropTests.droppingOneFromALitStackDropsTheLitTorch(helper)

    @GameTest
    fun droppingTheLastTorchEmptiesTheSlot(helper: GameTestHelper): Unit =
        SuperStackDropTests.droppingTheLastTorchEmptiesTheSlot(helper)

    @GameTest
    fun aDroppedSuperStackSplitsIntoPlainTorches(helper: GameTestHelper): Unit =
        SuperStackDropTests.aDroppedSuperStackSplitsIntoPlainTorches(helper)
}
