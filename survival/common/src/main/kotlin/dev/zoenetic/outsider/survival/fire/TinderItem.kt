package dev.zoenetic.outsider.survival.fire

import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlockTags.LIGHTABLE_WITH_DRILL
import dev.zoenetic.outsider.survival.work.tools.WorkingToolItem
import net.minecraft.core.BlockPos
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties

public class TinderItem(properties: Properties) :
    Item(properties),
    WorkingToolItem {

    override val targetBlockTags: List<TagKey<Block>> = listOf(LIGHTABLE_WITH_DRILL)

    override fun isValidTarget(blockPos: BlockPos, blockState: BlockState): Boolean =
        super.isValidTarget(blockPos, blockState) &&
            !blockState.getValueOrElse(BlockStateProperties.LIT, true)

    public companion object {
        public const val TICKS_PER_SOMETHING: Long = 20L // TODO rename this once it does something
    }
}
