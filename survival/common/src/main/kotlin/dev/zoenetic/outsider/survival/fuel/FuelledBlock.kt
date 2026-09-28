package dev.zoenetic.outsider.survival.fuel

import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Time
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

public interface FuelledBlock {
    public val burnRate: Duration
    public val fuelCapacity: Fuel
        get() = Fuel.MAX

    public fun getFuel(state: BlockState): Fuel
    public fun setFuel(state: BlockState, fuel: Fuel): BlockState
    public fun getBurnout(existingBurnout: Time?, now: Time, fuel: Fuel): Burnout
    public fun exhausted(state: BlockState): BlockState

    public companion object {
        public fun Block.fuelledOrNull(): FuelledBlock? = this as? FuelledBlock
    }
}