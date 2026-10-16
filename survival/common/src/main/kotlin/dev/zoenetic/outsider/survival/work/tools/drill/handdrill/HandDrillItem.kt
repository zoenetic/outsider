package dev.zoenetic.outsider.survival.work.tools.drill.handdrill

import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlockTags.LIGHTABLE_WITH_DRILL
import dev.zoenetic.outsider.survival.work.tools.WorkingToolItem
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.TagKey
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties

public class HandDrillItem(properties: Properties) :
    Item(properties),
    WorkingToolItem {

    override val targetBlockTags: List<TagKey<Block>> = listOf(LIGHTABLE_WITH_DRILL)

    override fun getUseDuration(itemStack: ItemStack, user: LivingEntity): Int = MAX_DRILL_TICKS

    override fun isValidTarget(blockPos: BlockPos, blockState: BlockState): Boolean =
        super.isValidTarget(blockPos, blockState) &&
            !blockState.getValueOrElse(BlockStateProperties.LIT, true)

    override fun useOn(context: UseOnContext): InteractionResult {
        val player = context.player ?: return InteractionResult.PASS
        val hand = context.hand
        val blockPos = context.clickedPos
        val blockState = context.level.getBlockState(blockPos)
        if (!isValidTarget(blockPos, blockState)) return InteractionResult.PASS
        val level = context.level as? ServerLevel
        if (level != null && player is ServerPlayer) {
            LightingFire.start(level, player, hand, blockPos)
        }
        player.startUsingItem(hand)
        return InteractionResult.CONSUME
        // TODO: Handle tinder-in-offhand either here or in LightingFire
    }

    public companion object {
        public const val BASE_DUST_TICKS: Int = 160
        public const val DUST_JITTER_TICKS: Int = 60
        public const val MAX_DRILL_TICKS: Int = 600
        public const val TICKS_PER_WEAR: Long = 20L
        public const val USES: Int = 120
    }
}
