package dev.zoenetic.outsider.survival.fabric.gametest

import dev.zoenetic.outsider.survival.gametest.LooseStoneTests
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.gametest.framework.GameTestHelper

class LooseStoneGameTests {

    @GameTest
    fun looseStonesNeedASturdyFloor(helper: GameTestHelper): Unit =
        LooseStoneTests.looseStonesNeedASturdyFloor(helper)

    @GameTest
    fun looseStonesBreakWhenTheirSupportIsRemoved(helper: GameTestHelper): Unit =
        LooseStoneTests.looseStonesBreakWhenTheirSupportIsRemoved(helper)

    @GameTest
    fun waterloggedLooseStonesHoldWater(helper: GameTestHelper): Unit =
        LooseStoneTests.waterloggedLooseStonesHoldWater(helper)

    @GameTest
    fun addingAStoneKeepsTheOthersInPlace(helper: GameTestHelper): Unit =
        LooseStoneTests.addingAStoneKeepsTheOthersInPlace(helper)

    @GameTest
    fun theOutlineFollowsTheRenderedArrangement(helper: GameTestHelper): Unit =
        LooseStoneTests.theOutlineFollowsTheRenderedArrangement(helper)

    @GameTest
    fun theFeaturesRunAfterVanillaVegetationInPlains(helper: GameTestHelper): Unit =
        LooseStoneTests.theFeaturesRunAfterVanillaVegetationInPlains(helper)

    @GameTest
    fun placingLooseStonesStacksUpToFour(helper: GameTestHelper): Unit =
        LooseStoneTests.placingLooseStonesStacksUpToFour(helper)

    @GameTest
    fun breakingAClusterDropsOneStonePerStone(helper: GameTestHelper): Unit =
        LooseStoneTests.breakingAClusterDropsOneStonePerStone(helper)
}
