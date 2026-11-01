package dev.zoenetic.outsider.survival.emission

import dev.zoenetic.outsider.survival.units.Light
import dev.zoenetic.outsider.survival.units.Power
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

public interface EmittingBlock {

    public fun getHeat(state: BlockState): Power
    public val maxHeat: Power
    public val heatTable: HeatTable

    public fun getLight(state: BlockState): Light
    public val maxLight: Light
    public val lightTable: LightTable

    public companion object {
        public fun simple(heat: Power, light: Light): EmittingBlock = object : EmittingBlock {
            override fun getHeat(state: BlockState): Power = maxHeat
            override val maxHeat: Power = heat
            override val heatTable: HeatTable = HeatTable.constant(heat)

            override fun getLight(state: BlockState): Light = maxLight
            override val maxLight: Light = light
            override val lightTable: LightTable = LightTable.from { it }
        }

        public fun Block.emitterOrNull(): EmittingBlock? =
            VANILLA_EMITTERS[this] ?: this as? EmittingBlock
    }
}
