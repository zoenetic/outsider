package dev.zoenetic.outsider.survival.fabric.gametest

import dev.zoenetic.outsider.survival.gametest.TorchLootTests
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.gametest.framework.GameTestHelper

class TorchLootGameTests {

    @GameTest
    fun aBrokenTorchDropsWithItsLitState(helper: GameTestHelper): Unit =
        TorchLootTests.aBrokenTorchDropsWithLitState(helper)
}
