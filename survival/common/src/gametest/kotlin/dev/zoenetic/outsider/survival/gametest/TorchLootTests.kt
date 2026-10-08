package dev.zoenetic.outsider.survival.gametest

import dev.zoenetic.outsider.survival.registry.OutsiderBlocks
import dev.zoenetic.outsider.survival.registry.OutsiderComponents
import dev.zoenetic.outsider.survival.registry.OutsiderItems
import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT

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

    fun aBrokenTorchDropsWithLitState(helper: GameTestHelper) {
        helper.setBlock(TORCH_POS.below(), Blocks.STONE)
        val drop = helper.breakAndCollectTorch(
            OutsiderBlocks.TORCH.defaultBlockState()
                .setValue(LIT, true),
        )
        if (!drop.has(OutsiderComponents.LIT)) {
            throw helper.assertionException("a lit torch should drop lit")
        }
        helper.succeed()
    }

    val ALL: List<SurvivalTest> = listOf(
        SurvivalTest(
            "a_broken_torch_drops_with_its_lit_state",
            TEST_TICKS,
            ::aBrokenTorchDropsWithLitState,
        ),
    )
}
