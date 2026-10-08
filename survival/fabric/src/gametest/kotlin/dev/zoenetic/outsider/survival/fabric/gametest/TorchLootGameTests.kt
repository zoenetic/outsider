package dev.zoenetic.outsider.survival.fabric.gametest

import dev.zoenetic.outsider.survival.gametest.TorchLootTests
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.gametest.framework.GameTestHelper

class TorchLootGameTests {

    @GameTest
    fun aBrokenTorchDropsWithItsLitState(helper: GameTestHelper): Unit =
        TorchLootTests.aBrokenTorchDropsWithLitState(helper)

    @GameTest
    fun aLitTorchPastItsGracePeriodDropsDeadAndUnlit(helper: GameTestHelper): Unit =
        TorchLootTests.aLitTorchPastItsGracePeriodDropsDeadAndUnlit(helper)

    @GameTest
    fun aTorchBrokenInItsGracePeriodDropsFuelled(helper: GameTestHelper): Unit =
        TorchLootTests.aTorchBrokenInItsGracePeriodDropsFuelled(helper)

    @GameTest
    fun aWallTorchBrokenInItsGracePeriodDropsFuelled(helper: GameTestHelper): Unit =
        TorchLootTests.aWallTorchBrokenInItsGracePeriodDropsFuelled(helper)

    @GameTest
    fun aBurntOutTorchDropsDead(helper: GameTestHelper): Unit =
        TorchLootTests.aBurntOutTorchDropsDead(helper)
}
