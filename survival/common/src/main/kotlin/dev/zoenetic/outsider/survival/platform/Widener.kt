package dev.zoenetic.outsider.survival.platform

import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType

public interface Widener {
    public fun addValidBlocks(type: BlockEntityType<*>, blocks: () -> List<Block>)
}