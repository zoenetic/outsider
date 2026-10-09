package dev.zoenetic.outsider.survival.gametest

import dev.zoenetic.outsider.survival.emission.EmittingBlock
import dev.zoenetic.outsider.survival.fuel.Fuel
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlockStateProperties.FUEL_LEVEL
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlocks
import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.level.LightLayer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT

private const val TEST_TICKS = 20
private val GROUND = BlockPos(1, 1, 1)

private fun Block.litAtFullFuel(): BlockState =
    defaultBlockState().setValue(LIT, true).setValue(FUEL_LEVEL, Fuel.MAX.level)

/**
 * Vanilla caches light emission per block state while the states are built
 * (`BlockStateBaseMixin`). Regression: `lightTable` was a stored field, read before it was
 * initialised, so every torch state cached light 0.
 */
object TorchLightTests {

    // The cached field is what's under test, and lightEmission is its only reader; Mojang's
    // @Deprecated here means "override, don't call".
    @Suppress("DEPRECATION")
    fun everyTorchStateCachesItsOwnLight(helper: GameTestHelper) {
        for (block in listOf(OutsiderBlocks.TORCH, OutsiderBlocks.WALL_TORCH)) {
            val emitter = block as EmittingBlock
            val wrong = block.stateDefinition.possibleStates
                .filter { it.lightEmission != emitter.getLight(it).value }
            helper.ensure(wrong.isEmpty()) {
                "cached light differs from getLight for ${wrong.size} states, e.g. ${wrong.first()}"
            }
            helper.ensure(block.litAtFullFuel().lightEmission > 0) {
                "a lit, fully fuelled ${block.name.string} emits no light"
            }
        }
        helper.succeed()
    }

    fun aPlacedLitTorchLightsItsBlock(helper: GameTestHelper) {
        helper.setBlock(GROUND, Blocks.STONE)
        helper.setBlock(GROUND.above(), OutsiderBlocks.TORCH.litAtFullFuel())
        val at = helper.absolutePos(GROUND.above())

        helper.succeedWhen {
            val emitted = helper.level.getLightEmission(at)
            val light = helper.level.getBrightness(LightLayer.BLOCK, at)
            helper.ensure(emitted > 0 && light == emitted) {
                "expected block light $emitted at the torch, found $light"
            }
        }
    }

    val ALL: List<SurvivalTest> = listOf(
        SurvivalTest(
            "every_torch_state_caches_its_own_light",
            TEST_TICKS,
            ::everyTorchStateCachesItsOwnLight,
        ),
        SurvivalTest(
            "a_placed_lit_torch_lights_its_block",
            TEST_TICKS,
            ::aPlacedLitTorchLightsItsBlock,
        ),
    )
}
