package dev.zoenetic.outsider.survival.fabric.gametest

import dev.zoenetic.outsider.survival.gametest.SuperStackPlacementTests
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.gametest.framework.GameTestHelper

class SuperStackPlacementGameTests {

    @GameTest
    fun placingTheLitTorchLeavesTheStackUnlit(helper: GameTestHelper): Unit =
        SuperStackPlacementTests.placingTheLitTorchLeavesTheStackUnlit(helper)

    @GameTest
    fun placingTheLitTorchInCreativeLeavesTheStackUnlit(helper: GameTestHelper): Unit =
        SuperStackPlacementTests.placingTheLitTorchInCreativeLeavesTheStackUnlit(helper)

    @GameTest
    fun aFailedPlacementKeepsTheTorchLit(helper: GameTestHelper): Unit =
        SuperStackPlacementTests.aFailedPlacementKeepsTheTorchLit(helper)
}
