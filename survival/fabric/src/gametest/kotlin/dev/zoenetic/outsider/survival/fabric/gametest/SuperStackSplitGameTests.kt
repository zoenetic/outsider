package dev.zoenetic.outsider.survival.fabric.gametest

import dev.zoenetic.outsider.survival.gametest.SuperStackSplitTests
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.gametest.framework.GameTestHelper

class SuperStackSplitGameTests {

    @GameTest
    fun takingHalfOfAnEvenStack(helper: GameTestHelper): Unit =
        SuperStackSplitTests.takingHalfOfAnEvenStack(helper)

    @GameTest
    fun takingHalfOfAnOddStackRoundsUp(helper: GameTestHelper): Unit =
        SuperStackSplitTests.takingHalfOfAnOddStackRoundsUp(helper)

    @GameTest
    fun takingHalfTakesTheLitTorch(helper: GameTestHelper): Unit =
        SuperStackSplitTests.takingHalfTakesTheLitTorch(helper)

    @GameTest
    fun takingHalfOfASingleTorchEmptiesTheSlot(helper: GameTestHelper): Unit =
        SuperStackSplitTests.takingHalfOfASingleTorchEmptiesTheSlot(helper)

    @GameTest
    fun takingHalfTakesTheLeastFuelFirst(helper: GameTestHelper): Unit =
        SuperStackSplitTests.takingHalfTakesTheLeastFuelFirst(helper)
}
