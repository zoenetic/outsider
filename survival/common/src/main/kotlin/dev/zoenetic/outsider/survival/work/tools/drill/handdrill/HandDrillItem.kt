package dev.zoenetic.outsider.survival.work.tools.drill.handdrill

import dev.zoenetic.outsider.survival.fire.LightingFireWithDrillState
import dev.zoenetic.outsider.survival.registry.OutsiderSounds
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlockTags.LIGHTABLE_WITH_DRILL
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Time
import dev.zoenetic.outsider.survival.work.Work
import dev.zoenetic.outsider.survival.work.WorkState
import dev.zoenetic.outsider.survival.work.tools.WorkingToolItem
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.TagKey
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemUseAnimation
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties

public class HandDrillItem(properties: Properties) :
    Item(properties),
    WorkingToolItem {

    override val targetBlockTags: List<TagKey<Block>> = listOf(LIGHTABLE_WITH_DRILL)

    override fun isValidTarget(blockPos: BlockPos, blockState: BlockState): Boolean =
        super.isValidTarget(blockPos, blockState) &&
            !blockState.getValueOrElse(BlockStateProperties.LIT, true)

    override fun newWorkState(
        level: ServerLevel,
        blockPos: BlockPos,
        blockState: BlockState,
        hand: InteractionHand,
    ): WorkState = LightingFireWithDrillState.new(
        blockPos,
        hand,
        drillingTimeRequired(level),
    )

    override fun tickWork(
        level: ServerLevel,
        player: Player,
        state: WorkState,
    ): LightingFireWithDrillState? {
        if (state !is LightingFireWithDrillState) return null
        when (val phase = state.phase) {
            is LightingFireWithDrillState.Drilling -> {
                if (!player.isDrilling(state.hand)) return state
                val itemStack = player.getItemInHand(state.hand)
                if (phase.elapsed.value > 0L && phase.elapsed.value % 20L == 0L) {
                    itemStack.hurtAndBreak(1, player, state.hand)
                }
                if (phase.elapsed >= phase.required) {
                    return state.copy(
                        phase = LightingFireWithDrillState.BlowingEmbers(
                            embersDieAt(level),
                            blowingTimeRequired(level),
                        ),
                    )
                }
                return state.copy(phase = phase.copy(elapsed = phase.elapsed + Duration(1)))
            }

            is LightingFireWithDrillState.BlowingEmbers -> {
                if (!player.isBlowing()) return state
                val blockState = level.getBlockState(state.blockPos)
                level.setBlock(
                    state.blockPos,
                    blockState.setValue(BlockStateProperties.LIT, true),
                    Block.UPDATE_ALL,
                )
                playFireSuccessSound(player, state)
                return null
            }
        }
    }

    override fun tickWorkEffects(player: Player, state: WorkState) {
        if (state !is LightingFireWithDrillState) return
        when (val phase = state.phase) {
            is LightingFireWithDrillState.Drilling -> {
                maybePlayUseDrillSound(player, state, phase)
                return
            }

            is LightingFireWithDrillState.BlowingEmbers -> return
        }
    }

    private fun Player.isDrilling(hand: InteractionHand) = isUsingItem && usedItemHand == hand

    private fun Player.isBlowing() = true // TODO: fix

    private fun maybePlayUseDrillSound(
        player: Player,
        state: LightingFireWithDrillState,
        phase: LightingFireWithDrillState.Drilling,
    ) {
        if (player.isDrilling(state.hand)) {
            if (phase.elapsed.value % 7L == 0L) {
                player.level().playSound(
                    player,
                    state.blockPos,
                    OutsiderSounds.USE_DRILL_SOUND,
                    SoundSource.BLOCKS,
                )
            }
        }
    }

    private fun playFireSuccessSound(player: Player, state: LightingFireWithDrillState) {
        player.level().playSound(
            null,
            state.blockPos,
            OutsiderSounds.FIRE_SUCCESS,
            SoundSource.BLOCKS,
            1F,
            1F,
        )
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        val player = context.player ?: return InteractionResult.PASS
        val hand = context.hand
        val blockPos = context.clickedPos
        val blockState = context.level.getBlockState(blockPos)
        if (!isValidTarget(blockPos, blockState)) return InteractionResult.PASS
        val level = context.level as? ServerLevel
        if (level != null && player is ServerPlayer) {
            val workState = newWorkState(level, blockPos, blockState, hand)
            Work.start(player, workState)
        }
        player.startUsingItem(hand)
        return InteractionResult.CONSUME
    }

    override fun getUseAnimation(itemStack: ItemStack): ItemUseAnimation = ItemUseAnimation.NONE

    override fun getUseDuration(itemStack: ItemStack, user: LivingEntity): Int {
        // TODO: Factor in climate and weather for dryness
        return MAX_DRILL_TICKS
    }

    private fun drillingTimeRequired(level: ServerLevel): Duration = Duration(
        level.random.triangle(
            BASE_DRILL_TICKS.toDouble(),
            DRILL_JITTER_TICKS.toDouble(),
        ).toLong(),
    )

    private fun embersDieAt(level: ServerLevel): Time = Time(level.gameTime) + Duration(20)

    private fun blowingTimeRequired(level: ServerLevel): Duration = Duration(
        20, // TODO: Magic number, wrong
    )

    public companion object {
        public const val MAX_DRILL_TICKS: Int = 600
        public const val BASE_DRILL_TICKS: Int = 160
        public const val DRILL_JITTER_TICKS: Int = 60
        public const val USES: Int = 120
    }
}
