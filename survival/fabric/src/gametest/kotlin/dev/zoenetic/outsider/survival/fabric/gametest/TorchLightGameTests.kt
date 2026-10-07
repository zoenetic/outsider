package dev.zoenetic.outsider.survival.fabric.gametest

import dev.zoenetic.outsider.survival.gametest.TorchLightTests
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.gametest.framework.GameTestHelper

class TorchLightGameTests {

    @GameTest
    fun everyTorchStateCachesItsOwnLight(helper: GameTestHelper): Unit =
        TorchLightTests.everyTorchStateCachesItsOwnLight(helper)

    @GameTest
    fun aPlacedLitTorchLightsItsBlock(helper: GameTestHelper): Unit =
        TorchLightTests.aPlacedLitTorchLightsItsBlock(helper)
}
