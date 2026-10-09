package dev.zoenetic.outsider.survival.fire

import dev.zoenetic.outsider.survival.ServerState
import dev.zoenetic.outsider.survival.emission.EmitterIndex
import dev.zoenetic.outsider.survival.fire.client.ClientFireAttempt
import dev.zoenetic.outsider.survival.fuel.Fuel
import dev.zoenetic.outsider.survival.fuel.FuelValues
import dev.zoenetic.outsider.survival.fuel.FuelledBlock.Companion.fuelledOrNull
import dev.zoenetic.outsider.survival.registry.OutsiderSounds
import dev.zoenetic.outsider.survival.registry.items.OutsiderComponents
import dev.zoenetic.outsider.survival.registry.items.OutsiderItems
import dev.zoenetic.outsider.survival.units.Time
import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.util.Unit
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import kotlin.math.pow

public object FireInteractions {

    // TODO: Fuel checks

    @JvmStatic
    public fun maybeLightInHand(
        mainHandItem: ItemStack,
        offHandItem: ItemStack,
        level: Level,
        player: Player,
    ): InteractionResult {
        val mainHandHasLitItem = mainHandItem.has(OutsiderComponents.LIT)
        val offHandHasLitItem = offHandItem.has(OutsiderComponents.LIT)
        if (mainHandHasLitItem == offHandHasLitItem) return InteractionResult.PASS
        val target = if (mainHandHasLitItem) offHandItem else mainHandItem
        if (!target.`is`(OutsiderItems.TORCH)) return InteractionResult.PASS
        if (level.isClientSide) return InteractionResult.CONSUME
        target.set(OutsiderComponents.LIT, Unit.INSTANCE)
        return InteractionResult.CONSUME
    }

    @JvmStatic
    public fun maybeLightFromLitBlock(
        state: BlockState,
        itemStack: ItemStack,
        level: Level,
        pos: BlockPos,
        player: Player,
    ): InteractionResult {
        if (!itemStack.`is`(OutsiderItems.TORCH)) return InteractionResult.PASS
        if (itemStack.has(OutsiderComponents.LIT)) return InteractionResult.PASS
        if (!state.getValueOrElse(BlockStateProperties.LIT, false)) return InteractionResult.PASS
        if (level.isClientSide) return InteractionResult.CONSUME
        itemStack.set(OutsiderComponents.LIT, Unit.INSTANCE)
        return InteractionResult.CONSUME
    }

    @JvmStatic
    public fun maybeLightWithHandDrill(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
    ): InteractionResult {
        val k = 4.0
        val l = 90.0
        fun chance(attempt: Int): Double = (k / l) * (attempt / l).pow(k - 1)
        if (!state.hasProperty(BlockStateProperties.LIT) ||
            state.getValue(BlockStateProperties.LIT)
        ) {
            return InteractionResult.PASS
        }
        if (!player.hasEmptyHands()) return InteractionResult.PASS
        val time = level.gameTime
        if (level.isClientSide) {
            ClientFireAttempt.record(time)
            return InteractionResult.CONSUME
        }
        val fireAttempts = ServerState.fireAttempts()
        val attempt = fireAttempts.recordAttempt(player.uuid, pos, time)
        val lit = level.random.nextDouble() < chance(attempt)
        if (lit) {
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, true), 3)
            fireAttempts.clear(player.uuid, pos)
        }
        level.playSound(null, pos, lightingSound(lit), SoundSource.BLOCKS, 1F, 1F)
        return InteractionResult.CONSUME
    }

    @JvmStatic
    public fun maybeLightWithLitItem(
        state: BlockState,
        itemStack: ItemStack,
        level: Level,
        pos: BlockPos,
        player: Player,
    ): InteractionResult {
        if (!state.hasProperty(BlockStateProperties.LIT) ||
            state.getValue(BlockStateProperties.LIT)
        ) {
            return InteractionResult.PASS
        }
        if (!itemStack.has(OutsiderComponents.LIT)) return InteractionResult.PASS
        if (level.isClientSide) return InteractionResult.CONSUME
        level.setBlock(pos, state.setValue(BlockStateProperties.LIT, true), 3)
        return InteractionResult.CONSUME
    }

    @JvmStatic
    public fun maybeRefuel(
        state: BlockState,
        itemStack: ItemStack,
        level: Level,
        pos: BlockPos,
        player: Player,
    ): InteractionResult {
        val added = FuelValues.get(itemStack.item)
        if (added == Fuel.EMPTY) return InteractionResult.PASS
        val fuelled = state.block.fuelledOrNull() ?: return InteractionResult.PASS
        val current = fuelled.getFuel(state)
        if (current >= Fuel.MAX) return InteractionResult.CONSUME
        if (level.isClientSide) return InteractionResult.CONSUME
        val chunk = level.getChunkAt(pos)
        val stored = EmitterIndex.burnoutAtPos(chunk, pos)
        if (stored == null) {
            level.setBlock(pos, fuelled.setFuel(state, (current + added).coerceAtMost(Fuel.MAX)), 3)
        } else {
            val now = Time(level.gameTime)
            val refuelled = stored.refuel(added, now, fuelled.burnRate)
            level.setBlock(pos, fuelled.setFuel(state, refuelled.fuelAt(now, fuelled.burnRate)), 3)
            EmitterIndex.set(chunk, pos, refuelled)
        }
        itemStack.consume(1, player)
        level.playSound(null, pos, OutsiderSounds.REFUEL_FIRE, SoundSource.BLOCKS, 1F, 1F)
        return InteractionResult.CONSUME
    }

    private fun Player.hasEmptyHands(): Boolean =
        getItemInHand(InteractionHand.MAIN_HAND).isEmpty &&
            getItemInHand(InteractionHand.OFF_HAND).isEmpty

    private fun lightingSound(lit: Boolean): SoundEvent =
        if (lit) OutsiderSounds.FIRE_SUCCESS else OutsiderSounds.FIRE_FAILURE
}
