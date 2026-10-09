package dev.zoenetic.outsider.survival.gametest

import dev.zoenetic.outsider.survival.fuel.Fuel
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlockStateProperties.FUEL_LEVEL
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlocks
import dev.zoenetic.outsider.survival.registry.items.OutsiderComponents
import dev.zoenetic.outsider.survival.registry.items.OutsiderItems
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT

private const val TEST_TICKS = 20

/** The fuel levels the loot table treats as still fuelled: the top two steps. */
private val GRACE_LEVELS = Fuel.MAX.level - 1..Fuel.MAX.level

object TorchLootTests {

    private val TORCH_POS = BlockPos(1, 2, 1)

    private fun GameTestHelper.surroundWithStone() {
        setBlock(TORCH_POS.below(), Blocks.STONE)
        for (side in Direction.Plane.HORIZONTAL) setBlock(TORCH_POS.relative(side), Blocks.STONE)
    }

    // Block.getDrops runs the block's loot table on its state, without spawning item entities
    // (which aren't reliably visible on the tick they spawn).
    private fun GameTestHelper.breakAndCollect(state: BlockState): ItemStack {
        surroundWithStone()
        setBlock(TORCH_POS, state)
        val drops = Block.getDrops(state, level, absolutePos(TORCH_POS), null)
        ensure(drops.size == 1) { "expected one drop from $state, found $drops" }
        return drops.single()
    }

    private fun GameTestHelper.assertDrops(state: BlockState, expected: Item) {
        val drop = breakAndCollect(state)
        ensure(drop.`is`(expected)) { "$state dropped $drop, expected $expected" }
    }

    private fun Block.atFuel(level: Int): BlockState =
        defaultBlockState().setValue(FUEL_LEVEL, level)

    private fun GameTestHelper.assertGracePeriod(block: Block) {
        for (level in 1..Fuel.MAX.level) {
            val expected = when (level) {
                in GRACE_LEVELS -> OutsiderItems.TORCH
                else -> OutsiderItems.DEAD_TORCH
            }
            assertDrops(block.atFuel(level), expected)
        }
    }

    fun aBrokenTorchDropsWithLitState(helper: GameTestHelper) {
        for (block in listOf(OutsiderBlocks.TORCH, OutsiderBlocks.WALL_TORCH)) {
            val lit = block.defaultBlockState().setValue(LIT, true)
            val drop = helper.breakAndCollect(lit)
            helper.ensure(drop.has(OutsiderComponents.LIT)) { "a lit $lit should drop lit" }
        }
        helper.succeed()
    }

    fun aLitTorchPastItsGracePeriodDropsDeadAndUnlit(helper: GameTestHelper) {
        val pastGrace = GRACE_LEVELS.first - 1
        for (block in listOf(OutsiderBlocks.TORCH, OutsiderBlocks.WALL_TORCH)) {
            val lit = block.atFuel(pastGrace).setValue(LIT, true)
            val drop = helper.breakAndCollect(lit)
            helper.ensure(drop.`is`(OutsiderItems.DEAD_TORCH)) { "$lit dropped $drop" }
            helper.ensure(!drop.has(OutsiderComponents.LIT)) { "a dead drop from $lit is lit" }
        }
        helper.succeed()
    }

    fun aTorchBrokenInItsGracePeriodDropsFuelled(helper: GameTestHelper) {
        helper.assertGracePeriod(OutsiderBlocks.TORCH)
        helper.succeed()
    }

    fun aWallTorchBrokenInItsGracePeriodDropsFuelled(helper: GameTestHelper) {
        helper.assertGracePeriod(OutsiderBlocks.WALL_TORCH)
        helper.succeed()
    }

    fun aBurntOutTorchDropsDead(helper: GameTestHelper) {
        for (block in listOf(OutsiderBlocks.DEAD_TORCH, OutsiderBlocks.DEAD_WALL_TORCH)) {
            helper.assertDrops(block.defaultBlockState(), OutsiderItems.DEAD_TORCH)
        }
        helper.succeed()
    }

    val ALL: List<SurvivalTest> = listOf(
        SurvivalTest(
            "a_broken_torch_drops_with_its_lit_state",
            TEST_TICKS,
            ::aBrokenTorchDropsWithLitState,
        ),
        SurvivalTest(
            "a_lit_torch_past_its_grace_period_drops_dead_and_unlit",
            TEST_TICKS,
            ::aLitTorchPastItsGracePeriodDropsDeadAndUnlit,
        ),
        SurvivalTest(
            "a_torch_broken_in_its_grace_period_drops_fuelled",
            TEST_TICKS,
            ::aTorchBrokenInItsGracePeriodDropsFuelled,
        ),
        SurvivalTest(
            "a_wall_torch_broken_in_its_grace_period_drops_fuelled",
            TEST_TICKS,
            ::aWallTorchBrokenInItsGracePeriodDropsFuelled,
        ),
        SurvivalTest(
            "a_burnt_out_torch_drops_dead",
            TEST_TICKS,
            ::aBurntOutTorchDropsDead,
        ),
    )
}
