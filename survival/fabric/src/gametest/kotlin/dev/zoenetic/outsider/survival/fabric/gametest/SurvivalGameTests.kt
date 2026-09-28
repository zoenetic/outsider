package dev.zoenetic.outsider.survival.fabric.gametest

import dev.zoenetic.outsider.survival.gametest.SurvivalTests
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.gametest.framework.GameTestHelper

class SurvivalGameTests {

    @GameTest
    fun aRealServerLevelIsAvailable(helper: GameTestHelper): Unit =
        SurvivalTests.aRealServerLevelIsAvailable(helper)

    @GameTest
    fun conditionsComeFromThePlayersOwnChunk(helper: GameTestHelper): Unit =
        SurvivalTests.conditionsComeFromThePlayersOwnChunk(helper)

    @GameTest
    fun conditionsRecordTheCurrentGameTime(helper: GameTestHelper): Unit =
        SurvivalTests.conditionsRecordTheCurrentGameTime(helper)

    @GameTest
    fun theLevelTickStoresConditionsForPlayersInTheWorld(helper: GameTestHelper): Unit =
        SurvivalTests.theLevelTickStoresConditionsForPlayersInTheWorld(helper)

    @GameTest(maxTicks = 200)
    fun theProductionLoopDrivesBodyTemperature(helper: GameTestHelper): Unit =
        SurvivalTests.theProductionLoopDrivesBodyTemperature(helper)

    @GameTest
    fun placingACampfireRegistersAHeatSource(helper: GameTestHelper): Unit =
        SurvivalTests.placingACampfireRegistersAHeatSource(helper)

    @GameTest
    fun breakingACampfireDeregistersTheHeatSource(helper: GameTestHelper): Unit =
        SurvivalTests.breakingACampfireDeregistersTheHeatSource(helper)

    @GameTest
    fun aNearbyCampfireIsFoundAsAHeatSource(helper: GameTestHelper): Unit =
        SurvivalTests.aNearbyCampfireIsFoundAsAHeatSource(helper)
}
