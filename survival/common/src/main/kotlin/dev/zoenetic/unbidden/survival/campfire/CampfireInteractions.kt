package dev.zoenetic.unbidden.survival.campfire

import dev.zoenetic.unbidden.survival.ServerState
import dev.zoenetic.unbidden.survival.campfire.client.ClientFireAttempt
import dev.zoenetic.unbidden.survival.fuel.Fuel
import dev.zoenetic.unbidden.survival.fuel.FuelValues
import dev.zoenetic.unbidden.survival.registry.UnbiddenBlockStateProperties.FUEL_LEVEL
import dev.zoenetic.unbidden.survival.registry.UnbiddenSounds
import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.CampfireBlock.LIT
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import kotlin.math.pow

public object CampfireInteractions {

    @JvmStatic
    public fun maybeLight(
        state: BlockState, level: Level, pos: BlockPos,
        player: Player
    ): InteractionResult {

        val k = 4.0
        val l = 90.0
        fun chance(attempt: Int): Double = (k / l) * (attempt / l).pow(k - 1)

        if (state.getValue(BlockStateProperties.LIT)) return InteractionResult.PASS
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
            level.setBlock(pos, state.setValue(LIT, true), 3)
            fireAttempts.clear(player.uuid, pos)
        }

        level.playSound(null, pos, lightingSound(lit), SoundSource.BLOCKS, 1F, 1F)
        return InteractionResult.CONSUME
    }

    @JvmStatic
    public fun maybeRefuel(
        state: BlockState,
        itemStack: ItemStack,
        level: Level,
        pos: BlockPos,
        player: Player
    ): InteractionResult {
        val fuelValueOfItem = FuelValues.get(itemStack.item)
        if (fuelValueOfItem == Fuel.EMPTY) return InteractionResult.PASS
        val currentFuel = Fuel(state.getValue(FUEL_LEVEL))
        val maxFuel = Fuel.MAX
        if (currentFuel >= maxFuel) return InteractionResult.CONSUME
        if (level.isClientSide) return InteractionResult.CONSUME
        val newFuel = (currentFuel + fuelValueOfItem).coerceAtMost(maxFuel)
        level.setBlock(pos, state.setValue(FUEL_LEVEL, newFuel.level), 3)
        itemStack.consume(1, player)
        level.playSound(null, pos, UnbiddenSounds.REFUEL_FIRE, SoundSource.BLOCKS, 1F, 1F)
        return InteractionResult.CONSUME
    }

    private fun Player.hasEmptyHands(): Boolean =
        getItemInHand(InteractionHand.MAIN_HAND).isEmpty &&
                getItemInHand(InteractionHand.OFF_HAND).isEmpty

    private fun lightingSound(lit: Boolean): SoundEvent =
        if (lit) UnbiddenSounds.FIRE_SUCCESS else UnbiddenSounds.FIRE_FAILURE
}
