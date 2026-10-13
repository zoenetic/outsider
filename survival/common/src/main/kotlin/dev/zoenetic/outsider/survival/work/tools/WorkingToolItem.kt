package dev.zoenetic.outsider.survival.work.tools

import dev.zoenetic.outsider.survival.work.WorkState
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

public interface WorkingToolItem {
    public val targetBlockTags: List<TagKey<Block>>

    public fun canWorkOn(state: BlockState): Boolean = targetBlockTags.any { state.`is`(it) }
    public fun tickWork(player: Player, state: WorkState): WorkState?

    public companion object {
        public fun ItemStack.asWorkingToolItemOrNull(): WorkingToolItem? =
            this.item as? WorkingToolItem
    }
}
