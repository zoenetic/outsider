package dev.zoenetic.outsider.survival.emission

import dev.zoenetic.outsider.survival.units.Light
import dev.zoenetic.outsider.survival.units.Power
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

internal val VANILLA_EMITTERS: Map<Block, EmittingBlock> by lazy {
    mapOf(
        Blocks.FURNACE to EmittingBlock.simple(Power(4.0), Light(15)),
        Blocks.LAVA to EmittingBlock.simple(Power(12.0), Light(15)),
    )
}
