package dev.zoenetic.outsider.survival.fabric.gametest

import dev.zoenetic.outsider.survival.gametest.PlantFibreTests
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.gametest.framework.GameTestHelper

class PlantFibreGameTests {

    @GameTest
    fun shortGrassDropsPlantFibre(helper: GameTestHelper): Unit =
        PlantFibreTests.shortGrassDropsPlantFibre(helper)

    @GameTest
    fun fernsDropPlantFibre(helper: GameTestHelper): Unit =
        PlantFibreTests.fernsDropPlantFibre(helper)

    @GameTest
    fun shearsNeverGetPlantFibre(helper: GameTestHelper): Unit =
        PlantFibreTests.shearsNeverGetPlantFibre(helper)

    @GameTest
    fun twoSticksMakeAHandDrill(helper: GameTestHelper): Unit =
        PlantFibreTests.twoSticksMakeAHandDrill(helper)

    @GameTest
    fun eachTinderMaterialMakesOneTinder(helper: GameTestHelper): Unit =
        PlantFibreTests.eachTinderMaterialMakesOneTinder(helper)

    @GameTest
    fun otherPlantsDontMakeTinder(helper: GameTestHelper): Unit =
        PlantFibreTests.otherPlantsDontMakeTinder(helper)
}
