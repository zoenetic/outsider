package dev.zoenetic.outsider.survival.work.tools

import dev.zoenetic.outsider.survival.work.tools.WorkingToolItem.Companion.asWorkingToolItemOrNull
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.EntitySelector
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

public fun Player.workingBlockTargetOrNull(tool: WorkingToolItem): Pair<BlockPos, BlockState>? {
    if (!isSecondaryUseActive) return null
    val hit = ProjectileUtil.getHitResultOnViewVector(
        this,
        EntitySelector.CAN_BE_PICKED,
        blockInteractionRange(),
    ) as? BlockHitResult ?: return null
    if (hit.type != HitResult.Type.BLOCK) return null
    val state = level().getBlockState(hit.blockPos)
    return if (tool.canWorkOn(state)) Pair(hit.blockPos, state) else null
}

public fun Player.workingBlockTargetOrNull(): Pair<BlockPos, BlockState>? {
    val tool =
        mainHandItem.asWorkingToolItemOrNull() ?: offhandItem.asWorkingToolItemOrNull()
            ?: return null
    return workingBlockTargetOrNull(tool)
}
