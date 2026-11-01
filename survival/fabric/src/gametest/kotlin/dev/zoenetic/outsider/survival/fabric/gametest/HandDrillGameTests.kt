package dev.zoenetic.outsider.survival.fabric.gametest

import dev.zoenetic.outsider.survival.gametest.HandDrillTests
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.gametest.framework.GameTestHelper

class HandDrillGameTests {

    @GameTest(maxTicks = 900)
    fun outOfBreathTheBlowPausesToInhale(helper: GameTestHelper): Unit =
        HandDrillTests.outOfBreathTheBlowPausesToInhale(helper)

    @GameTest
    fun startingWorkLeavesThePlayerWhereTheyStand(helper: GameTestHelper): Unit =
        HandDrillTests.startingWorkLeavesThePlayerWhereTheyStand(helper)

    @GameTest
    fun theDrillCantStartOffTheGround(helper: GameTestHelper): Unit =
        HandDrillTests.theDrillCantStartOffTheGround(helper)

    @GameTest
    fun theDrillCantStartWithTheBoardOverALedge(helper: GameTestHelper): Unit =
        HandDrillTests.theDrillCantStartWithTheBoardOverALedge(helper)

    @GameTest
    fun losingTheBoardsFootingEndsTheWork(helper: GameTestHelper): Unit =
        HandDrillTests.losingTheBoardsFootingEndsTheWork(helper)

    @GameTest
    fun leavingTheGroundEndsTheWork(helper: GameTestHelper): Unit =
        HandDrillTests.leavingTheGroundEndsTheWork(helper)

    @GameTest
    fun aUseWhileLookingAwayDoesNothing(helper: GameTestHelper): Unit =
        HandDrillTests.aUseWhileLookingAwayDoesNothing(helper)

    @GameTest(maxTicks = 900)
    fun blowingCarriesOnWhileLookingAway(helper: GameTestHelper): Unit =
        HandDrillTests.blowingCarriesOnWhileLookingAway(helper)

    @GameTest
    fun theBodyFacesTheWorkWhileLookingAway(helper: GameTestHelper): Unit =
        HandDrillTests.theBodyFacesTheWorkWhileLookingAway(helper)

    @GameTest
    fun usingTheDrillStartsWorkWithTheBoardInFront(helper: GameTestHelper): Unit =
        HandDrillTests.usingTheDrillStartsWorkWithTheBoardInFront(helper)

    @GameTest
    fun usingTheDrillOnABlockStartsWork(helper: GameTestHelper): Unit =
        HandDrillTests.usingTheDrillOnABlockStartsWork(helper)

    @GameTest
    fun aDrillInTheOffHandStartsWork(helper: GameTestHelper): Unit =
        HandDrillTests.aDrillInTheOffHandStartsWork(helper)

    @GameTest
    fun tinderCantStartWork(helper: GameTestHelper): Unit =
        HandDrillTests.tinderCantStartWork(helper)

    @GameTest
    fun tinderDoesNothingWhileDrilling(helper: GameTestHelper): Unit =
        HandDrillTests.tinderDoesNothingWhileDrilling(helper)

    @GameTest
    fun aUseAnywhereContinuesTheWork(helper: GameTestHelper): Unit =
        HandDrillTests.aUseAnywhereContinuesTheWork(helper)

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

    @GameTest(maxTicks = 900)
    fun theTinderCatchesAsAWholeStackAndEndsTheWork(helper: GameTestHelper): Unit =
        HandDrillTests.theTinderCatchesAsAWholeStackAndEndsTheWork(helper)

    @GameTest(maxTicks = 900)
    fun lightingTheCampfireUsesOneTinder(helper: GameTestHelper): Unit =
        HandDrillTests.lightingTheCampfireUsesOneTinder(helper)

    @GameTest(maxTicks = 900)
    fun lightingTheCampfireWithTheLastTinderUsesItUp(helper: GameTestHelper): Unit =
        HandDrillTests.lightingTheCampfireWithTheLastTinderUsesItUp(helper)

    @GameTest(maxTicks = 1100)
    fun litTinderLeftAloneBurnsOut(helper: GameTestHelper): Unit =
        HandDrillTests.litTinderLeftAloneBurnsOut(helper)

    @GameTest(maxTicks = 1100)
    fun holdingTheLitTinderTooLongBurnsItOutInTheHand(helper: GameTestHelper): Unit =
        HandDrillTests.holdingTheLitTinderTooLongBurnsItOutInTheHand(helper)

    @GameTest(maxTicks = 1100)
    fun litTinderBurnsOutOnItsOwnTimer(helper: GameTestHelper): Unit =
        HandDrillTests.litTinderBurnsOutOnItsOwnTimer(helper)

    @GameTest
    fun litTinderWithoutABurnOutTimeBurnsOut(helper: GameTestHelper): Unit =
        HandDrillTests.litTinderWithoutABurnOutTimeBurnsOut(helper)

    @GameTest
    fun litTinderLightsACampfire(helper: GameTestHelper): Unit =
        HandDrillTests.litTinderLightsACampfire(helper)

    @GameTest
    fun litTinderLightsAFuelledTorch(helper: GameTestHelper): Unit =
        HandDrillTests.litTinderLightsAFuelledTorch(helper)

    @GameTest
    fun litTinderLightsAFuelledWallTorch(helper: GameTestHelper): Unit =
        HandDrillTests.litTinderLightsAFuelledWallTorch(helper)

    @GameTest
    fun litTinderLeavesASpentTorchDark(helper: GameTestHelper): Unit =
        HandDrillTests.litTinderLeavesASpentTorchDark(helper)

    @GameTest
    fun litTinderPassesOnALitCampfire(helper: GameTestHelper): Unit =
        HandDrillTests.litTinderPassesOnALitCampfire(helper)

    @GameTest
    fun litTinderLightsACandle(helper: GameTestHelper): Unit =
        HandDrillTests.litTinderLightsACandle(helper)

    @GameTest
    fun litTinderLightsACandleCake(helper: GameTestHelper): Unit =
        HandDrillTests.litTinderLightsACandleCake(helper)

    @GameTest
    fun litTinderLeavesAWaterloggedCandleUnlit(helper: GameTestHelper): Unit =
        HandDrillTests.litTinderLeavesAWaterloggedCandleUnlit(helper)

    @GameTest
    fun litTinderLeavesAWaterloggedCampfireUnlit(helper: GameTestHelper): Unit =
        HandDrillTests.litTinderLeavesAWaterloggedCampfireUnlit(helper)

    @GameTest
    fun litTinderSetsFireBesideAFlammableBlock(helper: GameTestHelper): Unit =
        HandDrillTests.litTinderSetsFireBesideAFlammableBlock(helper)

    @GameTest
    fun litTinderSetsFireOnTheGround(helper: GameTestHelper): Unit =
        HandDrillTests.litTinderSetsFireOnTheGround(helper)

    @GameTest
    fun litTinderPassesWhereFireCantSit(helper: GameTestHelper): Unit =
        HandDrillTests.litTinderPassesWhereFireCantSit(helper)

    @GameTest(maxTicks = 60)
    fun aDrillThatBreaksEndsTheWork(helper: GameTestHelper): Unit =
        HandDrillTests.aDrillThatBreaksEndsTheWork(helper)

    @GameTest
    fun theDrillPassesWhileHoldingLitTinder(helper: GameTestHelper): Unit =
        HandDrillTests.theDrillPassesWhileHoldingLitTinder(helper)

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
