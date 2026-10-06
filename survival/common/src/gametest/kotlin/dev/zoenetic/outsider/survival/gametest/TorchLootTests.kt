package dev.zoenetic.outsider.survival.gametest

import dev.zoenetic.outsider.survival.fuel.Fuel
import dev.zoenetic.outsider.survival.registry.OutsiderBlockStateProperties.FUEL_LEVEL
import dev.zoenetic.outsider.survival.registry.OutsiderBlocks
import dev.zoenetic.outsider.survival.registry.OutsiderComponents
import dev.zoenetic.outsider.survival.registry.OutsiderItems
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.WallTorchBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT

private const val PART_BURNT = 5
private const val NEARLY_SPENT = 3
private const val TEST_TICKS = 20

object TorchLootTests {

    private val TORCH_POS = BlockPos(1, 2, 1)

    private fun GameTestHelper.breakAndCollectTorch(state: BlockState): ItemStack {
        setBlock(TORCH_POS, state)
        val _ = level.destroyBlock(absolutePos(TORCH_POS), true)
        val drops = getEntities(EntityTypes.ITEM, TORCH_POS, 1.0)
            .map { it.item }
            .filter { it.`is`(OutsiderItems.TORCH) }
        if (drops.size != 1) {
            throw assertionException("expected one torch to drop, found ${drops.size}")
        }
        return drops.single()
    }

    private fun GameTestHelper.assertFuel(drop: ItemStack, expected: Int) {
        val fuel = drop.get(OutsiderComponents.FUEL_LEVEL)
        if (fuel != Fuel(expected)) {
            throw assertionException("expected the drop to carry fuel $expected, found $fuel")
        }
    }

    fun aBrokenTorchDropsWithItsFuelAndFlame(helper: GameTestHelper) {
        helper.setBlock(TORCH_POS.below(), Blocks.STONE)
        val drop = helper.breakAndCollectTorch(
            OutsiderBlocks.TORCH.defaultBlockState()
                .setValue(LIT, true)
                .setValue(FUEL_LEVEL, PART_BURNT),
        )
        helper.assertFuel(drop, PART_BURNT)
        if (!drop.has(OutsiderComponents.LIT)) {
            throw helper.assertionException("a lit torch should drop lit")
        }
        helper.succeed()
    }

    fun aBrokenWallTorchDropsWithItsFuel(helper: GameTestHelper) {
        helper.setBlock(TORCH_POS.south(), Blocks.STONE)
        val drop = helper.breakAndCollectTorch(
            OutsiderBlocks.WALL_TORCH.defaultBlockState()
                .setValue(WallTorchBlock.FACING, Direction.NORTH)
                .setValue(LIT, false)
                .setValue(FUEL_LEVEL, NEARLY_SPENT),
        )
        helper.assertFuel(drop, NEARLY_SPENT)
        if (drop.has(OutsiderComponents.LIT)) {
            throw helper.assertionException("an unlit wall torch should drop unlit")
        }
        helper.succeed()
    }

    val ALL: List<SurvivalTest> = listOf(
        SurvivalTest(
            "a_broken_torch_drops_with_its_fuel_and_flame",
            TEST_TICKS,
            ::aBrokenTorchDropsWithItsFuelAndFlame,
        ),
        SurvivalTest(
            "a_broken_wall_torch_drops_with_its_fuel",
            TEST_TICKS,
            ::aBrokenWallTorchDropsWithItsFuel,
        ),
    )
}
