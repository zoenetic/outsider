package dev.zoenetic.outsider.survival.fire

import dev.zoenetic.outsider.survival.ServerState
import dev.zoenetic.outsider.survival.fire.client.ClientFireAttempt
import dev.zoenetic.outsider.survival.fuel.Fuel
import dev.zoenetic.outsider.survival.fuel.FuelValues
import dev.zoenetic.outsider.survival.registry.OutsiderBlockStateProperties.FUEL_LEVEL
import dev.zoenetic.outsider.survival.registry.OutsiderComponents
import dev.zoenetic.outsider.survival.registry.OutsiderItems
import dev.zoenetic.outsider.survival.registry.OutsiderSounds
import dev.zoenetic.outsider.survival.superstack.isOrContains
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
        if (!target.isOrContains(OutsiderItems.TORCH)) return InteractionResult.PASS
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
        if (!itemStack.isOrContains(OutsiderItems.TORCH)) return InteractionResult.PASS
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
        val fuelValueOfItem = FuelValues.get(itemStack.item)
        if (fuelValueOfItem == Fuel.EMPTY) return InteractionResult.PASS
        val currentFuel = Fuel(state.getValue(FUEL_LEVEL))
        if (currentFuel >= Fuel.MAX) return InteractionResult.CONSUME
        if (level.isClientSide) return InteractionResult.CONSUME
        val newFuel = (currentFuel + fuelValueOfItem).coerceAtMost(Fuel.MAX)
        level.setBlock(pos, state.setValue(FUEL_LEVEL, newFuel.level), 3)
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
