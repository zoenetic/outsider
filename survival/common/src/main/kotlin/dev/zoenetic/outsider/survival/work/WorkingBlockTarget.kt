package dev.zoenetic.outsider.survival.work

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState

public data class WorkingBlockTarget(public val blockPos: BlockPos, public val state: BlockState)
