package dev.zoenetic.outsider.survival.registry

import dev.zoenetic.outsider.survival.platform.addValidBlocks
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlocks
import net.minecraft.world.level.block.entity.BlockEntityTypes

public object OutsiderWideners {
    public fun init() {
        BlockEntityTypes.CAMPFIRE.addValidBlocks { listOf(OutsiderBlocks.CAMPFIRE) }
    }
}
