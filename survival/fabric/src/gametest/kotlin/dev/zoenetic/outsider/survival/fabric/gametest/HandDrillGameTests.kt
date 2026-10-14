package dev.zoenetic.outsider.survival.fabric.gametest

import dev.zoenetic.outsider.survival.gametest.HandDrillTests
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.gametest.framework.GameTestHelper

class HandDrillGameTests {

    @GameTest
    fun usingTheDrillStartsWorkOnTheCampfire(helper: GameTestHelper): Unit =
        HandDrillTests.usingTheDrillStartsWorkOnTheCampfire(helper)

    @GameTest(maxTicks = 300)
    fun drillingLightsTheCampfireAfterTheRolledTime(helper: GameTestHelper): Unit =
        HandDrillTests.drillingLightsTheCampfireAfterTheRolledTime(helper)

    @GameTest(maxTicks = 300)
    fun drillingWearsTheDrillOncePerSecond(helper: GameTestHelper): Unit =
        HandDrillTests.drillingWearsTheDrillOncePerSecond(helper)

    @GameTest
    fun releasingPausesDrilling(helper: GameTestHelper): Unit =
        HandDrillTests.releasingPausesDrilling(helper)

    @GameTest
    fun usingTheDrillAgainResumesDrilling(helper: GameTestHelper): Unit =
        HandDrillTests.usingTheDrillAgainResumesDrilling(helper)

    @GameTest
    fun lookingAwayEndsWorkWithoutLighting(helper: GameTestHelper): Unit =
        HandDrillTests.lookingAwayEndsWorkWithoutLighting(helper)

    @GameTest
    fun puttingTheDrillAwayEndsWork(helper: GameTestHelper): Unit =
        HandDrillTests.puttingTheDrillAwayEndsWork(helper)

    @GameTest
    fun aLitCampfireCantBeDrilled(helper: GameTestHelper): Unit =
        HandDrillTests.aLitCampfireCantBeDrilled(helper)
}
