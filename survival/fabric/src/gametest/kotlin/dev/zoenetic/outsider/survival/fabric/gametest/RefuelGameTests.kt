package dev.zoenetic.outsider.survival.fabric.gametest

import dev.zoenetic.outsider.survival.gametest.RefuelTests
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.gametest.framework.GameTestHelper

class RefuelGameTests {

    @GameTest(maxTicks = 1000)
    fun refuellingALitCampfireKeepsItsPartialStep(helper: GameTestHelper): Unit =
        RefuelTests.refuellingALitCampfireKeepsItsPartialStep(helper)

    @GameTest(maxTicks = 1000)
    fun refuellingAnUnlitCampfireAddsToItsLevel(helper: GameTestHelper): Unit =
        RefuelTests.refuellingAnUnlitCampfireAddsToItsLevel(helper)

    @GameTest(maxTicks = 1000)
    fun aRefuelledCampfireDropsAtItsNextStep(helper: GameTestHelper): Unit =
        RefuelTests.aRefuelledCampfireDropsAtItsNextStep(helper)
}
