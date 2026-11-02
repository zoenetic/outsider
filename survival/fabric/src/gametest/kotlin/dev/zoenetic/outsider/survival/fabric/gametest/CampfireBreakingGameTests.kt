package dev.zoenetic.outsider.survival.fabric.gametest

import dev.zoenetic.outsider.survival.gametest.CampfireBreakingTests
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.gametest.framework.GameTestHelper

class CampfireBreakingGameTests {

    @GameTest
    fun aDeadCampfireBreaksInstantly(helper: GameTestHelper): Unit =
        CampfireBreakingTests.aDeadCampfireBreaksInstantly(helper)

    @GameTest
    fun aDeadCampfireIsAsLowAsItsModel(helper: GameTestHelper): Unit =
        CampfireBreakingTests.aDeadCampfireIsAsLowAsItsModel(helper)

    @GameTest
    fun aDeadCampfireDropsOneToFourCharcoal(helper: GameTestHelper): Unit =
        CampfireBreakingTests.aDeadCampfireDropsOneToFourCharcoal(helper)

    @GameTest
    fun aCampfireSinksAsItBurns(helper: GameTestHelper): Unit =
        CampfireBreakingTests.aCampfireSinksAsItBurns(helper)

    @GameTest
    fun aCampfireOnItsLastFuelDropsCharcoalLikeADeadOne(helper: GameTestHelper): Unit =
        CampfireBreakingTests.aCampfireOnItsLastFuelDropsCharcoalLikeADeadOne(helper)

    @GameTest
    fun aBarelyUsedCampfireDropsWhole(helper: GameTestHelper): Unit =
        CampfireBreakingTests.aBarelyUsedCampfireDropsWhole(helper)

    @GameTest
    fun aBurningDownCampfireDropsItsFuel(helper: GameTestHelper): Unit =
        CampfireBreakingTests.aBurningDownCampfireDropsItsFuel(helper)
}
