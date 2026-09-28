package dev.zoenetic.outsider.survival.emission

import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.units.Light
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

public interface EmittingBlock {

    public fun getHeat(state: BlockState): Heat
    public val maxHeat: Heat

    public fun getLight(state: BlockState): Light
    public val maxLight: Light
    public val lightTable: LightTable

    public companion object {
        public fun simple(heat: Heat, light: Light): EmittingBlock = object : EmittingBlock {
            override fun getHeat(state: BlockState): Heat = maxHeat
            override val maxHeat: Heat = heat

            override fun getLight(state: BlockState): Light = maxLight
            override val maxLight: Light = light
            override val lightTable: LightTable = LightTable.from { it }
        }

        public fun Block.emitterOrNull(): EmittingBlock? = VANILLA_EMITTERS[this] ?: this as? EmittingBlock
    }
}

