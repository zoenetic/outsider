package dev.zoenetic.outsider.survival.drill.handdrill

import dev.zoenetic.outsider.survival.registry.OutsiderBlockTags.LIGHTABLE_WITH_DRILL
import dev.zoenetic.outsider.survival.registry.OutsiderSounds
import dev.zoenetic.outsider.survival.registry.items.OutsiderComponents
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.EntitySelector
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemUseAnimation
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

public class HandDrillItem(properties: Properties) : Item(properties) {

    override fun useOn(context: UseOnContext): InteractionResult {
        val player = context.player ?: return InteractionResult.PASS
        if (getValidBlockPosAndBlockStateOrNull(context.level, player) != null) {
            val level = context.level
            if (level is ServerLevel) {
                context.itemInHand.set(
                    OutsiderComponents.DRILL_TIME_REQUIRED,
                    drillTimeRequired(level),
                )
            }
            player.startUsingItem(context.hand)
            return InteractionResult.CONSUME
        }
        return InteractionResult.PASS
    }

    override fun getUseAnimation(itemStack: ItemStack): ItemUseAnimation = ItemUseAnimation.NONE

    override fun getUseDuration(itemStack: ItemStack, user: LivingEntity): Int {
        // TODO: Factor in climate and weather for dryness
        return MAX_DRILL_TICKS
    }

    override fun onUseTick(
        level: Level,
        livingEntity: LivingEntity,
        itemStack: ItemStack,
        ticksRemaining: Int,
    ) {
        val player = livingEntity as? Player ?: return livingEntity.releaseUsingItem()
        val (pos, state) = getValidBlockPosAndBlockStateOrNull(level, player)
            ?: return livingEntity.releaseUsingItem()
        val useDuration = this.getUseDuration(itemStack, livingEntity)
        val elapsed = useDuration - ticksRemaining
        if (elapsed % 7 == 0) {
            level.playSound(player, pos, OutsiderSounds.USE_DRILL_SOUND, SoundSource.BLOCKS)
        }
        if (level is ServerLevel) {
            val timeRequired = itemStack.get(OutsiderComponents.DRILL_TIME_REQUIRED) ?: return
            if (elapsed > 0 && elapsed % 20 == 0) {
                itemStack.hurtAndBreak(1, livingEntity, livingEntity.usedItemHand)
            }
            if (elapsed >= timeRequired) {
                level.setBlock(
                    pos,
                    state.setValue(BlockStateProperties.LIT, true),
                    Block.UPDATE_ALL,
                )
                player.releaseUsingItem()
                level.playSound(
                    null,
                    pos,
                    OutsiderSounds.FIRE_SUCCESS,
                    SoundSource.BLOCKS,
                    1F,
                    1F,
                )
            }
        } else {
            if (state.shouldSpawnTerrainParticles() &&
                state.renderShape != RenderShape.INVISIBLE
            ) {
                // TODO: Particles
            }
        }
    }

    override fun finishUsingItem(
        itemStack: ItemStack,
        level: Level,
        entity: LivingEntity,
    ): ItemStack {
        itemStack.remove(OutsiderComponents.DRILL_TIME_REQUIRED)
        return itemStack
    }

    override fun releaseUsing(
        itemStack: ItemStack,
        level: Level,
        entity: LivingEntity,
        remainingTime: Int,
    ): Boolean {
        itemStack.remove(OutsiderComponents.DRILL_TIME_REQUIRED)
        return super.releaseUsing(itemStack, level, entity, remainingTime)
    }

    private fun drillTimeRequired(level: ServerLevel): Int =
        level.random.triangle(BASE_DRILL_TICKS.toDouble(), DRILL_JITTER_TICKS.toDouble()).toInt()

    private fun getValidBlockPosAndBlockStateOrNull(
        level: Level,
        player: Player,
    ): Pair<BlockPos, BlockState>? {
        val hit = ProjectileUtil.getHitResultOnViewVector(
            player,
            EntitySelector.CAN_BE_PICKED,
            player.blockInteractionRange(),
        ) as? BlockHitResult ?: return null
        return if (hit.type ==
            HitResult.Type.BLOCK
        ) {
            val pos = hit.blockPos
            val state = level.getBlockState(pos)
            if (state.`is`(LIGHTABLE_WITH_DRILL) &&
                !state.getValueOrElse(BlockStateProperties.LIT, false)
            ) {
                Pair(pos, state)
            } else {
                null
            }
        } else {
            null
        }
    }

    public companion object {
        public const val MAX_DRILL_TICKS: Int = 600
        public const val BASE_DRILL_TICKS: Int = 160
        public const val DRILL_JITTER_TICKS: Int = 60
        public const val USES: Int = 120
    }
}
