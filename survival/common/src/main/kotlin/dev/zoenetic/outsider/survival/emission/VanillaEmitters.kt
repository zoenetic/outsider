package dev.zoenetic.outsider.survival.emission

import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.units.Light
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

internal val VANILLA_EMITTERS: Map<Block, EmittingBlock> by lazy {
    mapOf(
        Blocks.FURNACE to EmittingBlock.simple(Heat(20.0), Light(15)),
        Blocks.LAVA to EmittingBlock.simple(Heat(20.0), Light(15)),
    )
}
