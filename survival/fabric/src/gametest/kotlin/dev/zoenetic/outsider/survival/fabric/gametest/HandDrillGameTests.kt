package dev.zoenetic.outsider.survival.fabric.gametest

import dev.zoenetic.outsider.survival.gametest.HandDrillTests
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.gametest.framework.GameTestHelper

class HandDrillGameTests {

    @GameTest
    fun usingTheDrillStartsWorkOnTheCampfire(helper: GameTestHelper): Unit =
        HandDrillTests.usingTheDrillStartsWorkOnTheCampfire(helper)

    @GameTest
    fun aDrillInTheOffHandStartsWork(helper: GameTestHelper): Unit =
        HandDrillTests.aDrillInTheOffHandStartsWork(helper)

    @GameTest
    fun aLitCampfireCantBeDrilled(helper: GameTestHelper): Unit =
        HandDrillTests.aLitCampfireCantBeDrilled(helper)

    @GameTest
    fun tinderCantStartWork(helper: GameTestHelper): Unit =
        HandDrillTests.tinderCantStartWork(helper)

    @GameTest
    fun tinderDoesNothingWhileDrilling(helper: GameTestHelper): Unit =
        HandDrillTests.tinderDoesNothingWhileDrilling(helper)

    @GameTest
    fun usingTheDrillOnAnotherCampfireDoesNothing(helper: GameTestHelper): Unit =
        HandDrillTests.usingTheDrillOnAnotherCampfireDoesNothing(helper)

    @GameTest(maxTicks = 900)
    fun drillingFormsAnEmberInOneHold(helper: GameTestHelper): Unit =
        HandDrillTests.drillingFormsAnEmberInOneHold(helper)

    @GameTest(maxTicks = 900)
    fun drillingWearsTheDrillOncePerSecond(helper: GameTestHelper): Unit =
        HandDrillTests.drillingWearsTheDrillOncePerSecond(helper)

    @GameTest
    fun releasingPausesDrilling(helper: GameTestHelper): Unit =
        HandDrillTests.releasingPausesDrilling(helper)

    @GameTest(maxTicks = 30)
    fun releasingBanksTheDrillingSoFar(helper: GameTestHelper): Unit =
        HandDrillTests.releasingBanksTheDrillingSoFar(helper)

    @GameTest
    fun usingTheDrillAgainResumesDrilling(helper: GameTestHelper): Unit =
        HandDrillTests.usingTheDrillAgainResumesDrilling(helper)

    @GameTest
    fun lookingAwayKeepsWorking(helper: GameTestHelper): Unit =
        HandDrillTests.lookingAwayKeepsWorking(helper)

    @GameTest
    fun lookingAwayPausesDrilling(helper: GameTestHelper): Unit =
        HandDrillTests.lookingAwayPausesDrilling(helper)

    @GameTest(maxTicks = 60)
    fun lookingAwayMidDrillBanksOnlyTheDrillingDone(helper: GameTestHelper): Unit =
        HandDrillTests.lookingAwayMidDrillBanksOnlyTheDrillingDone(helper)

    @GameTest(maxTicks = 30)
    fun lookingAwayRestsWithoutChangingTheState(helper: GameTestHelper): Unit =
        HandDrillTests.lookingAwayRestsWithoutChangingTheState(helper)

    @GameTest
    fun puttingTheDrillAwayRestsWithoutEndingWork(helper: GameTestHelper): Unit =
        HandDrillTests.puttingTheDrillAwayRestsWithoutEndingWork(helper)

    @GameTest(maxTicks = 900)
    fun anEmberLeftAloneGoesBackToDrilling(helper: GameTestHelper): Unit =
        HandDrillTests.anEmberLeftAloneGoesBackToDrilling(helper)

    @GameTest(maxTicks = 900)
    fun drillInTheMainHandAndTinderInTheOffHandLightTheCampfire(helper: GameTestHelper): Unit =
        HandDrillTests.drillInTheMainHandAndTinderInTheOffHandLightTheCampfire(helper)

    @GameTest(maxTicks = 900)
    fun tinderInTheMainHandAndDrillInTheOffHandLightTheCampfire(helper: GameTestHelper): Unit =
        HandDrillTests.tinderInTheMainHandAndDrillInTheOffHandLightTheCampfire(helper)

    @GameTest(maxTicks = 900)
    fun swappingOneHandFromDrillToTinderLightsTheCampfire(helper: GameTestHelper): Unit =
        HandDrillTests.swappingOneHandFromDrillToTinderLightsTheCampfire(helper)

    @GameTest
    fun closingWorkEndsIt(helper: GameTestHelper): Unit =
        HandDrillTests.closingWorkEndsIt(helper)

    @GameTest
    fun closingWorkWithoutWorkDoesNothing(helper: GameTestHelper): Unit =
        HandDrillTests.closingWorkWithoutWorkDoesNothing(helper)

    @GameTest
    fun theDrillStartsFreshWorkAfterClosing(helper: GameTestHelper): Unit =
        HandDrillTests.theDrillStartsFreshWorkAfterClosing(helper)

    @GameTest(maxTicks = 900)
    fun closingWorkWhileNursingTheEmberLeavesTheCampfireUnlit(helper: GameTestHelper): Unit =
        HandDrillTests.closingWorkWhileNursingTheEmberLeavesTheCampfireUnlit(helper)
}
