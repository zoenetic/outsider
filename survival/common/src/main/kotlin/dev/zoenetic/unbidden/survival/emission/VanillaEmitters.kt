package dev.zoenetic.unbidden.survival.emission

import dev.zoenetic.unbidden.survival.units.Heat
import dev.zoenetic.unbidden.survival.units.Light
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

internal val VANILLA_EMITTERS: Map<Block, EmittingBlock> by lazy {
    mapOf(
        Blocks.FURNACE to EmittingBlock.simple(Heat(20.0), Light(15)),
        Blocks.LAVA to EmittingBlock.simple(Heat(20.0), Light(15))
    )
}