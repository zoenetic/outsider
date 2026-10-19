package dev.zoenetic.outsider.survival.fire

import dev.zoenetic.outsider.survival.registry.OutsiderSounds
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlockTags
import dev.zoenetic.outsider.survival.tools.TinderItem
import dev.zoenetic.outsider.survival.tools.drill.handdrill.DrillingConditions
import dev.zoenetic.outsider.survival.tools.drill.handdrill.HandDrillItem
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.work.Work
import dev.zoenetic.outsider.survival.work.WorkContext
import dev.zoenetic.outsider.survival.work.WorkEffectsContext
import dev.zoenetic.outsider.survival.work.WorkState
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.EntitySelector
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.item.Item
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

public class LightingFire(id: Identifier) : Work<LightingFireState>(id) {

    override val stateCodec: StreamCodec<RegistryFriendlyByteBuf, LightingFireState>
        get() = LightingFireState.STREAM_CODEC

    override fun tick(context: WorkContext, state: LightingFireState): LightingFireState? {
        if (!context.level.getBlockState(state.blockPos).isLightable()) return null
        when (val phase = state.phase) {
            is LightingFireState.Drilling -> {
                val phase = tickDrilling(context, state.blockPos, phase) ?: return null
                return state.copy(phase = phase)
            }

            is LightingFireState.NursingEmbers -> {
                val phase = tickNursingEmbers(context, state.blockPos, phase) ?: return null
                return state.copy(phase = phase)
            }
        }
    }

    override fun use(
        player: Player,
        hand: InteractionHand,
        item: Item,
        blockPos: BlockPos,
    ): InteractionResult {
        if (accepts(player, item, blockPos)) {
            if (player is ServerPlayer && get(player) == null) {
                val context = WorkContext.of(player)
                val _ = start(player, LightingFireState(blockPos, drilling(blockPos, context)))
                    ?: return InteractionResult.PASS
            }
            player.startUsingItem(hand)
            return InteractionResult.CONSUME
        }
        return InteractionResult.PASS
    }

    private fun accepts(player: Player, item: Item, blockPos: BlockPos): Boolean {
        val blockState = player.level().getBlockState(blockPos)
        return blockState.isLightable() && when (val current = get(player)) {
            is LightingFireState -> {
                blockPos == current.blockPos && when (current.phase) {
                    is LightingFireState.Drilling ->
                        item is HandDrillItem

                    is LightingFireState.NursingEmbers -> item is TinderItem
                }
            }

            is WorkState -> false

            null -> item is HandDrillItem
        }
    }

    private fun BlockState.isLightable(): Boolean = `is`(OutsiderBlockTags.LIGHTABLE_WITH_DRILL) &&
        !getValueOrElse(BlockStateProperties.LIT, true)

    private fun Player.isLookingAt(blockPos: BlockPos): Boolean {
        val hit = ProjectileUtil.getHitResultOnViewVector(
            this,
            EntitySelector.CAN_BE_PICKED,
            this.blockInteractionRange(),
        ) as? BlockHitResult ?: return false
        return hit.type == HitResult.Type.BLOCK && hit.blockPos == blockPos
    }

    private fun tickDrilling(
        context: WorkContext,
        blockPos: BlockPos,
        phase: LightingFireState.Drilling,
    ): LightingFireState.Phase? {
        val level = context.level
        val player = context.player
        val now = context.now
        val conditions = this.conditions(level, blockPos)
        val dust = phase.dustAt(now)
        val heat = phase.heatAt(now, conditions)
        val hand = player.handWorkingOn<HandDrillItem>(blockPos)
        val held = hand != null
        if (held != phase.held) {
            return phase.copy(dust = dust, heat = heat, held = held, timestamp = now)
        }
        if (!held) return phase // TODO: cool the embers
        if (dust > Duration.NONE && dust.value % HandDrillItem.TICKS_PER_WEAR == 0L) {
            player.getItemInHand(hand).hurtAndBreak(1, player, hand)
        }
        if (dust >= phase.dustRequired && heat >= conditions.ignition) {
            player.stopUsingItem()
            return nursingEmbers(blockPos, context)
        }
        return phase
    }

