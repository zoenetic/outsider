package dev.zoenetic.outsider.survival.fabric.gametest

import dev.zoenetic.outsider.survival.gametest.WorkingModeTests
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.gametest.framework.GameTestHelper

class WorkingModeGameTests {

    @GameTest
    fun theDrillAtAnUnlitCampfireIsReady(helper: GameTestHelper): Unit =
        WorkingModeTests.theDrillAtAnUnlitCampfireIsReady(helper)

    @GameTest
    fun theDrillInTheOffHandCounts(helper: GameTestHelper): Unit =
        WorkingModeTests.theDrillInTheOffHandCounts(helper)

    @GameTest
    fun withoutAWorkingToolIsNotReady(helper: GameTestHelper): Unit =
        WorkingModeTests.withoutAWorkingToolIsNotReady(helper)

    @GameTest
    fun lookingAwayIsNotReady(helper: GameTestHelper): Unit =
        WorkingModeTests.lookingAwayIsNotReady(helper)

    @GameTest
    fun aLitCampfireIsNotReady(helper: GameTestHelper): Unit =
        WorkingModeTests.aLitCampfireIsNotReady(helper)
}
