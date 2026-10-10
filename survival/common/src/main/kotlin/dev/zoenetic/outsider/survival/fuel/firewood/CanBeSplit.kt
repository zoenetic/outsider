package dev.zoenetic.outsider.survival.fuel.firewood

import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.state.BlockState

public fun BlockState.canBeSplit(): Boolean = this.`is`(BlockTags.LOGS)
