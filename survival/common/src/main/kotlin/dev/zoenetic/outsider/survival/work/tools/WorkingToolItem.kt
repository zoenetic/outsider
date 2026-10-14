package dev.zoenetic.outsider.survival.work.tools

import dev.zoenetic.outsider.survival.work.WorkState
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.TagKey
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

public interface WorkingToolItem {
    public val targetBlockTags: List<TagKey<Block>>

    public fun isValidTarget(blockPos: BlockPos, blockState: BlockState): Boolean =
        targetBlockTags.any {
            blockState.`is`(it)
        }

    public fun newWorkState(
        level: ServerLevel,
        blockPos: BlockPos,
        blockState: BlockState,
        hand: InteractionHand,
    ): WorkState

    public fun tickWork(level: ServerLevel, player: Player, state: WorkState): WorkState?
    public fun tickWorkEffects(player: Player, state: WorkState): Unit = Unit

    public companion object {
        public fun ItemStack.asWorkingToolItemOrNull(): WorkingToolItem? =
            this.item as? WorkingToolItem
    }
}
