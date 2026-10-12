package dev.zoenetic.outsider.survival.fabric.gametest

import dev.zoenetic.outsider.survival.gametest.HandDrillTests
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.gametest.framework.GameTestHelper

class HandDrillGameTests {

    @GameTest
    fun drillingLightsTheCampfireAtTheRolledTime(helper: GameTestHelper): Unit =
        HandDrillTests.drillingLightsTheCampfireAtTheRolledTime(helper)

    @GameTest
    fun drillingWearsTheDrillOncePerSecond(helper: GameTestHelper): Unit =
        HandDrillTests.drillingWearsTheDrillOncePerSecond(helper)

    @GameTest
    fun lookingAwayStopsDrillingWithoutLighting(helper: GameTestHelper): Unit =
        HandDrillTests.lookingAwayStopsDrillingWithoutLighting(helper)

    @GameTest
    fun aLitCampfireCantBeDrilled(helper: GameTestHelper): Unit =
        HandDrillTests.aLitCampfireCantBeDrilled(helper)

    @GameTest
    fun standingCantStartDrilling(helper: GameTestHelper): Unit =
        HandDrillTests.standingCantStartDrilling(helper)

    @GameTest
    fun standingUpStopsDrillingWithoutLighting(helper: GameTestHelper): Unit =
        HandDrillTests.standingUpStopsDrillingWithoutLighting(helper)

    @GameTest
    fun reachingTheLimitKeepsTheDrill(helper: GameTestHelper): Unit =
        HandDrillTests.reachingTheLimitKeepsTheDrill(helper)
}