    // TODO: finish
    private fun tickNursingEmbers(
        context: WorkContext,
        blockPos: BlockPos,
        phase: LightingFireState.NursingEmbers,
    ): LightingFireState.Phase? {
        val level = context.level
        val player = context.player
        val now = context.now
        if (now >= phase.diesAt) {
            return drilling(blockPos, context)
        }
        val blown = phase.blownAt(now)
        val hand = player.handWorkingOn<TinderItem>(blockPos)
        val held = hand != null
        if (held != phase.held) {
            return phase.copy(
                blown = blown,
                held = held,
                timestamp = now,
            ) // TODO: extend dies at a bit each time the player blows?
        }
        if (!held) return phase
        if (blown > Duration.NONE) { // TODO: % some value derived from the player's vitals?
            // TODO: Something about exertion?
        }
        if (blown >= phase.blownRequired) {
            player.getItemInHand(hand).hurtAndBreak(1, player, hand)
            player.stopUsingItem()
            val blockState = level.getBlockState(blockPos)
            context.level.playSound(
                null,
                blockPos,
                OutsiderSounds.FIRE_SUCCESS,
                SoundSource.BLOCKS,
            )
            level.setBlock(
                blockPos,
                blockState.trySetValue(BlockStateProperties.LIT, true),
                Block.UPDATE_ALL,
            )
            return null
        }
        return phase
    }

    override fun tickEffects(context: WorkEffectsContext, state: LightingFireState) {
        when (val phase = state.phase) {
            is LightingFireState.Drilling -> {
                if (phase.held && phase.dustAt(context.now).value % 7L == 0L) {
                    context.level.playSound(
                        context.player,
                        state.blockPos,
                        OutsiderSounds.USE_DRILL_SOUND,
                        SoundSource.BLOCKS,
                    )
                }
            }

            is LightingFireState.NursingEmbers -> {
                if (phase.held && phase.blownAt(context.now).value % 7L == 0L) {
                    context.level.playSound(
                        context.player,
                        state.blockPos,
                        OutsiderSounds.BLOWING,
                        SoundSource.BLOCKS,
                    )
                }
            }
        }
    }

    private fun conditions(level: Level, blockPos: BlockPos): DrillingConditions =
        // TODO: calculate these from world
        DrillingConditions(
            ambient = Heat(20.0),
            risePerTick = Heat(4.0),
            halfLife = Duration(40),
            ignition = Heat(400.0),
        )

    public fun drilling(blockPos: BlockPos, context: WorkContext): LightingFireState.Drilling {
        val conditions = conditions(context.level, blockPos)
        return LightingFireState.Drilling(
            dust = Duration.NONE,
            dustRequired = Duration(600), // TODO: plug conditions in here?
            heat = conditions.ambient,
            held = false,
            timestamp = context.now,
        )
    }

    public fun nursingEmbers(
        blockPos: BlockPos,
        context: WorkContext,
    ): LightingFireState.NursingEmbers {
        val conditions = conditions(context.level, blockPos)
        return LightingFireState.NursingEmbers(
            diesAt = context.now + Duration(40), // TODO: plug conditions in here?
            blown = Duration.NONE,
            blownRequired = Duration(20),
            held = false,
            timestamp = context.now,
        )
    }

    private inline fun <reified T : Item> ServerPlayer.handUsing(): InteractionHand? =
        if (isUsingItem && useItem.item is T &&
            getItemInHand(usedItemHand).item is T
        ) {
            return usedItemHand
        } else {
            null
        }

    private inline fun <reified T : Item> ServerPlayer.handWorkingOn(
        blockPos: BlockPos,
    ): InteractionHand? {
        val hand = handUsing<T>() ?: return null
        if (isLookingAt(blockPos)) return hand
        stopUsingItem()
        return null
    }
}
